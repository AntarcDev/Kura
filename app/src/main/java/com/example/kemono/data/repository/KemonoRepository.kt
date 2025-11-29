package com.example.kemono.data.repository

import com.example.kemono.data.local.CacheDao
import com.example.kemono.data.local.FavoriteDao
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.toCached
import com.example.kemono.data.model.toCreator
import com.example.kemono.data.model.toPost
import com.example.kemono.data.remote.KemonoApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Singleton
class KemonoRepository
@Inject
constructor(
        private val api: KemonoApi,
        private val favoriteDao: FavoriteDao,
        private val cacheDao: CacheDao,
        private val networkMonitor: com.example.kemono.util.NetworkMonitor
) {
    suspend fun getCreators(): List<Creator> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
            return try {
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }
            } catch (e: Exception) {
                emptyList()
            }
        }

        return try {
            val jsonElement = api.getCreators()
            val creators = if (jsonElement.isJsonArray) {
                val jsonArray = jsonElement.asJsonArray
                val gson = com.google.gson.Gson()
                jsonArray.map { gson.fromJson(it, Creator::class.java) }
            } else if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                val gson = com.google.gson.Gson()
                // Try known keys
                val list = when {
                    jsonObject.has("creators") -> jsonObject.getAsJsonArray("creators")
                    jsonObject.has("results") -> jsonObject.getAsJsonArray("results")
                    jsonObject.has("users") -> jsonObject.getAsJsonArray("users")
                    jsonObject.has("data") -> jsonObject.getAsJsonArray("data")
                    else -> com.google.gson.JsonArray()
                }
                list.map { gson.fromJson(it, Creator::class.java) }
            } else {
                emptyList()
            }
            
            if (creators.isNotEmpty()) {
                val validCreators = creators.filter { it.id != null }
                cacheDao.cacheCreators(validCreators.map { it.toCached() })
                validCreators
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            try {
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }
            } catch (cacheError: Exception) {
                throw e
            }
        }
    }

    suspend fun getPopularCreators(): List<Creator> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) {
             // In offline mode, we can't fetch popular posts to determine popularity.
             // Fallback: Return all cached creators (or maybe favorites?)
             // For now, let's return cached creators to show *something*
             return getCreators()
        }

        // Fetch popular posts to find popular creators
        val response = api.getPopularPosts()
        val popularCreatorIds = response.posts.map { it.user }.distinct()

        // We need full creator details. We can filter the cached creators or fetch them.
        // For efficiency, let's try to get them from our full list if available,
        // otherwise we might need to fetch profiles (which is expensive for a list).
        // Best approach: Get all creators (cached) and filter by the popular IDs.
        val allCreators = getCreators()
        return allCreators.filter { it.id in popularCreatorIds }
    }

    suspend fun getPopularPosts(date: String? = null, period: String? = null, offset: Int = 0): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList() // TODO: Implement caching for popular posts?

        val response = api.getPopularPosts(date, period, offset)
        return response.posts
    }

    suspend fun getRandomPosts(): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList()

        // Fetch 5 random posts in parallel
        return kotlinx.coroutines.coroutineScope {
            (1..5).map {
                async {
                    try {
                        val redirect = api.getRandomPostRedirect()
                        val response = api.getPost(redirect.service, redirect.artistId, redirect.postId)
                        response.post
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull().filter { it.id != null }
        }
    }

    suspend fun getRecentPosts(offset: Int = 0, query: String? = null, tags: List<String>? = null): List<Post> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return emptyList()

        val response = api.getRecentPosts(offset, query, tags)
        val posts = if (response.posts.isNotEmpty()) response.posts else response.results
        return posts.filter { it.id != null }
    }

    suspend fun getCreatorProfile(service: String, creatorId: String): Creator {
        val isOnline = networkMonitor.isOnline.first()
        
        if (!isOnline) {
             val cached = cacheDao.getCachedCreator(creatorId)
             return cached?.toCreator() ?: throw Exception("Creator not found in cache")
        }

        return try {
            val creator = api.getCreatorProfile(service, creatorId)
            cacheDao.cacheCreators(listOf(creator.toCached()))
            creator
        } catch (e: Exception) {
            val cached = cacheDao.getCachedCreator(creatorId)
            cached?.toCreator() ?: throw e
        }
    }

    suspend fun getCreatorPosts(
            service: String,
            creatorId: String,
            offset: Int = 0,
            query: String? = null
    ): List<Post> {
        val isOnline = networkMonitor.isOnline.first()

        if (!isOnline) {
             if (offset == 0 && query == null) {
                 return try {
                    cacheDao.getCachedPosts(service, creatorId).first().map { it.toPost() }
                 } catch (e: Exception) {
                     emptyList()
                 }
             } else {
                 return emptyList()
             }
        }

        return try {
            val posts = api.getCreatorPosts(service, creatorId, offset, query)
            cacheDao.cachePosts(posts.map { it.toCached() })
            posts.filter { it.id != null }
        } catch (e: Exception) {
            if (offset == 0 && query == null) {
                try {
                    cacheDao.getCachedPosts(service, creatorId).first().map { it.toPost() }
                } catch (cacheError: Exception) {
                    throw e
                }
            } else {
                throw e
            }
        }
    }



    suspend fun getCreatorAnnouncements(service: String, creatorId: String): List<com.example.kemono.data.model.Announcement> {
        return try {
            api.getCreatorAnnouncements(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorTags(service: String, creatorId: String): List<String> {
        return try {
            api.getCreatorTags(service, creatorId).map { it.tag }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorLinks(service: String, creatorId: String): List<com.example.kemono.data.model.CreatorLink> {
        return try {
            api.getCreatorLinks(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCreatorFancards(service: String, creatorId: String): List<com.example.kemono.data.model.Fancard> {
        return try {
            api.getCreatorFancards(service, creatorId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPost(service: String, creatorId: String, postId: String): Post {
        val isOnline = networkMonitor.isOnline.first()
        
        if (!isOnline) {
            return cacheDao.getCachedPost(postId)?.toPost() ?: throw Exception("Post not found in cache")
        }

        return try {
            val post = api.getPost(service, creatorId, postId).post
            if (post.id != null) {
                cacheDao.cachePosts(listOf(post.toCached()))
            }
            post
        } catch (e: Exception) {
            cacheDao.getCachedPost(postId)?.toPost() ?: throw e
        }
    }

    suspend fun cleanExpiredCache() {
        val expiryTime = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) // 7 days
        cacheDao.deleteExpiredCreators(expiryTime)
        cacheDao.deleteExpiredPosts(expiryTime)
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteCreator>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun addFavorite(creator: FavoriteCreator) {
        favoriteDao.insertFavorite(creator)
    }

    suspend fun removeFavorite(creator: FavoriteCreator) {
        favoriteDao.deleteFavorite(creator)
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return favoriteDao.isFavorite(id)
    }

    suspend fun getTags(): List<String> {
        return api.getTags().map { it.tag }
    }
}

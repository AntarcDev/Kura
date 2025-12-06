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
            val responseBody = api.getCreators()
            val creators = mutableListOf<Creator>()
            
            responseBody.charStream().use { charStream ->
                val reader = com.google.gson.stream.JsonReader(charStream)
                val gson = com.google.gson.GsonBuilder()
                    .registerTypeAdapter(Creator::class.java, com.example.kemono.data.model.CreatorDeserializer())
                    .create()
                
                // Determine if it's an array or object
                val token = reader.peek()
                if (token == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        try {
                            val creator = gson.fromJson<Creator>(reader, Creator::class.java)
                            if (creator.id != null) {
                                creators.add(creator)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("KemonoRepo", "Error parsing creator: ${e.message}")
                        }
                    }
                    reader.endArray()
                } else if (token == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    // Try to find "creators" or "results" array inside object
                    // This is complex for streaming, but we can do a simple check
                    // For now, if it's an object, it might be an error or unexpected format.
                    // Given we verified it's a list, we focus on that.
                    // But to be robust:
                    val jsonObject = gson.fromJson<com.google.gson.JsonObject>(reader, com.google.gson.JsonObject::class.java)
                     val list = when {
                        jsonObject.has("creators") -> jsonObject.getAsJsonArray("creators")
                        jsonObject.has("results") -> jsonObject.getAsJsonArray("results")
                        jsonObject.has("users") -> jsonObject.getAsJsonArray("users")
                        jsonObject.has("data") -> jsonObject.getAsJsonArray("data")
                        else -> com.google.gson.JsonArray()
                    }
                    creators.addAll(list.map { gson.fromJson(it, Creator::class.java) }.filter { it.id != null })
                }
            }
            
            if (creators.isNotEmpty()) {
                // Batch insert to avoid transaction limits/OOM
                val chunkSize = 1000
                creators.chunked(chunkSize).forEach { chunk ->
                    try {
                        cacheDao.cacheCreators(chunk.map { it.toCached() })
                    } catch (e: Exception) {
                        android.util.Log.e("KemonoRepo", "Failed to cache chunk: ${e.message}")
                    }
                }
                creators
            } else {
                // If parsing resulted in empty list but no exception, try cache
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }
            }
        } catch (e: Exception) {
            android.util.Log.e("KemonoRepo", "Failed to fetch creators: ${e.message}")
            try {
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }
            } catch (cacheError: Exception) {
                // If cache fails too, rethrow original error to let ViewModel handle it
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
        return response.posts.filter { it.id != null }
    }

    suspend fun getCreatorProfile(service: String, creatorId: String): Creator {
        val isOnline = networkMonitor.isOnline.first()
        
        if (!isOnline) {
             val cached = cacheDao.getCachedCreator(creatorId)
             return cached?.toCreator() ?: throw Exception("Creator not found in cache")
        }

        return try {
            val responseBody = api.getCreatorProfile(service, creatorId)
            val jsonString = responseBody.string()
            
            // Debug log content
            // android.util.Log.d("KemonoRepo", "Profile JSON for $creatorId: $jsonString")
            
            val gson = com.google.gson.GsonBuilder()
                .registerTypeAdapter(Creator::class.java, com.example.kemono.data.model.CreatorDeserializer())
                .create()
            try {
                val creator = gson.fromJson(jsonString, Creator::class.java)
                cacheDao.cacheCreators(listOf(creator.toCached()))
                creator
            } catch (e: Exception) {
                 android.util.Log.e("KemonoRepo", "Failed to parse profile JSON: $jsonString", e)
                 throw e
            }
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

    suspend fun getDiscordChannels(serverId: String): List<com.example.kemono.data.model.DiscordChannel> {
        return try {
            api.getDiscordChannels(serverId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiscordChannelPosts(channelId: String, offset: Int = 0): List<com.example.kemono.data.model.DiscordPost> {
        return try {
            api.getDiscordChannelPosts(channelId, offset)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

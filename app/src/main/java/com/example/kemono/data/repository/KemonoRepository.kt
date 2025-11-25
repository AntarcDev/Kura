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

@Singleton
class KemonoRepository
@Inject
constructor(
        private val api: KemonoApi,
        private val favoriteDao: FavoriteDao,
        private val cacheDao: CacheDao
) {
    suspend fun getCreators(): List<Creator> {
        return try {
            val creators = api.getCreators()
            cacheDao.cacheCreators(creators.map { it.toCached() })
            creators
        } catch (e: Exception) {
            try {
                cacheDao.getAllCachedCreators().first().map { it.toCreator() }
            } catch (cacheError: Exception) {
                throw e
            }
        }
    }

    suspend fun getRecentPosts(offset: Int = 0, query: String? = null): List<Post> {
        return api.getRecentPosts(offset, query)
    }

    suspend fun getCreatorProfile(service: String, creatorId: String): Creator {
        return api.getCreatorProfile(service, creatorId)
    }

    suspend fun getCreatorPosts(
            service: String,
            creatorId: String,
            offset: Int = 0,
            query: String? = null
    ): List<Post> {
        return try {
            val posts = api.getCreatorPosts(service, creatorId, offset, query)
            cacheDao.cachePosts(posts.map { it.toCached() })
            posts
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

    suspend fun getPost(service: String, creatorId: String, postId: String): Post {
        return try {
            val post = api.getPost(service, creatorId, postId).post
            cacheDao.cachePosts(listOf(post.toCached()))
            post
        } catch (e: Exception) {
            cacheDao.getCachedPost(postId)?.toPost() ?: throw e
        }
    }

    suspend fun cleanExpiredCache() {
        val expiryTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours
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
}

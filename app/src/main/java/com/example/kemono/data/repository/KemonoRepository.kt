package com.example.kemono.data.repository

import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.Post
import com.example.kemono.data.remote.KemonoApi
import javax.inject.Inject
import javax.inject.Singleton

import com.example.kemono.data.local.FavoriteDao
import com.example.kemono.data.model.FavoriteCreator
import kotlinx.coroutines.flow.Flow

@Singleton
class KemonoRepository @Inject constructor(
    private val api: KemonoApi,
    private val favoriteDao: FavoriteDao
) {
    suspend fun getCreators(): List<Creator> {
        return api.getCreators()
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
        return api.getCreatorPosts(service, creatorId, offset, query)
    }

    suspend fun getPost(service: String, creatorId: String, postId: String): Post {
        return api.getPost(service, creatorId, postId).post
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

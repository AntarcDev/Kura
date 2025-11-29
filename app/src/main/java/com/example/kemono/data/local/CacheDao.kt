package com.example.kemono.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kemono.data.model.CachedCreator
import com.example.kemono.data.model.CachedPost
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    // Creators
    @Query("SELECT * FROM cached_creators") fun getAllCachedCreators(): Flow<List<CachedCreator>>

    @Query("SELECT * FROM cached_creators WHERE id = :id")
    suspend fun getCachedCreator(id: String): CachedCreator?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheCreators(creators: List<CachedCreator>)

    @Query("DELETE FROM cached_creators WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredCreators(expiryTime: Long)

    // Posts
    @Query("SELECT * FROM cached_posts WHERE user = :userId AND service = :service ORDER BY published DESC")
    fun getCachedPosts(service: String, userId: String): Flow<List<CachedPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun cachePosts(posts: List<CachedPost>)

    @Query("SELECT * FROM cached_posts WHERE id = :postId")
    suspend fun getCachedPost(postId: String): CachedPost?

    @Query("DELETE FROM cached_posts WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredPosts(expiryTime: Long)

    // Cache management
    @Query("DELETE FROM cached_creators") suspend fun clearCreatorCache()

    @Query("DELETE FROM cached_posts") suspend fun clearPostCache()
}

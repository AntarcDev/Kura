package com.example.kemono.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kemono.data.model.FavoriteCreator
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_creators")
    fun getAllFavorites(): Flow<List<FavoriteCreator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(creator: FavoriteCreator)

    @Delete
    suspend fun deleteFavorite(creator: FavoriteCreator)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_creators WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>
}

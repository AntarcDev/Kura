package com.example.kemono.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM blacklist ORDER BY addedAt DESC")
    fun getAllBlacklistedItems(): Flow<List<BlacklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToBlacklist(item: BlacklistEntity)

    @Delete
    suspend fun removeFromBlacklist(item: BlacklistEntity)
    
    @Query("SELECT * FROM blacklist WHERE type = :type ORDER BY name ASC")
    fun getBlacklistByType(type: BlacklistType): Flow<List<BlacklistEntity>>
}

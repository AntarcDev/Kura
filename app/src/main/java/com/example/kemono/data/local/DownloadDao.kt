package com.example.kemono.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kemono.data.model.DownloadedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: DownloadedItem)

    @Query("SELECT * FROM downloaded_items ORDER BY downloadedAt DESC")
    fun getAllDownloadedItems(): Flow<List<DownloadedItem>>

    @Delete suspend fun delete(item: DownloadedItem)
}

package com.example.kemono.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kemono.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearchHistory(limit: Int = 10): Flow<List<SearchHistory>>

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}

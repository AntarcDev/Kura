package com.example.kemono.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kemono.data.model.CachedCreator
import com.example.kemono.data.model.CachedPost
import com.example.kemono.data.model.DownloadedItem
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.FavoritePost
import com.example.kemono.data.model.SearchHistory

@Database(
        entities =
                [
                        FavoriteCreator::class,
                        FavoritePost::class,
                        CachedCreator::class,
                        CachedPost::class,
                        DownloadedItem::class,
                        SearchHistory::class,
                        BlacklistEntity::class],
        version = 8,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
        abstract fun favoriteDao(): FavoriteDao
        abstract fun cacheDao(): CacheDao
        abstract fun downloadDao(): DownloadDao
        abstract fun searchHistoryDao(): SearchHistoryDao
        abstract fun blacklistDao(): BlacklistDao
}

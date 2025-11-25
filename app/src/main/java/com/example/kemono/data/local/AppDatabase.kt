package com.example.kemono.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kemono.data.model.CachedCreator
import com.example.kemono.data.model.CachedPost
import com.example.kemono.data.model.FavoriteCreator

@Database(
        entities = [FavoriteCreator::class, CachedCreator::class, CachedPost::class],
        version = 2,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cacheDao(): CacheDao
}

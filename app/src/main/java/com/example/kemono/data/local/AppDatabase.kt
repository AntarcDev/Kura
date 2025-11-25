package com.example.kemono.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kemono.data.model.FavoriteCreator

@Database(entities = [FavoriteCreator::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}

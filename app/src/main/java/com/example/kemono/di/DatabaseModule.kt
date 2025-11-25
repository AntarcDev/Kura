package com.example.kemono.di

import android.content.Context
import androidx.room.Room
import com.example.kemono.data.local.AppDatabase
import com.example.kemono.data.local.CacheDao
import com.example.kemono.data.local.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "kemono_db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideCacheDao(database: AppDatabase): CacheDao {
        return database.cacheDao()
    }
}

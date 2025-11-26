package com.example.kemono.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GRID_SIZE = stringPreferencesKey("grid_size")
        val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "System"
    }

    val gridSize: Flow<String> = dataStore.data.map { preferences ->
        preferences[GRID_SIZE] ?: "Comfortable"
    }

    val downloadLocation: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_LOCATION]
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setGridSize(size: String) {
        dataStore.edit { preferences ->
            preferences[GRID_SIZE] = size
        }
    }

    suspend fun setDownloadLocation(uri: String) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_LOCATION] = uri
        }
    }
}

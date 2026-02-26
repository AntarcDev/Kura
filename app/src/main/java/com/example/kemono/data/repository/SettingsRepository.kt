package com.example.kemono.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    // Companion object moved below to include all keys


    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "System"
    }

    val gridSize: Flow<String> = dataStore.data.map { preferences ->
        preferences[GRID_SIZE] ?: "Comfortable"
    }

    val downloadLocation: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_LOCATION]
    }

    val crashReportingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CRASH_REPORTING_ENABLED] ?: false
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

    suspend fun setCrashReportingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CRASH_REPORTING_ENABLED] = enabled
        }
    }

    val artistLayoutMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[ARTIST_LAYOUT_MODE] ?: "Grid" // Default to Grid for Artists
    }
    val postLayoutMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[POST_LAYOUT_MODE] ?: "List" // Default to List for Posts
    }
    val downloadLayoutMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[DOWNLOAD_LAYOUT_MODE] ?: "List"
    }
    val favoriteLayoutMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[FAVORITE_LAYOUT_MODE] ?: "Grid"
    }
    val gridDensity: Flow<String> = dataStore.data.map { preferences ->
        preferences[GRID_DENSITY] ?: "Medium"
    }

    val autoplayGifs: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTOPLAY_GIFS] ?: true // Default to true
    }

    val isAppLockEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_APP_LOCK_ENABLED] ?: false
    }

    val appLockPin: Flow<String?> = dataStore.data.map { preferences ->
        preferences[APP_LOCK_PIN]
    }

    val isIncognitoKeyboardEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_INCOGNITO_KEYBOARD_ENABLED] ?: false
    }

    val cacheSizeLimitRatio: Flow<Float> = dataStore.data.map { preferences ->
        preferences[CACHE_SIZE_LIMIT_RATIO] ?: 0f // 0f means "No Limit"
    }

    val autoDownloadFavorites: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_DOWNLOAD_FAVORITES] ?: false
    }

    val startVideosMuted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[START_VIDEOS_MUTED] ?: false
    }

    val useExternalVideoPlayer: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_EXTERNAL_VIDEO_PLAYER] ?: false
    }

    val imageQuality: Flow<String> = dataStore.data.map { preferences ->
        preferences[IMAGE_QUALITY] ?: "Sample"
    }

    val customColorSchemeType: Flow<String> = dataStore.data.map { preferences ->
        preferences[CUSTOM_COLOR_SCHEME_TYPE] ?: "Default"
    }

    suspend fun setArtistLayoutMode(mode: String) {
        dataStore.edit { preferences -> preferences[ARTIST_LAYOUT_MODE] = mode }
    }
    suspend fun setPostLayoutMode(mode: String) {
        dataStore.edit { preferences -> preferences[POST_LAYOUT_MODE] = mode }
    }
    suspend fun setDownloadLayoutMode(mode: String) {
        dataStore.edit { preferences -> preferences[DOWNLOAD_LAYOUT_MODE] = mode }
    }
    suspend fun setFavoriteLayoutMode(mode: String) {
        dataStore.edit { preferences -> preferences[FAVORITE_LAYOUT_MODE] = mode }
    }
    suspend fun setGridDensity(density: String) {
        dataStore.edit { preferences -> preferences[GRID_DENSITY] = density }
    }
    suspend fun setAutoplayGifs(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[AUTOPLAY_GIFS] = enabled }
    }

    suspend fun setIsAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[IS_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockPin(pin: String?) {
        dataStore.edit { preferences ->
            if (pin == null) {
                preferences.remove(APP_LOCK_PIN)
            } else {
                preferences[APP_LOCK_PIN] = pin
            }
        }
    }

    suspend fun setIsIncognitoKeyboardEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[IS_INCOGNITO_KEYBOARD_ENABLED] = enabled }
    }

    suspend fun setCacheSizeLimitRatio(ratio: Float) {
        dataStore.edit { preferences -> preferences[CACHE_SIZE_LIMIT_RATIO] = ratio }
    }

    suspend fun setAutoDownloadFavorites(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[AUTO_DOWNLOAD_FAVORITES] = enabled }
    }

    suspend fun setStartVideosMuted(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[START_VIDEOS_MUTED] = enabled }
    }

    suspend fun setUseExternalVideoPlayer(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[USE_EXTERNAL_VIDEO_PLAYER] = enabled }
    }

    suspend fun setImageQuality(quality: String) {
        dataStore.edit { preferences -> preferences[IMAGE_QUALITY] = quality }
    }

    suspend fun setCustomColorSchemeType(type: String) {
        dataStore.edit { preferences -> preferences[CUSTOM_COLOR_SCHEME_TYPE] = type }
    }

    // Page Size
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GRID_SIZE = stringPreferencesKey("grid_size") // Deprecated, use specific layouts + density
        val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")
        val CRASH_REPORTING_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("crash_reporting_enabled")
        
        val ARTIST_LAYOUT_MODE = stringPreferencesKey("artist_layout_mode")
        val POST_LAYOUT_MODE = stringPreferencesKey("post_layout_mode")
        val DOWNLOAD_LAYOUT_MODE = stringPreferencesKey("download_layout_mode")
        val FAVORITE_LAYOUT_MODE = stringPreferencesKey("favorite_layout_mode")
        val GRID_DENSITY = stringPreferencesKey("grid_density")
        val AUTOPLAY_GIFS = androidx.datastore.preferences.core.booleanPreferencesKey("autoplay_gifs")
        
        val IS_APP_LOCK_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("is_app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val IS_INCOGNITO_KEYBOARD_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("is_incognito_keyboard_enabled")
        val CACHE_SIZE_LIMIT_RATIO = androidx.datastore.preferences.core.floatPreferencesKey("cache_size_limit_ratio")
        val AUTO_DOWNLOAD_FAVORITES = androidx.datastore.preferences.core.booleanPreferencesKey("auto_download_favorites")
        val START_VIDEOS_MUTED = androidx.datastore.preferences.core.booleanPreferencesKey("start_videos_muted")
        val USE_EXTERNAL_VIDEO_PLAYER = androidx.datastore.preferences.core.booleanPreferencesKey("use_external_video_player")
        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val CUSTOM_COLOR_SCHEME_TYPE = stringPreferencesKey("custom_color_scheme_type")
    }
}

package com.example.kemono.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.kemono.data.local.SessionManager
import com.example.kemono.data.repository.KemonoRepository

import com.example.kemono.data.model.Account
import com.example.kemono.data.repository.SettingsRepository
import com.example.kemono.data.repository.UpdateRepository
import com.example.kemono.data.model.GithubRelease
import com.example.kemono.BuildConfig
import androidx.core.content.FileProvider
import android.content.Intent
import android.net.Uri
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        private val sessionManager: SessionManager,
        private val okHttpClient: OkHttpClient,
        private val repository: KemonoRepository,
        private val settingsRepository: SettingsRepository,
        private val updateRepository: UpdateRepository,
        @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sessionCookie = MutableStateFlow(sessionManager.getSessionCookie() ?: "")
    val sessionCookie: StateFlow<String> = _sessionCookie.asStateFlow()

    private val _hasSession = MutableStateFlow(sessionManager.hasSession())
    val hasSession: StateFlow<Boolean> = _hasSession.asStateFlow()

    private val _initStatus = MutableStateFlow("")
    val initStatus: StateFlow<String> = _initStatus.asStateFlow()

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _updateAvailable = MutableStateFlow<GithubRelease?>(null)
    val updateAvailable: StateFlow<GithubRelease?> = _updateAvailable.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    private val _readyToInstall = MutableStateFlow<File?>(null)
    val readyToInstall: StateFlow<File?> = _readyToInstall.asStateFlow()

    val appThemeMode =
            settingsRepository.themeMode.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    "System"
            )
    val artistLayoutMode = settingsRepository.artistLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Grid")
    val postLayoutMode = settingsRepository.postLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "List")
    val downloadLayoutMode = settingsRepository.downloadLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "List")
    val favoriteLayoutMode = settingsRepository.favoriteLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Grid")
    val gridDensity = settingsRepository.gridDensity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")
    val crashReportingEnabled = settingsRepository.crashReportingEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val downloadLocation = settingsRepository.downloadLocation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val autoplayGifs = settingsRepository.autoplayGifs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAppLockEnabled = settingsRepository.isAppLockEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val appLockPin = settingsRepository.appLockPin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val isIncognitoKeyboardEnabled = settingsRepository.isIncognitoKeyboardEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val cacheSizeLimitRatio = settingsRepository.cacheSizeLimitRatio.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
    val autoDownloadFavorites = settingsRepository.autoDownloadFavorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val startVideosMuted = settingsRepository.startVideosMuted.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val useExternalVideoPlayer = settingsRepository.useExternalVideoPlayer.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val imageQuality = settingsRepository.imageQuality.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sample")
    val customColorSchemeType = settingsRepository.customColorSchemeType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Default")

    // Stats
    data class CacheStats(val mediaCacheSize: Long, val networkCacheSize: Long, val totalSize: Long)
    private val _cacheStats = MutableStateFlow(CacheStats(0, 0, 0))
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()

    val account = repository.accountState
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Initialize cache stats
        refreshCacheStats()
        
        viewModelScope.launch {
            repository.loginEvent.collect {
                // Trigger refresh if needed, though Flow might handle it if repo updates validity
                 repository.refreshAccount()
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }
    
    // Deprecated but kept for compatibility logic helper if needed
    fun setGridSize(size: String) {
        viewModelScope.launch {
            settingsRepository.setGridSize(size)
            // Auto-migrate for better UX
            if (size == "Compact") {
                settingsRepository.setGridDensity("Small")
                settingsRepository.setArtistLayoutMode("Grid")
            } else {
                settingsRepository.setGridDensity("Medium")
            }
        }
    }

    fun setArtistLayoutMode(mode: String) = viewModelScope.launch { settingsRepository.setArtistLayoutMode(mode) }
    fun setPostLayoutMode(mode: String) = viewModelScope.launch { settingsRepository.setPostLayoutMode(mode) }
    fun setDownloadLayoutMode(mode: String) = viewModelScope.launch { settingsRepository.setDownloadLayoutMode(mode) }
    fun setFavoriteLayoutMode(mode: String) = viewModelScope.launch { settingsRepository.setFavoriteLayoutMode(mode) }
    fun setGridDensity(density: String) = viewModelScope.launch { settingsRepository.setGridDensity(density) }
    fun setCrashReportingEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setCrashReportingEnabled(enabled) }
    fun setDownloadLocation(uri: String) = viewModelScope.launch { settingsRepository.setDownloadLocation(uri) }
    fun setAutoplayGifs(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoplayGifs(enabled) }
    
    fun setIsAppLockEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setIsAppLockEnabled(enabled) }
    fun setAppLockPin(pin: String?) = viewModelScope.launch { settingsRepository.setAppLockPin(pin) }
    fun setIsIncognitoKeyboardEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setIsIncognitoKeyboardEnabled(enabled) }
    fun setCacheSizeLimitRatio(ratio: Float) = viewModelScope.launch { settingsRepository.setCacheSizeLimitRatio(ratio) }
    fun setAutoDownloadFavorites(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoDownloadFavorites(enabled) }
    fun setStartVideosMuted(enabled: Boolean) = viewModelScope.launch { settingsRepository.setStartVideosMuted(enabled) }
    fun setUseExternalVideoPlayer(enabled: Boolean) = viewModelScope.launch { settingsRepository.setUseExternalVideoPlayer(enabled) }
    fun setImageQuality(quality: String) = viewModelScope.launch { settingsRepository.setImageQuality(quality) }
    fun setCustomColorSchemeType(type: String) = viewModelScope.launch { settingsRepository.setCustomColorSchemeType(type) }

    fun refreshCacheStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val imageCache = context.cacheDir.resolve("image_cache")
            val imageHttpCache = context.cacheDir.resolve("image_http_cache")
            val httpCache = context.cacheDir.resolve("http_cache")
            val githubCache = context.cacheDir.resolve("github_cache")

            val mediaSize = getDirSize(imageCache) + getDirSize(imageHttpCache)
            val networkSize = getDirSize(httpCache) + getDirSize(githubCache)
            val totalSize = mediaSize + networkSize

            _cacheStats.value = CacheStats(
                mediaCacheSize = mediaSize,
                networkCacheSize = networkSize,
                totalSize = totalSize
            )
        }
    }

    fun clearMediaCache() {
        viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.resolve("image_cache").deleteRecursively()
            context.cacheDir.resolve("image_http_cache").deleteRecursively()
            // Reset coil memory cache if possible, or trigger reload.
            // For now just file deletion.
            // Coil.imageLoader(context).memoryCache?.clear() // Requires main thread usually or context
            
            refreshCacheStats()
        }
        viewModelScope.launch(Dispatchers.Main) {
             coil.Coil.imageLoader(context).memoryCache?.clear()
             coil.Coil.imageLoader(context).diskCache?.clear()
        }
    }

    fun clearNetworkCache() {
        viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.resolve("http_cache").deleteRecursively()
            context.cacheDir.resolve("github_cache").deleteRecursively()
            
            repository.cleanExpiredCache() // Clean internal repo cache
            
            refreshCacheStats()
        }
    }
    
    private fun getDirSize(dir: File): Long {
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }


    fun updateSessionCookie(cookie: String) {
        _sessionCookie.value = cookie
        sessionManager.saveSessionCookie(cookie)
        _hasSession.value = sessionManager.hasSession()
    }

    fun clearSession() {
        _sessionCookie.value = ""
        sessionManager.clearSession()
        _hasSession.value = false
        repository.logout()
    }
    
    fun logout() {
        clearSession()
    }

    fun initializeDDoSGuard() {
        viewModelScope.launch {
            try {
                _isInitializing.value = true
                _initStatus.value = "Initializing DDoS-Guard cookies..."

                withContext(Dispatchers.IO) {
                    // Make a request to the main site to get DDoS-Guard cookies
                    val request = Request.Builder().url("https://kemono.cr/").build()

                    val response = okHttpClient.newCall(request).execute()
                    val responseCode = response.code
                    response.close()

                    withContext(Dispatchers.Main) {
                        _initStatus.value =
                                when {
                                    responseCode in 200..299 ->
                                            "✓ DDoS-Guard cookies initialized successfully"
                                    responseCode == 403 ->
                                            "✓ DDoS-Guard cookies received (403 is normal)"
                                    else -> "⚠ Got response code: $responseCode"
                                }
                    }
                }
            } catch (e: Exception) {
                _initStatus.value = "✗ Error: ${e.message ?: "Unknown error"}"
                e.printStackTrace()
            } finally {
                _isInitializing.value = false
            }
        }
        }


    fun checkForUpdate() {
        viewModelScope.launch {
            _updateError.value = null
            try {
                val result = updateRepository.checkForUpdate(BuildConfig.VERSION_NAME)
                result.fold(
                    onSuccess = { release ->
                        if (release != null) {
                            _updateAvailable.value = release
                        } else {
                            _updateError.value = "Kura is up to date"
                        }
                    },
                    onFailure = { e ->
                        _updateError.value = "Check failed: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _updateError.value = "Failed to check for updates: ${e.message}"
            }
        }
    }

    fun downloadUpdate() {
        val release = _updateAvailable.value ?: return
        val asset = release.assets.firstOrNull { it.name.endsWith(".apk") }
        if (asset == null) {
            _updateError.value = "No APK found in release"
            return
        }

        viewModelScope.launch {
            try {
                _downloadProgress.value = 0f
                updateRepository.downloadApk(asset.downloadUrl).collect { progress ->
                    _downloadProgress.value = progress
                }
                _downloadProgress.value = null
                _readyToInstall.value = updateRepository.getApkFile()
            } catch (e: Exception) {
                _downloadProgress.value = null
                _updateError.value = "Download failed: ${e.message}"
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateAvailable.value = null
        _updateError.value = null
        _readyToInstall.value = null
    }

    fun getInstallIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    private val _backupRestoreStatus = MutableStateFlow<String?>(null)
    val backupRestoreStatus: StateFlow<String?> = _backupRestoreStatus.asStateFlow()

    fun clearBackupRestoreStatus() {
        _backupRestoreStatus.value = null
    }

    fun backupData(uri: Uri) {
        viewModelScope.launch {
            _backupRestoreStatus.value = "Backing up..."
            val result = com.example.kemono.util.BackupRestoreUtils.createBackup(context, uri)
            _backupRestoreStatus.value = if (result.isSuccess) "Backup successful!" else "Backup failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun restoreData(uri: Uri) {
        viewModelScope.launch {
            _backupRestoreStatus.value = "Restoring..."
            val result = com.example.kemono.util.BackupRestoreUtils.restoreBackup(context, uri)
            _backupRestoreStatus.value = if (result.isSuccess) "Restore successful! Please restart the app." else "Restore failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun importFavorites() {
        viewModelScope.launch {
            _importStatus.value = "Importing..."
            try {
                val count = withContext(Dispatchers.IO) {
                    repository.importFavorites()
                }
                _importStatus.value = "Successfully imported $count favorites"
            } catch (e: Exception) {
                _importStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun pushFavorites() {
        viewModelScope.launch {
            _importStatus.value = "Pushing favorites..."
            try {
                val count = withContext(Dispatchers.IO) {
                    repository.pushFavoritesToAccount()
                }
                _importStatus.value = "Successfully pushed $count favorites"
            } catch (e: Exception) {
                _importStatus.value = "Error: ${e.message}"
            }
        }
    }
    
    fun clearImportStatus() {
        _importStatus.value = null
    }
}

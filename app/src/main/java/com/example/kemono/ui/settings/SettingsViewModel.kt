package com.example.kemono.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.kemono.data.local.SessionManager
import com.example.kemono.data.repository.KemonoRepository

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

    val themeMode =
            settingsRepository.themeMode.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    "System"
            )
    val gridSize =
            settingsRepository.gridSize.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    "Comfortable"
            )
    val downloadLocation =
            settingsRepository.downloadLocation.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
            )

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setGridSize(size: String) {
        viewModelScope.launch { settingsRepository.setGridSize(size) }
    }

    fun setDownloadLocation(uri: String) {
        viewModelScope.launch { settingsRepository.setDownloadLocation(uri) }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearCache() {
        viewModelScope.launch {
            context.imageLoader.memoryCache?.clear()
            context.imageLoader.diskCache?.clear()
            repository.cleanExpiredCache()
        }
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
                            _updateError.value = "App is up to date"
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
}

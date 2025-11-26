package com.example.kemono.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.kemono.data.local.SessionManager
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.data.repository.SettingsRepository
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
}

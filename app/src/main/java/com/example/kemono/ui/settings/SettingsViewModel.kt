package com.example.kemono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _sessionCookie = MutableStateFlow(sessionManager.getSessionCookie() ?: "")
    val sessionCookie: StateFlow<String> = _sessionCookie.asStateFlow()

    private val _hasSession = MutableStateFlow(sessionManager.hasSession())
    val hasSession: StateFlow<Boolean> = _hasSession.asStateFlow()

    private val _initStatus = MutableStateFlow("")
    val initStatus: StateFlow<String> = _initStatus.asStateFlow()

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

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
                    val request = Request.Builder()
                        .url("https://kemono.cr/")
                        .build()
                    
                    val response = okHttpClient.newCall(request).execute()
                    val responseCode = response.code
                    response.close()
                    
                    withContext(Dispatchers.Main) {
                        _initStatus.value = when {
                            responseCode in 200..299 -> "✓ DDoS-Guard cookies initialized successfully"
                            responseCode == 403 -> "✓ DDoS-Guard cookies received (403 is normal)"
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

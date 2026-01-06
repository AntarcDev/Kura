package com.example.kemono.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: KemonoRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Username and password cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = repository.login(_username.value, _password.value)
            
            result.onSuccess {
                _loginSuccess.value = true
            }.onFailure {
                _error.value = it.message ?: "Login failed"
            }
            
            _isLoading.value = false
        }
    }
}

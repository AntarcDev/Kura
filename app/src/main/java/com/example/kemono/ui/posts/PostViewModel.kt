package com.example.kemono.ui.posts

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.kemono.data.model.Post
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.util.NetworkMonitor
import com.example.kemono.worker.DownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PostViewModel
@Inject
constructor(
        private val repository: KemonoRepository,
        savedStateHandle: SavedStateHandle,
        private val application: Application,
        networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline =
            networkMonitor.isOnline.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    true
            )

    private val service: String = checkNotNull(savedStateHandle["service"])
    private val creatorId: String = checkNotNull(savedStateHandle["creatorId"])
    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchPost()
    }

    fun fetchPost() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _post.value = repository.getPost(service, creatorId, postId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load post"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadFile(url: String, fileName: String) {
        val workManager = androidx.work.WorkManager.getInstance(application)
        val data = workDataOf("key_file_url" to url, "key_file_name" to fileName)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(data).build()
        workManager.enqueue(request)
    }
}

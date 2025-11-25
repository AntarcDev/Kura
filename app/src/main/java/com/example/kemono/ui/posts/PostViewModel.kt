package com.example.kemono.ui.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Post
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.util.NetworkMonitor
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
        private val downloadRepository: com.example.kemono.data.repository.DownloadRepository,
        savedStateHandle: SavedStateHandle,
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

    fun downloadMedia() {
        val currentPost = _post.value ?: return
        viewModelScope.launch {
            // Download main file
            currentPost.file?.let { file ->
                if (!file.path.isNullOrEmpty()) {
                    val url = "https://kemono.cr${file.path}"
                    val fileName = file.name ?: "file_${currentPost.id}"
                    val mediaType =
                            if (com.example.kemono.util.getMediaType(file.path) ==
                                            com.example.kemono.util.MediaType.VIDEO
                            )
                                    "VIDEO"
                            else "IMAGE"
                    downloadRepository.downloadFile(
                            url,
                            fileName,
                            currentPost.id,
                            currentPost.user,
                            mediaType
                    )
                }
            }

            // Download attachments
            currentPost.attachments.forEach { attachment ->
                if (!attachment.path.isNullOrEmpty()) {
                    val url = "https://kemono.cr${attachment.path}"
                    val fileName = attachment.name ?: "attachment_${currentPost.id}"
                    val mediaType =
                            if (com.example.kemono.util.getMediaType(attachment.path) ==
                                            com.example.kemono.util.MediaType.VIDEO
                            )
                                    "VIDEO"
                            else "IMAGE"
                    downloadRepository.downloadFile(
                            url,
                            fileName,
                            currentPost.id,
                            currentPost.user,
                            mediaType
                    )
                }
            }
        }
    }
}

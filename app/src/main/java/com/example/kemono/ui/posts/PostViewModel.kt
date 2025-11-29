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

    private val _creatorName = MutableStateFlow<String?>(null)

    init {
        fetchPost()
        fetchCreatorProfile()
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

    private fun fetchCreatorProfile() {
        viewModelScope.launch {
            try {
                val creator = repository.getCreatorProfile(service, creatorId)
                _creatorName.value = creator.name
            } catch (e: Exception) {
                // Ignore error, fallback to creatorId if name fetch fails
                e.printStackTrace()
            }
        }
    }

    fun downloadMedia() {
        val currentPost = _post.value ?: return
        
        viewModelScope.launch {
            // Ensure we have the creator name
            var creatorName = _creatorName.value
            if (creatorName == null) {
                try {
                    val creator = repository.getCreatorProfile(service, creatorId)
                    _creatorName.value = creator.name
                    creatorName = creator.name
                } catch (e: Exception) {
                    e.printStackTrace()
                    creatorName = currentPost.user // Fallback to ID
                }
            }

            // Download main file
            currentPost.file?.let { file ->
                if (!file.path.isNullOrEmpty()) {
                    val url = "https://kemono.su${file.path}"
                    val fileName = file.name
                    val mediaType =
                            if (com.example.kemono.util.getMediaType(file.path) ==
                                            com.example.kemono.util.MediaType.VIDEO
                            )
                                    "VIDEO"
                            else "IMAGE"
                    downloadRepository.downloadFile(
                            url,
                            fileName,
                            currentPost.id ?: "",
                            currentPost.title ?: "",
                            currentPost.user ?: "",
                            creatorName ?: "",
                            mediaType
                    )
                }
            }

            // Download attachments
            currentPost.attachments.forEach { attachment ->
                if (!attachment.path.isNullOrEmpty()) {
                    val url = "https://kemono.su${attachment.path}"
                    val fileName = attachment.name
                    val mediaType =
                            if (com.example.kemono.util.getMediaType(attachment.path) ==
                                            com.example.kemono.util.MediaType.VIDEO
                            )
                                    "VIDEO"
                            else "IMAGE"
                    downloadRepository.downloadFile(
                            url,
                            fileName,
                            currentPost.id ?: "",
                            currentPost.title ?: "",
                            currentPost.user ?: "",
                            creatorName ?: "",
                            mediaType
                    )
                }
            }

            // Download inline images
            currentPost.content?.let { htmlContent ->
                val contentNodes = com.example.kemono.util.HtmlConverter.parseHtmlContent(htmlContent)
                contentNodes.forEach { node ->
                    if (node is com.example.kemono.util.ContentNode.Image) {
                        val url = node.url
                        val fileName = url.substringAfterLast('/')
                        downloadRepository.downloadFile(
                            url,
                            fileName,
                            currentPost.id ?: "",
                            currentPost.title ?: "",
                            currentPost.user ?: "",
                            creatorName ?: "",
                            "IMAGE"
                        )
                    }
                }
            }
        }
    }
    fun downloadFile(url: String, fileName: String, mediaType: String) {
        val currentPost = _post.value ?: return
        
        viewModelScope.launch {
            // Ensure we have the creator name
            var creatorName = _creatorName.value
            if (creatorName == null) {
                try {
                    val creator = repository.getCreatorProfile(service, creatorId)
                    _creatorName.value = creator.name
                    creatorName = creator.name
                } catch (e: Exception) {
                    e.printStackTrace()
                    creatorName = currentPost.user // Fallback to ID
                }
            }

            downloadRepository.downloadFile(
                    url,
                    fileName,
                    currentPost.id ?: "",
                    currentPost.title ?: "",
                    currentPost.user ?: "",
                    creatorName ?: "",
                    mediaType
            )
        }
    }
}

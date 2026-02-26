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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PostViewModel
@Inject
constructor(
        private val repository: KemonoRepository,
        private val downloadRepository: com.example.kemono.data.repository.DownloadRepository,
        private val settingsRepository: com.example.kemono.data.repository.SettingsRepository,
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

    val isFavorite = repository.isPostFavorite(postId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentPost = _post.value ?: return@launch
            val favPost = com.example.kemono.data.model.FavoritePost(
                id = currentPost.id ?: return@launch,
                service = currentPost.service ?: "",
                user = currentPost.user ?: "",
                title = currentPost.title ?: "",
                content = currentPost.content ?: "",
                thumbnailPath = currentPost.file?.path ?: currentPost.attachments.firstOrNull()?.path,
                published = currentPost.published ?: ""
            )
            val isFav = isFavorite.value
            if (isFav) {
                repository.removeFavoritePost(favPost)
            } else {
                repository.addFavoritePost(favPost)
                
                val autoDownload = settingsRepository.autoDownloadFavorites.firstOrNull() ?: false
                if (autoDownload) {
                    downloadMedia()
                }
            }
        }
    }

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
            
            downloadRepository.downloadPostMedia(currentPost, creatorName ?: currentPost.user ?: "")
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

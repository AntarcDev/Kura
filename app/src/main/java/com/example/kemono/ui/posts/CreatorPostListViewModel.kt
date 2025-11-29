package com.example.kemono.ui.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.Post
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatorPostListViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val downloadRepository: com.example.kemono.data.repository.DownloadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val service: String = checkNotNull(savedStateHandle["service"])
    val creatorId: String = checkNotNull(savedStateHandle["creatorId"])

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _creator = MutableStateFlow<Creator?>(null)
    val creator: StateFlow<Creator?> = _creator.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _announcements = MutableStateFlow<List<com.example.kemono.data.model.Announcement>>(emptyList())
    val announcements: StateFlow<List<com.example.kemono.data.model.Announcement>> = _announcements.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _links = MutableStateFlow<List<com.example.kemono.data.model.CreatorLink>>(emptyList())
    val links: StateFlow<List<com.example.kemono.data.model.CreatorLink>> = _links.asStateFlow()

    private val _fancards = MutableStateFlow<List<com.example.kemono.data.model.Fancard>>(emptyList())
    val fancards: StateFlow<List<com.example.kemono.data.model.Fancard>> = _fancards.asStateFlow()

    init {
        fetchCreatorProfile()
        fetchPosts()
        checkIfFavorite()
        fetchProfileDetails()
    }

    private fun fetchProfileDetails() {
        viewModelScope.launch {
            // Fetch in parallel
            launch { _announcements.value = repository.getCreatorAnnouncements(service, creatorId) }
            launch { _tags.value = repository.getCreatorTags(service, creatorId) }
            launch { _links.value = repository.getCreatorLinks(service, creatorId) }
            if (service == "fanbox") {
                launch { _fancards.value = repository.getCreatorFancards(service, creatorId) }
            }
        }
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            repository.isFavorite(creatorId).collect {
                _isFavorite.value = it
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentCreator = _creator.value ?: return@launch
            val favoriteCreator = FavoriteCreator(
                id = currentCreator.id,
                name = currentCreator.name,
                service = currentCreator.service,
                updated = currentCreator.updated.toString()
            )
            
            if (_isFavorite.value) {
                repository.removeFavorite(favoriteCreator)
            } else {
                repository.addFavorite(favoriteCreator)
            }
        }
    }

    private fun fetchCreatorProfile() {
        viewModelScope.launch {
            try {
                _creator.value = repository.getCreatorProfile(service, creatorId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetching first page for now
                _posts.value = repository.getCreatorPosts(service, creatorId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load posts"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Selection State
    private val _selectedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPostIds: StateFlow<Set<String>> = _selectedPostIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun toggleSelection(post: Post) {
        val current = _selectedPostIds.value
        val postId = post.id ?: return
        if (current.contains(postId)) {
            _selectedPostIds.value = current - postId
            if (_selectedPostIds.value.isEmpty()) {
                _isSelectionMode.value = false
            }
        } else {
            _selectedPostIds.value = current + postId
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedPostIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun downloadSelectedPosts() {
        val selectedIds = _selectedPostIds.value
        val postsToDownload = _posts.value.filter { it.id in selectedIds }
        
        viewModelScope.launch {
            postsToDownload.forEach { post ->
                // Ensure we have creator name, or fallback
                val creatorName = post.user // Ideally fetch name if needed, but ID is safe for now
                
                // Download main file
                post.file?.let { file ->
                    if (!file.path.isNullOrEmpty()) {
                        val url = "https://kemono.su${file.path}"
                        val mediaType = if (com.example.kemono.util.getMediaType(file.path) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                        downloadRepository.downloadFile(
                            url,
                            file.name ?: "file",
                            post.id ?: "",
                            post.title ?: "",
                            post.user ?: "",
                            creatorName ?: "",
                            mediaType
                        )
                    }
                }

                // Download attachments
                post.attachments.forEach { attachment ->
                    if (!attachment.path.isNullOrEmpty()) {
                        val url = "https://kemono.su${attachment.path}"
                        val mediaType = if (com.example.kemono.util.getMediaType(attachment.path) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                        downloadRepository.downloadFile(
                            url,
                            attachment.name ?: "attachment",
                            post.id ?: "",
                            post.title ?: "",
                            post.user ?: "",
                            creatorName ?: "",
                            mediaType
                        )
                    }
                }
            }
            clearSelection()
        }
    }
}

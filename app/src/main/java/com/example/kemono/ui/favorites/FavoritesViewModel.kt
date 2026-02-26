package com.example.kemono.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val settingsRepository: com.example.kemono.data.repository.SettingsRepository,
    private val downloadRepository: com.example.kemono.data.repository.DownloadRepository
) : ViewModel() {

    val layoutMode = settingsRepository.favoriteLayoutMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Grid")
        
    val gridDensity = settingsRepository.gridDensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val favorites: StateFlow<List<Creator>> = 
        kotlinx.coroutines.flow.combine(repository.getAllFavorites(), _searchQuery) { favs, query ->
            val filteredFavs = if (query.isBlank()) {
                favs
            } else {
                favs.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.service.contains(query, ignoreCase = true) 
                }
            }
            
            filteredFavs.map { fav ->
                Creator(
                    id = fav.id,
                    service = fav.service,
                    name = fav.name,
                    indexed = 0L,
                    updated = fav.updated.toLongOrNull() ?: 0L
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePosts = repository.getAllFavoritePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavoritePost(post: com.example.kemono.data.model.Post) {
        viewModelScope.launch {
            // Need to convert Post to FavoritePost
            val isFav = repository.isPostFavorite(post.id ?: return@launch).first()
             val favoritePost = com.example.kemono.data.model.FavoritePost(
                id = post.id,
                service = post.service ?: "",
                user = post.user ?: "", // creatorId
                title = post.title ?: "",
                content = post.content ?: "",
                thumbnailPath = post.file?.path,
                published = post.published ?: "",
            )
            
            if (isFav) {
                repository.removeFavoritePost(favoritePost)
            } else {
                repository.addFavoritePost(favoritePost)
                
                val autoDownload = settingsRepository.autoDownloadFavorites.firstOrNull() ?: false
                if (autoDownload) {
                    // Try to map to an existing favorite creator name
                    val creatorName = favorites.value.find { it.id == post.user }?.name ?: post.user ?: "Unknown"
                    downloadRepository.downloadPostMedia(post, creatorName)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Selection State
    private val _selectedPostIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val selectedPostIds: StateFlow<Set<String>> = _selectedPostIds.asStateFlow()

    private val _isSelectionMode = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun toggleSelection(post: com.example.kemono.data.model.Post) {
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
        val allFavorites = favoritePosts.value
        val postsToDownload = allFavorites.filter { it.id in selectedIds }
        
        viewModelScope.launch {
            postsToDownload.forEach { favPost ->
                try {
                    // Fetch full post to get file/attachment details
                    if (favPost.service.isNotBlank() && !favPost.user.isNullOrBlank() && favPost.id.isNotBlank()) {
                         val fullPost = repository.getPost(favPost.service, favPost.user, favPost.id)
                         val creatorName = fullPost.user // or favPost.user name lookup? FullPost has user ID usually.
                         // Actually Post.user is often the creator ID. Name might need lookup or just use ID.

                         fullPost.file?.let { file ->
                            if (!file.path.isNullOrEmpty()) {
                                val url = "https://kemono.cr${file.path}"
                                val mediaType = if (com.example.kemono.util.getMediaType(file.path) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                                downloadRepository.downloadFile(
                                    url,
                                    file.name ?: "file",
                                    fullPost.id ?: "",
                                    fullPost.title ?: "",
                                    fullPost.user ?: "",
                                    creatorName ?: "",
                                    mediaType
                                )
                            }
                        }

                        fullPost.attachments.forEach { attachment ->
                            if (!attachment.path.isNullOrEmpty()) {
                                val url = "https://kemono.cr${attachment.path}"
                                val mediaType = if (com.example.kemono.util.getMediaType(attachment.path) == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                                downloadRepository.downloadFile(
                                    url,
                                    attachment.name ?: "attachment",
                                    fullPost.id ?: "",
                                    fullPost.title ?: "",
                                    fullPost.user ?: "",
                                    creatorName ?: "",
                                    mediaType
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Log or handle error (e.g. offline and not cached)
                    e.printStackTrace()
                }
            }
            clearSelection()
        }
    }
}

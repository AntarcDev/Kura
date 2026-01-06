package com.example.kemono.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Account
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.model.FavoritePost
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus = _importStatus.asStateFlow()

    // Favorites Logic
    val favorites: StateFlow<List<FavoriteCreator>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePosts: StateFlow<List<FavoritePost>> = repository.getAllFavoritePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val layoutMode = settingsRepository.favoriteLayoutMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "List")

    val gridDensity = settingsRepository.gridDensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        fetchAccount()
        viewModelScope.launch {
            repository.loginEvent.collect {
                fetchAccount()
            }
        }
    }

    fun fetchAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchAccount()
            result.onSuccess {
                _account.value = it
                _error.value = null
            }.onFailure {
                // If 401/403, we are not logged in.
                _error.value = "Not logged in"
                _account.value = null
            }
            _isLoading.value = false
        }
    }

    fun importFavorites() {
        viewModelScope.launch {
            _importStatus.value = "Importing..."
            try {
                // Use the new repository method that doesn't need a manually passed cookie
                // as it uses the session from CookieJar
                val count = repository.importFavorites() 
                _importStatus.value = "Successfully imported $count favorites"
                // Refresh favorites is automatic via Flow
            } catch (e: Exception) {
                _importStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Implement filtering if needed, currently UI filters or we can filter the flow here
    }

    fun toggleFavoritePost(post: com.example.kemono.data.model.Post) {
        viewModelScope.launch {
            // Re-construct FavoritePost from Post (simplified)
             val favPost = FavoritePost(
                id = post.id ?: "",
                service = post.service ?: "",
                user = post.user ?: "", // Assuming user/service might be null based on error
                title = post.title ?: "",
                content = post.content ?: "",
                published = post.published ?: "",
                thumbnailPath = post.file?.path ?: post.attachments.firstOrNull()?.path,
                added = System.currentTimeMillis()
            )
            
            // Check existence and toggle
            // Ideally Repo should have toggle, but we can check flow or try catch insert/delete
            // For now assuming add for simplicity or we need isFavorite check
            // Check if already favorites
            val exists = favoritePosts.value.any { it.id == post.id }
            
             try {
                if (exists) {
                    repository.removeFavoritePost(favPost)
                } else {
                    repository.addFavoritePost(favPost)
                }
            } catch (e: Exception) {
               // Handle error
            }
        }
    }

    fun toggleFavoriteCreator(creator: Creator) {
        viewModelScope.launch {
            val favCreator = FavoriteCreator(
                id = creator.id,
                service = creator.service,
                name = creator.name,
                updated = creator.updated.toString()
            )
            
            val exists = favorites.value.any { it.id == creator.id }
            
            try {
                if (exists) {
                    repository.removeFavorite(favCreator)
                } else {
                    repository.addFavorite(favCreator)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

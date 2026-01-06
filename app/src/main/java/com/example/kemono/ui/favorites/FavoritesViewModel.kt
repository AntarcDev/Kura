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
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: KemonoRepository,
    private val settingsRepository: SettingsRepository
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
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

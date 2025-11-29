package com.example.kemono.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.repository.KemonoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: KemonoRepository
) : ViewModel() {

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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

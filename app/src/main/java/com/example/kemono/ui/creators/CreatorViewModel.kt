package com.example.kemono.ui.creators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CreatorViewModel
@Inject
constructor(private val repository: KemonoRepository, networkMonitor: NetworkMonitor) :
        ViewModel() {

    val isOnline =
            networkMonitor.isOnline.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    true
            )

    private val _allCreators = MutableStateFlow<List<Creator>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val creators: StateFlow<List<Creator>> =
            combine(_allCreators, _searchQuery) { creators, query ->
                        if (query.isBlank()) creators
                        else
                                creators.filter {
                                    it.name.contains(query, ignoreCase = true) ||
                                            it.id.contains(query, ignoreCase = true)
                                }
                    }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchCreators()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun fetchCreators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getCreators()
                _allCreators.value = result.sortedByDescending { it.updated }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    val favorites =
            repository
                    .getAllFavorites()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(creator: Creator) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.id == creator.id }
            val favCreator =
                    com.example.kemono.data.model.FavoriteCreator(
                            id = creator.id,
                            service = creator.service,
                            name = creator.name,
                            updated = creator.updated.toString()
                    )
            if (isFav) {
                repository.removeFavorite(favCreator)
            } else {
                repository.addFavorite(favCreator)
            }
        }
    }
}

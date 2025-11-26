package com.example.kemono.ui.creators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.FavoriteCreator
import com.example.kemono.data.repository.KemonoRepository
import com.example.kemono.data.repository.SettingsRepository
import com.example.kemono.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CreatorViewModel
@Inject
constructor(
        private val repository: KemonoRepository,
        networkMonitor: NetworkMonitor,
        private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isOnline =
            networkMonitor.isOnline.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    true
            )

    val favorites =
            repository
                    .getAllFavorites()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gridSize =
            settingsRepository.gridSize.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    "Comfortable"
            )

    private val _allCreators = MutableStateFlow<List<Creator>>(emptyList())
    private val _popularCreators = MutableStateFlow<List<Creator>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.Updated)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _selectedServices = MutableStateFlow<Set<String>>(emptySet())
    val selectedServices: StateFlow<Set<String>> = _selectedServices.asStateFlow()

    val availableServices: StateFlow<List<String>> =
            _allCreators
                    .map { creators -> creators.map { it.service }.distinct().sorted() }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine filter and sort options first
    private val _filterState =
            combine(_searchQuery, _sortOption, _selectedServices) { query, sort, services ->
                Triple(query, sort, services)
            }

    val creators: StateFlow<List<Creator>> =
            combine(_allCreators, _popularCreators, favorites, _filterState) {
                            all,
                            popular,
                            favs,
                            (query, sort, services) ->
                        val sourceList = if (sort == SortOption.Popular) popular else all
                        var result = sourceList

                        // Filter by service
                        if (services.isNotEmpty()) {
                            result = result.filter { it.service in services }
                        }

                        // Filter by query
                        if (query.isNotBlank()) {
                            result =
                                    result.filter {
                                        it.name.contains(query, ignoreCase = true) ||
                                                it.id.contains(query, ignoreCase = true)
                                    }
                        }

                        // Sort
                        when (sort) {
                            SortOption.Name -> result.sortedBy { it.name }
                            SortOption.Updated -> result.sortedByDescending { it.updated }
                            SortOption.Favorites -> {
                                val favIds = favs.map { it.id }.toSet()
                                result.sortedWith(
                                        compareByDescending<Creator> { it.id in favIds }
                                                .thenByDescending { it.updated }
                                )
                            }
                            SortOption.Popular -> result
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

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        if (option == SortOption.Popular && _popularCreators.value.isEmpty()) {
            fetchPopularCreators()
        }
    }

    fun toggleServiceFilter(service: String) {
        val current = _selectedServices.value
        if (current.contains(service)) {
            _selectedServices.value = current - service
        } else {
            _selectedServices.value = current + service
        }
    }

    fun clearFilters() {
        _selectedServices.value = emptySet()
        _sortOption.value = SortOption.Updated
    }

    fun fetchCreators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getCreators()
                _allCreators.value = result.sortedByDescending { it.updated }

                // If we are in popular mode, refresh that too
                if (_sortOption.value == SortOption.Popular) {
                    fetchPopularCreators()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchPopularCreators() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPopularCreators()
                _popularCreators.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to fetch popular creators"
                // Fallback to updated if failed?
                if (_popularCreators.value.isEmpty()) {
                    _sortOption.value = SortOption.Updated
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

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

enum class SortOption {
    Name,
    Updated,
    Favorites,
    Popular
}

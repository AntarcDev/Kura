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
    
    // New Ascending State
    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _selectedServices = MutableStateFlow<Set<String>>(emptySet())
    val selectedServices: StateFlow<Set<String>> = _selectedServices.asStateFlow()

    val availableServices: StateFlow<List<String>> =
        _allCreators
            .map { creators -> creators.map { it.service }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchMode = MutableStateFlow(SearchMode.Artists)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val _posts = MutableStateFlow<List<com.example.kemono.data.model.Post>>(emptyList())
    val posts: StateFlow<List<com.example.kemono.data.model.Post>> = _posts.asStateFlow()

    private var currentOffset = 0

    // Selection State
    private val _selectedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPostIds: StateFlow<Set<String>> = _selectedPostIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @Inject
    lateinit var downloadRepository: com.example.kemono.data.repository.DownloadRepository

    // Combine filter and sort options first
    private val _debouncedSearchQuery = MutableStateFlow("")

    private data class FilterState(
        val query: String,
        val sort: SortOption,
        val services: Set<String>,
        val ascending: Boolean
    )

    private val _filterState =
        combine(_debouncedSearchQuery, _sortOption, _selectedServices, _sortAscending) { query, sort, services, asc ->
            FilterState(query, sort, services, asc)
        }

    val creators: StateFlow<List<Creator>> =
        combine(_allCreators, _popularCreators, favorites, _filterState) {
            all,
            popular,
            favs,
            (query, sort, services, ascending) ->
            
            if (query == "52635557") {
                 val exists = all.any { it.id == "52635557" }
                 // Direct Check removed
            }

            // Start with all creators or popular list depending on sort option
            var result = when (sort) {
                SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> popular.ifEmpty { all }
                else -> all
            }

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
            val sorted = when (sort) {
                SortOption.Name -> result.sortedBy { it.name }
                SortOption.Updated -> result.sortedBy { it.updated }
                SortOption.Indexed -> result.sortedBy { it.indexed }
                SortOption.Service -> result.sortedBy { it.service }
                SortOption.Popular, SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> {
                    result.sortedBy { it.favorited ?: 0 }
                }
                SortOption.Random -> result.shuffled()
            }

            // Apply ascending/descending
            if (ascending) sorted else sorted.reversed()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCreators()
        fetchTags()
    }

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
        if (mode == SearchMode.Posts && _posts.value.isEmpty()) {
            fetchPosts()
        }
    }

    fun toggleTag(tag: String) {
        val current = _selectedTags.value
        if (current.contains(tag)) {
            _selectedTags.value = current - tag
        } else {
            _selectedTags.value = current + tag
        }
        if (_searchMode.value == SearchMode.Posts) {
            fetchPosts()
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce 500ms for both modes

            if (_searchMode.value == SearchMode.Posts) {
                fetchPosts()
            } else {
                _debouncedSearchQuery.value = query
            }
        }
    }

    fun loadMorePosts() {
        if (!_isLoading.value) {
            fetchPosts(reset = false)
        }
    }

    fun fetchPosts(reset: Boolean = true) {
        viewModelScope.launch {
            if (reset) {
                _isLoading.value = true
                currentOffset = 0
            }
            try {
                val query = _searchQuery.value
                val tags = _selectedTags.value.toList()
                val sort = _sortOption.value

                val result = when (sort) {
                    SortOption.Popular, SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> {
                        val period = when (sort) {
                            SortOption.PopularDay -> "day"
                            SortOption.PopularWeek -> "week"
                            SortOption.PopularMonth -> "month"
                            else -> "week" // Default to week
                        }
                        repository.getPopularPosts(period = period, offset = currentOffset)
                    }
                    SortOption.Random -> {
                        if (reset) repository.getRandomPosts() else emptyList()
                    }
                    else -> {
                        repository.getRecentPosts(
                            offset = currentOffset,
                            query = if (query.isBlank()) null else query,
                            tags = if (tags.isEmpty()) null else tags
                        )
                    }
                }

                if (reset) {
                    _posts.value = result
                } else {
                    _posts.value = _posts.value + result
                }

                if (result.isNotEmpty() && sort != SortOption.Random) {
                    currentOffset += 50
                }
            } catch (e: Exception) {
                if (e is retrofit2.HttpException && e.code() == 429) {
                    _error.value = "Too many requests. Please wait a moment."
                } else {
                    _error.value = e.message ?: "Failed to fetch posts"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchTags() {
        viewModelScope.launch {
            try {
                val result = repository.getTags()
                _tags.value = result.sorted()
            } catch (e: Exception) {
                _error.value = "Tags error: ${e.message}"
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        if ((option == SortOption.PopularDay || option == SortOption.PopularWeek || option == SortOption.PopularMonth) && _popularCreators.value.isEmpty()) {
            fetchPopularCreators()
        }
        if (_searchMode.value == SearchMode.Posts) {
            fetchPosts()
        }
    }
    
    fun toggleSortAscending() {
        _sortAscending.value = !_sortAscending.value
    }

    fun toggleServiceFilter(service: String) {
        val current = _selectedServices.value
        if (current.contains(service)) {
            _selectedServices.value = current - service
        } else {
            _selectedServices.value = current + service
        }
        if (_searchMode.value == SearchMode.Posts) {
            fetchPosts()
        }
    }

    fun clearFilters() {
        _selectedServices.value = emptySet()
        _selectedTags.value = emptySet()
        _sortOption.value = SortOption.Updated
        if (_searchMode.value == SearchMode.Posts) {
            fetchPosts()
        }
    }

    fun fetchCreators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getCreators()
                _allCreators.value = result.sortedByDescending { it.updated }

                if (_sortOption.value == SortOption.PopularDay || _sortOption.value == SortOption.PopularWeek || _sortOption.value == SortOption.PopularMonth) {
                    fetchPopularCreators()
                }
            } catch (e: Exception) {
                if (e is retrofit2.HttpException && e.code() == 429) {
                    _error.value = "Too many requests. Please wait a moment."
                } else {
                    _error.value = e.message ?: "Unknown error occurred"
                }
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
                if (e is retrofit2.HttpException && e.code() == 429) {
                    _error.value = "Too many requests. Please wait a moment."
                } else {
                    _error.value = e.message ?: "Failed to fetch popular creators"
                }
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
                FavoriteCreator(
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
        val postsToDownload = _posts.value.filter { it.id in selectedIds }
        
        viewModelScope.launch {
            postsToDownload.forEach { post ->
                val creatorName = post.user 
                
                post.file?.let { file ->
                    if (!file.path.isNullOrEmpty()) {
                        val url = "https://kemono.cr${file.path}"
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

                post.attachments.forEach { attachment ->
                    if (!attachment.path.isNullOrEmpty()) {
                        val url = "https://kemono.cr${attachment.path}"
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

enum class SortOption {
    Name,
    Updated,
    Indexed,
    Service,
    Popular,
    PopularDay,
    PopularWeek,
    PopularMonth,
    Random
}

enum class SearchMode {
    Artists,
    Posts
}

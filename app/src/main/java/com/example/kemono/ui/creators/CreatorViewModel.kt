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
    // We need a debounced query for the filter state to avoid rapid updates
    private val _debouncedSearchQuery = MutableStateFlow("")

    private val _filterState =
            combine(_debouncedSearchQuery, _sortOption, _selectedServices) { query, sort, services ->
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
                            SortOption.Popular, SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> result
                            SortOption.Random -> result.shuffled() // Simple shuffle for creators if random is selected
                        }
                    }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    @Inject
    lateinit var downloadRepository: com.example.kemono.data.repository.DownloadRepository

    init {
        fetchCreators()
        fetchTags()
    }

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
        // Clear search query when switching modes to avoid confusion?
        // _searchQuery.value = ""
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
                        // Popular posts API doesn't support tags/query in the same way, or maybe it does?
                        // API def: getPopularPosts(date, period, offset)
                        // It doesn't seem to support query/tags.
                        repository.getPopularPosts(period = period, offset = currentOffset)
                    }
                    SortOption.Random -> {
                        if (reset) repository.getRandomPosts() else emptyList() // Random doesn't support pagination really
                    }
                    else -> {
                        // Default / Recent
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
        if (option == SortOption.Popular && _popularCreators.value.isEmpty()) {
            fetchPopularCreators()
        }
        if (_searchMode.value == SearchMode.Posts) {
            fetchPosts()
        }
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

                // If we are in popular mode, refresh that too
                if (_sortOption.value == SortOption.Popular) {
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
                // Ensure we have creator name, or fallback
                val creatorName = post.user // Ideally fetch name if needed, but ID is safe for now
                
                // Download main file
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

                // Download attachments
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
    Favorites,
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

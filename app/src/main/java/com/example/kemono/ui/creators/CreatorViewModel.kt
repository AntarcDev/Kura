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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.kemono.data.paging.PostPagingSource

@HiltViewModel
class CreatorViewModel
@Inject
constructor(
    private val repository: KemonoRepository,
    networkMonitor: NetworkMonitor,
    private val settingsRepository: SettingsRepository,
    private val downloadRepository: com.example.kemono.data.repository.DownloadRepository
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

    val favoritePostIds: StateFlow<Set<String>> =
        repository
            .getAllFavoritePosts()
            .map { posts -> posts.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val artistLayoutMode = settingsRepository.artistLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Grid")
    val postLayoutMode = settingsRepository.postLayoutMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "List")
    val gridDensity = settingsRepository.gridDensity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

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
// Duplicates removed
    
    private val _searchMode = MutableStateFlow(SearchMode.Artists)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagedPosts: Flow<PagingData<com.example.kemono.data.model.Post>> = combine(_debouncedSearchQuery, _sortOption, _selectedTags) { query, sort, tags ->
        Triple(query, sort, tags)
    }.flatMapLatest { (query, sort, tags) ->
        when (sort) {
            SortOption.Popular, SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> {
                val period = when (sort) {
                    SortOption.PopularDay -> "day"
                    SortOption.PopularWeek -> "week"
                    SortOption.PopularMonth -> "month"
                    else -> "week"
                }
                repository.getPagedPopularPosts(period = period)
            }
            SortOption.Random -> {
                Pager(config = PagingConfig(pageSize = 5, initialLoadSize = 5)) {
                    PostPagingSource { _, _ -> repository.getRandomPosts() }
                }.flow
            }
            else -> {
                repository.getPagedRecentPosts(
                    query = query.ifBlank { null },
                    tags = tags.toList().ifEmpty { null }
                )
            }
        }
    }.cachedIn(viewModelScope)

    // Selection State
    private val _selectedPosts = MutableStateFlow<Set<com.example.kemono.data.model.Post>>(emptySet())
    val selectedPosts: StateFlow<Set<com.example.kemono.data.model.Post>> = _selectedPosts.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val downloadedPostIds: StateFlow<Set<String>> = downloadRepository.getDownloadedPostIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Combine filter and sort options first
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
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchCreators()
        fetchTags()
    }

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
    }

    fun toggleTagFilter(tag: String) {
        val current = _selectedTags.value
        if (current.contains(tag)) {
            _selectedTags.value = current - tag
        } else {
            _selectedTags.value = current + tag
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Debounce 1000ms
            _debouncedSearchQuery.value = query
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
    }

    fun clearFilters() {
        _selectedServices.value = emptySet()
        _selectedTags.value = emptySet()
        _sortOption.value = SortOption.Updated
    }

    fun fetchCreators(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
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
                _isRefreshing.value = false
            }
        }
    }

    private fun fetchPopularCreators() {
        viewModelScope.launch {
            // We don't necessarily need to trigger global loading for background fetching popular creators if not refreshing
            // but for simplicity we can keep it light.
            // If called from fetchCreators/setSortOption, the parent function handles loading state mostly.
            // But if called standalone, we might want it.
            // However, seeing usage, it's mostly auxiliary.
            
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

    fun toggleFavoritePost(post: com.example.kemono.data.model.Post) {
        viewModelScope.launch {
            val postId = post.id ?: return@launch
            val isFav = favoritePostIds.value.contains(postId)
            val favPost = com.example.kemono.data.model.FavoritePost(
                id = postId,
                service = post.service ?: "",
                user = post.user ?: "",
                title = post.title ?: "",
                content = post.content ?: "",
                thumbnailPath = post.file?.path ?: post.attachments.firstOrNull()?.path,
                published = post.published ?: ""
            )
            if (isFav) {
                repository.removeFavoritePost(favPost)
            } else {
                repository.addFavoritePost(favPost)
            }
        }
    }

    fun toggleSelection(post: com.example.kemono.data.model.Post) {
        val current = _selectedPosts.value
        val postId = post.id ?: return
        if (current.any { it.id == postId }) {
            _selectedPosts.value = current.filterNot { it.id == postId }.toSet()
            if (_selectedPosts.value.isEmpty()) {
                _isSelectionMode.value = false
            }
        } else {
            _selectedPosts.value = current + post
            _isSelectionMode.value = true
        }
    }

    fun clearSelection() {
        _selectedPosts.value = emptySet()
        _isSelectionMode.value = false
    }

    // Search History
    val searchHistory = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            repository.addToSearchHistory(query)
        }
    }

    fun removeFromSearchHistory(query: String) {
        viewModelScope.launch {
            repository.removeFromSearchHistory(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    // Existing methods...
    
    fun downloadSelectedPosts() {
        val postsToDownload = _selectedPosts.value
        
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

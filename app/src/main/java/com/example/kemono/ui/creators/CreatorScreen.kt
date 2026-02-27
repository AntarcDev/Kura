package com.example.kemono.ui.creators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.compose.itemContentType
import androidx.paging.LoadState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.Post
import com.example.kemono.ui.components.CreatorItemSkeleton
import com.example.kemono.ui.components.PostItemSkeleton
import com.example.kemono.ui.components.CreatorTile
import com.example.kemono.ui.components.SelectionTopAppBar
import com.example.kemono.ui.components.UnifiedTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    viewModel: CreatorViewModel = hiltViewModel(),
    settingsViewModel: com.example.kemono.ui.settings.SettingsViewModel = hiltViewModel(),
    onCreatorClick: (Creator) -> Unit,
    onPostClick: (Post) -> Unit
) {
    val creators by viewModel.creators.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoritePostIds by viewModel.favoritePostIds.collectAsState()
    val downloadedPostIds by viewModel.downloadedPostIds.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val artistLayoutMode by viewModel.artistLayoutMode.collectAsState()
    val postLayoutMode by viewModel.postLayoutMode.collectAsState()
    val gridDensity by viewModel.gridDensity.collectAsState()
    val autoplayGifs by settingsViewModel.autoplayGifs.collectAsState()
    val imageQuality by settingsViewModel.imageQuality.collectAsState()



    val searchMode by viewModel.searchMode.collectAsState()
    val posts = viewModel.pagedPosts.collectAsLazyPagingItems()
    val tags by viewModel.tags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPosts by viewModel.selectedPosts.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    // Sync SearchMode -> Pager
    LaunchedEffect(searchMode) {
        val targetPage = if (searchMode == SearchMode.Artists) 0 else 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
    
    // Sync Pager -> SearchMode
    LaunchedEffect(pagerState.currentPage) {
        val newMode = if (pagerState.currentPage == 0) SearchMode.Artists else SearchMode.Posts
        if (searchMode != newMode) {
            viewModel.setSearchMode(newMode)
        }
    }
    
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (creators.isEmpty() && !isLoading) {
            viewModel.fetchCreators()
        }
    }

    if (showFilterSheet) {
        com.example.kemono.ui.components.FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            searchMode = searchMode,
            sortOption = sortOption,
            onSortOptionSelected = { viewModel.setSortOption(it) },
            isAscending = sortAscending,
            onAscendingToggle = viewModel::toggleSortAscending,
            selectedServices = selectedServices,
            onServiceToggle = viewModel::toggleServiceFilter,
            onClearServices = viewModel::clearFilters,
            availableServices = availableServices,
            availableTags = tags,
            selectedTags = selectedTags,
            onTagToggle = viewModel::toggleTagFilter
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedPosts.size,
                    onClearSelection = viewModel::clearSelection,
                    onDownloadSelected = viewModel::downloadSelectedPosts
                )
            } else {
                UnifiedTopBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { 
                        if (searchMode == SearchMode.Artists) viewModel.fetchCreators() 
                        viewModel.addToSearchHistory(searchQuery)
                    },
                    onClearSearch = { viewModel.onSearchQueryChange("") },
                    placeholderText = if (searchMode == SearchMode.Artists) "Search creators..." else "Search posts...",
                    onFilterClick = { showFilterSheet = true },
                    
                    searchHistory = searchHistory,
                    onHistoryItemClick = { query -> 
                        viewModel.onSearchQueryChange(query)
                        if (searchMode == SearchMode.Artists) viewModel.fetchCreators()
                        viewModel.addToSearchHistory(query)
                    },
                    onHistoryItemRemove = { viewModel.removeFromSearchHistory(it) },
                    onClearHistory = { viewModel.clearSearchHistory() }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = if (searchMode == SearchMode.Artists) isRefreshing else posts.loadState.refresh is LoadState.Loading,
            onRefresh = { 
                if (searchMode == SearchMode.Artists) viewModel.fetchCreators(isRefresh = true) 
                else posts.refresh() 
            },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Mode Toggle (Tabs)
                val scope = rememberCoroutineScope()
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = { Text("Creators") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = { Text("Posts") }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f) // Fill remaining space
                ) { page ->
                    if (error != null) {
                         Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { 
                                if (page == 0) viewModel.fetchCreators() else posts.retry() 
                            }) {
                                Text("Retry")
                            }
                        }
                    } else if (page == 0) {
                        // Artists View (Moved inside Pager)
                        val density = when (gridDensity) {
                            "Small" -> 120.dp
                            "Large" -> 200.dp
                            else -> 150.dp // Medium
                        }
                        if (isLoading && creators.isEmpty()) {
                            // Loading Skeleton
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = density),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(12) {
                                    CreatorItemSkeleton(compact = true)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                                val scope = rememberCoroutineScope()
                                
                                if (artistLayoutMode == "List") {
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            count = creators.size,
                                            key = { index -> creators[index].id ?: "creator_$index" }
                                        ) { index ->
                                            val creator = creators[index]
                                            CreatorTile(
                                                creator = creator,
                                                isFavorite = favorites.any { it.id == creator.id },
                                                onClick = { onCreatorClick(creator) },
                                                onFavoriteClick = { viewModel.toggleFavorite(creator) },
                                                compact = false,
                                                autoplayGifs = autoplayGifs
                                            )
                                        }
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Adaptive(minSize = density),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            count = creators.size,
                                            key = { index -> creators[index].id ?: "creator_$index" }
                                        ) { index ->
                                            val creator = creators[index]
                                            CreatorTile(
                                                creator = creator,
                                                isFavorite = favorites.any { it.id == creator.id },
                                                onClick = { onCreatorClick(creator) },
                                                onFavoriteClick = { viewModel.toggleFavorite(creator) },
                                                compact = true,
                                                autoplayGifs = autoplayGifs
                                            )
                                        }
                                    }
                                }
                                
                                val showButton by remember {
                                    derivedStateOf {
                                        if (artistLayoutMode == "List") listState.firstVisibleItemIndex > 0
                                        else gridState.firstVisibleItemIndex > 0
                                    }
                                }
                                
                                com.example.kemono.ui.components.ScrollToTopButton(
                                    visible = showButton,
                                    onClick = {
                                        scope.launch {
                                            if (artistLayoutMode == "List") listState.animateScrollToItem(0)
                                            else gridState.animateScrollToItem(0)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                                )
                            }
                        }
                    } else {
                        // Posts View (Moved inside conditional -> else block of Pager page 1)
                        val density = when (gridDensity) {
                            "Small" -> 120.dp
                            "Large" -> 200.dp
                            else -> 150.dp // Medium
                        }
                        if (posts.itemCount == 0 && posts.loadState.refresh is LoadState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                                val scope = rememberCoroutineScope()
                                
                                if (postLayoutMode == "List") {
                                    // List requires state to keep scroll position
    
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            count = posts.itemCount,
                                            key = posts.itemKey { it.id ?: it.hashCode() },
                                            contentType = posts.itemContentType { "post" }
                                        ) { index ->
                                            val post = posts[index]
                                            
                                            if (post != null) {
                                                com.example.kemono.ui.components.PostItem(
                                                    post = post,
                                                    selected = selectedPosts.any { it.id == post.id },
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            viewModel.toggleSelection(post)
                                                        } else {
                                                            onPostClick(post)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        viewModel.toggleSelection(post)
                                                    },
                                                    onCreatorClick = {
                                                        val creator = Creator(
                                                            id = post.user ?: "",
                                                            service = post.service ?: "",
                                                            name = "Unknown",
                                                            updated = 0,
                                                            indexed = 0
                                                        )
                                                        onCreatorClick(creator)
                                                    },
                                                    isFavorite = favoritePostIds.contains(post.id),
                                                    onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                    isDownloaded = downloadedPostIds.contains(post.id),
                                                    autoplayGifs = autoplayGifs,
                                                    imageQuality = imageQuality
                                                )
                                            }
                                        }
                                        when (val state = posts.loadState.append) {
                                            is LoadState.Loading -> {
                                                item {
                                                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }
                                            is LoadState.Error -> {
                                                item {
                                                    Text("Error appending: ${state.error.localizedMessage}", modifier = Modifier.padding(16.dp))
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Adaptive(minSize = density),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            count = posts.itemCount,
                                            key = posts.itemKey { it.id ?: it.hashCode() },
                                            contentType = posts.itemContentType { "post" }
                                        ) { index ->
                                            val post = posts[index]
    
                                            if (post != null) {
                                                com.example.kemono.ui.components.PostGridItem(
                                                    post = post,
                                                    selected = selectedPosts.any { it.id == post.id },
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            viewModel.toggleSelection(post)
                                                        } else {
                                                            onPostClick(post)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        viewModel.toggleSelection(post)
                                                    },
                                                    isFavorite = favoritePostIds.contains(post.id),
                                                    onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                    isDownloaded = downloadedPostIds.contains(post.id),
                                                    autoplayGifs = autoplayGifs,
                                                    imageQuality = imageQuality,
                                                    showCreator = true,
                                                    onCreatorClick = {
                                                         val creator = com.example.kemono.data.model.Creator(
                                                            id = post.user ?: "",
                                                            service = post.service ?: "",
                                                            name = "Unknown",
                                                            updated = 0,
                                                            indexed = 0
                                                        )
                                                        onCreatorClick(creator)
                                                    }
                                                )
                                            }
                                        }
                                        when (val state = posts.loadState.append) {
                                            is LoadState.Loading -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }
                                            is LoadState.Error -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Text("Error appending: ${state.error.localizedMessage}", modifier = Modifier.padding(16.dp))
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                
                                val showButton by remember {
                                    derivedStateOf {
                                        if (postLayoutMode == "List") listState.firstVisibleItemIndex > 0
                                        else gridState.firstVisibleItemIndex > 0
                                    }
                                }
                                
                                com.example.kemono.ui.components.ScrollToTopButton(
                                    visible = showButton,
                                    onClick = {
                                        scope.launch {
                                            if (postLayoutMode == "List") listState.animateScrollToItem(0)
                                            else gridState.animateScrollToItem(0)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                                )
                            }
                        }
                } 
            }
        }
    }
    }
}


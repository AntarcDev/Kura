package com.example.kemono.ui.creators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    viewModel: CreatorViewModel = hiltViewModel(),
    onCreatorClick: (Creator) -> Unit,
    onPostClick: (Post) -> Unit
) {
    val creators by viewModel.creators.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()
    val gridSize by viewModel.gridSize.collectAsState()



    var showTagsDialog by remember { mutableStateOf(false) }

    val searchMode by viewModel.searchMode.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()


    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPostIds by viewModel.selectedPostIds.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        com.example.kemono.ui.components.FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            sortOption = sortOption,
            onSortOptionSelected = { viewModel.setSortOption(it) },
            isAscending = sortAscending,
            onAscendingToggle = viewModel::toggleSortAscending,
            selectedServices = selectedServices,
            onServiceToggle = viewModel::toggleServiceFilter,
            onClearServices = viewModel::clearFilters, // Or create specific clear service function if clearFilters clears everything
            availableServices = availableServices
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedPostIds.size,
                    onClearSelection = viewModel::clearSelection,
                    onDownloadSelected = viewModel::downloadSelectedPosts
                )
            } else {
                val sortOptions = if (searchMode == SearchMode.Artists) {
                    emptyList() // We use BottomSheet
                } else {
                    listOf("Recent", "Popular (Day)", "Popular (Week)", "Popular (Month)", "Random")
                }
                
                com.example.kemono.ui.components.UnifiedTopBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = { 
                        if (searchMode == SearchMode.Posts) viewModel.fetchPosts()
                    },
                    onClearSearch = { viewModel.onSearchQueryChange("") },
                    sortOptions = sortOptions,
                    selectedSort = when (sortOption) {
                        SortOption.Name -> "Name"
                        SortOption.Updated -> if (searchMode == SearchMode.Artists) "Updated" else "Recent"
                        // SortOption.Favorites removed
                        SortOption.Popular, SortOption.PopularDay, SortOption.PopularWeek, SortOption.PopularMonth -> "Popular"
                        SortOption.Indexed -> "Indexed"
                        SortOption.Service -> "Service"
                        SortOption.Random -> "Random"
                    },
                    onSortSelected = { option -> 
                        // Only for Posts mode legacy dropdown
                        val newSort = when (option) {
                            "Recent" -> SortOption.Updated
                            "Popular (Day)" -> SortOption.PopularDay
                            "Popular (Week)" -> SortOption.PopularWeek
                            "Popular (Month)" -> SortOption.PopularMonth
                            "Random" -> SortOption.Random
                            else -> SortOption.Updated
                        }
                        viewModel.setSortOption(newSort)
                    },
                    filterOptions = emptyList(), // We use BottomSheet for artists, nothing for Posts yet?
                    selectedFilters = emptyList(),
                    onFilterSelected = {},
                    onFilterClick = if (searchMode == SearchMode.Artists) { { showFilterSheet = true } } else null,
                    onTagsClick = if (searchMode == SearchMode.Posts) { { showTagsDialog = true } } else null,
                    placeholderText = if (searchMode == SearchMode.Artists) "Search artists or IDs..." else "Search posts..."
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { 
                if (searchMode == SearchMode.Artists) viewModel.fetchCreators() 
                else viewModel.fetchPosts() 
            },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = searchMode == SearchMode.Artists,
                        onClick = { viewModel.setSearchMode(SearchMode.Artists) },
                        label = { Text("Artists") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = searchMode == SearchMode.Posts,
                        onClick = { viewModel.setSearchMode(SearchMode.Posts) },
                        label = { Text("Posts") }
                    )
                }



                if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    if (searchMode == SearchMode.Artists) {
                        val isCompact = gridSize == "Compact"
                        
                        if (isRefreshing && creators.isEmpty()) {
                            if (isCompact) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(12) {
                                        CreatorItemSkeleton(compact = true)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(10) {
                                        CreatorItemSkeleton(compact = false)
                                    }
                                }
                            }
                        } else if (isCompact) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(creators) { creator ->
                                    CreatorTile(
                                        creator = creator,
                                        isFavorite = favorites.any { it.id == creator.id },
                                        onClick = { onCreatorClick(creator) },
                                        onFavoriteClick = { viewModel.toggleFavorite(creator) },
                                        compact = true
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(creators) { creator ->
                                    CreatorTile(
                                        creator = creator,
                                        isFavorite = favorites.any { it.id == creator.id },
                                        onClick = { onCreatorClick(creator) },
                                        onFavoriteClick = { viewModel.toggleFavorite(creator) },
                                        compact = false
                                    )
                                }
                            }
                        }
                    } else {
                        // Posts List
                        if (isRefreshing && posts.isEmpty()) {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(10) {
                                    PostItemSkeleton()
                                }
                            }
                        } else {
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                            
                            // Infinite scroll handler
                            val reachedBottom by remember {
                                derivedStateOf {
                                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                    lastVisibleItem?.index != 0 && lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
                                }
                            }
    
                            LaunchedEffect(reachedBottom) {
                                if (reachedBottom) {
                                    viewModel.loadMorePosts()
                                }
                            }
    
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(posts) { post ->
                                    com.example.kemono.ui.components.PostItem(
                                        post = post,
                                        selected = selectedPostIds.contains(post.id),
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
                                            // Create a minimal creator object for navigation
                                            val creator = Creator(
                                                id = post.user ?: "",
                                                service = post.service ?: "",
                                                name = "Unknown", // Name will be fetched in profile
                                                updated = 0,
                                                indexed = 0
                                            )
                                            onCreatorClick(creator)
                                        }
                                    )
                                }
                                
                                if (isRefreshing && posts.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showTagsDialog) {
            TagsDialog(
                tags = tags,
                selectedTags = selectedTags,
                onTagToggle = viewModel::toggleTag,
                onDismiss = { showTagsDialog = false }
            )
        }
    }
}

@Composable
fun TagsDialog(
    tags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredTags = remember(tags, searchQuery) {
        if (searchQuery.isBlank()) tags else tags.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Tags") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search tags...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Show selected tags first
                    val (selected, unselected) = filteredTags.partition { selectedTags.contains(it) }
                    
                    items(selected) { tag ->
                        TagItem(tag = tag, selected = true, onToggle = { onTagToggle(tag) })
                    }
                    
                    if (selected.isNotEmpty() && unselected.isNotEmpty()) {
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    }
                    
                    items(unselected) { tag ->
                        TagItem(tag = tag, selected = false, onToggle = { onTagToggle(tag) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun TagItem(tag: String, selected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = tag, style = MaterialTheme.typography.bodyMedium)
    }
}

// CreatorItem moved to ui/components/CreatorItem.kt



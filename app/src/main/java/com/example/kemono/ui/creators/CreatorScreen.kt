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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.kemono.data.model.Creator
import com.example.kemono.data.model.Post
import com.example.kemono.ui.components.CreatorItemSkeleton
import com.example.kemono.ui.components.PostItemSkeleton
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

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val searchMode by viewModel.searchMode.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPostIds by viewModel.selectedPostIds.collectAsState()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedPostIds.size,
                    onClearSelection = viewModel::clearSelection,
                    onDownloadSelected = viewModel::downloadSelectedPosts
                )
            } else {
                TopAppBar(
                    title = { Text("Kura") },
                    actions = {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Filter & Sort")
                        }
                    }
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
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text(if (searchMode == SearchMode.Artists) "Search creators..." else "Search posts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Search Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                                    CreatorItem(
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
                                    CreatorItem(
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
                                                id = post.user,
                                                service = post.service,
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

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                FilterSortBottomSheet(
                    sortOption = sortOption,
                    onSortOptionSelected = viewModel::setSortOption,
                    availableServices = availableServices,
                    selectedServices = selectedServices,
                    onServiceToggle = viewModel::toggleServiceFilter,
                    tags = tags,
                    selectedTags = selectedTags,
                    onTagToggle = viewModel::toggleTag,
                    onReset = viewModel::clearFilters
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSortBottomSheet(
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    availableServices: List<String>,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    tags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onReset: () -> Unit
) {
    var tagSearchQuery by remember { mutableStateOf("") }
    val filteredTags = remember(tags, tagSearchQuery) {
        if (tagSearchQuery.isBlank()) tags else tags.filter { it.contains(tagSearchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sort By", style = MaterialTheme.typography.titleMedium)
        Column {
            SortOption.values().forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortOptionSelected(option) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = sortOption == option,
                        onClick = { onSortOptionSelected(option) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option.name)
                }
            }
        }

        HorizontalDivider()

        Text("Filter by Service", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availableServices.forEach { service ->
                FilterChip(
                    selected = selectedServices.contains(service),
                    onClick = { onServiceToggle(service) },
                    label = { Text(service) }
                )
            }
        }

        HorizontalDivider()

        Text("Filter by Tags", style = MaterialTheme.typography.titleMedium)
        
        // Selected Tags
        if (selectedTags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedTags.forEach { tag ->
                    FilterChip(
                        selected = true,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = tagSearchQuery,
            onValueChange = { tagSearchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search tags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredTags.take(20).forEach { tag ->
                if (!selectedTags.contains(tag)) { // Don't show already selected tags in the list
                    FilterChip(
                        selected = false,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) }
                    )
                }
            }
            if (filteredTags.size > 20) {
                Text("...and ${filteredTags.size - 20} more", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Reset Filters") }
    }
}

@Composable
fun CreatorItem(
    creator: Creator,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    compact: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        if (compact) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = "https://kemono.cr/icons/${creator.service}/${creator.id}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = "https://kemono.cr/icons/${creator.service}/${creator.id}",
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = creator.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = creator.service,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ID: ${creator.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}



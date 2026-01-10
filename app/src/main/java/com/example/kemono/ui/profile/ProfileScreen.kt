package com.example.kemono.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kemono.data.model.Creator
import com.example.kemono.ui.components.CreatorTile
import com.example.kemono.ui.components.PostItem
import com.example.kemono.ui.components.UnifiedTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: com.example.kemono.ui.settings.SettingsViewModel = hiltViewModel(),
    onCreatorClick: (Creator) -> Unit,
    onPostClick: (com.example.kemono.data.model.Post) -> Unit,
    onLoginClick: () -> Unit
) {
    val account by viewModel.account.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    
    val favorites by viewModel.favorites.collectAsState()
    val favoritePosts by viewModel.favoritePosts.collectAsState()
    val downloadedPostIds by viewModel.downloadedPostIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val gridDensity by viewModel.gridDensity.collectAsState()
    val autoplayGifs by settingsViewModel.autoplayGifs.collectAsState()

    val minSize = when (gridDensity) {
        "Small" -> 120.dp
        "Large" -> 200.dp
        else -> 150.dp
    }
    
    // 0 = Creators, 1 = Posts
    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("Creators", "Posts")
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    
    // Sync Tab -> Pager
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }
    
    // Sync Pager -> Tab
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    // Filter favorites based on query
    val filteredFavorites = remember(favorites, searchQuery) {
        if (searchQuery.isBlank()) favorites else favorites.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    val filteredPosts = remember(favoritePosts, searchQuery) {
        if (searchQuery.isBlank()) favoritePosts else favoritePosts.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Column {
                UnifiedTopBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClearSearch = { viewModel.onSearchQueryChange("") },
                    trailingContent = {
                       if (account != null) {
                           IconButton(onClick = viewModel::importFavorites) {
                               Icon(Icons.Default.Refresh, contentDescription = "Sync Favorites")
                           }
                       }
                    }
                )
                
                // Account Header (Collapsible would be nice, but simple column is fine for now)
                if (account != null) {
                    ListItem(
                        headlineContent = { Text(account!!.username) },
                        supportingContent = { Text("ID: ${account!!.id} | Role: ${account!!.role}") },
                        leadingContent = {
                             Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                         colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else if (!isLoading && error != null) {
                     // Not Logged In
                     Button(
                         onClick = onLoginClick,
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(16.dp)
                     ) {
                         Text("Log In to Kemono")
                     }
                }
                
                if (importStatus != null) {
                    Text(
                        text = importStatus!!,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TabRow(selectedTabIndex = selectedTab) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { 
                                selectedTab = index
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (viewModel.isLoading.collectAsState().value && account == null && favorites.isEmpty()) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                         // Creators Tab
                        if (filteredFavorites.isEmpty()) {
                            Box(Modifier.fillMaxSize()) {
                                Text(
                                    text = "No favorites found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                                
                                if (layoutMode == "Grid") {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Adaptive(minSize),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredFavorites) { creator ->
                                            CreatorTile(
                                                creator = creator.toCreator(),
                                                isFavorite = true,
                                                onClick = { onCreatorClick(creator.toCreator()) },
                                                onFavoriteClick = { viewModel.toggleFavoriteCreator(creator.toCreator()) },
                                                compact = true,
                                                autoplayGifs = autoplayGifs
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredFavorites) { creator ->
                                            CreatorTile(
                                                creator = creator.toCreator(),
                                                isFavorite = true,
                                                onClick = { onCreatorClick(creator.toCreator()) },
                                                onFavoriteClick = { viewModel.toggleFavoriteCreator(creator.toCreator()) },
                                                compact = false,
                                                autoplayGifs = autoplayGifs
                                            )
                                        }
                                    }
                                }
                                
                                val showButton by remember {
                                    derivedStateOf {
                                        if (layoutMode == "Grid") gridState.firstVisibleItemIndex > 0
                                        else listState.firstVisibleItemIndex > 0
                                    }
                                }
                                
                                com.example.kemono.ui.components.ScrollToTopButton(
                                    visible = showButton,
                                    onClick = {
                                        scope.launch {
                                            if (layoutMode == "Grid") gridState.animateScrollToItem(0)
                                            else listState.animateScrollToItem(0)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                                )
                            }
                        }
                    } else {
                         // Posts Tab
                         if (filteredPosts.isEmpty()) {
                            Box(Modifier.fillMaxSize()) {
                                Text(
                                    text = "No favorite posts found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                         } else {
                             // Similar logic for posts
                             Box(modifier = Modifier.fillMaxSize()) {
                                 val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                 val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

                                 if (layoutMode == "Grid") {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Adaptive(minSize),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredPosts) { favPost ->
                                             val post = favPost.toPost()
                                            com.example.kemono.ui.components.PostGridItem(
                                                 post = post,
                                                 selected = false,
                                                 onClick = { onPostClick(post) },
                                                 onLongClick = { },
                                                 isFavorite = true,
                                                 onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                 isDownloaded = downloadedPostIds.contains(post.id),
                                                 autoplayGifs = autoplayGifs
                                            )
                                        }
                                    }
                                 } else {
                                     LazyColumn(
                                           state = listState,
                                           contentPadding = PaddingValues(16.dp),
                                           verticalArrangement = Arrangement.spacedBy(8.dp),
                                           modifier = Modifier.fillMaxSize()
                                     ) {
                                         items(filteredPosts) { favPost ->
                                             val post = favPost.toPost()
                                             PostItem(
                                                   post = post,
                                                   onClick = { onPostClick(post) },
                                                   onCreatorClick = {
                                                       onCreatorClick(
                                                           Creator(
                                                               id = post.user ?: "", // Handle potential null
                                                               service = post.service ?: "", // Handle potential null
                                                               name = "", // Name unknown from Post
                                                               favorited = 0,
                                                               indexed = 0,
                                                               updated = 0
                                                           )
                                                       )
                                                   },
                                                   onLongClick = { viewModel.toggleFavoritePost(post) },
                                                   isFavorite = true,
                                                   onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                   isDownloaded = downloadedPostIds.contains(post.id),
                                                   autoplayGifs = autoplayGifs
                                             )
                                         }
                                     }
                                 }
                                 
                                val showButton by remember {
                                    derivedStateOf {
                                        if (layoutMode == "Grid") gridState.firstVisibleItemIndex > 0
                                        else listState.firstVisibleItemIndex > 0
                                    }
                                }
                                
                                com.example.kemono.ui.components.ScrollToTopButton(
                                    visible = showButton,
                                    onClick = {
                                        scope.launch {
                                            if (layoutMode == "Grid") gridState.animateScrollToItem(0)
                                            else listState.animateScrollToItem(0)
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

private fun com.example.kemono.data.model.FavoriteCreator.toCreator(): Creator {
    return Creator(
        id = this.id,
        service = this.service,
        name = this.name,
        updated = this.updated.toLongOrNull() ?: 0L,
        indexed = 0,
        favorited = 0
    )
}

private fun com.example.kemono.data.model.FavoritePost.toPost(): com.example.kemono.data.model.Post {
    return com.example.kemono.data.model.Post(
        id = this.id,
        service = this.service,
        user = this.user,
        title = this.title,
        content = this.content,
        published = this.published,
        file = if (this.thumbnailPath != null) com.example.kemono.data.model.KemonoFile(name = "", path = this.thumbnailPath) else null,
        attachments = emptyList()
    )
}

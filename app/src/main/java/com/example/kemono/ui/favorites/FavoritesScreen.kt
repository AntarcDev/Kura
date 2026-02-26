package com.example.kemono.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kemono.data.model.Creator
import com.example.kemono.ui.components.CreatorTile
import com.example.kemono.ui.components.SelectionTopAppBar
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

@Composable
fun FavoritesScreen(
        viewModel: FavoritesViewModel = hiltViewModel(),
        settingsViewModel: com.example.kemono.ui.settings.SettingsViewModel = hiltViewModel(),
        onCreatorClick: (Creator) -> Unit,
        onPostClick: (com.example.kemono.data.model.Post) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val gridDensity by viewModel.gridDensity.collectAsState()
    val autoplayGifs by settingsViewModel.autoplayGifs.collectAsState()
    val imageQuality by settingsViewModel.imageQuality.collectAsState()
    
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPostIds by viewModel.selectedPostIds.collectAsState()

    val minSize = when (gridDensity) {
        "Small" -> 120.dp
        "Large" -> 200.dp
        else -> 150.dp
    }

    val favoritePosts by viewModel.favoritePosts.collectAsState()
    
    // 0 = Creators, 1 = Posts
    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("Creators", "Posts")

    Scaffold(
            topBar = {
                if (isSelectionMode) {
                    SelectionTopAppBar(
                        selectedCount = selectedPostIds.size,
                        onClearSelection = viewModel::clearSelection,
                        onDownloadSelected = viewModel::downloadSelectedPosts
                    )
                } else {
                    Column {
                        com.example.kemono.ui.components.UnifiedTopBar(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onClearSearch = { viewModel.onSearchQueryChange("") }
                        )
                        TabRow(selectedTabIndex = selectedTab) {
                            titles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }
                }
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (selectedTab == 0) {
                // Creators Tab
                if (favorites.isEmpty()) {
                    Text(
                            text = "No favorite creators yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (layoutMode == "Grid") {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favorites) { creator ->
                                CreatorTile(
                                    creator = creator,
                                    isFavorite = true,
                                    onClick = { onCreatorClick(creator) },
                                    onFavoriteClick = { /* Optional: Remove from favorites */ },
                                    compact = true,
                                    autoplayGifs = autoplayGifs
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                        ) {
                            items(favorites) { creator ->
                                CreatorTile(
                                        creator = creator,
                                        isFavorite = true,
                                        onClick = { onCreatorClick(creator) },
                                        onFavoriteClick = { /* Optional: Remove from favorites */},
                                        compact = false,
                                        autoplayGifs = autoplayGifs
                                )
                            }
                        }
                    }
                }
            } else {
                // Posts Tab
                if (favoritePosts.isEmpty()) {
                    Text(
                        text = "No favorite posts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                     if (layoutMode == "Grid") {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoritePosts) { favPost ->
                                val post = com.example.kemono.data.model.Post(
                                    id = favPost.id,
                                    service = favPost.service,
                                    user = favPost.user,
                                    title = favPost.title,
                                    content = favPost.content,
                                    published = favPost.published,
                                    file = if (favPost.thumbnailPath != null) com.example.kemono.data.model.KemonoFile(name = "", path = favPost.thumbnailPath) else null,
                                    attachments = emptyList() // Not stored in favorites for now
                                )
                                com.example.kemono.ui.components.PostGridItem(
                                    post = post,
                                    selected = selectedPostIds.contains(post.id),
                                    onClick = { 
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(post)
                                        } else {
                                            onPostClick(post)
                                        }
                                    }, 
                                    onLongClick = { viewModel.toggleSelection(post) },
                                    isFavorite = true,
                                    onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                    autoplayGifs = autoplayGifs,
                                    imageQuality = imageQuality,
                                    showCreator = true,
                                    onCreatorClick = { 
                                        if (post.user != null) {
                                             val creator = com.example.kemono.data.model.Creator(
                                                id = post.user,
                                                service = post.service ?: "",
                                                name = "Unknown", // Ideally we be able to navigate even without name
                                                indexed = 0,
                                                updated = 0
                                            )
                                            onCreatorClick(creator)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                        ) {
                            items(favoritePosts) { favPost ->
                                val post = com.example.kemono.data.model.Post(
                                    id = favPost.id,
                                    service = favPost.service,
                                    user = favPost.user,
                                    title = favPost.title,
                                    content = favPost.content,
                                    published = favPost.published,
                                    file = if (favPost.thumbnailPath != null) com.example.kemono.data.model.KemonoFile(name = "", path = favPost.thumbnailPath) else null,
                                    attachments = emptyList()
                                )
                                com.example.kemono.ui.components.PostItem(
                                    post = post,
                                    selected = false,
                                    onClick = { onPostClick(post) },
                                    onLongClick = { viewModel.toggleFavoritePost(post) },
                                    isFavorite = true,
                                    onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                    autoplayGifs = autoplayGifs,
                                    imageQuality = imageQuality
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

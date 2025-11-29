package com.example.kemono.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kemono.data.model.Post
import com.example.kemono.ui.components.PostItem
import com.example.kemono.ui.components.CreatorProfileHeader
import com.example.kemono.data.model.Announcement
import com.example.kemono.data.model.CreatorLink
import com.example.kemono.data.model.Fancard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorPostListScreen(
    viewModel: CreatorPostListViewModel = hiltViewModel(),
    onPostClick: (Post) -> Unit,
    onBackClick: () -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val creator by viewModel.creator.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val announcements by viewModel.announcements.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val links by viewModel.links.collectAsState()
    val fancards by viewModel.fancards.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedPostIds by viewModel.selectedPostIds.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                com.example.kemono.ui.components.SelectionTopAppBar(
                    selectedCount = selectedPostIds.size,
                    onClearSelection = viewModel::clearSelection,
                    onDownloadSelected = viewModel::downloadSelectedPosts
                )
            } else {
                TopAppBar(
                    title = { Text(text = creator?.name ?: "Posts") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && creator == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && creator == null) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    creator?.let {
                        CreatorProfileHeader(
                            creator = it,
                            isFavorite = isFavorite,
                            onFavoriteClick = { viewModel.toggleFavorite() }
                        )
                    }
                    
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Posts (${posts.size})") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Profile") }
                        )
                    }

                    when (selectedTabIndex) {
                        0 -> {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(posts) { post ->
                                    PostItem(
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
                                        onCreatorClick = {}
                                    )
                                }
                            }
                        }
                        1 -> {
                            ProfileDetailsContent(
                                announcements = announcements,
                                tags = tags,
                                links = links,
                                fancards = fancards
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsContent(
    announcements: List<Announcement>,
    tags: List<String>,
    links: List<CreatorLink>,
    fancards: List<Fancard>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (announcements.isNotEmpty()) {
            item {
                Text(text = "Announcements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                announcements.forEach { announcement ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = announcement.content ?: "No content", style = MaterialTheme.typography.bodyMedium)
                            Text(text = announcement.added ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (fancards.isNotEmpty()) {
            item {
                Text(text = "Fancards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(fancards.chunked(2)) { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { fancard ->
                        FancardItem(fancard = fancard, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (tags.isNotEmpty()) {
            item {
                Text(text = "Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                // Simple flow row implementation or just comma separated for now
                Text(text = tags.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (links.isNotEmpty()) {
            item {
                Text(text = "Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                links.forEach { link ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = "• ", style = MaterialTheme.typography.bodyMedium)
                        // Make clickable later
                        Text(text = link.title ?: link.url ?: "No URL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun FancardItem(fancard: Fancard, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column {
            AsyncImage(
                model = fancard.coverUrl ?: "https://kemono.su/icons/fanbox/${fancard.userId}", // Fallback
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = fancard.title ?: "Untitled", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(text = fancard.price ?: "Free", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                if (!fancard.description.isNullOrBlank()) {
                    Text(text = fancard.description ?: "", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }
    }
}

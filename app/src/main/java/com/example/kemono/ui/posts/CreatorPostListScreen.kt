package com.example.kemono.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.Creator
import com.example.kemono.ui.components.PostItem
import com.example.kemono.ui.components.CreatorTile
import com.example.kemono.ui.components.CreatorProfileHeader
import com.example.kemono.data.model.Announcement
import com.example.kemono.data.model.CreatorLink
import com.example.kemono.data.model.Fancard
import com.example.kemono.data.model.DiscordChannel
import com.example.kemono.data.model.DiscordPost
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.BasicText

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorPostListScreen(
    viewModel: CreatorPostListViewModel = hiltViewModel(),
    settingsViewModel: com.example.kemono.ui.settings.SettingsViewModel = hiltViewModel(),
    onPostClick: (Post) -> Unit,
    onBackClick: () -> Unit,
    onCreatorClick: (com.example.kemono.data.model.Creator) -> Unit
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

    val favoritePostIds by viewModel.favoritePostIds.collectAsState()
    val downloadedPostIds by viewModel.downloadedPostIds.collectAsState()
    val autoplayGifs by settingsViewModel.autoplayGifs.collectAsState()
    val postLayoutMode by settingsViewModel.postLayoutMode.collectAsState()
    val gridDensity by settingsViewModel.gridDensity.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedFancard by remember { mutableStateOf<Fancard?>(null) }
    val context = LocalContext.current

    val discordChannels by viewModel.discordChannels.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val discordPosts by viewModel.discordPosts.collectAsState()

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
                            onFavoriteClick = { viewModel.toggleFavorite() },
                            autoplayGifs = autoplayGifs
                        )
                    }
                    
                    val pagerState = rememberPagerState(pageCount = { 2 })
                    
                    LaunchedEffect(selectedTabIndex) {
                        pagerState.animateScrollToPage(selectedTabIndex)
                    }
                    
                    LaunchedEffect(pagerState.currentPage) {
                        selectedTabIndex = pagerState.currentPage
                    }

                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { 
                                if (viewModel.service == "discord") {
                                    Text("Chat")
                                } else {
                                    Text("Posts (${posts.size})") 
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Profile") }
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                if (viewModel.service == "discord") {
                                    DiscordContent(
                                        channels = discordChannels,
                                        selectedChannel = selectedChannel,
                                        posts = discordPosts,
                                        onChannelSelect = viewModel::selectChannel
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                                        val scope = rememberCoroutineScope()

                                        if (postLayoutMode == "Grid") {
                                            val minSize = when (gridDensity) {
                                                "Small" -> 120.dp
                                                "Large" -> 200.dp
                                                else -> 150.dp
                                            }
                                            LazyVerticalGrid(
                                                state = gridState,
                                                columns = GridCells.Adaptive(minSize),
                                                contentPadding = PaddingValues(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                itemsIndexed(posts, key = { _, post -> post.id ?: post.hashCode() }) { index, post ->
                                                    if (index >= posts.size - 1) {
                                                        LaunchedEffect(Unit) {
                                                            viewModel.loadMorePosts()
                                                        }
                                                    }
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
                                                        isFavorite = favoritePostIds.contains(post.id),
                                                        onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                        isDownloaded = downloadedPostIds.contains(post.id),
                                                        autoplayGifs = autoplayGifs,
                                                        showCreator = false // Already on profile
                                                    )
                                                }
                                            }
                                        } else {
                                            LazyColumn(
                                                state = listState,
                                                contentPadding = PaddingValues(bottom = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                itemsIndexed(posts, key = { _, post -> post.id ?: post.hashCode() }) { index, post ->
                                                    if (index >= posts.size - 1) {
                                                        LaunchedEffect(Unit) {
                                                            viewModel.loadMorePosts()
                                                        }
                                                    }
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
                                                        onCreatorClick = {},
                                                        showCreator = false,
                                                        isFavorite = favoritePostIds.contains(post.id),
                                                        onFavoriteClick = { viewModel.toggleFavoritePost(post) },
                                                        isDownloaded = downloadedPostIds.contains(post.id),
                                                        autoplayGifs = autoplayGifs,
                                                        showService = false
                                                    )
                                                }
                                            }
                                        }
                                        
                                        val showButton by remember {
                                            derivedStateOf {
                                                if (postLayoutMode == "Grid") gridState.firstVisibleItemIndex > 0
                                                else listState.firstVisibleItemIndex > 0
                                            }
                                        }
                                        
                                        com.example.kemono.ui.components.ScrollToTopButton(
                                            visible = showButton,
                                            onClick = {
                                                scope.launch {
                                                    if (postLayoutMode == "Grid") gridState.animateScrollToItem(0)
                                                    else listState.animateScrollToItem(0)
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                                        )
                                    }
                                }
                            }
                            1 -> {
                                ProfileDetailsContent(
                                    announcements = announcements,
                                    tags = tags,
                                    links = links,
                                    fancards = fancards,
                                    service = viewModel.service,
                                    onFancardClick = { fancard ->
                                        selectedFancard = fancard
                                    },
                                    onCreatorClick = onCreatorClick,
                                    autoplayGifs = autoplayGifs
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedFancard != null) {
        FancardFullscreenViewer(
            fancard = selectedFancard!!,
            service = viewModel.service,
            onDismiss = { selectedFancard = null },
            onDownload = {
                viewModel.downloadFancard(it)
                Toast.makeText(context, "Downloading Fancard ${it.id}...", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProfileDetailsContent(
    announcements: List<Announcement>,
    tags: List<String>,
    links: List<CreatorLink>,
    fancards: List<Fancard>,
    service: String,
    onFancardClick: (Fancard) -> Unit,
    onCreatorClick: (com.example.kemono.data.model.Creator) -> Unit,
    autoplayGifs: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp), // Increased spacing for clear separation
        modifier = Modifier.fillMaxSize()
    ) {
        // Announcements Section
        item {
            ProfileSection(title = "Announcements", isEmpty = announcements.isEmpty()) {
                announcements.forEach { announcement ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val content = remember(announcement.content) {
                                com.example.kemono.util.HtmlConverter.fromHtml(announcement.content ?: "")
                            }
                            Text(text = content, style = MaterialTheme.typography.bodyMedium)
                            Text(text = announcement.added ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Fancards Section (Fanbox Only)
        if (service == "fanbox") {
            item {
                Text(
                    text = "Fancards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (fancards.isEmpty()) {
                item {
                    Text(
                        text = "There's nothing here... (╥﹏╥)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                // Chunk logic for grid-like rows in LazyColumn
                val chunkedFancards = fancards.chunked(2)
                items(chunkedFancards) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { fancard ->
                            FancardItem(
                                fancard = fancard,
                                service = service,
                                modifier = Modifier.weight(1f),
                                onClick = { onFancardClick(fancard) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Tags Section
        item {
            ProfileSection(title = "Tags", isEmpty = tags.isEmpty()) {
                Text(text = tags.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Linked Accounts Section
        item {
            ProfileSection(title = "Linked Accounts", isEmpty = links.isEmpty()) {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                links.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { link ->
                            val linkedCreator = Creator(
                                id = link.id ?: "",
                                service = link.service ?: "",
                                name = link.name ?: link._title ?: "Unknown",
                                indexed = 0,
                                updated = 0
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                CreatorTile(
                                    creator = linkedCreator,
                                    isFavorite = false, // We don't know the favorite status of linked accounts yet
                                    onClick = { 
                                        if (!link.service.isNullOrBlank() && !link.id.isNullOrBlank()) {
                                            onCreatorClick(linkedCreator)
                                        } else {
                                           val url = link._url
                                           url?.let {
                                               try {
                                                   uriHandler.openUri(it)
                                               } catch (e: Exception) {
                                                   e.printStackTrace()
                                               }
                                           }
                                        }
                                    },
                                    onFavoriteClick = {}, // No-op for now
                                    compact = true,
                                    autoplayGifs = autoplayGifs
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    isEmpty: Boolean,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (isEmpty) {
            Text(
                text = "There's nothing here... (╥﹏╥)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            content()
        }
    }
}

@Composable
fun FancardItem(
    fancard: Fancard, 
    service: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        Column {
            val imageUrl = remember(fancard, service) {
                if (fancard.file?.path != null) {
                    // file.path usually starts with /data, so we just prepend /thumbnail
                    // e.g. /data/72/7b/... -> https://kemono.cr/thumbnail/data/72/7b/...
                    val path = fancard.file.path!!
                    if (path.startsWith("/data")) {
                        "https://kemono.cr/thumbnail$path"
                    } else {
                        "https://kemono.cr/thumbnail/data$path"
                    }
                } else if (fancard.hash != null && fancard.ext != null) {
                    // Construct hashed path for thumbnail: /thumbnail/data/{hash[0:2]}/{hash[2:4]}/{hash}.{ext}
                    val hash = fancard.hash
                    val ext = fancard.ext
                    val prefix1 = hash.take(2)
                    val prefix2 = hash.drop(2).take(2)
                    val baseUrl = fancard.server ?: "https://kemono.cr"
                    // Use kemono.cr for thumbnails as specific servers might not have the /thumbnail endpoint
                    "https://kemono.cr/thumbnail/data/$prefix1/$prefix2/$hash$ext"
                } else {
                    fancard.coverUrl ?: "https://kemono.cr/icons/fanbox/${fancard.userId}"
                }
            }
            
            // Debug logging
            LaunchedEffect(imageUrl) {
                println("DEBUG: FancardItem URL: $imageUrl")
                println("DEBUG: Fancard Data: $fancard")
            }
            
            val context = LocalContext.current
            val imageLoader = context.imageLoader
            val imageRequest = remember(imageUrl) {
                ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .addHeader("Referer", "https://kemono.cr/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()
            }

            AsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop,
                onError = { state ->
                    println("DEBUG: AsyncImage Error: ${state.result.throwable.message}")
                    state.result.throwable.printStackTrace()
                }
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = fancard.title ?: "Fancard ${fancard.id}", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                if (!fancard.added.isNullOrBlank()) {
                    Text(text = fancard.added.take(10), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun FancardFullscreenViewer(
    fancard: Fancard,
    service: String,
    onDismiss: () -> Unit,
    onDownload: (Fancard) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val imageUrl = remember(fancard, service) {
                if (fancard.file?.path != null) {
                    "https://kemono.cr/data${fancard.file.path}"
                } else if (fancard.hash != null && fancard.ext != null) {
                    // Construct hashed path: /data/{hash[0:2]}/{hash[2:4]}/{hash}.{ext}
                    val hash = fancard.hash
                    val ext = fancard.ext
                    val prefix1 = hash.take(2)
                    val prefix2 = hash.drop(2).take(2)
                    // Use server from API if available, otherwise default to kemono.cr
                    // Note: API might return n2.kemono.cr while image is on n3.kemono.cr, 
                    // but kemono.cr usually handles redirects correctly.
                    val baseUrl = fancard.server ?: "https://kemono.cr"
                    "$baseUrl/data/$prefix1/$prefix2/$hash$ext"
                } else {
                    fancard.coverUrl ?: "https://kemono.cr/icons/fanbox/${fancard.userId}"
                }
            }
            
            // Debug logging
            LaunchedEffect(imageUrl) {
                println("DEBUG: Fullscreen URL: $imageUrl")
            }
            
            val context = LocalContext.current
            val imageLoader = context.imageLoader
            val imageRequest = remember(imageUrl) {
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    // Explicitly add headers again just in case
                    .addHeader("Referer", "https://kemono.cr/")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .build()
            }
            
            AsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onError = { state ->
                    println("DEBUG: AsyncImage Error: ${state.result.throwable.message}")
                    state.result.throwable.printStackTrace()
                }
            )
            
            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onDownload(fancard) }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
            }
            
            // Bottom Info Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fancard ${fancard.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                if (!fancard.added.isNullOrBlank()) {
                    Text(
                        text = "Added: ${fancard.added}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun DiscordContent(
    channels: List<com.example.kemono.data.model.DiscordChannel>,
    selectedChannel: com.example.kemono.data.model.DiscordChannel?,
    posts: List<com.example.kemono.data.model.DiscordPost>,
    onChannelSelect: (com.example.kemono.data.model.DiscordChannel) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Channel Selector
        if (channels.isNotEmpty()) {
        // Channel Selector
        if (channels.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(channels) { channel ->
                    val isSelected = channel == selectedChannel
                    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(containerColor)
                            .clickable { onChannelSelect(channel) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        }

        // Chat List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(posts, key = { it.id }) { post ->
                DiscordPostItem(post = post)
            }
        }
    }
}

@Composable
fun DiscordPostItem(post: com.example.kemono.data.model.DiscordPost) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Avatar
        AsyncImage(
            model = "https://cdn.discordapp.com/avatars/${post.author?.id}/${post.author?.avatar}.png",
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.author?.username ?: "Unknown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.published?.take(10) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Content
            if (!post.content.isNullOrBlank()) {
                val uriHandler = LocalUriHandler.current
                val (annotatedString, inlineContent) = remember(post.content) {
                    parseDiscordContent(post.content)
                }
                
                val layoutResult = remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                val pressIndicator = Modifier.pointerInput(Unit) {
                    detectTapGestures { pos ->
                        layoutResult.value?.let { layoutResult ->
                            val offset = layoutResult.getOffsetForPosition(pos)
                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        uriHandler.openUri(annotation.item)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                        }
                    }
                }

                BasicText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    inlineContent = inlineContent,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .then(pressIndicator),
                    onTextLayout = { layoutResult.value = it }
                )
            }
            
            // Attachments
            if (post.attachments.isNotEmpty()) {
                post.attachments.forEach { attachment ->
                    if (attachment.path != null) {
                        val imageUrl = "https://kemono.cr${attachment.path}"
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = attachment.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 4.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            // Embeds (Rich handling)
            if (post.embeds.isNotEmpty()) {
                post.embeds.forEach { embed ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (embed.provider?.name != null) {
                                Text(text = embed.provider.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (embed.author?.name != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (embed.author.iconUrl != null) {
                                        AsyncImage(
                                            model = embed.author.iconUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(text = embed.author.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!embed.title.isNullOrBlank()) {
                                Text(text = embed.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            if (!embed.description.isNullOrBlank()) {
                                Text(text = embed.description, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            // Embed Image
                            val embedImageUrl = embed.image?.path?.let { if (it.startsWith("/")) "https://kemono.cr$it" else it }
                            if (embedImageUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(embedImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Embed Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(vertical = 4.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            // Embed Thumbnail (if no main image)
                            if (embedImageUrl == null) {
                                val embedThumbnailUrl = embed.thumbnail?.path?.let { if (it.startsWith("/")) "https://kemono.cr$it" else it }
                                if (embedThumbnailUrl != null) {
                                     AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(embedThumbnailUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Embed Thumbnail",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(vertical = 4.dp)
                                            .clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            
                            // For now, let's just render the main embed URL as a link if present
                            if (!embed.url.isNullOrBlank()) {
                                Text(text = embed.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseDiscordContent(content: String): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        // Regex for URLs and Emotes
        // URL: https?://...
        // Emote: <(a?):([^:]+):(\d+)>
        val regex = "((https?://\\S+)|(<a?:[^:]+:(\\d+)>))".toRegex()
        
        regex.findAll(content).forEach { matchResult ->
            val matchStart = matchResult.range.first
            val matchEnd = matchResult.range.last + 1
            val value = matchResult.value
            
            // Append text before match
            if (matchStart > currentIndex) {
                append(content.substring(currentIndex, matchStart))
            }
            
            if (value.startsWith("http")) {
                // URL
                pushStringAnnotation(tag = "URL", annotation = value)
                withStyle(style = SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF00B0F4), textDecoration = TextDecoration.Underline)) {
                    append(value)
                }
                pop()
            } else {
                // Emote
                val isAnimated = value.startsWith("<a")
                val parts = value.trim('<', '>').split(':')
                if (parts.size == 3) {
                    val id = parts[2]
                    val extension = if (isAnimated) "gif" else "png"
                    val emoteUrl = "https://cdn.discordapp.com/emojis/$id.$extension"
                    val inlineId = "emote_$id"
                    
                    appendInlineContent(inlineId, "[emote]")
                    inlineContent[inlineId] = InlineTextContent(
                        Placeholder(width = 1.5.em, height = 1.5.em, placeholderVerticalAlign = PlaceholderVerticalAlign.Center)
                    ) {
                        AsyncImage(
                            model = emoteUrl,
                            contentDescription = "Emote",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    append(value)
                }
            }
            currentIndex = matchEnd
        }
        
        // Append remaining text
        if (currentIndex < content.length) {
            append(content.substring(currentIndex))
        }
    }
    return annotatedString to inlineContent
}

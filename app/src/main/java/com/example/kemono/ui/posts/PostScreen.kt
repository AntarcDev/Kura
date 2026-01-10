package com.example.kemono.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

import com.example.kemono.ui.settings.SettingsViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
        viewModel: PostViewModel = hiltViewModel(),
        settingsViewModel: SettingsViewModel = hiltViewModel(),
        onBackClick: () -> Unit,
        onImageClick: (Int) -> Unit
) {
    val post by viewModel.post.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val autoplayGifs by settingsViewModel.autoplayGifs.collectAsState()
    val lowResMode by settingsViewModel.lowResMode.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { 
                            Text(
                                text = post?.title ?: "Post",
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            val isFavorite by viewModel.isFavorite.collectAsState()
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.downloadMedia() }) {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                            }
                        }
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column {
                    val isOnline by viewModel.isOnline.collectAsState()
                    if (!isOnline) {
                        Text(
                                text = "Offline Mode - Showing cached content",
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(
                                                        MaterialTheme.colorScheme.errorContainer
                                                )
                                                .padding(8.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    post?.let { currentPost ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                            val scope = rememberCoroutineScope()

                            androidx.compose.foundation.lazy.LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                            ) {
                            item {
                                Text(
                                        text = currentPost.title ?: "Untitled",
                                        style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text = "Published: ${currentPost.published ?: "Unknown"}",
                                        style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Render HTML content
                                val htmlContent = currentPost.content ?: "No content available"
                                val contentNodes = remember(htmlContent) {
                                    com.example.kemono.util.HtmlConverter.parseHtmlContent(htmlContent)
                                }
                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                                contentNodes.forEach { node ->
                                    when (node) {
                                        is com.example.kemono.util.ContentNode.Text -> {
                                            androidx.compose.foundation.text.ClickableText(
                                                text = node.text,
                                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                                onClick = { offset ->
                                                    node.text.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                                        .firstOrNull()?.let { annotation ->
                                                            try {
                                                                uriHandler.openUri(annotation.item)
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                        }
                                                },
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                        is com.example.kemono.util.ContentNode.Image -> {
                                            val isGif = node.url.endsWith(".gif", ignoreCase = true)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                AsyncImage(
                                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                        .data(node.url)
                                                        .crossfade(!isGif)
                                                        .apply {
                                                            if (!autoplayGifs) {
                                                                decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                                                memoryCacheKey(node.url + "_static")
                                                            }
                                                        }
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.FillWidth
                                                )
                                                
                                                // Separate Download Button
                                                androidx.compose.material3.OutlinedButton(
                                                    onClick = { 
                                                        val fileName = node.url.substringAfterLast('/')
                                                        viewModel.downloadFile(
                                                            url = node.url,
                                                            fileName = fileName,
                                                            mediaType = "IMAGE"
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Download Image")
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                currentPost.file?.let { file ->
                                    if (!file.path.isNullOrEmpty()) {
                                        val url = "https://kemono.cr${file.path}"
                                        // Fix: Handle query parameters in extension
                                        val extension = file.path!!.substringAfterLast('.', "").substringBefore('?').lowercase()
                                        val mediaType = com.example.kemono.util.getMediaType(extension)
                                        val isAudio = extension in listOf("mp3", "wav", "ogg", "m4a")
                                        val isArchive = extension in listOf("zip", "rar", "7z", "tar", "gz", "xz")
                                        val isPsd = extension == "psd"
                                        val isClip = extension in listOf("clip", "csp")
                                        
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            // Determine Preview URL based on Low Res Mode
                                            val previewUrl = if (lowResMode && !file.thumbnailPath.isNullOrEmpty()) {
                                                "https://kemono.cr${file.thumbnailPath}"
                                            } else {
                                                "https://kemono.cr${file.path}"
                                            }

                                            when {
                                                isAudio -> {
                                                    com.example.kemono.ui.components.AudioPlayer(
                                                        url = url,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (file.name ?: "file").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "AUDIO"
                                                            )
                                                        }
                                                    )
                                                }
                                                isPsd -> {
                                                    if (!file.thumbnailPath.isNullOrEmpty()) {
                                                        // Try loading the PSD file directly if supported, or fallback
                                                        val thumbnailUrl = "https://kemono.cr${file.path}"
                                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                            AsyncImage(
                                                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                                    .data(thumbnailUrl)
                                                                    .crossfade(true)
                                                                    .apply {
                                                                        if (!autoplayGifs) {
                                                                            decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                                                            memoryCacheKey(thumbnailUrl + "_static")
                                                                        }
                                                                    }
                                                                    .build(),
                                                                contentDescription = "PSD Thumbnail",
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(300.dp)
                                                                    .clickable { onImageClick(0) },
                                                                contentScale = ContentScale.Fit
                                                            )
                                                            // PSD Badge
                                                            Text(
                                                                text = "PSD",
                                                                modifier = Modifier
                                                                    .align(Alignment.TopStart)
                                                                    .padding(8.dp)
                                                                    .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                    } else {
                                                    com.example.kemono.ui.components.FileCard(
                                                            fileName = file.name ?: "PSD File",
                                                            path = file.path,
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                            onDownloadClick = {
                                                                val safeFileName = (file.name ?: "file").take(50) + "." + extension
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "IMAGE"
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                                isArchive -> {
                                                    com.example.kemono.ui.components.FileCard(
                                                        fileName = file.name ?: "Archive",
                                                        path = file.path,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (file.name ?: "file").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "ARCHIVE"
                                                            )
                                                        }
                                                    )
                                                }
                                                isClip -> {
                                                    com.example.kemono.ui.components.FileCard(
                                                        fileName = file.name ?: "CLIP File",
                                                        path = file.path,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (file.name ?: "file").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "IMAGE"
                                                            )
                                                        }
                                                    )
                                                }
                                                mediaType == com.example.kemono.util.MediaType.VIDEO -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        com.example.kemono.ui.components.VideoPlayer(
                                                                url = url,
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(300.dp)
                                                                                .padding(vertical = 4.dp)
                                                                                .clip(RoundedCornerShape(8.dp))
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        androidx.compose.material3.Button(
                                                            onClick = { 
                                                                val safeFileName = (file.name ?: "file").take(50)
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "VIDEO"
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Icon(Icons.Default.Download, contentDescription = null)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Download Video")
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        AsyncImage(
                                                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                                .data(previewUrl)
                                                                .crossfade(mediaType != com.example.kemono.util.MediaType.GIF)
                                                                .apply {
                                                                    if (!autoplayGifs) {
                                                                        decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                                                        memoryCacheKey(previewUrl + "_static")
                                                                    }
                                                                }
                                                                .build(),
                                                            contentDescription = null,
                                                            modifier =
                                                                    Modifier.fillMaxWidth()
                                                                            .padding(vertical = 4.dp)
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .clickable {
                                                                                onImageClick(0)
                                                                            },
                                                            contentScale = ContentScale.FillWidth
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        androidx.compose.material3.OutlinedButton(
                                                            onClick = { 
                                                                val safeFileName = (file.name ?: "file").take(50)
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "IMAGE"
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Icon(Icons.Default.Download, contentDescription = null)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Download Image")
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Removed overlaid Download button block
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            // Attachments Header
                            if (currentPost.attachments.isNotEmpty()) {
                                item {
                                    Text(
                                            text = "Attachments:",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(currentPost.attachments.size) { index ->
                                    val attachment = currentPost.attachments[index]
                                    if (!attachment.path.isNullOrEmpty()) {
                                        val url = "https://kemono.cr${attachment.path}"
                                        // Fix: Handle query parameters in extension
                                        val extension = attachment.path!!.substringAfterLast('.', "").substringBefore('?').lowercase()
                                        val mediaType =
                                                com.example.kemono.util.getMediaType(
                                                        extension
                                                )
                                        val isAudio = extension in listOf("mp3", "wav", "ogg", "m4a")
                                        val isArchive = extension in listOf("zip", "rar", "7z", "tar", "gz", "xz")
                                        val isPsd = extension == "psd"
                                        val isClip = extension in listOf("clip", "csp")

                                        val mainFileExists = !currentPost.file?.path.isNullOrEmpty()
                                        val globalIndex = index + (if (mainFileExists) 1 else 0)

                                        Column {
                                            // Determine Preview URL based on Low Res Mode
                                            val previewUrl = if (lowResMode && !attachment.thumbnailPath.isNullOrEmpty()) {
                                                "https://kemono.cr${attachment.thumbnailPath}"
                                            } else {
                                                "https://kemono.cr${attachment.path}"
                                            }

                                            when {
                                                isAudio -> {
                                                    com.example.kemono.ui.components.AudioPlayer(
                                                        url = url,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (attachment.name ?: "attachment").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "AUDIO"
                                                            )
                                                        }
                                                    )
                                                }
                                                isPsd -> {
                                                    if (!attachment.thumbnailPath.isNullOrEmpty()) {
                                                        val thumbnailUrl = "https://kemono.cr${attachment.path}"
                                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                            AsyncImage(
                                                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                                    .data(thumbnailUrl)
                                                                    .crossfade(true)
                                                                    .apply {
                                                                        if (!autoplayGifs) {
                                                                            decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                                                            memoryCacheKey(thumbnailUrl + "_static")
                                                                        }
                                                                    }
                                                                    .build(),
                                                                contentDescription = "PSD Thumbnail",
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(300.dp)
                                                                    .clickable { onImageClick(globalIndex) },
                                                                contentScale = ContentScale.Fit
                                                            )
                                                            Text(
                                                                text = "PSD",
                                                                modifier = Modifier
                                                                    .align(Alignment.TopStart)
                                                                    .padding(8.dp)
                                                                    .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                    } else {
                                                    com.example.kemono.ui.components.FileCard(
                                                            fileName = attachment.name ?: "PSD File",
                                                            path = attachment.path,
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            onDownloadClick = {
                                                                val safeFileName = (attachment.name ?: "attachment").take(50) + "." + extension
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "IMAGE"
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                                isArchive -> {
                                                    com.example.kemono.ui.components.FileCard(
                                                        fileName = attachment.name ?: "Archive",
                                                        path = attachment.path,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (attachment.name ?: "attachment").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "ARCHIVE"
                                                            )
                                                        }
                                                    )
                                                }
                                                isClip -> {
                                                    com.example.kemono.ui.components.FileCard(
                                                        fileName = attachment.name ?: "CLIP File",
                                                        path = attachment.path,
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        onDownloadClick = {
                                                            val safeFileName = (attachment.name ?: "attachment").take(50) + "." + extension
                                                            viewModel.downloadFile(
                                                                url = url,
                                                                fileName = safeFileName,
                                                                mediaType = "IMAGE"
                                                            )
                                                        }
                                                    )
                                                }
                                                mediaType == com.example.kemono.util.MediaType.VIDEO -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        com.example.kemono.ui.components.VideoPlayer(
                                                                url = url,
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(300.dp)
                                                                                .padding(vertical = 4.dp)
                                                                                .clip(RoundedCornerShape(8.dp))
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        androidx.compose.material3.Button(
                                                            onClick = { 
                                                                val safeFileName = (attachment.name ?: "attachment").take(50)
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "VIDEO"
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Icon(Icons.Default.Download, contentDescription = null)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Download Video")
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        AsyncImage(
                                                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                                .data(previewUrl)
                                                                .crossfade(mediaType != com.example.kemono.util.MediaType.GIF)
                                                                .apply {
                                                                    if (!autoplayGifs) {
                                                                        decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                                                        memoryCacheKey(previewUrl + "_static")
                                                                    }
                                                                }
                                                                .build(),
                                                            contentDescription = attachment.name,
                                                            modifier =
                                                                    Modifier.fillMaxWidth()
                                                                            .padding(vertical = 4.dp)
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .clickable {
                                                                                onImageClick(
                                                                                        globalIndex
                                                                                )
                                                                            },
                                                            contentScale = ContentScale.FillWidth
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        androidx.compose.material3.OutlinedButton(
                                                            onClick = {
                                                                val safeFileName = (attachment.name ?: "attachment").take(50)
                                                                viewModel.downloadFile(
                                                                    url = url,
                                                                    fileName = safeFileName,
                                                                    mediaType = "IMAGE"
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Icon(Icons.Default.Download, contentDescription = null)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Download ${attachment.name ?: "Image"}")
                                                        }
                                                    }
                                                }
                                            }

                                            // Removed Row block logic for simple images/videos as it's replaced by buttons above
                                            // Logic for Audio/Archive/PSD/Clip handles its own buttons or FileCards
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                }
                            }
                            }
                            
                            val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
                            com.example.kemono.ui.components.ScrollToTopButton(
                                visible = showButton,
                                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

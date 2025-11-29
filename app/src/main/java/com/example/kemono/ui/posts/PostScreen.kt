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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
        viewModel: PostViewModel = hiltViewModel(),
        onBackClick: () -> Unit,
        onImageClick: (Int) -> Unit
) {
    val post by viewModel.post.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(text = post?.title ?: "Post") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
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
                        androidx.compose.foundation.lazy.LazyColumn(
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
                                val contentNodes = com.example.kemono.util.HtmlConverter.parseHtmlContent(htmlContent)
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
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                AsyncImage(
                                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                        .data(node.url)
                                                        .crossfade(!isGif)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    contentScale = ContentScale.FillWidth
                                                )
                                                
                                                // Download button overlay
                                                IconButton(
                                                    onClick = { 
                                                        val fileName = node.url.substringAfterLast('/')
                                                        viewModel.downloadFile(
                                                            url = node.url,
                                                            fileName = fileName,
                                                            mediaType = "IMAGE"
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = "Download image")
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
                                        val url = "https://kemono.su${file.path}"
                                        val mediaType = com.example.kemono.util.getMediaType(file.path)
                                        
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            when (mediaType) {
                                                com.example.kemono.util.MediaType.VIDEO -> {
                                                    com.example.kemono.ui.components.VideoPlayer(
                                                            url = url,
                                                            modifier =
                                                                    Modifier.fillMaxWidth().height(300.dp)
                                                    )
                                                }
                                                else -> {
                                                AsyncImage(
                                                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                            .data(url)
                                                            .crossfade(mediaType != com.example.kemono.util.MediaType.GIF)
                                                            .build(),
                                                        contentDescription = null,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(300.dp)
                                                                        .clickable {
                                                                            onImageClick(0)
                                                                        },
                                                        contentScale = ContentScale.Fit
                                                )
                                                }
                                            }
                                            
                                            // Download button overlay
                                            IconButton(
                                                onClick = { 
                                                    viewModel.downloadFile(
                                                        url = url,
                                                        fileName = file.name ?: "file",
                                                        mediaType = if (mediaType == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                                                    )
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = "Download file")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
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
                                        val url = "https://kemono.su${attachment.path}"
                                        val mediaType =
                                                com.example.kemono.util.getMediaType(
                                                        attachment.path
                                                )
                                        // Offset index by 1 if main file exists, but here we just use index + 1 for simplicity in logic if needed
                                        // Actually imageIndex logic was complex in previous code. 
                                        // Let's just pass the index relative to attachments or global?
                                        // The original code incremented imageIndex for both main file and attachments.
                                        // We need to maintain that if we want gallery to work correctly.
                                        // But calculating global index in LazyColumn items is tricky if we don't know if main file exists.
                                        // For now, let's assume onImageClick takes the index in the list of *images*.
                                        // We can calculate the offset based on main file presence.
                                        val mainFileExists = !currentPost.file?.path.isNullOrEmpty()
                                        val globalIndex = index + (if (mainFileExists) 1 else 0)

                                        Column {
                                            when (mediaType) {
                                                com.example.kemono.util.MediaType.VIDEO -> {
                                                    com.example.kemono.ui.components.VideoPlayer(
                                                            url = url,
                                                            modifier =
                                                                    Modifier.fillMaxWidth()
                                                                            .height(300.dp)
                                                                            .padding(vertical = 4.dp)
                                                    )
                                                }
                                                else -> {
                                                AsyncImage(
                                                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                            .data(url)
                                                            .crossfade(mediaType != com.example.kemono.util.MediaType.GIF)
                                                            .build(),
                                                        contentDescription = attachment.name,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(300.dp)
                                                                        .padding(vertical = 4.dp)
                                                                        .clickable {
                                                                            onImageClick(
                                                                                    globalIndex
                                                                            )
                                                                        },
                                                        contentScale = ContentScale.Fit
                                                )
                                                }
                                            }

                                            Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                        text = attachment.name ?: "Attachment",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.downloadFile(
                                                            url = url,
                                                            fileName = attachment.name ?: "attachment",
                                                            mediaType = if (mediaType == com.example.kemono.util.MediaType.VIDEO) "VIDEO" else "IMAGE"
                                                        )
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = "Download attachment")
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

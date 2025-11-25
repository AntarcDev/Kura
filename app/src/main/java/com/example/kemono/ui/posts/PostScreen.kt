package com.example.kemono.ui.posts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun PostScreen(viewModel: PostViewModel = hiltViewModel(), onBackClick: () -> Unit) {
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
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(16.dp)
                        ) {
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
                            val spanned =
                                    remember(htmlContent) {
                                        if (android.os.Build.VERSION.SDK_INT >=
                                                        android.os.Build.VERSION_CODES.N
                                        ) {
                                            android.text.Html.fromHtml(
                                                    htmlContent,
                                                    android.text.Html.FROM_HTML_MODE_COMPACT
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            android.text.Html.fromHtml(htmlContent)
                                        }
                                    }

                            Text(
                                    text = spanned.toString(),
                                    modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            currentPost.file?.let { file ->
                                if (!file.path.isNullOrEmpty()) {
                                    val url = "https://kemono.cr${file.path}"
                                    val mediaType = com.example.kemono.util.getMediaType(file.path)

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
                                                    model = url,
                                                    contentDescription = null,
                                                    modifier =
                                                            Modifier.fillMaxWidth().height(300.dp),
                                                    contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Attachments
                            if (currentPost.attachments.isNotEmpty()) {
                                Text(
                                        text = "Attachments:",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                )
                                currentPost.attachments.forEach { attachment ->
                                    if (!attachment.path.isNullOrEmpty()) {
                                        val url = "https://kemono.cr${attachment.path}"
                                        val mediaType =
                                                com.example.kemono.util.getMediaType(
                                                        attachment.path
                                                )

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
                                                        model = url,
                                                        contentDescription = attachment.name,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(300.dp)
                                                                        .padding(vertical = 4.dp),
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

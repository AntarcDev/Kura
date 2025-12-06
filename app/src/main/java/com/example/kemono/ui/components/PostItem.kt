package com.example.kemono.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kemono.data.model.Post

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post, 
    onClick: () -> Unit, 
    onCreatorClick: () -> Unit = {},
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
    showCreator: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            post.file?.let { file ->
                if (!file.path.isNullOrEmpty()) {
                    val url = if (!file.thumbnailPath.isNullOrEmpty()) {
                        "https://kemono.su${file.thumbnailPath}"
                    } else {
                        "https://kemono.su/thumbnail${file.path}"
                    }
                    val isGif = file.path.endsWith(".gif", ignoreCase = true)
                    val isPsd = file.path.endsWith(".psd", ignoreCase = true)
                    
                    Box(modifier = Modifier.size(80.dp).padding(end = 8.dp)) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(url)
                                .crossfade(!isGif)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isPsd) {
                            Text(
                                text = "PSD",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            Column {
                Text(
                    text = post.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = post.published ?: "Unknown date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showCreator) {
                    Text(
                        text = "View Creator",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onCreatorClick() }.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

package com.example.kemono.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kemono.data.model.Post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post, 
    onClick: () -> Unit, 
    onCreatorClick: () -> Unit = {},
    selected: Boolean = false,
    onLongClick: () -> Unit = {}
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
                    val url = "https://kemono.su/thumbnail${file.path}"
                    val isGif = file.path.endsWith(".gif", ignoreCase = true)
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(url)
                            .crossfade(!isGif)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Crop
                    )
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

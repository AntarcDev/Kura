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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AttachFile
import com.example.kemono.data.model.Post
import com.example.kemono.util.ServiceMapper
import com.example.kemono.util.DateUtils
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post, 
    onClick: () -> Unit, 
    onCreatorClick: () -> Unit = {},
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
    showCreator: Boolean = true,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},

    autoplayGifs: Boolean = true,
    showService: Boolean = true
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
                    // Use direct file URL (kemono.cr) as thumbnail since /thumbnail endpoint is unreliable
                    val url = "https://kemono.cr${file.path}"
                    val isGif = file.path.endsWith(".gif", ignoreCase = true)
                    val isPsd = file.path.endsWith(".psd", ignoreCase = true)
                    
                    Box(modifier = Modifier.size(80.dp).padding(end = 8.dp)) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(url)
                                .crossfade(!isGif)
                                .apply {
                                    if (!autoplayGifs) {
                                        // Force static image decoding (first frame only) for ALL images if autoplay is off.
                                        // This ensures GIFs, WebPs, etc. do not animate even if detection fails.
                                        decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                        // Use a different cache key to avoid retrieving the animated version from memory cache
                                        memoryCacheKey(url + "_static")
                                    }
                                }
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
            Column(modifier = Modifier.weight(1f)) {
                // Service Badge
                if (showService) {
                    val serviceColor = ServiceMapper.getServiceColor(post.service)
                    val serviceName = ServiceMapper.getDisplayName(post.service)
                    
                    Text(
                        text = serviceName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(serviceColor, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = if (post.title.isNullOrBlank()) "Untitled" else post.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = DateUtils.formatPublishedDate(post.published),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Metadata Row (Attachments)
                if (post.attachments.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.AttachFile,
                            contentDescription = "Attachments",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.attachments.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showCreator) {
                    Text(
                        text = "View Creator",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { onCreatorClick() }
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.IconButton(onClick = onFavoriteClick) {
                    androidx.compose.material3.Icon(
                        imageVector = if (isFavorite) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if ((post.favCount ?: 0) > 0) {
                     Text(
                        text = "${post.favCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

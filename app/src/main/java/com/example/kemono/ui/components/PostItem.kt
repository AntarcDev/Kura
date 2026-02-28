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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil.compose.SubcomposeAsyncImage
import com.example.kemono.ui.components.shimmerEffect
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.AttachFile
import com.example.kemono.data.model.Post
import com.example.kemono.util.DateUtils
import com.example.kemono.util.ServiceMapper
import androidx.compose.material.icons.Icons

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
    isDownloaded: Boolean = false,

    autoplayGifs: Boolean = true,
    showService: Boolean = true,
    imageQuality: String = "Sample"
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
            val preview = com.example.kemono.util.PostPreviewUtils.getPreviewContent(post)
            
            if (preview !is com.example.kemono.util.PreviewContent.None) {
                Box(modifier = Modifier.size(80.dp).padding(end = 8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))) {
                    when (preview) {
                        is com.example.kemono.util.PreviewContent.Image -> {
                            val url = preview.url
                            val isGif = url.endsWith(".gif", ignoreCase = true)
                             val model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(url)
                                .crossfade(!isGif)
                                .apply {
                                    when (imageQuality) {
                                        "Low" -> size(300)
                                        "Sample" -> size(800)
                                    }
                                    if (!autoplayGifs) {
                                        decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                        memoryCacheKey(url + "_static")
                                    }
                                }
                                .build()
                                
                             SubcomposeAsyncImage(
                                model = model,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                                }
                            )
                        }
                        is com.example.kemono.util.PreviewContent.Icon -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(preview.containerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = preview.vector,
                                    contentDescription = preview.label,
                                    tint = preview.color,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = preview.label,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(preview.color.copy(alpha = 0.8f), androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = Color.White
                                )
                            }
                        }
                        else -> {}
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
                
                if (isDownloaded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.DownloadDone,
                            contentDescription = "Downloaded",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Downloaded",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
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

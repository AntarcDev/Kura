package com.example.kemono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.kemono.data.model.Post
import com.example.kemono.util.ServiceMapper
import androidx.compose.material.icons.filled.AttachFile

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
/**
 * A grid item composable representing a single post.
 *
 * @param post The post data to display.
 * @param selected Whether the item is currently selected in multi-select mode.
 * @param onClick Callback when the item is clicked.
 * @param onLongClick Callback when the item is long-clicked.
 * @param isFavorite Whether the post is marked as a favorite.
 * @param onFavoriteClick Callback when the favorite button is clicked.
 * @param autoplayGifs Whether GIFs should autoplay.
 * @param showCreator Whether to show the creator's avatar/button (used in Global/Favorite grids).
 * @param onCreatorClick Callback when the creator button is clicked.
 */
@Composable
fun PostGridItem(
    post: Post,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    isDownloaded: Boolean = false,
    autoplayGifs: Boolean = true,
    showCreator: Boolean = false,
    onCreatorClick: () -> Unit = {},
    imageQuality: String = "Sample"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square aspect ratio
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                val preview = com.example.kemono.util.PostPreviewUtils.getPreviewContent(post)
                
                when (preview) {
                    is com.example.kemono.util.PreviewContent.Image -> {
                        val url = preview.url
                        val model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(url)
                            .crossfade(true)
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
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = preview.vector,
                                    contentDescription = preview.label,
                                    tint = preview.color,
                                    modifier = Modifier.size(48.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preview.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = preview.color,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                             // Keep empty or transparent if nothing to show
                             // Or show specific "Empty" placeholder if desired
                        }
                    }
                }
            }
            
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                 // Counts Overlay (Top Left)
                 Column(modifier = Modifier.align(Alignment.TopStart), horizontalAlignment = Alignment.Start) {
                     // Attachments
                     if ((post.attachments?.size ?: 0) > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attachments",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                     }

                     // Downloaded Badge
                     if (isDownloaded) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "Downloaded",
                                tint = Color.Green,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                     }

                     // Favorites
                     if ((post.favCount ?: 0) > 0) {
                         Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "♥ ${post.favCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                // Buttons Overlay (Bottom Right)
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showCreator) {
                        androidx.compose.material3.IconButton(
                            onClick = onCreatorClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "View Creator",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp) // Matched size
                            )
                        }
                    }
                    
                    androidx.compose.material3.IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                // Service Dot + Title
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                     Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ServiceMapper.getServiceColor(post.service), androidx.compose.foundation.shape.CircleShape)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = post.title ?: "Untitled",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

package com.example.kemono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.kemono.ui.components.shimmerEffect
import com.example.kemono.data.model.Creator
import com.example.kemono.util.ServiceMapper
import androidx.compose.ui.unit.sp

@Composable
fun CreatorTile(
    creator: Creator,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    compact: Boolean = false,
    autoplayGifs: Boolean = true
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        if (compact) {
            Box(modifier = Modifier.fillMaxSize().height(180.dp)) {
                // Banner Background
                SubcomposeAsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data("https://kemono.cr/banners/${creator.service}/${creator.id}")
                        .crossfade(true)
                        .apply {
                            if (!autoplayGifs) {
                                decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                memoryCacheKey("https://kemono.cr/banners/${creator.service}/${creator.id}" + "_static")
                            }
                        }
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    },
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    }
                )
                
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SubcomposeAsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data("https://kemono.cr/icons/${creator.service}/${creator.id}")
                            .crossfade(true)
                            .apply {
                                if (!autoplayGifs) {
                                    decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                    memoryCacheKey("https://kemono.cr/icons/${creator.service}/${creator.id}" + "_static")
                                }
                            }
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        error = {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    },
                        loading = {
                            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ServiceMapper.getDisplayName(creator.service),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(ServiceMapper.getServiceColor(creator.service), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if ((creator.favorited ?: 0) > 0) {
                         Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "♥ ${creator.favorited}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                // Banner Background
                SubcomposeAsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data("https://kemono.cr/banners/${creator.service}/${creator.id}")
                        .crossfade(true)
                        .apply {
                            if (!autoplayGifs) {
                                decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                memoryCacheKey("https://kemono.cr/banners/${creator.service}/${creator.id}" + "_static")
                            }
                        }
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    },
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    }
                )

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    SubcomposeAsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data("https://kemono.cr/icons/${creator.service}/${creator.id}")
                            .crossfade(true)
                            .apply {
                                if (!autoplayGifs) {
                                    decoderFactory(coil.decode.BitmapFactoryDecoder.Factory())
                                    memoryCacheKey("https://kemono.cr/icons/${creator.service}/${creator.id}" + "_static")
                                }
                            }
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        error = {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                    },
                        loading = {
                            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = creator.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onFavoriteClick) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                    if ((creator.favorited ?: 0) > 0) {
                                         Text(
                                            text = "${creator.favorited}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ServiceMapper.getDisplayName(creator.service),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ServiceMapper.getServiceColor(creator.service),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${creator.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

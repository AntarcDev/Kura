package com.example.kemono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kemono.data.model.Creator
import com.example.kemono.util.ServiceMapper
import androidx.compose.ui.unit.sp

@Composable
fun CreatorProfileHeader(
    creator: Creator,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    autoplayGifs: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Banner Background
        AsyncImage(
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
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop,
            onError = { 
                // Fallback to blurred icon if banner fails
            }
        )
        
        // Fallback blurred icon (if banner fails or while loading, but AsyncImage handles loading)
        // We can put the blurred icon behind the banner so it shows if banner is missing/transparent?
        // Actually, let's just use the banner as primary.


        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
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
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if ((creator.favorited ?: 0) > 0) {
                        Text(
                            text = "${creator.favorited}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = creator.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ServiceMapper.getDisplayName(creator.service),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ServiceMapper.getServiceColor(creator.service),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " • ID: ${creator.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

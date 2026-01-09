package com.example.kemono.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import kotlinx.coroutines.flow.map
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.kemono.data.model.DownloadedItem
import java.io.File

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
        viewModel: GalleryViewModel = hiltViewModel(),
        onItemClick: (DownloadedItem, Int) -> Unit
) {
    val items by viewModel.galleryItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val gridDensity by viewModel.gridDensity.collectAsState()

    val minSize = when (gridDensity) {
        "Small" -> 120.dp
        "Large" -> 200.dp
        else -> 150.dp
    }

    Scaffold(
        topBar = { 
            com.example.kemono.ui.components.UnifiedTopBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = { viewModel.onSearchQueryChange("") }
            ) 
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (items.isEmpty()) {
                Text(
                        text = "No downloaded items yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        when (item) {
                            is GalleryUiItem.Header -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier =
                                                    Modifier.padding(
                                                            horizontal = 8.dp,
                                                            vertical = 8.dp
                                                    )
                                    )
                                }
                            }
                            is GalleryUiItem.Image -> {
                                item(span = { GridItemSpan(1) }) {
                                    GalleryItem(
                                            item = item.item,
                                            onClick = { 
                                                // Calculate index relative to images only
                                                val images = items.filterIsInstance<GalleryUiItem.Image>().map { it.item }
                                                val realIndex = images.indexOf(item.item)
                                                if (realIndex != -1) {
                                                    onItemClick(item.item, realIndex) 
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(item: DownloadedItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(onClick = onClick)) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.filePath)
                    .videoFrameMillis(1000)
                    .build(),
                contentDescription = item.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (item.mediaType == "VIDEO") {
                // Overlay for video indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}

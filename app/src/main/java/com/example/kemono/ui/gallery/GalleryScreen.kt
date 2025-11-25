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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.example.kemono.data.model.DownloadedItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
        viewModel: GalleryViewModel = hiltViewModel(),
        onItemClick: (DownloadedItem, Int) -> Unit
) {
    val items by viewModel.downloadedItems.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Gallery") }) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (items.isEmpty()) {
                Text(
                        text = "No downloaded items yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        GalleryItem(item = item, onClick = { onItemClick(item, index) })
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
                    model = File(item.filePath),
                    contentDescription = item.fileName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
            )
            if (item.mediaType == "VIDEO") {
                // Overlay for video indicator
                Box(
                        modifier =
                                Modifier.align(Alignment.BottomEnd).padding(4.dp).clickable(
                                                enabled = false
                                        ) {} // Consume clicks
                ) {
                    Text(
                            text = "VIDEO",
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier =
                                    Modifier.background(
                                                    androidx.compose.ui.graphics.Color.Black.copy(
                                                            alpha = 0.6f
                                                    ),
                                                    androidx.compose.foundation.shape
                                                            .RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

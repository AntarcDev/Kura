package com.example.kemono.ui.downloads

import android.app.DownloadManager
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import com.example.kemono.util.MediaType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
        viewModel: DownloadManagerViewModel = hiltViewModel(),
        onOpenClick: () -> Unit // Navigate to gallery
) {
        val items by viewModel.downloadItems.collectAsState()
        val layoutMode by viewModel.layoutMode.collectAsState()
        val gridDensity by viewModel.gridDensity.collectAsState()

        val minSize = when (gridDensity) {
            "Small" -> 120.dp
            "Large" -> 200.dp
            else -> 150.dp
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Downloads") },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onSurface
                                        )
                        )
                }
        ) { paddingValues ->
                if (items.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(paddingValues),
                                contentAlignment = Alignment.Center
                        ) { Text("No downloads yet", style = MaterialTheme.typography.bodyLarge) }
                } else {
                        if (layoutMode == "Grid") {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize().padding(paddingValues)
                            ) {
                                items(items, key = { it.item.id }) { uiState ->
                                    DownloadGridItem(
                                        uiState = uiState,
                                        onDelete = { viewModel.deleteDownload(uiState.item) },
                                        onOpen = onOpenClick
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    items(items, key = { it.item.id }) { uiState ->
                                            DownloadItemCard(
                                                    uiState = uiState,
                                                    onDelete = {
                                                            viewModel.deleteDownload(uiState.item)
                                                    },
                                                    onOpen = onOpenClick
                                            )
                                    }
                            }
                        }
                }
        }
}

@Composable
fun DownloadGridItem(
    uiState: DownloadItemUiState,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val item = uiState.item
    val status = uiState.status
    
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                            .data(item.filePath)
                    .videoFrameMillis(1000)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (status != null && status.status == DownloadManager.STATUS_RUNNING) {
                        LinearProgressIndicator(
                            progress = status.progress,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
                        )
                    } else if (status?.status == DownloadManager.STATUS_SUCCESSFUL) {
                         Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.Green
                        )
                    }
                }
            }
            
            // Delete button on top right
             IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
            ) {
                 Box(modifier = Modifier.background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))) {
                     Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(4.dp)
                     )
                 }
            }
        }
    }
}

@Composable
fun DownloadItemCard(uiState: DownloadItemUiState, onDelete: () -> Unit, onOpen: () -> Unit) {
    val item = uiState.item
    val status = uiState.status

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.filePath)
                            .videoFrameMillis(1000)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (item.mediaType == MediaType.VIDEO.name) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center).size(24.dp),
                            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date(item.downloadedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status != null) {
                when (status.status) {
                    DownloadManager.STATUS_RUNNING -> {
                        LinearProgressIndicator(
                            progress = status.progress,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "Downloading... ${(status.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    DownloadManager.STATUS_PENDING -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Pending...", style = MaterialTheme.typography.bodySmall)
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        LinearProgressIndicator(
                            progress = status.progress,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("Paused", style = MaterialTheme.typography.bodySmall)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Text(
                            "Failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Button(
                            onClick = onOpen,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) { Text("Open in Gallery") }
                    }
                }
            } else {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("Open in Gallery") }
            }
        }
    }
}

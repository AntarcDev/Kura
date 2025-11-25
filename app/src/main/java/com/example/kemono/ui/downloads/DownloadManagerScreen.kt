package com.example.kemono.ui.downloads

import android.app.DownloadManager
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                        LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(paddingValues),
                                contentPadding =
                                        androidx.compose.foundation.layout.PaddingValues(16.dp),
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

@Composable
fun DownloadItemCard(uiState: DownloadItemUiState, onDelete: () -> Unit, onOpen: () -> Unit) {
        val item = uiState.item
        val status = uiState.status

        Card(
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                modifier = Modifier.fillMaxWidth()
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(
                                        imageVector =
                                                if (item.mediaType == MediaType.VIDEO.name)
                                                        Icons.Default.Movie
                                                else Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = item.fileName,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                                text =
                                                        SimpleDateFormat(
                                                                        "MMM dd, HH:mm",
                                                                        Locale.getDefault()
                                                                )
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
                                                        text =
                                                                "Downloading... ${(status.progress * 100).toInt()}%",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                )
                                        }
                                        DownloadManager.STATUS_PENDING -> {
                                                LinearProgressIndicator(
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(
                                                        "Pending...",
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                        }
                                        DownloadManager.STATUS_PAUSED -> {
                                                LinearProgressIndicator(
                                                        progress = status.progress,
                                                        modifier = Modifier.fillMaxWidth(),
                                                )
                                                Text(
                                                        "Paused",
                                                        style = MaterialTheme.typography.bodySmall
                                                )
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
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer,
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                ) { Text("Open in Gallery") }
                                        }
                                }
                        } else {
                                // Assuming successful if no status found (or old download)
                                // But wait, if downloadId is -1, it's an old download.
                                // If downloadId is set but status is null, maybe it's finished and
                                // cleared from
                                // system?
                                // Let's assume if status is null, we check if file exists?
                                // For now, let's show "Open" if status is null or successful.
                                Button(
                                        onClick = onOpen,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer,
                                                        contentColor =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                )
                                ) { Text("Open in Gallery") }
                        }
                }
        }
}

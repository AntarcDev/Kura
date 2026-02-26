package com.example.kemono.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayer(
    url: String,
    modifier: Modifier = Modifier,
    onDownloadClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                isPlaying = isPlayingState
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Polling for progress
    LaunchedEffect(isPlaying, isDragging) {
        if (isPlaying && !isDragging) {
            while (true) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                delay(500) // Update every 500ms
            }
        }
    }

    // Ensure duration is set
    LaunchedEffect(Unit) {
        while(duration <= 0) {
             if (exoPlayer.duration > 0) {
                 duration = exoPlayer.duration
             }
             delay(1000)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                    
                    Slider(
                        value = if (isDragging) sliderValue else progress,
                        onValueChange = { 
                            isDragging = true
                            sliderValue = it
                        },
                        onValueChangeFinished = {
                            if (duration > 0) {
                                val newPosition = (sliderValue * duration).toLong()
                                exoPlayer.seekTo(newPosition)
                                currentPosition = newPosition
                            }
                            isDragging = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatDuration(if (isDragging) (sliderValue * duration).toLong() else currentPosition),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (onDownloadClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download"
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

package com.example.kemono.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Box(modifier = modifier) {
        AndroidView(
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams =
                                FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                    }
                },
                modifier = Modifier.matchParentSize()
        )

        androidx.compose.material3.IconButton(
                onClick = {
                    FullscreenVideoActivity.launch(context, url, exoPlayer.currentPosition)
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
        ) {
            androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

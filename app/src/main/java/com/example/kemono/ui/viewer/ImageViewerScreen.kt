package com.example.kemono.ui.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kemono.ui.components.ZoomableImage
import com.example.kemono.util.MediaType
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(viewModel: ImageViewerViewModel = hiltViewModel(), onBackClick: () -> Unit) {
    val items by viewModel.mediaItems.collectAsState()
    val initialIndex = viewModel.initialIndex

    if (items.isNotEmpty()) {
        val pagerState = rememberPagerState(initialPage = initialIndex) { items.size }

        Scaffold(
                containerColor = Color.Black,
                topBar = {
                    TopAppBar(
                            title = {
                                Text(
                                        text = "${pagerState.currentPage + 1} / ${items.size}",
                                        color = Color.White
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                    )
                                }
                            },
                            colors =
                                    TopAppBarDefaults.topAppBarColors(
                                            containerColor = Color.Black.copy(alpha = 0.5f)
                                    )
                    )
                }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val item = items[page]
                    if (item.type == MediaType.VIDEO) {
                        // For video, we might want to show a placeholder or a play button
                        // Since ZoomableImage uses AsyncImage, it might show a thumbnail
                        // But we really want to play it.
                        // For now, let's just show the thumbnail/image and maybe a play icon
                        // overlay?
                        // Or better, reuse VideoPlayer if it's a video?
                        // But VideoPlayer inside a Pager might be heavy.
                        // Let's stick to ZoomableImage for now (it will show thumbnail) and maybe
                        // add a "Play" overlay
                        // that launches the fullscreen player?
                        // Actually, the user asked for "Image Gallery Viewer".
                        // But if it's a video, they probably want to watch it.
                        // Let's use ZoomableImage and if it's a video, show a play icon.
                        Box(modifier = Modifier.fillMaxSize()) {
                            ZoomableImage(
                                    model = if (item.isLocal) File(item.url) else item.url,
                                    contentDescription = item.name
                            )
                            // Play button overlay
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Play Video",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier =
                                            Modifier.align(Alignment.Center).size(80.dp).clickable {
                                                com.example.kemono.ui.components
                                                        .FullscreenVideoActivity.launch(
                                                        context,
                                                        item.url
                                                )
                                            }
                            )
                        }
                    } else {
                        ZoomableImage(
                                model = if (item.isLocal) File(item.url) else item.url,
                                contentDescription = item.name
                        )
                    }
                }
            }
        }
    } else {
        // Loading or empty
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
    }
}

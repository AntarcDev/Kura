package com.example.kemono.ui.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.kemono.ui.creators.CreatorScreen
import com.example.kemono.ui.downloads.DownloadManagerScreen
import com.example.kemono.ui.favorites.FavoritesScreen
import com.example.kemono.ui.gallery.GalleryScreen
import com.example.kemono.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Creators : BottomNavItem("creators", Icons.Default.Home, "Creators")
    data object Downloads : BottomNavItem("downloads", Icons.Default.Download, "Downloads")
    data object Gallery : BottomNavItem("gallery", Icons.Default.Image, "Gallery")
    data object Favorites : BottomNavItem("favorites", Icons.Default.Favorite, "Favorites")
    data object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
        onCreatorClick: (com.example.kemono.data.model.Creator) -> Unit,
        onNavigateToGalleryItem: (Int) -> Unit
) {
    val items =
            listOf(
                    BottomNavItem.Creators,
                    BottomNavItem.Downloads,
                    BottomNavItem.Gallery,
                    BottomNavItem.Favorites,
                    BottomNavItem.Settings
            )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    // Permission Request
    val permissionLauncher =
            rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts
                            .RequestMultiplePermissions()
            ) { _ ->
                // Permissions granted or denied. We can handle specific logic here if needed.
                // For now, we just request them to ensure access.
            }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                    arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                    )
            )
        } else {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    LaunchedEffect(pagerState.currentPage) { selectedItem = pagerState.currentPage }

    Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selectedItem == index,
                                onClick = {
                                    selectedItem = index
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }
            }
    ) { paddingValues ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(paddingValues)) { page ->
            when (items[page]) {
                BottomNavItem.Creators -> CreatorScreen(onCreatorClick = onCreatorClick)
                BottomNavItem.Downloads ->
                        DownloadManagerScreen(
                                onOpenClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(2)
                                    } // Switch to Gallery
                                }
                        )
                BottomNavItem.Gallery ->
                        GalleryScreen(onItemClick = { _, index -> onNavigateToGalleryItem(index) })
                BottomNavItem.Favorites -> FavoritesScreen(onCreatorClick = onCreatorClick)
                BottomNavItem.Settings -> SettingsScreen()
            }
        }
    }
}

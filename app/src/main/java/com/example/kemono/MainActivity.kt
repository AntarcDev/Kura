package com.example.kemono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kemono.data.repository.SettingsRepository
import com.example.kemono.ui.main.MainScreen
import com.example.kemono.ui.posts.CreatorPostListScreen
import com.example.kemono.ui.posts.PostScreen
import com.example.kemono.ui.theme.KemonoTheme
import com.example.kemono.ui.viewer.ImageViewerScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "System")
            val darkTheme =
                    when (themeMode) {
                        "Light" -> false
                        "Dark" -> true
                        else -> androidx.compose.foundation.isSystemInDarkTheme()
                    }

            KemonoTheme(darkTheme = darkTheme) {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                    onCreatorClick = { creator ->
                                        navController.navigate(
                                                "creator/${creator.service}/${creator.id}"
                                        )
                                    },
                                    onNavigateToGalleryItem = { index ->
                                        navController.navigate("viewer/gallery/all/$index")
                                    }
                            )
                        }
                        composable(
                                route = "creator/{service}/{creatorId}",
                                arguments =
                                        listOf(
                                                navArgument("service") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("creatorId") {
                                                    type = NavType.StringType
                                                }
                                        )
                        ) {
                            CreatorPostListScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onPostClick = { post ->
                                        navController.navigate(
                                                "post/${post.service}/${post.user}/${post.id}"
                                        )
                                    }
                            )
                        }
                        composable(
                                route = "post/{service}/{creatorId}/{postId}",
                                arguments =
                                        listOf(
                                                navArgument("service") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("creatorId") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("postId") { type = NavType.StringType }
                                        )
                        ) {
                            PostScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onImageClick = { index ->
                                        val service = it.arguments?.getString("service") ?: ""
                                        val creatorId = it.arguments?.getString("creatorId") ?: ""
                                        val postId = it.arguments?.getString("postId") ?: ""
                                        val id = "$service|$creatorId|$postId"
                                        navController.navigate("viewer/post/$id/$index")
                                    }
                            )
                        }
                        composable(
                                route = "viewer/{type}/{id}/{initialIndex}",
                                arguments =
                                        listOf(
                                                navArgument("type") { type = NavType.StringType },
                                                navArgument("id") { type = NavType.StringType },
                                                navArgument("initialIndex") {
                                                    type = NavType.StringType
                                                }
                                        )
                        ) {
                            ImageViewerScreen(onBackClick = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

package com.example.kemono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kemono.ui.creators.CreatorScreen
import com.example.kemono.ui.favorites.FavoritesScreen
import com.example.kemono.ui.posts.CreatorPostListScreen
import com.example.kemono.ui.posts.PostScreen
import com.example.kemono.ui.settings.SettingsScreen
import com.example.kemono.ui.theme.KemonoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KemonoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "creators") {
                        composable("creators") {
                            CreatorScreen(
                                onCreatorClick = { creator ->
                                    navController.navigate("creator/${creator.service}/${creator.id}")
                                },
                                onFavoritesClick = {
                                    navController.navigate("favorites")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable(
                            route = "creator/{service}/{creatorId}",
                            arguments = listOf(
                                navArgument("service") { type = NavType.StringType },
                                navArgument("creatorId") { type = NavType.StringType }
                            )
                        ) {
                            CreatorPostListScreen(
                                onBackClick = { navController.popBackStack() },
                                onPostClick = { post ->
                                    navController.navigate("post/${post.service}/${post.user}/${post.id}")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                onCreatorClick = { creator ->
                                    // Handle click, maybe navigate to posts
                                }
                            )
                        }
                        composable(
                            route = "post/{service}/{creatorId}/{postId}",
                            arguments = listOf(
                                navArgument("service") { type = NavType.StringType },
                                navArgument("creatorId") { type = NavType.StringType },
                                navArgument("postId") { type = NavType.StringType }
                            )
                        ) {
                            PostScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


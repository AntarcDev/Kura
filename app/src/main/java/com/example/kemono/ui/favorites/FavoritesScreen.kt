package com.example.kemono.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kemono.data.model.Creator
import com.example.kemono.ui.creators.CreatorItem

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onCreatorClick: (Creator) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (favorites.isEmpty()) {
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favorites) { creator ->
                        // We can reuse CreatorItem, but we need to handle the favorite toggle if we want it here too.
                        // For now, let's just show it as a list.
                        // To reuse CreatorItem fully, we'd need to pass isFavorite=true and a dummy or actual toggle.
                        // Since it's the favorites screen, isFavorite is always true.
                        CreatorItem(
                            creator = creator,
                            isFavorite = true,
                            onClick = { onCreatorClick(creator) },
                            onFavoriteClick = { /* Optional: Remove from favorites */ }
                        )
                    }
                }
            }
        }
    }
}

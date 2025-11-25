package com.example.kemono.ui.creators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kemono.data.model.Creator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
        viewModel: CreatorViewModel = hiltViewModel(),
        onCreatorClick: (Creator) -> Unit
) {
    val creators by viewModel.creators.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isLoading.collectAsState() // Reusing isLoading for refresh state

    val sortOption by viewModel.sortOption.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            FilterSortBottomSheet(
                    sortOption = sortOption,
                    onSortOptionSelected = viewModel::setSortOption,
                    availableServices = availableServices,
                    selectedServices = selectedServices,
                    onServiceToggle = viewModel::toggleServiceFilter,
                    onReset = viewModel::clearFilters
            )
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Kemono") },
                        actions = {
                            IconButton(onClick = { showBottomSheet = true }) {
                                Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Filter & Sort"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.fetchCreators() },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Redesigned Search Bar
                TextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clip(RoundedCornerShape(50)),
                        placeholder = { Text("Search creators...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        colors =
                                TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                )
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && creators.isEmpty()) {
                        // Initial load
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (error != null && creators.isEmpty()) {
                        Text(
                                text = error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column {
                            val isOnline by viewModel.isOnline.collectAsState()
                            if (!isOnline) {
                                Text(
                                        text = "Offline Mode - Showing cached content",
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .errorContainer
                                                        )
                                                        .padding(8.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Center
                                )
                            }
                            LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(creators) { creator ->
                                    val isFavorite = favorites.any { it.id == creator.id }
                                    CreatorItem(
                                            creator = creator,
                                            isFavorite = isFavorite,
                                            onClick = { onCreatorClick(creator) },
                                            onFavoriteClick = { viewModel.toggleFavorite(creator) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSortBottomSheet(
        sortOption: SortOption,
        onSortOptionSelected: (SortOption) -> Unit,
        availableServices: List<String>,
        selectedServices: Set<String>,
        onServiceToggle: (String) -> Unit,
        onReset: () -> Unit
) {
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp), // Add some bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sort By", style = MaterialTheme.typography.titleMedium)
        Column {
            SortOption.values().forEach { option ->
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { onSortOptionSelected(option) }
                                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                            selected = sortOption == option,
                            onClick = { onSortOptionSelected(option) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option.name)
                }
            }
        }

        HorizontalDivider()

        Text("Filter by Service", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availableServices.forEach { service ->
                FilterChip(
                        selected = selectedServices.contains(service),
                        onClick = { onServiceToggle(service) },
                        label = { Text(service) }
                )
            }
        }

        Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Reset Filters") }
    }
}

@Composable
fun CreatorItem(
        creator: Creator,
        isFavorite: Boolean,
        onClick: () -> Unit,
        onFavoriteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Profile Picture
            AsyncImage(
                    model = "https://kemono.cr/icons/${creator.service}/${creator.id}",
                    contentDescription = null,
                    modifier =
                            Modifier.size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = creator.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                                imageVector =
                                        if (isFavorite) Icons.Default.Favorite
                                        else Icons.Default.FavoriteBorder,
                                contentDescription =
                                        if (isFavorite) "Remove from favorites"
                                        else "Add to favorites",
                                tint =
                                        if (isFavorite) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                            text = creator.service,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = "ID: ${creator.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

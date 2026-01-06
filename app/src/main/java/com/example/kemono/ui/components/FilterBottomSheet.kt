package com.example.kemono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kemono.ui.creators.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    searchMode: com.example.kemono.ui.creators.SearchMode,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    isAscending: Boolean,
    onAscendingToggle: () -> Unit,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onClearServices: () -> Unit,
    availableServices: List<String>,
    availableTags: List<String> = emptyList(),
    selectedTags: Set<String> = emptySet(),
    onTagToggle: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                width = 40.dp,
                height = 4.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        
        // Adjust tabs based on mode
        val showTags = searchMode == com.example.kemono.ui.creators.SearchMode.Posts
        val tabs = if (showTags) {
            listOf("Services", "Tags", "Sorting")
        } else {
            listOf("Services", "Sorting") // 0=Services, 1=Sorting
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Full Width Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                if (showTags) {
                    when (selectedTabIndex) {
                        0 -> ServicesContent(selectedServices, onServiceToggle, onClearServices, availableServices)
                        1 -> TagsContent(selectedTags, onTagToggle, availableTags)
                        2 -> SortingContent(sortOption, onSortOptionSelected, availableServices, searchMode, isAscending, onAscendingToggle)
                    }
                } else {
                    when (selectedTabIndex) {
                        0 -> ServicesContent(selectedServices, onServiceToggle, onClearServices, availableServices)
                        1 -> SortingContent(sortOption, onSortOptionSelected, availableServices, searchMode, isAscending, onAscendingToggle)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsContent(
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    availableTags: List<String>
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTags = remember(searchQuery, availableTags) {
        if (searchQuery.isBlank()) {
            availableTags.take(50)
        } else {
            availableTags.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tags...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) } }
            } else null
        )
        
        // Pinned Selected Tags
        if (selectedTags.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedTags.toList().size) { index ->
                    val tag = selectedTags.toList()[index]
                    InputChip(
                        selected = true,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
            HorizontalDivider()
        }

        if (availableTags.isEmpty()) {
             Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No tags available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                 item {
                    val message = if (searchQuery.isBlank() && availableTags.size > 50) {
                        "Showing top 50 tags. Search to find more."
                    } else null

                    if (message != null) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredTags.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTagToggle(tag) },
                                label = { Text(tag) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServicesContent(
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onClearServices: () -> Unit,
    availableServices: List<String>
) {
    // Hex codes mapping
    val serviceColors = mapOf(
        "patreon" to Color(0xFFf96854),
        "fanbox" to Color(0xFF0096fa),
        "discord" to Color(0xFF5865f2),
        "fantia" to Color(0xFFea4c89),
        "boosty" to Color(0xFFf15f2c),
        "gumroad" to Color(0xFFff90e8),
        "subscribestar" to Color(0xFF429488),
        "dlsite" to Color(0xFF052A83) // 052A83FF -> 0xFF...
    )

    fun getDisplayName(service: String): String {
        return when (service.lowercase()) {
            "dlsite" -> "DLsite"
            "subscribestar" -> "SubscribeStar"
            "fanbox" -> "Pixiv Fanbox"
            else -> service.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // All Services Button - Centered above
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val isAllSelected = selectedServices.isEmpty()
            FilterChip(
                selected = isAllSelected,
                onClick = onClearServices,
                label = { Text("All Services") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Define fixed list or use available
                    val knownServices = listOf("patreon", "fanbox", "discord", "fantia", "boosty", "gumroad", "subscribestar", "dlsite")
                    val dynamicServices = availableServices.filter { !knownServices.contains(it) }
                    val displayList = knownServices + dynamicServices

                    displayList.forEach { service ->
                        val isSelected = selectedServices.contains(service)
                        val baseColor = serviceColors[service] ?: MaterialTheme.colorScheme.primary
                        
                        val containerColor = if (isSelected) baseColor else baseColor.copy(alpha = 0.1f)
                        val contentColor = if (isSelected) Color.White else baseColor
                        val borderColor = if (isSelected) Color.Transparent else baseColor.copy(alpha = 0.5f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .clickable { onServiceToggle(service) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = getDisplayName(service),
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortingContent(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    availableServices: List<String>,
    searchMode: com.example.kemono.ui.creators.SearchMode? = null,
    isAscending: Boolean? = null,
    onAscendingToggle: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Ascending/Descending Toggle for Artists
        if (searchMode == com.example.kemono.ui.creators.SearchMode.Artists && isAscending != null && onAscendingToggle != null) {
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAscendingToggle() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isAscending) "Ascending Order" else "Descending Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(thickness = 2.dp)
        }

        val allOptions = listOf(
            SortOption.Popular to "Popularity",
            SortOption.Indexed to "Date Indexed",
            SortOption.Updated to "Date Updated",
            SortOption.Name to "Alphabetical",
            SortOption.Service to "Service"
        )
        
        val options = if (searchMode == com.example.kemono.ui.creators.SearchMode.Posts) {
            // For Posts, we effectively only support Recent (Indexed/Updated) and Popular
             allOptions.filter { 
                it.first == SortOption.Popular || 
                it.first == SortOption.Indexed || 
                it.first == SortOption.Updated
            }
        } else {
            allOptions
        }
        
        options.forEach { (option, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSortSelected(option) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentSort == option,
                    onClick = { onSortSelected(option) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

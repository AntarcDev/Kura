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
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    isAscending: Boolean,
    onAscendingToggle: () -> Unit,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onClearServices: () -> Unit,
    availableServices: List<String>
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Add padding for navigation bar
        ) {
            // Header Row: Tabs + Asc/Desc Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)), // Rounded Tabs
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Services") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Sorting") }
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Asc/Desc Toggle
                FilledTonalIconButton(
                    onClick = onAscendingToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = if (isAscending) "Ascending" else "Descending"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (selectedTabIndex == 0) {
                    ServicesContent(
                        selectedServices = selectedServices,
                        onServiceToggle = onServiceToggle,
                        onClearServices = onClearServices,
                        availableServices = availableServices
                    )
                } else {
                    SortingContent(
                        currentSort = sortOption,
                        onSortSelected = onSortOptionSelected,
                        availableServices = availableServices
                    )
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
    availableServices: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val options = listOf(
            SortOption.Popular to "Popularity",
            SortOption.Indexed to "Date Indexed",
            SortOption.Updated to "Date Updated",
            SortOption.Name to "Alphabetical",
            SortOption.Service to "Service"
        )
        
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

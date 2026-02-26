package com.example.kemono.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit = {},
    onClearSearch: () -> Unit = {},
    sortOptions: List<String> = emptyList(),
    selectedSort: String? = null,
    onSortSelected: (String) -> Unit = {},
    filterOptions: List<String> = emptyList(),
    selectedFilters: List<String> = emptyList(),
    onFilterSelected: (String) -> Unit = {},
    onFilterClick: (() -> Unit)? = null,
    onTagsClick: (() -> Unit)? = null,
    placeholderText: String = "Search...",
    
    // Search History
    searchHistory: List<com.example.kemono.data.model.SearchHistory> = emptyList(),
    onHistoryItemClick: (String) -> Unit = {},
    onHistoryItemRemove: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},

    trailingContent: @Composable (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    androidx.compose.material3.CenterAlignedTopAppBar(
        title = {
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { 
                        onQueryChange(it)
                        showHistory = true 
                    },
                    placeholder = { Text(placeholderText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { showHistory = it.isFocused },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {
                                    onQueryChange("")
                                    onClearSearch()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                            
                            // Filter Logic inside Search Bar
                            if (sortOptions.isNotEmpty() || filterOptions.isNotEmpty() || onFilterClick != null) {
                                IconButton(onClick = { 
                                    if (onFilterClick != null) {
                                        onFilterClick()
                                    } else {
                                        showMenu = true 
                                    }
                                }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
                                }
                                
                                if (onTagsClick != null) {
                                  IconButton(onClick = onTagsClick) {
                                      Icon(Icons.Default.List, contentDescription = "Tags")
                                  }
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    if (sortOptions.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Sort By", style = MaterialTheme.typography.labelLarge) },
                                            onClick = { }
                                        )
                                        sortOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(option)
                                                    }
                                                },
                                                onClick = {
                                                    onSortSelected(option)
                                                    showMenu = false
                                                },
                                                trailingIcon = if (option == selectedSort) {
                                                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                                } else null
                                            )
                                        }
                                    }
                                    
                                    if (sortOptions.isNotEmpty() && filterOptions.isNotEmpty()) {
                                        androidx.compose.material3.HorizontalDivider()
                                    }

                                    if (filterOptions.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Filter By", style = MaterialTheme.typography.labelLarge) },
                                            onClick = { }
                                        )
                                        filterOptions.forEach { option ->
                                            val isSelected = selectedFilters.contains(option)
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    onFilterSelected(option)
                                                },
                                                trailingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Extra Content (e.g. Sync button)
                            trailingContent?.invoke()
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = if (com.example.kemono.LocalIncognitoKeyboard.current) androidx.compose.ui.text.input.KeyboardType.Password else androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch()
                        showHistory = false
                        focusManager.clearFocus()
                    })
                )

                DropdownMenu(
                    expanded = showHistory && searchHistory.isNotEmpty() && query.isEmpty(), // Show when focused and empty (recent history)
                    onDismissRequest = { showHistory = false },
                    properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.Center)
                ) {
                     searchHistory.forEach { apiHistory ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(apiHistory.query, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            },
                            onClick = {
                                onHistoryItemClick(apiHistory.query)
                                showHistory = false
                                focusManager.clearFocus()
                            },
                            trailingIcon = {
                                IconButton(onClick = { onHistoryItemRemove(apiHistory.query) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Clear Search History", 
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        onClick = {
                            onClearHistory()
                            showHistory = false
                        }
                    )
                }
            }
        },
        actions = actions
    )
}

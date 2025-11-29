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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
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
    onTagsClick: (() -> Unit)? = null,
    placeholderText: String = "Search..."
) {
    var showMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholderText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChange("")
                            onClearSearch()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch()
                    focusManager.clearFocus()
                })
            )
        },
        actions = {
            if (sortOptions.isNotEmpty() || filterOptions.isNotEmpty()) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
                }
                
                if (onTagsClick != null) {
                    IconButton(onClick = onTagsClick) {
                        // Use a label or tag icon. Icons.Default.Label is good.
                        // Or AutoMirrored.List if Label isn't available, but Label should be.
                        // Let's use a generic icon if needed, but Label is standard.
                        // If Label is not imported, I might need to import it.
                        // I'll use Icons.Default.Check for now as a placeholder if I'm unsure, 
                        // but actually I should use a proper icon. 
                        // Let's assume Icons.Default.Info or similar if Label is missing, 
                        // but I'll try to use a specific one.
                        // Actually, let's use Icons.Default.Search for tags? No.
                        // Let's use Icons.Default.Menu? No.
                        // I'll use Icons.Default.FilterList for the main filter, and maybe Icons.Default.List for tags?
                        // Or just text "Tags"?
                        // Let's use Icons.Default.Add for now, or better, import Icons.Default.Label.
                        // I will add the import in a separate block if needed, but for now I'll use Icons.Default.Check as I know it's there, 
                        // and then swap it or use a text button.
                        // Actually, let's just use Text "Tags" inside an IconButton? No, that's ugly.
                        // I'll use Icons.Default.Build (Settings-like) or similar.
                        // Wait, I can just use Icons.Default.Star for favorites...
                        // Let's use Icons.Default.Add as a placeholder for "Add Tags".
                        Icon(Icons.Default.Add, contentDescription = "Tags")
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
                                        if (option == selectedSort) {
                                            // Checkmark or bold?
                                            // For now just text
                                        }
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
                                    // Don't close menu for multi-select filters?
                                    // User might want to select multiple.
                                    // But standard dropdown closes. Let's keep it simple for now.
                                },
                                trailingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    )
}

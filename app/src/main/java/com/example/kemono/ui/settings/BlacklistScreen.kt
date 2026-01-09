package com.example.kemono.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kemono.data.local.BlacklistEntity
import com.example.kemono.data.local.BlacklistType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlacklistScreen(
    onBackClick: () -> Unit,
    viewModel: BlacklistViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val titles = listOf("Creators", "Tags", "Keywords")
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        /* topBar removed to avoid duplication with SettingsScreen */
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> BlockedCreatorsList(viewModel)
                    1 -> BlockedTagsList(viewModel)
                    2 -> BlockedKeywordsList(viewModel)
                }
            }
        }
        
        if (showAddDialog) {
            val currentType = when (pagerState.currentPage) {
                0 -> BlacklistType.CREATOR
                1 -> BlacklistType.TAG
                else -> BlacklistType.KEYWORD
            }
            AddBlacklistDialog(
                type = currentType,
                onDismiss = { showAddDialog = false },
                onAdd = { name, id, service ->
                    when (currentType) {
                        BlacklistType.CREATOR -> viewModel.addCreator(id ?: name, name, service ?: "")
                        BlacklistType.TAG -> viewModel.addTag(name)
                        BlacklistType.KEYWORD -> viewModel.addKeyword(name)
                        else -> {}
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BlockedCreatorsList(viewModel: BlacklistViewModel) {
    val creators by viewModel.creators.collectAsState()
    BlockedList(items = creators, onDelete = { viewModel.removeItem(it) }, emptyMessage = "No blocked creators")
}

@Composable
fun BlockedTagsList(viewModel: BlacklistViewModel) {
    val tags by viewModel.tags.collectAsState()
    BlockedList(items = tags, onDelete = { viewModel.removeItem(it) }, emptyMessage = "No blocked tags")
}

@Composable
fun BlockedKeywordsList(viewModel: BlacklistViewModel) {
    val keywords by viewModel.keywords.collectAsState()
    BlockedList(items = keywords, onDelete = { viewModel.removeItem(it) }, emptyMessage = "No blocked keywords")
}

@Composable
fun BlockedList(
    items: List<BlacklistEntity>,
    onDelete: (BlacklistEntity) -> Unit,
    emptyMessage: String
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(items, key = { it.id + it.type }) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = if (item.service != null) { { Text(item.service) } } else null,
                    trailingContent = {
                        IconButton(onClick = { onDelete(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Unblock")
                        }
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun AddBlacklistDialog(
    type: BlacklistType,
    onDismiss: () -> Unit,
    onAdd: (name: String, id: String?, service: String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var creatorId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block ${type.name.lowercase().capitalize()}") },
        text = {
            Column {
                if (type == BlacklistType.CREATOR) {
                     Text("Note: To block a creator efficiently, use their ID. Blocking by name is not supported yet.", style = MaterialTheme.typography.bodySmall)
                     OutlinedTextField(
                        value = creatorId,
                        onValueChange = { creatorId = it },
                        label = { Text("Creator ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                     OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Creator Name (Display)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = service,
                        onValueChange = { service = it },
                        label = { Text("Service (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(if (type == BlacklistType.TAG) "Tag Name" else "Keyword") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (type == BlacklistType.CREATOR) {
                        if (creatorId.isNotBlank()) onAdd(text.ifBlank { creatorId }, creatorId, service)
                    } else {
                        if (text.isNotBlank()) onAdd(text, null, null)
                    }
                },
                enabled = if (type == BlacklistType.CREATOR) creatorId.isNotBlank() else text.isNotBlank()
            ) {
                Text("Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

package com.example.kemono.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val sessionCookie by viewModel.sessionCookie.collectAsState()
    val hasSession by viewModel.hasSession.collectAsState()
    val initStatus by viewModel.initStatus.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    var cookieInput by remember { mutableStateOf(sessionCookie) }

    Scaffold(topBar = { TopAppBar(title = { Text("Kura Settings") }) }) { paddingValues ->
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Settings
            Text(text = "App Settings", style = MaterialTheme.typography.titleLarge)

            // Theme Setting
            var expandedTheme by remember { mutableStateOf(false) }
            val themeMode by viewModel.themeMode.collectAsState()
            ExposedDropdownMenuBox(
                expanded = expandedTheme,
                onExpandedChange = { expandedTheme = !expandedTheme }
            ) {
                OutlinedTextField(
                    value = themeMode,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Theme") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTheme,
                    onDismissRequest = { expandedTheme = false }
                ) {
                    listOf("System", "Light", "Dark").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                viewModel.setThemeMode(mode)
                                expandedTheme = false
                            }
                        )
                    }
                }
            }

            // Grid Size Setting
            var expandedGrid by remember { mutableStateOf(false) }
            val gridSize by viewModel.gridSize.collectAsState()
            ExposedDropdownMenuBox(
                expanded = expandedGrid,
                onExpandedChange = { expandedGrid = !expandedGrid }
            ) {
                OutlinedTextField(
                    value = gridSize,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Grid Size") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrid) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedGrid,
                    onDismissRequest = { expandedGrid = false }
                ) {
                    listOf("Compact", "Comfortable").forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = {
                                viewModel.setGridSize(size)
                                expandedGrid = false
                            }
                        )
                    }
                }
            }

            // Clear Cache
            Button(
                onClick = { viewModel.clearCache() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Cache")
            }

            HorizontalDivider()

            // DDoS-Guard Section
            Text(text = "DDoS-Guard Protection", style = MaterialTheme.typography.titleLarge)
            
            // ... (rest of DDoS section remains similar, just condensed for brevity in this replacement block if needed, but I'll keep the structure)
            Text(
                    text = "kemono.cr uses DDoS-Guard protection. Initialize cookies before using the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                    onClick = { viewModel.initializeDDoSGuard() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isInitializing
            ) {
                if (isInitializing) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isInitializing) "Initializing..." else "Initialize DDoS-Guard Cookies")
            }

            if (initStatus.isNotEmpty()) {
                Card(
                        colors = CardDefaults.cardColors(
                                containerColor = when {
                                    initStatus.startsWith("✓") -> MaterialTheme.colorScheme.primaryContainer
                                    initStatus.startsWith("⚠") -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.errorContainer
                                }
                        )
                ) { Text(text = initStatus, modifier = Modifier.padding(16.dp)) }
            }

            HorizontalDivider()

            // Session Cookie Section
            Text(text = "Session Cookie (Optional)", style = MaterialTheme.typography.titleLarge)
            
            OutlinedTextField(
                    value = cookieInput,
                    onValueChange = { cookieInput = it },
                    label = { Text("Session Cookie") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
            )

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                        onClick = { viewModel.updateSessionCookie(cookieInput) },
                        modifier = Modifier.weight(1f)
                ) { Text("Save Cookie") }

                if (hasSession) {
                    OutlinedButton(
                            onClick = {
                                viewModel.clearSession()
                                cookieInput = ""
                            },
                            modifier = Modifier.weight(1f)
                    ) { Text("Clear Session") }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Section
            AboutSection()
        }
    }
}

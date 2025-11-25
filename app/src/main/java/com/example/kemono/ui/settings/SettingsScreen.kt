package com.example.kemono.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val sessionCookie by viewModel.sessionCookie.collectAsState()
    val hasSession by viewModel.hasSession.collectAsState()
    val initStatus by viewModel.initStatus.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    var cookieInput by remember { mutableStateOf(sessionCookie) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DDoS-Guard Section
            Text(
                text = "DDoS-Guard Protection",
                style = MaterialTheme.typography.titleLarge
            )
            
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
                ) {
                    Text(
                        text = initStatus,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Session Cookie (Optional)",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "To access the API, you need to provide your session cookie from kemono.cr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How to get your session cookie:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("1. Open kemono.cr in your browser", style = MaterialTheme.typography.bodySmall)
                    Text("2. Log in to your account", style = MaterialTheme.typography.bodySmall)
                    Text("3. Open Developer Tools (F12)", style = MaterialTheme.typography.bodySmall)
                    Text("4. Go to Network tab and refresh the page", style = MaterialTheme.typography.bodySmall)
                    Text("5. Click any request to kemono.cr", style = MaterialTheme.typography.bodySmall)
                    Text("6. In Headers, find 'Cookie:' under Request Headers", style = MaterialTheme.typography.bodySmall)
                    Text("7. Copy the entire cookie value", style = MaterialTheme.typography.bodySmall)
                    Text("", style = MaterialTheme.typography.bodySmall)
                    Text("Format: session=xxx; __ddg1_=xxx", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

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
                    onClick = {
                        viewModel.updateSessionCookie(cookieInput)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Cookie")
                }

                if (hasSession) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearSession()
                            cookieInput = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Session")
                    }
                }
            }

            if (hasSession) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Session cookie is set",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

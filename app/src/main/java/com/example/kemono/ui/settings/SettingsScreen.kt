package com.example.kemono.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.kemono.ui.components.UpdateDialog

enum class SettingsRoute {
    Main,
    General,
    Appearance,
    Data,
    Advanced
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var currentRoute by remember { mutableStateOf(SettingsRoute.Main) }
    
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val updateError by viewModel.updateError.collectAsState()
    val readyToInstall by viewModel.readyToInstall.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(readyToInstall) {
        readyToInstall?.let { file ->
            val intent = viewModel.getInstallIntent(file)
            context.startActivity(intent)
            viewModel.dismissUpdateDialog()
        }
    }

    LaunchedEffect(updateError) {
        updateError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    if (updateAvailable != null) {
        UpdateDialog(
            release = updateAvailable!!,
            downloadProgress = downloadProgress,
            onUpdateClick = { viewModel.downloadUpdate() },
            onDismissClick = { viewModel.dismissUpdateDialog() }
        )
    }

    // Handle back press to navigate up in settings hierarchy
    BackHandler(enabled = currentRoute != SettingsRoute.Main) {
        currentRoute = SettingsRoute.Main
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            SettingsRoute.Main -> "Settings"
                            SettingsRoute.General -> "General"
                            SettingsRoute.Appearance -> "Appearance"
                            SettingsRoute.Data -> "Data & Storage"
                            SettingsRoute.Advanced -> "Advanced"
                        }
                    )
                },
                navigationIcon = {
                    if (currentRoute != SettingsRoute.Main) {
                        IconButton(onClick = { currentRoute = SettingsRoute.Main }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentRoute) {
                SettingsRoute.Main -> SettingsMain(
                    onNavigate = { currentRoute = it }
                )
                SettingsRoute.General -> SettingsGeneral(viewModel)
                SettingsRoute.Appearance -> SettingsAppearance(viewModel)
                SettingsRoute.Data -> SettingsData(viewModel)
                SettingsRoute.Advanced -> SettingsAdvanced(viewModel)
            }
        }
    }
}

@Composable
fun SettingsMain(onNavigate: (SettingsRoute) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsCategoryItem(
            icon = Icons.Default.Info,
            title = "General",
            subtitle = "App information and about",
            onClick = { onNavigate(SettingsRoute.General) }
        )
        SettingsCategoryItem(
            icon = Icons.Default.Edit,
            title = "Appearance",
            subtitle = "Theme, grid size, and UI customization",
            onClick = { onNavigate(SettingsRoute.Appearance) }
        )
        SettingsCategoryItem(
            icon = Icons.Default.Delete,
            title = "Data & Storage",
            subtitle = "Cache management",
            onClick = { onNavigate(SettingsRoute.Data) }
        )
        SettingsCategoryItem(
            icon = Icons.Default.Build,
            title = "Advanced",
            subtitle = "DDoS-Guard, cookies, and developer tools",
            onClick = { onNavigate(SettingsRoute.Advanced) }
        )
    }
}

@Composable
fun SettingsCategoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

@Composable
fun SettingsGeneral(viewModel: SettingsViewModel) {
    val downloadLocation by viewModel.downloadLocation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsItem(
            title = "Check for Updates",
            subtitle = "Current version: ${com.example.kemono.BuildConfig.VERSION_NAME}",
            onClick = { viewModel.checkForUpdate() }
        )
        SettingsItem(
            title = "Download Location",
            subtitle = downloadLocation ?: "Default",
            onClick = { /* TODO: Open folder picker */ }
        )
        AboutSection()
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearance(viewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
    }
}

@Composable
fun SettingsData(viewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Storage Management",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Clearing the cache will remove all temporary images and data. Your downloads will not be affected.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = { viewModel.clearCache() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Image Cache")
        }
    }
}

@Composable
fun SettingsAdvanced(viewModel: SettingsViewModel) {
    val sessionCookie by viewModel.sessionCookie.collectAsState()
    val hasSession by viewModel.hasSession.collectAsState()
    val initStatus by viewModel.initStatus.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    var cookieInput by remember { mutableStateOf(sessionCookie) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DDoS-Guard Section
        Text(text = "DDoS-Guard Protection", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        Text(
            text = "kemono.cr uses DDoS-Guard protection. Initialize cookies before using the app if you are experiencing connection issues.",
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
                        initStatus.startsWith("⚠") -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = initStatus,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        HorizontalDivider()

        // Session Cookie Section
        Text(text = "Session Cookie", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        Text(
            text = "Manually enter your session cookie if automatic initialization fails.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = cookieInput,
            onValueChange = { cookieInput = it },
            label = { Text("Cookie Value") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.updateSessionCookie(cookieInput) },
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
    }
}

package com.example.kemono.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
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
    Account,
    Appearance,
    Data,
    Advanced,
    Licenses,
    Privacy
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLoginClick: () -> Unit
) {
    var currentRoute by remember { mutableStateOf(SettingsRoute.Main) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    
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

    val account by viewModel.account.collectAsState()

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
                            SettingsRoute.Account -> "Account"
                            SettingsRoute.Appearance -> "Appearance"
                            SettingsRoute.Data -> "Data & Storage"
                            SettingsRoute.Advanced -> "Advanced"
                            SettingsRoute.Licenses -> "Licenses"
                            SettingsRoute.Privacy -> "Privacy Policy"
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
                    onNavigate = { currentRoute = it },
                    viewModel = viewModel
                )
                SettingsRoute.Account -> SettingsAccount(
                    viewModel = viewModel,
                    onLoginClick = onLoginClick,
                    onLogoutClick = { viewModel.logout() }
                )
                SettingsRoute.Appearance -> SettingsAppearance(viewModel)
                SettingsRoute.Data -> SettingsData(viewModel)
                SettingsRoute.Advanced -> SettingsAdvanced(viewModel)
                SettingsRoute.Licenses -> LicensesScreen(onBackClick = { currentRoute = SettingsRoute.Main })
                SettingsRoute.Privacy -> PrivacyPolicyScreen(onBackClick = { currentRoute = SettingsRoute.Main })
            }
        }
    }
}

@Composable
fun SettingsMain(
    onNavigate: (SettingsRoute) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val updateAvailable by viewModel.updateAvailable.collectAsState()

        SettingsCategoryItem(
             icon = Icons.Default.Person,
             title = "Account",
             subtitle = "Profile, login, and sync settings",
             onClick = { onNavigate(SettingsRoute.Account) }
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
            subtitle = "Downloads and cache management",
            onClick = { onNavigate(SettingsRoute.Data) }
        )
        SettingsCategoryItem(
            icon = Icons.Default.Build,
            title = "Advanced",
            subtitle = "DDoS-Guard, cookies, and developer tools",
            onClick = { onNavigate(SettingsRoute.Advanced) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            title = "Check for Updates",
            subtitle = "Current version: ${com.example.kemono.BuildConfig.VERSION_NAME}",
            onClick = { viewModel.checkForUpdate() }
        )
        
        AboutSection(
            onLicensesClick = { onNavigate(SettingsRoute.Licenses) },
            onPrivacyClick = { onNavigate(SettingsRoute.Privacy) }
        )
    }
}

@Composable
fun SettingsAccount(
    viewModel: SettingsViewModel,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val account by viewModel.account.collectAsState()
    val sessionCookie by viewModel.sessionCookie.collectAsState()
    val hasSession by viewModel.hasSession.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (account != null) {
            SettingsCategoryItem(
                 icon = Icons.Default.Person,
                 title = "Profile",
                 subtitle = "Logged in as ${account!!.username} (ID: ${account!!.id})",
                 onClick = { /* Could navigate to full profile if we had route traversal here */ }
            )
            SettingsCategoryItem(
                 icon = Icons.AutoMirrored.Filled.ExitToApp,
                 title = "Log Out",
                 subtitle = "Clear session and cached data",
                 onClick = onLogoutClick
            )
        } else {
             SettingsCategoryItem(
                 icon = Icons.Default.Person,
                 title = "Log In",
                 subtitle = "Connect your kemono.cr account",
                 onClick = onLoginClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Favorites Synchronization", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Text(
                text = "Sync favorite artists between the app and your kemono.cr account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.importFavorites() },
                modifier = Modifier.fillMaxWidth(),
                enabled = importStatus != "Importing..."
            ) {
                if (importStatus == "Importing...") {
                     CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Import Favorites from Website")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.pushFavorites() },
                modifier = Modifier.fillMaxWidth(),
                enabled = importStatus != "Importing..." && importStatus != "Pushing favorites..."
            ) {
                 if (importStatus == "Pushing favorites...") {
                     CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Push Favorites to Account")
            }
            
            if (importStatus != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                     colors = CardDefaults.cardColors(
                        containerColor = if (importStatus!!.startsWith("Error")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Text(
                            text = importStatus!!,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { viewModel.clearImportStatus() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear")
                        }
                    }
                }
            }
        }
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
    val themeMode by viewModel.appThemeMode.collectAsState()
    val gridDensity by viewModel.gridDensity.collectAsState()
    val artistLayoutMode by viewModel.artistLayoutMode.collectAsState()
    val postLayoutMode by viewModel.postLayoutMode.collectAsState()
    val downloadLayoutMode by viewModel.downloadLayoutMode.collectAsState()
    val favoriteLayoutMode by viewModel.favoriteLayoutMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Theme Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("System", "Light", "Dark").forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(mode) },
                        leadingIcon = if (themeMode == mode) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }



        // Grid Density Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Grid Density",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Controls the size of items in grid views.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Small", "Medium", "Large").forEach { density ->
                    FilterChip(
                        selected = gridDensity == density,
                        onClick = { viewModel.setGridDensity(density) },
                        label = { Text(density) },
                        leadingIcon = if (gridDensity == density) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }



        // Layout Options Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Layout Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            LayoutOptionSelector(
                label = "Creators Layout",
                currentValue = artistLayoutMode,
                onValueChange = { viewModel.setArtistLayoutMode(it) }
            )

            LayoutOptionSelector(
                label = "Posts Layout",
                currentValue = postLayoutMode,
                onValueChange = { viewModel.setPostLayoutMode(it) }
            )

            LayoutOptionSelector(
                label = "Downloads Layout",
                currentValue = downloadLayoutMode,
                onValueChange = { viewModel.setDownloadLayoutMode(it) }
            )

            LayoutOptionSelector(
                label = "Profile",
                currentValue = favoriteLayoutMode,
                onValueChange = { viewModel.setFavoriteLayoutMode(it) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LayoutOptionSelector(
    label: String,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("List", "Grid").forEach { option ->
                 FilterChip(
                    selected = currentValue == option,
                    onClick = { onValueChange(option) },
                    label = { Text(option) },
                    leadingIcon = if (currentValue == option) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun SettingsData(viewModel: SettingsViewModel) {
    val cacheStats by viewModel.cacheStats.collectAsState()
    val autoplayGifs by viewModel.autoplayGifs.collectAsState()
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Data Usage",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        val downloadLocation by viewModel.downloadLocation.collectAsState()
        SettingsItem(
            title = "Download Location",
            subtitle = downloadLocation ?: "Default",
            onClick = { /* TODO: Open folder picker */ }
        )

        ListItem(
            headlineContent = { Text("Autoplay GIFs") },
            supportingContent = { Text("Automatically play GIFs in grids and lists.") },
            trailingContent = {
                Switch(
                    checked = autoplayGifs,
                    onCheckedChange = { viewModel.setAutoplayGifs(it) }
                )
            }
        )



        Text(
            text = "Storage Management",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Media Cache Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Media Cache", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Images, thumbnails, and preview data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = android.text.format.Formatter.formatShortFileSize(context, cacheStats.mediaCacheSize),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.clearMediaCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Media")
                }
            }
        }

        // Network Cache Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Network Cache", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "API responses and temporary data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = android.text.format.Formatter.formatShortFileSize(context, cacheStats.networkCacheSize),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.clearNetworkCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Network")
                }
            }
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
        




        // Crash Reporting Section
        val crashReportingEnabled by viewModel.crashReportingEnabled.collectAsState()
        
        Text(text = "Crash Reporting", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        ListItem(
            headlineContent = { Text("Anonymous Crash Reporting") },
            supportingContent = { Text("Save crash logs locally and prompt to send via email on next launch.") },
            trailingContent = {
                Switch(
                    checked = crashReportingEnabled,
                    onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
                )
            }
        )
    }
}

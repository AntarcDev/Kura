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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import com.example.kemono.ui.components.UpdateDialog

enum class SettingsRoute {
    Main,
    Account,
    Appearance,
    Data,
    Advanced,
    Licenses,
    Privacy,
    Blacklist,
    Security
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
                            SettingsRoute.Blacklist -> "Blocked Content"
                            SettingsRoute.Security -> "Privacy & Security"
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
                SettingsRoute.Security -> SettingsSecurity(viewModel)
                SettingsRoute.Licenses -> LicensesScreen(onBackClick = { currentRoute = SettingsRoute.Main })
                SettingsRoute.Privacy -> PrivacyPolicyScreen(onBackClick = { currentRoute = SettingsRoute.Main })
                SettingsRoute.Blacklist -> BlacklistScreen(onBackClick = { currentRoute = SettingsRoute.Main })
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
             icon = Icons.Default.Block,
             title = "Blocked Content",
             subtitle = "Manage blocked creators, tags, and keywords",
             onClick = { onNavigate(SettingsRoute.Blacklist) }
        )

        SettingsCategoryItem(
            icon = Icons.Default.Lock,
            title = "Privacy & Security",
            subtitle = "App lock and incognito keyboard",
            onClick = { onNavigate(SettingsRoute.Security) }
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

@Composable
fun SettingsSecurity(viewModel: SettingsViewModel) {
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    val isIncognitoKeyboardEnabled by viewModel.isIncognitoKeyboardEnabled.collectAsState()
    
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinDialog = false
                pinInput = ""
            },
            title = { Text(if (isAppLockEnabled) "Disable PIN" else "Set PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isAppLockEnabled) {
                            if (pinInput == appLockPin) {
                                viewModel.setAppLockPin(null)
                                viewModel.setIsAppLockEnabled(false)
                                showPinDialog = false
                                pinInput = ""
                            } else {
                                // Maybe show error toast here, keeping simple for now
                            }
                        } else {
                            if (pinInput.length == 4) {
                                viewModel.setAppLockPin(pinInput)
                                viewModel.setIsAppLockEnabled(true)
                                showPinDialog = false
                                pinInput = ""
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPinDialog = false
                        pinInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Security",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ListItem(
            headlineContent = { Text("App Lock") },
            supportingContent = { Text(if (isAppLockEnabled) "App is locked with a PIN" else "Require a PIN to open the app") },
            trailingContent = {
                Switch(
                    checked = isAppLockEnabled,
                    onCheckedChange = { showPinDialog = true }
                )
            },
            modifier = Modifier.clickable { showPinDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Privacy",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ListItem(
            headlineContent = { Text("Incognito Keyboard") },
            supportingContent = { Text("Request the system keyboard not to learn or save history on text fields (e.g., search).") },
            trailingContent = {
                Switch(
                    checked = isIncognitoKeyboardEnabled,
                    onCheckedChange = { viewModel.setIsIncognitoKeyboardEnabled(it) }
                )
            }
        )
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
    
    LaunchedEffect(Unit) {
        viewModel.refreshCacheStats()
    }


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
        val imageQuality by viewModel.imageQuality.collectAsState()
        val cacheSizeRatio by viewModel.cacheSizeLimitRatio.collectAsState()
        val autoDownload by viewModel.autoDownloadFavorites.collectAsState()
        
        var showCacheDropdown by remember { mutableStateOf(false) }
        val cacheOptions = listOf(
            0.1f to "100 MB",
            0.5f to "500 MB",
            1.0f to "1 GB",
            2.0f to "2 GB",
            10.0f to "Unlimited (10 GB)"
        )
        val selectedCacheLabel = cacheOptions.find { it.first == cacheSizeRatio }?.second ?: "${(cacheSizeRatio * 1024).toInt()} MB"

        Box {
            ListItem(
                headlineContent = { Text("Image Cache Limit") },
                supportingContent = { Text("Current size: $cacheStats\nSelected maximum: $selectedCacheLabel") },
                modifier = Modifier.clickable { showCacheDropdown = true }
            )
            DropdownMenu(
                expanded = showCacheDropdown,
                onDismissRequest = { showCacheDropdown = false }
            ) {
                cacheOptions.forEach { (ratio, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            viewModel.setCacheSizeLimitRatio(ratio)
                            showCacheDropdown = false
                        }
                    )
                }
            }
        }
        
        ListItem(
            headlineContent = { Text("Auto-Download Favorites") },
            supportingContent = { Text("Automatically download posts when you add them to favorites.") },
            trailingContent = {
                Switch(
                    checked = autoDownload,
                    onCheckedChange = { viewModel.setAutoDownloadFavorites(it) }
                )
            }
        )

        var showQualityDropdown by remember { mutableStateOf(false) }
        val qualityOptions = listOf("Low", "Sample", "Original")

        Box {
            ListItem(
                headlineContent = { Text("Image Quality") },
                supportingContent = { 
                    Column {
                        Text("Controls how large images are decoded in memory. Lower quality drastically improves performance and prevents crashes.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ Original quality may cause severe lag and memory crashes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingContent = { Text(imageQuality) },
                modifier = Modifier.clickable { showQualityDropdown = true }
            )
            DropdownMenu(
                expanded = showQualityDropdown,
                onDismissRequest = { showQualityDropdown = false }
            ) {
                qualityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.setImageQuality(option)
                            showQualityDropdown = false
                        }
                    )
                }
            }
        }
        
        val folderPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                viewModel.setDownloadLocation(it.toString())
            }
        }

        SettingsItem(
            title = "Download Location",
            subtitle = if (downloadLocation != null) Uri.decode(downloadLocation) else "Default (Downloads/Kemono)",
            onClick = { folderPicker.launch(null) }
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
    
    val startVideosMuted by viewModel.startVideosMuted.collectAsState()
    val useExternalVideoPlayer by viewModel.useExternalVideoPlayer.collectAsState()

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
            maxLines = 3,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = if (com.example.kemono.LocalIncognitoKeyboard.current) androidx.compose.ui.text.input.KeyboardType.Password else androidx.compose.ui.text.input.KeyboardType.Text
            )
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
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Video Player Settings
        Text(text = "Media Playback", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        ListItem(
            headlineContent = { Text("Mute Videos by Default") },
            supportingContent = { Text("Start playing videos with no sound. You can unmute them from the player.") },
            trailingContent = {
                Switch(
                    checked = startVideosMuted,
                    onCheckedChange = { viewModel.setStartVideosMuted(it) }
                )
            }
        )

        ListItem(
            headlineContent = { Text("Use External Video Player") },
            supportingContent = { Text("Launch videos in an external app like VLC or MX Player instead of the built-in player.") },
            trailingContent = {
                Switch(
                    checked = useExternalVideoPlayer,
                    onCheckedChange = { viewModel.setUseExternalVideoPlayer(it) }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Backup & Restore Section
        val backupStatus by viewModel.backupRestoreStatus.collectAsState()
        
        Text(text = "Backup & Restore", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        Text(
            text = "Export or import your settings, favorites, and history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val createDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
        ) { uri ->
            uri?.let { viewModel.backupData(it) }
        }

        val openDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let { viewModel.restoreData(it) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { createDocumentLauncher.launch("KemonoBackup.zip") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Export Backup")
            }
            OutlinedButton(
                onClick = { openDocumentLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Import Backup")
            }
        }
        
        if (backupStatus != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (backupStatus!!.contains("failed")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = backupStatus!!,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = { viewModel.clearBackupRestoreStatus() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
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

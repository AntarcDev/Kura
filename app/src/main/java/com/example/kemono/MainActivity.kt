package com.example.kemono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kemono.data.repository.SettingsRepository
import com.example.kemono.ui.main.MainScreen
import com.example.kemono.ui.posts.CreatorPostListScreen
import com.example.kemono.ui.posts.PostScreen
import com.example.kemono.ui.theme.KemonoTheme
import com.example.kemono.ui.viewer.ImageViewerScreen
import com.example.kemono.ui.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val LocalIncognitoKeyboard = androidx.compose.runtime.compositionLocalOf { false }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "System")
            val darkTheme =
                    when (themeMode) {
                        "Light" -> false
                        "Dark" -> true
                        else -> androidx.compose.foundation.isSystemInDarkTheme()
                    }
            
            val crashReportingEnabled by settingsRepository.crashReportingEnabled.collectAsState(initial = false)
            var crashReportContent by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
            val context = androidx.compose.ui.platform.LocalContext.current

            androidx.compose.runtime.LaunchedEffect(crashReportingEnabled) {
                if (crashReportingEnabled) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val dir = java.io.File(filesDir, "crash_logs")
                            val file = java.io.File(dir, "crash.txt")
                            if (file.exists()) {
                                crashReportContent = file.readText()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            // Notification Permission Request
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission granted/denied
                }
                
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            if (crashReportContent != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { 
                        // Delete file on dismiss
                        deleteCrashLog()
                        crashReportContent = null 
                    },
                    title = { androidx.compose.material3.Text("Crash Detected") },
                            text = { androidx.compose.material3.Text("Kura crashed last time. Would you like to send an anonymous crash report to the developer via email?") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                sendCrashReport(context, crashReportContent!!)
                                // Don't delete immediately, let the coroutine handle it
                                crashReportContent = null
                            }
                        ) {
                            androidx.compose.material3.Text("Send Report")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                deleteCrashLog()
                                crashReportContent = null
                            }
                        ) {
                            androidx.compose.material3.Text("Dismiss")
                        }
                    }
                )
            }

            val isIncognitoKeyboardEnabled by settingsRepository.isIncognitoKeyboardEnabled.collectAsState(initial = false)

            androidx.compose.runtime.CompositionLocalProvider(LocalIncognitoKeyboard provides isIncognitoKeyboardEnabled) {
                KemonoTheme(darkTheme = darkTheme) {
                    Surface(
                            modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                    onCreatorClick = { creator ->
                                        navController.navigate(
                                                "creator/${creator.service}/${creator.id}"
                                        )
                                    },
                                    onPostClick = { post ->
                                        navController.navigate(
                                                "post/${post.service}/${post.user}/${post.id}"
                                        )
                                    },
                                    onNavigateToGalleryItem = { index ->
                                        navController.navigate("viewer/gallery/all/$index")
                                    },
                                    onLoginClick = {
                                        navController.navigate("login")
                                    }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onBackClick = { navController.popBackStack() },
                                onLoginSuccess = { 
                                    // Pop back to main screen (Profile) on success
                                    navController.popBackStack() 
                                }
                            )
                        }
                        composable(
                                route = "creator/{service}/{creatorId}",
                                arguments =
                                        listOf(
                                                navArgument("service") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("creatorId") {
                                                    type = NavType.StringType
                                                }
                                        )
                        ) {
                            CreatorPostListScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onPostClick = { post ->
                                        navController.navigate(
                                                "post/${post.service}/${post.user}/${post.id}"
                                        )
                                    },
                                    onCreatorClick = { creator ->
                                        navController.navigate(
                                                "creator/${creator.service}/${creator.id}"
                                        )
                                    }
                            )
                        }
                        composable(
                                route = "post/{service}/{creatorId}/{postId}",
                                arguments =
                                        listOf(
                                                navArgument("service") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("creatorId") {
                                                    type = NavType.StringType
                                                },
                                                navArgument("postId") { type = NavType.StringType }
                                        )
                        ) {
                            PostScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onImageClick = { index ->
                                        val service = it.arguments?.getString("service") ?: ""
                                        val creatorId = it.arguments?.getString("creatorId") ?: ""
                                        val postId = it.arguments?.getString("postId") ?: ""
                                        val id = "$service|$creatorId|$postId"
                                        navController.navigate("viewer/post/$id/$index")
                                    }
                            )
                        }
                        composable(
                                route = "viewer/{type}/{id}/{initialIndex}",
                                arguments =
                                        listOf(
                                                navArgument("type") { type = NavType.StringType },
                                                navArgument("id") { type = NavType.StringType },
                                                navArgument("initialIndex") {
                                                    type = NavType.StringType
                                                }
                                        )
                        ) {
                            ImageViewerScreen(onBackClick = { navController.popBackStack() })
                        }
                    }

                    // --- App Lock Overlay ---
                    val isAppLockEnabled by settingsRepository.isAppLockEnabled.collectAsState(initial = false)
                    val appLockPin by settingsRepository.appLockPin.collectAsState(initial = null)
                    var isUnlocked by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

                    if (isAppLockEnabled && !isUnlocked && appLockPin != null) {
                        var pinInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                        var isError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                        androidx.compose.ui.window.Dialog(
                            onDismissRequest = { /* Prevent dismiss */ },
                            properties = androidx.compose.ui.window.DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                                usePlatformDefaultWidth = false
                            )
                        ) {
                            androidx.compose.material3.Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier.fillMaxSize().padding(32.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                        contentDescription = "App Locked",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                                    androidx.compose.material3.Text(
                                        text = "App Locked",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                    androidx.compose.material3.Text(
                                        text = "Please enter your PIN",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
                                    
                                    androidx.compose.material3.OutlinedTextField(
                                        value = pinInput,
                                        onValueChange = { 
                                            isError = false
                                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                                pinInput = it 
                                                if (it.length == 4) {
                                                    if (it == appLockPin) {
                                                        isUnlocked = true
                                                    } else {
                                                        isError = true
                                                        pinInput = ""
                                                    }
                                                }
                                            }
                                        },
                                        label = { androidx.compose.material3.Text("PIN") },
                                        singleLine = true,
                                        isError = isError,
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                        )
                                    )
                                    if (isError) {
                                        androidx.compose.material3.Text(
                                            text = "Incorrect PIN",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // End KemonoTheme
            } // End CompositionLocalProvider
        }
    }

    private fun deleteCrashLog() {
        try {
            val dir = java.io.File(filesDir, "crash_logs")
            // Delete both txt and zip if they exist
            java.io.File(dir, "crash.txt").delete()
            java.io.File(dir, "crash_report.zip").delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendCrashReport(context: android.content.Context, report: String) {
        kotlinx.coroutines.MainScope().launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dir = java.io.File(filesDir, "crash_logs")
                val textFile = java.io.File(dir, "crash.txt")
                val zipFile = java.io.File(dir, "crash_report.zip")

                // Create Zip
                if (textFile.exists()) {
                    java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zipOut ->
                        java.io.FileInputStream(textFile).use { fis ->
                            val zipEntry = java.util.zip.ZipEntry(textFile.name)
                            zipOut.putNextEntry(zipEntry)
                            fis.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                    // Delete text file after zipping so dialog doesn't show next time
                    textFile.delete()
                }

                if (zipFile.exists()) {
                     val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        zipFile
                    )
                    
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("kxantarc@tuta.io"))
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Kura Crash Report")
                        putExtra(android.content.Intent.EXTRA_TEXT, "Crash report attached.")
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        try {
                             context.startActivity(android.content.Intent.createChooser(intent, "Send crash report via..."))
                        } catch (e: android.content.ActivityNotFoundException) {
                            // Fallback if no specific app found
                             val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/zip"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share crash report via..."))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

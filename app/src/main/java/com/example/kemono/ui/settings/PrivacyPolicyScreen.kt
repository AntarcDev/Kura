package com.example.kemono.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrivacyItem(
            title = "No Tracking",
            description = "Kura does not track your usage, collect personal data, or send analytics to third parties. We believe your browsing habits are your own business."
        )

        PrivacyItem(
            title = "Local Data Storage",
            description = "All application data, including your search history, favorites, and settings, is stored locally on your device. When you uninstall the app, this data is deleted."
        )

        PrivacyItem(
            title = "External Services",
            description = "The app communicates directly with kemono.cr APIs to fetch content. When you use the app, your IP address is visible to their servers, just like browsing the website."
        )

        PrivacyItem(
            title = "Crash Reporting",
            description = "If the app crashes, a log is generated on your device. This log is NEVER uploaded automatically. You will be prompted on the next launch if you wish to share the anonymized log with the developer to help fix the bug."
        )
        
        PrivacyItem(
            title = "Login Credentials",
            description = "When you log in, the app stores your session cookie securely on your device. This cookie is only used to authenticate requests with kemono.cr."
        )
    }
}

@Composable
fun PrivacyItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

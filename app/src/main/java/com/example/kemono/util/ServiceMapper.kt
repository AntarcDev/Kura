package com.example.kemono.util

import androidx.compose.ui.graphics.Color

object ServiceMapper {
    fun getServiceColor(service: String?): Color {
        return when (service?.lowercase()) {
            "patreon" -> Color(0xFFf96854)
            "fanbox" -> Color(0xFF0096fa)
            "discord" -> Color(0xFF5865f2)
            "fantia" -> Color(0xFFea4c89)
            "boosty" -> Color(0xFFf15f2c)
            "gumroad" -> Color(0xFFff90e8)
            "subscribestar" -> Color(0xFF429488)
            "dlsite" -> Color(0xFF052A83)
            else -> Color.Gray
        }
    }

    fun getDisplayName(service: String?): String {
        return when (service?.lowercase()) {
            "patreon" -> "Patreon"
            "fanbox" -> "Pixiv Fanbox"
            "discord" -> "Discord"
            "fantia" -> "Fantia"
            "boosty" -> "Boosty"
            "gumroad" -> "Gumroad"
            "subscribestar" -> "SubscribeStar"
            "dlsite" -> "DLsite"
            else -> service?.capitalize() ?: "Unknown"
        }
    }
    
    // Kotlin < 1.5 compat for capitalize if needed, or just use replaceFirstChar
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

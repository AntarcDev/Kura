package com.example.kemono.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import com.example.kemono.data.model.Post
import com.example.kemono.data.model.KemonoFile

sealed class PreviewContent {
    data class Image(val url: String) : PreviewContent()
    data class Icon(
        val vector: ImageVector,
        val label: String,
        val color: Color,
        val containerColor: Color
    ) : PreviewContent()
    object None : PreviewContent()
}

object PostPreviewUtils {
    
    fun getPreviewContent(post: Post): PreviewContent {
        // 1. Check Main File for Image
        post.file?.let { file ->
            if (isImage(file.path)) {
                 return PreviewContent.Image("https://kemono.cr${file.path}")
            }
        }

        // 2. Check Attachments (Scan for first image) - Prioritize showing ANY image over an icon
        post.attachments.forEach { file ->
             if (isImage(file.path)) {
                 return PreviewContent.Image("https://kemono.cr${file.path}")
            }
        }
        
        // 3. If no images found, check Main File for Special Types
        post.file?.let { file ->
            getIconForPath(file.path)?.let { return it }
        }

        // 4. Check Attachments for Special Types (if main file didn't yield one)
        post.attachments.forEach { file ->
             getIconForPath(file.path)?.let { return it }
        }
        
        // 5. Fallback: Generic document from Main File
        post.file?.let { file ->
             if (file.path != null) {
                 return PreviewContent.Icon(
                     vector = Icons.Default.Description,
                     label = file.path.substringAfterLast('.').uppercase().take(4),
                     color = Color.Gray,
                     containerColor = Color.LightGray.copy(alpha = 0.5f)
                 )
             }
        }
        
        // 6. Fallback: Generic document from First Attachment
        post.attachments.firstOrNull()?.let { file ->
             if (file.path != null) {
                 return PreviewContent.Icon(
                     vector = Icons.Default.Description,
                     label = file.path.substringAfterLast('.').uppercase().take(4),
                     color = Color.Gray,
                     containerColor = Color.LightGray.copy(alpha = 0.5f)
                 )
             }
        }

        // 7. No files/attachments -> Text Post
        if (post.file == null && post.attachments.isEmpty()) {
            return PreviewContent.Icon(
                vector = Icons.Default.Description,
                label = "TXT",
                color = Color(0xFF607D8B), // Blue Grey
                containerColor = Color(0xFFCFD8DC)
            )
        }

        return PreviewContent.None
    }

    private fun isImage(path: String?): Boolean {
        if (path == null) return false
        val ext = path.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    }

    fun getIconForPath(path: String?): PreviewContent.Icon? {
        if (path == null) return null
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "psd" -> PreviewContent.Icon(
                vector = Icons.Default.Brush,
                label = "PSD",
                color = Color(0xFF1976D2), // Blue
                containerColor = Color(0xFFBBDEFB)
            )
            "clip", "csp" -> PreviewContent.Icon(
                vector = Icons.Default.Brush,
                label = "CLIP",
                color = Color(0xFFF57C00), // Orange
                containerColor = Color(0xFFFFE0B2)
            )
            "zip", "rar", "7z", "tar", "gz", "xz" -> PreviewContent.Icon(
                vector = Icons.Default.FolderZip,
                label = "ARCHIVE",
                color = Color(0xFFFFA000), // Amber
                containerColor = Color(0xFFFFECB3)
            )
            "mp4", "m4v", "mov", "avi", "webm", "mkv" -> PreviewContent.Icon(
                vector = Icons.Default.PlayCircle,
                label = "VIDEO",
                color = Color(0xFFD32F2F), // Red
                containerColor = Color(0xFFFFCDD2)
            )
            "mp3", "wav", "flac", "ogg", "m4a", "wma", "aac" -> PreviewContent.Icon(
                vector = Icons.Default.AudioFile,
                label = "AUDIO",
                color = Color(0xFF7B1FA2), // Purple
                containerColor = Color(0xFFE1BEE7)
            )
            else -> null
        }
    }
}

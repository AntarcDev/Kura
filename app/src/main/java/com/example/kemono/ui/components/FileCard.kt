package com.example.kemono.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kemono.util.PostPreviewUtils
import com.example.kemono.util.PreviewContent

@Composable
fun FileCard(
    fileName: String,
    path: String?, // Used to determine icon/color
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit
) {
    // Determine icon and color based on path
    val iconInfo = PostPreviewUtils.getIconForPath(path) ?: PreviewContent.Icon(
        vector = Icons.Default.Description,
        label = path?.substringAfterLast('.', "")?.uppercase()?.take(4) ?: "FILE",
        color = Color.Gray,
        containerColor = Color.LightGray.copy(alpha = 0.5f)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconInfo.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconInfo.vector,
                    contentDescription = iconInfo.label,
                    tint = iconInfo.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = iconInfo.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconInfo.color
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

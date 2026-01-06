package com.example.kemono.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    // 2025-01-04T18:32:47 or similar ISO formats
    // The API might return non-standard ISO strings, need to be careful.
    // Assuming standard format for now based on typical output.
    
    fun formatPublishedDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Unknown Date"
        
        return try {
            // Check if it's a timestamp (digits only) or ISO string
            // User provided example says "published": "string", but "indexed": 1672534800 (Long)
            // If it's a Long timestamp string
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                // If it's a timestamp
                val date = Date(timestamp * 1000L) // Assuming seconds if standard unix
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                return outputFormat.format(date)
            }
            
            // Try ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString) ?: return dateString
            
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString // Return original if parse fails
        }
    }
    
    fun formatRelative(dateString: String?): String {
        // Implement relative time like "2 days ago" if needed
        return formatPublishedDate(dateString)
    }
}

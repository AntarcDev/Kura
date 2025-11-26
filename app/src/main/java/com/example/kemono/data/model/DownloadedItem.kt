package com.example.kemono.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_items")
data class DownloadedItem(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val postId: String,
        val creatorId: String,
        val creatorName: String,
        val fileName: String,
        val filePath: String,
        val mediaType: String, // "IMAGE" or "VIDEO"
        val downloadId: Long = -1L,
        val downloadedAt: Long = System.currentTimeMillis()
)

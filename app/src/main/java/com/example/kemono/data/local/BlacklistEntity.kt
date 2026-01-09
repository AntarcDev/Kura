package com.example.kemono.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist")
data class BlacklistEntity(
    @PrimaryKey val id: String, // Creator ID, Tag Name, or Keyword
    val type: BlacklistType,
    val name: String, // Display name
    val service: String? = null, // Only for creators
    val addedAt: Long = System.currentTimeMillis()
)

enum class BlacklistType {
    CREATOR,
    TAG,
    KEYWORD
}

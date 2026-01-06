package com.example.kemono.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_posts")
data class FavoritePost(
    @PrimaryKey val id: String,
    val service: String,
    val user: String, // creatorId
    val title: String,
    val content: String,
    val thumbnailPath: String?,
    val published: String,
    val added: Long = System.currentTimeMillis()
)

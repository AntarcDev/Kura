package com.example.kemono.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_creators")
data class FavoriteCreator(
    @PrimaryKey val id: String,
    val service: String,
    val name: String,
    val updated: String
)

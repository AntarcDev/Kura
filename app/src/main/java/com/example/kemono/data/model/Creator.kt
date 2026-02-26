package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName
import androidx.compose.runtime.Immutable

@Immutable
data class Creator(
    val id: String,
    val name: String,
    val service: String,
    val indexed: Long,
    val updated: Long,
    @SerializedName("favorited") val favorited: Int? = 0
)

package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName

data class Announcement(
    @SerializedName("service") val service: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("hash") val hash: String,
    @SerializedName("content") val content: String?,
    @SerializedName("added") val added: String?
)

data class CreatorLink(
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("position") val position: Int?
)

data class Fancard(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: String?, // Could be int, but string is safer
    @SerializedName("currency") val currency: String?,
    @SerializedName("cover_url") val coverUrl: String? // Assuming cover_url or similar
)

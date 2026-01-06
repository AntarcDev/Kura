package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    val id: String? = null,
    val user: String? = null,
    val service: String? = null,
    val title: String? = null,
    val content: String? = null,
    val substring: String? = null,
    val published: String? = null,
    val file: KemonoFile? = null,
    val attachments: List<KemonoFile> = emptyList(),
    @SerializedName("fav_count") val favCount: Int? = 0
)

data class KemonoFile(
    val name: String,
    val path: String,
    @SerializedName("thumbnail_path") val thumbnailPath: String? = null
)

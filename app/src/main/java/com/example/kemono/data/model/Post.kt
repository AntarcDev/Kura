package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName
import androidx.compose.runtime.Immutable

@Immutable
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
    @SerializedName("fav_count") val favCount: Int? = 0,
    val tags: List<String>? = null
)

@Immutable
data class KemonoFile(
    val name: String? = null,
    val path: String? = null,
    @SerializedName("thumbnail_path") val thumbnailPath: String? = null
)

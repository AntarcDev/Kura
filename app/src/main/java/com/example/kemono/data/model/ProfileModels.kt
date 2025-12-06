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
    @SerializedName("id") val id: String?,
    @SerializedName("public_id") val publicId: String?,
    @SerializedName("service") val service: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("indexed") val indexed: String?,
    @SerializedName("updated") val updated: String?,
    // Keep internal aliases if needed, but primary fields are above
    @SerializedName("title") val _title: String? = null,
    @SerializedName("url") val _url: String? = null
)

data class Fancard(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("file_id") val fileId: String?,
    @SerializedName("hash") val hash: String?,
    @SerializedName("mtime") val mtime: String?,
    @SerializedName("ctime") val ctime: String?,
    @SerializedName("mime") val mime: String?,
    @SerializedName("ext") val ext: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("size") val size: Long?,
    // Restoring potential fields just in case
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("content_url") val contentUrl: String?,
    @SerializedName("file") val file: KemonoFile?,
    @SerializedName("server") val server: String?
)



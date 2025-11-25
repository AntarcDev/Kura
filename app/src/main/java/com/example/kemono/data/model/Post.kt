package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    val id: String,
    val user: String,
    val service: String,
    val title: String,
    val content: String,
    val published: String,
    val file: KemonoFile?,
    val attachments: List<KemonoFile> = emptyList()
)

data class KemonoFile(
    val name: String,
    val path: String
)

package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName

data class Tag(
    val tag: String,
    @SerializedName("post_count")
    val postCount: Int
)

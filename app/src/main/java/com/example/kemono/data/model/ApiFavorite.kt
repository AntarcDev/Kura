package com.example.kemono.data.model

data class ApiFavorite(
    val id: String,
    val name: String,
    val service: String,
    val updated: String,
    val faved_seq: Int,
    val user: String? = null,
    val indexed: String? = null
)

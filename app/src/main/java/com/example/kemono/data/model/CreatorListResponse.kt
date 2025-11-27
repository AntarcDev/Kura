package com.example.kemono.data.model

data class CreatorListResponse(
    val creators: List<Creator> = emptyList(),
    val results: List<Creator> = emptyList(), // Fallback
    val count: Int = 0
)

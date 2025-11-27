package com.example.kemono.data.model

// It seems the API returns a wrapped list for searches/recent posts
data class PostListResponse(
    val posts: List<Post> = emptyList(),
    val results: List<Post> = emptyList() // Fallback if field name differs
)

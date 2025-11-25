package com.example.kemono.util

enum class MediaType {
    IMAGE,
    VIDEO,
    GIF,
    UNKNOWN
}

fun getMediaType(path: String?): MediaType {
    if (path == null) return MediaType.UNKNOWN

    val extension = path.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "webp", "bmp" -> MediaType.IMAGE
        "gif" -> MediaType.GIF
        "mp4", "webm", "mkv", "avi", "mov" -> MediaType.VIDEO
        else -> MediaType.UNKNOWN
    }
}

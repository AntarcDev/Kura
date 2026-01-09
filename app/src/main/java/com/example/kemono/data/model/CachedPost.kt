package com.example.kemono.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "cached_posts")
data class CachedPost(
        @PrimaryKey val id: String,
        val user: String,
        val service: String,
        val title: String?,
        val content: String?,
        val published: String?,
        val fileJson: String?, // Serialized JSON
        val attachmentsJson: String?, // Serialized JSON
        val tagsJson: String? = null, // Serialized JSON
        val cachedAt: Long = System.currentTimeMillis()
)

fun CachedPost.toPost(): Post {
    val gson = Gson()
    val file = if (fileJson != null) gson.fromJson(fileJson, KemonoFile::class.java) else null
    val attachmentsType = object : TypeToken<List<KemonoFile>>() {}.type
    val attachments =
            if (attachmentsJson != null)
                    gson.fromJson<List<KemonoFile>>(attachmentsJson, attachmentsType)
            else emptyList()
    
    val tagsType = object : TypeToken<List<String>>() {}.type
    val tags = if (tagsJson != null) gson.fromJson<List<String>>(tagsJson, tagsType) else null

    return Post(
            id = id,
            user = user,
            service = service,
            title = title,
            content = content,
            published = published,
            file = file,
            attachments = attachments,
            tags = tags
    )
}

fun Post.toCached(): CachedPost {
    val gson = Gson()
    return CachedPost(
            id = id ?: throw IllegalArgumentException("Post ID cannot be null for caching"),
            user = user ?: "",
            service = service ?: "",
            title = title,
            content = content,
            published = published,
            fileJson = if (file != null) gson.toJson(file) else null,
            attachmentsJson = gson.toJson(attachments),
            tagsJson = gson.toJson(tags)
    )
}

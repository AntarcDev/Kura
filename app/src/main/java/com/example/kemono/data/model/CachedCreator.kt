package com.example.kemono.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_creators")
data class CachedCreator(
        @PrimaryKey val id: String,
        val service: String,
        val name: String,
        val updated: Long,
        val indexed: Long,
        val cachedAt: Long = System.currentTimeMillis()
)

fun CachedCreator.toCreator(): Creator {
    return Creator(id = id, service = service, name = name, updated = updated, indexed = indexed)
}

fun Creator.toCached(): CachedCreator {
    return CachedCreator(
            id = id,
            service = service,
            name = name,
            updated = updated,
            indexed = indexed
    )
}

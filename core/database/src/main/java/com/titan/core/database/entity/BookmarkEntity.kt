package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val path: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)
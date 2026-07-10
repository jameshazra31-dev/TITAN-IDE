package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val path: String,
    val projectId: String = "",
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val language: String = "",
)
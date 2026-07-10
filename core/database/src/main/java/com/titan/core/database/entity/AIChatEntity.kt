package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chats")
data class AIChatEntity(
    @PrimaryKey val id: String,
    val title: String = "New Chat",
    val providerId: String = "",
    val model: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val workspaceContext: String = "",
)
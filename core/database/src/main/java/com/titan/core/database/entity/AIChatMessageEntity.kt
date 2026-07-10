package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = AIChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class AIChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String = "USER",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int = 0,
    val model: String = "",
    val providerId: String = "",
    val isError: Boolean = false,
    val errorMessage: String = "",
)
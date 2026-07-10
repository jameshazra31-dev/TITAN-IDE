package com.titan.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val chatId: String = "",
    val role: MessageRole = MessageRole.USER,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int = 0,
    val model: String = "",
    val providerId: String = "",
    val isError: Boolean = false,
    val errorMessage: String = "",
)

@Serializable
enum class MessageRole {
    SYSTEM, USER, ASSISTANT, FUNCTION
}

data class AIChat(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val messages: List<AIChatMessage> = emptyList(),
    val providerId: String = "",
    val model: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val workspaceContext: String = "",
)
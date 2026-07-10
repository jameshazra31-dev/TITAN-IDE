package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_providers")
data class AIProviderEntity(
    @PrimaryKey val id: String,
    val name: String = "",
    val providerType: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val organizationId: String = "",
    val customHeaders: String = "{}",
    val temperature: Double = 0.7,
    val topP: Double = 1.0,
    val maxTokens: Int = 4096,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0,
    val isStreaming: Boolean = true,
    val timeout: Long = 60_000L,
    val retryCount: Int = 3,
    val customSystemPrompt: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
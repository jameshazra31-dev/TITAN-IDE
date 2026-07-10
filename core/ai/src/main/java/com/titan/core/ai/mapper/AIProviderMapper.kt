package com.titan.core.ai.mapper

import com.titan.core.database.entity.AIProviderEntity
import com.titan.domain.model.AIProvider
import com.titan.domain.model.AIProviderType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AIProviderMapper {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun mapToDomain(entity: AIProviderEntity): AIProvider = AIProvider(
        id = entity.id,
        name = entity.name,
        providerType = AIProviderType.entries.firstOrNull { it.name == entity.providerType } ?: AIProviderType.OPENAI_COMPATIBLE,
        baseUrl = entity.baseUrl,
        apiKey = entity.apiKey,
        model = entity.model,
        organizationId = entity.organizationId,
        customHeaders = try { json.decodeFromString(entity.customHeaders) } catch (e: Exception) { emptyMap() },
        temperature = entity.temperature,
        topP = entity.topP,
        maxTokens = entity.maxTokens,
        frequencyPenalty = entity.frequencyPenalty,
        presencePenalty = entity.presencePenalty,
        isStreaming = entity.isStreaming,
        timeout = entity.timeout,
        retryCount = entity.retryCount,
        customSystemPrompt = entity.customSystemPrompt,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun mapToEntity(domain: AIProvider): AIProviderEntity = AIProviderEntity(
        id = domain.id,
        name = domain.name,
        providerType = domain.providerType.name,
        baseUrl = domain.baseUrl,
        apiKey = domain.apiKey,
        model = domain.model,
        organizationId = domain.organizationId,
        customHeaders = json.encodeToString(domain.customHeaders),
        temperature = domain.temperature,
        topP = domain.topP,
        maxTokens = domain.maxTokens,
        frequencyPenalty = domain.frequencyPenalty,
        presencePenalty = domain.presencePenalty,
        isStreaming = domain.isStreaming,
        timeout = domain.timeout,
        retryCount = domain.retryCount,
        customSystemPrompt = domain.customSystemPrompt,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )
}
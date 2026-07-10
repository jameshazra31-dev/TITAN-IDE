package com.titan.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AIProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val providerType: AIProviderType = AIProviderType.OPENAI_COMPATIBLE,
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val organizationId: String = "",
    val customHeaders: Map<String, String> = emptyMap(),
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
) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
    val maskedApiKey: String get() = if (apiKey.length > 8) "****${apiKey.takeLast(4)}" else "****"
}

enum class AIProviderType(val displayName: String, val defaultBaseUrl: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta"),
    CLAUDE("Claude", "https://api.anthropic.com/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    GROQ("Groq", "https://api.groq.com/openai/v1"),
    MISTRAL("Mistral", "https://api.mistral.ai/v1"),
    OLLAMA("Ollama", "http://localhost:11434/v1"),
    LM_STUDIO("LM Studio", "http://localhost:1234/v1"),
    OPENAI_COMPATIBLE("OpenAI Compatible", ""),
    CUSTOM("Custom", ""),
}
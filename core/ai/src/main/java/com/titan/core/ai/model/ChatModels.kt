package com.titan.core.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val top_p: Double = 1.0,
    val max_tokens: Int = 4096,
    val frequency_penalty: Double = 0.0,
    val presence_penalty: Double = 0.0,
    val stream: Boolean = false,
    @SerialName("stop") val stopSequences: List<String>? = null,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val error: ErrorResponse? = null,
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    val delta: DeltaMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class DeltaMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0,
)

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null,
)
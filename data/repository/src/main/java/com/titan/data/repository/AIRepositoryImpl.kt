package com.titan.data.repository

import com.titan.core.ai.client.AIHttpClient
import com.titan.core.ai.mapper.AIProviderMapper
import com.titan.core.ai.model.ChatCompletionRequest
import com.titan.core.ai.model.ChatMessage
import com.titan.core.common.base.Result
import com.titan.core.database.dao.AIChatDao
import com.titan.core.database.dao.AIProviderDao
import com.titan.core.database.entity.AIChatEntity
import com.titan.core.database.entity.AIChatMessageEntity
import com.titan.core.security.CryptoManager
import com.titan.domain.model.AIChat
import com.titan.domain.model.AIChatMessage
import com.titan.domain.model.AIProvider
import com.titan.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val aiProviderDao: AIProviderDao,
    private val aiChatDao: AIChatDao,
    private val cryptoManager: CryptoManager,
) : AIRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun getAllProviders(): Flow<List<AIProvider>> =
        aiProviderDao.getAllProviders().map { list -> list.map { AIProviderMapper.mapToDomain(it) } }

    override fun getActiveProvider(): Flow<AIProvider?> =
        aiProviderDao.getActiveProvider().map { it?.let { AIProviderMapper.mapToDomain(it) } }

    override suspend fun addProvider(provider: AIProvider): Result<AIProvider> {
        return try {
            val encryptedApiKey = if (provider.apiKey.isNotBlank()) cryptoManager.encrypt(provider.apiKey) else ""
            val entity = AIProviderMapper.mapToEntity(provider.copy(apiKey = encryptedApiKey))
            aiProviderDao.insertProvider(entity)
            Result.success(provider)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun updateProvider(provider: AIProvider): Result<AIProvider> {
        return try {
            val encryptedApiKey = if (provider.apiKey.isNotBlank()) cryptoManager.encrypt(provider.apiKey) else ""
            val entity = AIProviderMapper.mapToEntity(provider.copy(apiKey = encryptedApiKey))
            aiProviderDao.updateProvider(entity)
            Result.success(provider)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteProvider(id: String): Result<Unit> {
        return try {
            aiProviderDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun setActiveProvider(id: String): Result<Unit> {
        return try {
            aiProviderDao.deactivateAll()
            aiProviderDao.setActive(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun testConnection(providerId: String): Result<Boolean> {
        return try {
            val entity = aiProviderDao.getProviderById(providerId) ?: return Result.error(IllegalArgumentException("Provider not found"))
            val provider = AIProviderMapper.mapToDomain(entity)
            val decryptedApiKey = if (provider.apiKey.isNotBlank()) cryptoManager.decrypt(provider.apiKey) else ""
            val client = createRetrofit(provider.baseUrl, decryptedApiKey, provider.customHeaders, provider.timeout)
            val request = ChatCompletionRequest(
                model = provider.model,
                messages = listOf(ChatMessage(role = "user", content = "Hi")),
                maxTokens = 5,
            )
            val response = client.chatCompletion("${provider.baseUrl.trimEnd('/')}/chat/completions", request)
            Result.success(response.choices?.isNotEmpty() == true || response.error == null)
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    override suspend fun sendMessage(chatId: String, message: String, workspaceContext: String?): Flow<Result<AIChatMessage>> = flow {
        emit(Result.loading())
        try {
            val providerEntity = aiProviderDao.getActiveProvider().first()
                ?: throw IllegalStateException("No active AI provider configured")
            val provider = AIProviderMapper.mapToDomain(providerEntity)
            val decryptedApiKey = cryptoManager.decrypt(provider.apiKey)

            val userMessage = AIChatMessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = "USER",
                content = message,
                timestamp = System.currentTimeMillis(),
                model = provider.model,
                providerId = provider.id,
            )
            aiChatDao.insertMessage(userMessage)

            val existingMessages = aiChatDao.getMessagesForChat(chatId).first()
                .filter { it.role != "SYSTEM" }
                .map { ChatMessage(role = it.role.lowercase(), content = it.content) }
                .toMutableList()
            existingMessages.add(ChatMessage(role = "user", content = message))

            val systemPrompt = if (provider.customSystemPrompt.isNotBlank()) {
                listOf(ChatMessage(role = "system", content = provider.customSystemPrompt))
            } else emptyList()

            val client = createRetrofit(provider.baseUrl, decryptedApiKey, provider.customHeaders, provider.timeout)
            val request = ChatCompletionRequest(
                model = provider.model,
                messages = systemPrompt + existingMessages,
                temperature = provider.temperature,
                top_p = provider.topP,
                max_tokens = provider.maxTokens,
                frequency_penalty = provider.frequencyPenalty,
                presence_penalty = provider.presencePenalty,
                stream = false,
            )

            val response = client.chatCompletion("${provider.baseUrl.trimEnd('/')}/chat/completions", request)
            val assistantContent = response.choices?.firstOrNull()?.message?.content ?: response.error?.message ?: "No response"

            val assistantMessage = AIChatMessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                role = "ASSISTANT",
                content = assistantContent,
                timestamp = System.currentTimeMillis(),
                tokensUsed = response.usage?.totalTokens ?: 0,
                model = provider.model,
                providerId = provider.id,
            )
            aiChatDao.insertMessage(assistantMessage)

            emit(Result.success(AIChatMessage(
                id = assistantMessage.id,
                chatId = assistantMessage.chatId,
                role = com.titan.domain.model.MessageRole.ASSISTANT,
                content = assistantContent,
                timestamp = assistantMessage.timestamp,
                tokensUsed = assistantMessage.tokensUsed,
                model = assistantMessage.model,
                providerId = assistantMessage.providerId,
            )))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }

    override fun getChatHistory(): Flow<List<AIChat>> =
        aiChatDao.getAllChats().map { list -> list.map { it.toDomain() } }

    override fun getChatMessages(chatId: String): Flow<List<AIChatMessage>> =
        aiChatDao.getMessagesForChat(chatId).map { list -> list.map { it.toDomain() } }

    override suspend fun createChat(title: String): Result<AIChat> {
        return try {
            val entity = AIChatEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            aiChatDao.insertChat(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteChat(id: String): Result<Unit> {
        return try {
            aiChatDao.deleteMessagesForChat(id)
            aiChatDao.deleteChatById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun togglePinChat(id: String): Result<Unit> {
        return try { aiChatDao.togglePin(id); Result.success(Unit) }
        catch (e: Exception) { Result.error(e) }
    }

    override suspend fun clearChatHistory(chatId: String): Result<Unit> {
        return try { aiChatDao.deleteMessagesForChat(chatId); Result.success(Unit) }
        catch (e: Exception) { Result.error(e) }
    }

    override suspend fun generateCode(prompt: String, language: String, context: String?): Result<String> {
        val systemMessage = "You are an expert $language developer. Generate clean, production-ready code. Only output the code without explanations."
        return executeAIRequest(systemMessage, "Generate the following $language code: $prompt${context?.let { "\n\nContext:\n$it" } ?: ""}")
    }

    override suspend fun explainCode(code: String, language: String): Result<String> {
        val systemMessage = "You are an expert $language developer. Explain the code clearly and concisely."
        return executeAIRequest(systemMessage, "Explain this $language code:\n```\n$code\n```")
    }

    override suspend fun refactorCode(code: String, language: String, instruction: String): Result<String> {
        val systemMessage = "You are an expert $language developer. Refactor the code according to the instruction. Output only the refactored code."
        return executeAIRequest(systemMessage, "Refactor this $language code. Instruction: $instruction\n\nCode:\n```\n$code\n```")
    }

    override suspend fun generateCommitMessage(diff: String): Result<String> {
        val systemMessage = "You are a Git expert. Generate a concise, conventional commit message based on the diff."
        return executeAIRequest(systemMessage, "Generate a commit message for this diff:\n$diff")
    }

    override suspend fun generateTests(code: String, language: String): Result<String> {
        val systemMessage = "You are a testing expert for $language. Generate comprehensive unit tests."
        return executeAIRequest(systemMessage, "Generate unit tests for this $language code:\n```\n$code\n```")
    }

    override suspend fun fixBuildError(error: String, buildLog: String): Result<String> {
        val systemMessage = "You are an Android build system expert. Analyze the build error and provide a fix."
        return executeAIRequest(systemMessage, "Build Error: $error\n\nBuild Log:\n$buildLog")
    }

    override suspend fun generateDocumentation(code: String, language: String): Result<String> {
        val systemMessage = "You are a documentation expert for $language. Generate KDoc/Javadoc documentation."
        return executeAIRequest(systemMessage, "Generate documentation for this $language code:\n```\n$code\n```")
    }

    private suspend fun executeAIRequest(systemPrompt: String, userMessage: String): Result<String> {
        return try {
            val providerEntity = aiProviderDao.getActiveProvider().first()
                ?: return Result.error(IllegalStateException("No active AI provider"))
            val provider = AIProviderMapper.mapToDomain(providerEntity)
            val decryptedApiKey = cryptoManager.decrypt(provider.apiKey)

            val client = createRetrofit(provider.baseUrl, decryptedApiKey, provider.customHeaders, provider.timeout)
            val request = ChatCompletionRequest(
                model = provider.model,
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = userMessage),
                ),
                temperature = provider.temperature,
                max_tokens = provider.maxTokens,
            )
            val response = client.chatCompletion("${provider.baseUrl.trimEnd('/')}/chat/completions", request)
            val content = response.choices?.firstOrNull()?.message?.content ?: "No response generated"
            Result.success(content)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun createRetrofit(baseUrl: String, apiKey: String, headers: Map<String, String>, timeout: Long): AIHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(AIHttpClient::class.java)
    }

    private fun String.toMediaType() = okhttp3.MediaType.parse(this) ?: okhttp3.MediaType.get("application/json")

    private fun AIChatEntity.toDomain(): AIChat = AIChat(
        id = id, title = title, providerId = providerId, model = model,
        isPinned = isPinned, createdAt = createdAt, updatedAt = updatedAt, workspaceContext = workspaceContext,
    )

    private fun AIChatMessageEntity.toDomain(): AIChatMessage = AIChatMessage(
        id = id, chatId = chatId,
        role = com.titan.domain.model.MessageRole.entries.firstOrNull { it.name == role } ?: com.titan.domain.model.MessageRole.USER,
        content = content, timestamp = timestamp, tokensUsed = tokensUsed,
        model = model, providerId = providerId, isError = isError, errorMessage = errorMessage,
    )
}
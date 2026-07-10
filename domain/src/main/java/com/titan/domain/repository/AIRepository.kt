package com.titan.domain.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.AIChat
import com.titan.domain.model.AIChatMessage
import com.titan.domain.model.AIProvider
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    fun getAllProviders(): Flow<List<AIProvider>>
    fun getActiveProvider(): Flow<AIProvider?>
    suspend fun addProvider(provider: AIProvider): Result<AIProvider>
    suspend fun updateProvider(provider: AIProvider): Result<AIProvider>
    suspend fun deleteProvider(id: String): Result<Unit>
    suspend fun setActiveProvider(id: String): Result<Unit>
    suspend fun testConnection(providerId: String): Result<Boolean>
    suspend fun sendMessage(chatId: String, message: String, workspaceContext: String? = null): Flow<Result<AIChatMessage>>
    fun getChatHistory(): Flow<List<AIChat>>
    fun getChatMessages(chatId: String): Flow<List<AIChatMessage>>
    suspend fun createChat(title: String): Result<AIChat>
    suspend fun deleteChat(id: String): Result<Unit>
    suspend fun togglePinChat(id: String): Result<Unit>
    suspend fun clearChatHistory(chatId: String): Result<Unit>
    suspend fun generateCode(prompt: String, language: String, context: String? = null): Result<String>
    suspend fun explainCode(code: String, language: String): Result<String>
    suspend fun refactorCode(code: String, language: String, instruction: String): Result<String>
    suspend fun generateCommitMessage(diff: String): Result<String>
    suspend fun generateTests(code: String, language: String): Result<String>
    suspend fun fixBuildError(error: String, buildLog: String): Result<String>
    suspend fun generateDocumentation(code: String, language: String): Result<String>
}
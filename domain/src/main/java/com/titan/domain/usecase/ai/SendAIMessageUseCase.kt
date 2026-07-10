package com.titan.domain.usecase.ai

import com.titan.core.common.base.Result
import com.titan.domain.model.AIChatMessage
import com.titan.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendAIMessageUseCase @Inject constructor(
    private val aiRepository: AIRepository,
) {
    operator fun invoke(chatId: String, message: String, context: String? = null): Flow<Result<AIChatMessage>> {
        return aiRepository.sendMessage(chatId, message, context)
    }
}
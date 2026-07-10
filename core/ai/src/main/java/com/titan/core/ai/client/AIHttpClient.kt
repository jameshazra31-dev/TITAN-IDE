package com.titan.core.ai.client

import com.titan.core.ai.model.ChatCompletionRequest
import com.titan.core.ai.model.ChatCompletionResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AIHttpClient {

    @POST
    @Streaming
    @Headers("Content-Type: application/json")
    suspend fun streamChatCompletion(
        @Url url: String,
        @Body request: ChatCompletionRequest,
    ): ChatCompletionResponse

    @POST
    @Headers("Content-Type: application/json")
    suspend fun chatCompletion(
        @Url url: String,
        @Body request: ChatCompletionRequest,
    ): ChatCompletionResponse
}
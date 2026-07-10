package com.titan.data.remote

import com.titan.core.ai.client.AIHttpClient
import com.titan.core.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    val aiHttpClient: AIHttpClient,
    val networkMonitor: NetworkMonitor,
)
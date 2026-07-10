package com.titan.ide.di

import com.titan.core.database.dao.*
import com.titan.core.security.CryptoManager
import com.titan.data.repository.AIRepositoryImpl
import com.titan.data.repository.FileRepositoryImpl
import com.titan.data.repository.ProjectRepositoryImpl
import com.titan.domain.repository.AIRepository
import com.titan.domain.repository.FileRepository
import com.titan.domain.repository.GitRepository
import com.titan.domain.repository.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository

    @Binds
    @Singleton
    abstract fun bindGitRepository(impl: com.titan.data.repository.GitRepositoryImpl): GitRepository
}
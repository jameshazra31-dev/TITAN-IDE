package com.titan.ide.di

import android.content.Context
import androidx.room.Room
import com.titan.core.common.util.Constants
import com.titan.core.database.TitanDatabase
import com.titan.core.database.converter.Converters
import com.titan.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTitanDatabase(
        @ApplicationContext context: Context,
        converters: Converters,
    ): TitanDatabase {
        return Room.databaseBuilder(
            context,
            TitanDatabase::class.java,
            Constants.DATABASE_NAME,
        )
            .addTypeConverter(converters)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProjectDao(database: TitanDatabase): ProjectDao = database.projectDao()

    @Provides
    fun provideAIProviderDao(database: TitanDatabase): AIProviderDao = database.aiProviderDao()

    @Provides
    fun provideAIChatDao(database: TitanDatabase): AIChatDao = database.aiChatDao()

    @Provides
    fun provideRecentFileDao(database: TitanDatabase): RecentFileDao = database.recentFileDao()

    @Provides
    fun provideBookmarkDao(database: TitanDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideTerminalHistoryDao(database: TitanDatabase): TerminalHistoryDao = database.terminalHistoryDao()

    @Provides
    fun provideGitAccountDao(database: TitanDatabase): GitAccountDao = database.gitAccountDao()

    @Provides
    @Singleton
    fun provideConverters(): Converters = Converters()
}
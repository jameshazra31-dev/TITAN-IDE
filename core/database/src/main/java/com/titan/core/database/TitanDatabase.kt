package com.titan.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.titan.core.database.dao.*
import com.titan.core.database.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        AIProviderEntity::class,
        AIChatEntity::class,
        AIChatMessageEntity::class,
        RecentFileEntity::class,
        BookmarkEntity::class,
        TerminalHistoryEntity::class,
        GitAccountEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TitanDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun aiProviderDao(): AIProviderDao
    abstract fun aiChatDao(): AIChatDao
    abstract fun recentFileDao(): RecentFileDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun terminalHistoryDao(): TerminalHistoryDao
    abstract fun gitAccountDao(): GitAccountDao
}
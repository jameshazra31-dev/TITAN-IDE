package com.titan.data.local

import com.titan.core.database.dao.ProjectDao
import com.titan.core.database.dao.RecentFileDao
import com.titan.core.database.dao.BookmarkDao
import com.titan.core.database.dao.TerminalHistoryDao
import com.titan.core.database.dao.GitAccountDao
import com.titan.core.database.dao.AIProviderDao
import com.titan.core.database.dao.AIChatDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    val projectDao: ProjectDao,
    val aiProviderDao: AIProviderDao,
    val aiChatDao: AIChatDao,
    val recentFileDao: RecentFileDao,
    val bookmarkDao: BookmarkDao,
    val terminalHistoryDao: TerminalHistoryDao,
    val gitAccountDao: GitAccountDao,
)
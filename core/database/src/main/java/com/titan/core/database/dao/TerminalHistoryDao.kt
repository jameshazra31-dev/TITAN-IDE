package com.titan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titan.core.database.entity.TerminalHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TerminalHistoryDao {
    @Query("SELECT * FROM terminal_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommands(limit: Int = 100): Flow<List<TerminalHistoryEntity>>

    @Query("SELECT DISTINCT command FROM terminal_history WHERE command LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun searchCommands(query: String, limit: Int = 20): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCommand(command: TerminalHistoryEntity)

    @Query("DELETE FROM terminal_history")
    suspend fun clearHistory()
}
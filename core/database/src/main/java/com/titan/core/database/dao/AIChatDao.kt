package com.titan.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titan.core.database.entity.AIChatEntity
import com.titan.core.database.entity.AIChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIChatDao {
    @Query("SELECT * FROM ai_chats ORDER BY updatedAt DESC")
    fun getAllChats(): Flow<List<AIChatEntity>>

    @Query("SELECT * FROM ai_chats WHERE id = :id")
    suspend fun getChatById(id: String): AIChatEntity?

    @Query("SELECT * FROM ai_chats WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedChats(): Flow<List<AIChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: AIChatEntity)

    @Query("DELETE FROM ai_chats WHERE id = :id")
    suspend fun deleteChatById(id: String)

    @Query("UPDATE ai_chats SET isPinned = CASE WHEN isPinned = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun togglePin(id: String)

    @Query("UPDATE ai_chats SET title = :title, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateChatTitle(id: String, title: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM ai_chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<AIChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AIChatMessageEntity)

    @Query("DELETE FROM ai_chat_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
}
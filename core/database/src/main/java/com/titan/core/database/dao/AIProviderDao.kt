package com.titan.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.titan.core.database.entity.AIProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIProviderDao {
    @Query("SELECT * FROM ai_providers ORDER BY createdAt ASC")
    fun getAllProviders(): Flow<List<AIProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE id = :id")
    suspend fun getProviderById(id: String): AIProviderEntity?

    @Query("SELECT * FROM ai_providers WHERE isActive = 1 LIMIT 1")
    fun getActiveProvider(): Flow<AIProviderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: AIProviderEntity)

    @Update
    suspend fun updateProvider(provider: AIProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: AIProviderEntity)

    @Query("UPDATE ai_providers SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE ai_providers SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: String)

    @Query("DELETE FROM ai_providers WHERE id = :id")
    suspend fun deleteById(id: String)
}
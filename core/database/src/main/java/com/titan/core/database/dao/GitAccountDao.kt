package com.titan.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titan.core.database.entity.GitAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GitAccountDao {
    @Query("SELECT * FROM git_accounts ORDER BY createdAt ASC")
    fun getAllAccounts(): Flow<List<GitAccountEntity>>

    @Query("SELECT * FROM git_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<GitAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: GitAccountEntity)

    @Query("UPDATE git_accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE git_accounts SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: String)

    @Query("DELETE FROM git_accounts WHERE id = :id")
    suspend fun deleteById(id: String)
}
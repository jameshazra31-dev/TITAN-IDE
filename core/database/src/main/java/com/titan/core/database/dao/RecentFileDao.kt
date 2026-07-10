package com.titan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.titan.core.database.entity.RecentFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 50): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun removeRecentFile(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}
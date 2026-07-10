package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terminal_history")
data class TerminalHistoryEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val workingDirectory: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
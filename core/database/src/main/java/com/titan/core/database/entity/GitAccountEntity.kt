package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "git_accounts")
data class GitAccountEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val provider: String = "github",
    val username: String = "",
    val token: String = "",
    val email: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
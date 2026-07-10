package com.titan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.titan.domain.model.BuildSystem
import com.titan.domain.model.ProgrammingLanguage
import com.titan.domain.model.TemplateType

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val templateType: String = TemplateType.EMPTY.name,
    val buildSystem: String = BuildSystem.GRADLE_KTS.name,
    val minSdk: Int = 29,
    val targetSdk: Int = 35,
    val packageName: String = "",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val language: String = ProgrammingLanguage.KOTLIN.name,
    val isOpened: Boolean = false,
    val sizeInBytes: Long = 0L,
    val fileCount: Int = 0,
)
package com.titan.domain.model

import java.io.Serializable
import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val templateType: TemplateType = TemplateType.EMPTY,
    val buildSystem: BuildSystem = BuildSystem.GRADLE_KTS,
    val minSdk: Int = 29,
    val targetSdk: Int = 35,
    val packageName: String = "",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val language: ProgrammingLanguage = ProgrammingLanguage.KOTLIN,
    val isOpened: Boolean = false,
    val sizeInBytes: Long = 0L,
    val fileCount: Int = 0,
) : Serializable {
    val formattedSize: String get() = formatFileSize(sizeInBytes)
    val formattedDate: String get() = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(createdAt))

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
    }
}

enum class TemplateType(val displayName: String) {
    EMPTY("Empty Project"),
    KOTLIN("Kotlin App"),
    COMPOSE("Jetpack Compose"),
    MVVM("MVVM Architecture"),
    CLEAN_ARCHITECTURE("Clean Architecture"),
    JAVA("Java App"),
    LIBRARY("Android Library"),
    NAVIGATION("Navigation Component"),
    ROOM("Room Database"),
    RETROFIT("Retrofit Networking"),
    HILT("Hilt DI"),
}

enum class BuildSystem(val displayName: String, val fileExtension: String) {
    GRADLE_GROOVY("Gradle (Groovy)", "gradle"),
    GRADLE_KTS("Gradle (Kotlin DSL)", "gradle.kts"),
}

enum class ProgrammingLanguage(val displayName: String, val extension: String) {
    KOTLIN("Kotlin", "kt"),
    JAVA("Java", "java"),
    BOTH("Kotlin & Java", "kt"),
}
package com.titan.domain.model

data class EditorTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String,
    val fileName: String,
    val content: String = "",
    val language: String = "",
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val scrollPosition: Int = 0,
    val encoding: String = "UTF-8",
    val lineSeparator: String = "\n",
    val readOnly: Boolean = false,
    val isPinned: Boolean = false,
) {
    val extension: String get() = fileName.substringAfterLast('.', "")
    val displayName: String get() = if (isModified) "$fileName *" else fileName
}
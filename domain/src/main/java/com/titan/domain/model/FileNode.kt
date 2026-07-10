package com.titan.domain.model

import java.io.Serializable

data class FileNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val isHidden: Boolean = false,
    val isReadable: Boolean = true,
    val isWritable: Boolean = true,
    val extension: String = "",
    val children: List<FileNode> = emptyList(),
    val depth: Int = 0,
) : Serializable {
    val formattedSize: String get() = Project.formatFileSize(size)
    val formattedDate: String get() = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastModified))
    val fileType: FileType get() = FileType.fromExtension(extension)

    enum class FileType(val icon: String, val category: String) {
        KOTLIN("K", "Code"),
        JAVA("J", "Code"),
        XML("X", "Layout"),
        GRADLE("G", "Build"),
        JSON("{}", "Data"),
        YAML("Y", "Config"),
        MARKDOWN("M", "Doc"),
        IMAGE("IMG", "Image"),
        ARCHIVE("ZIP", "Archive"),
        UNKNOWN("?", "Other"),
        FOLDER("DIR", "Folder");

        companion object {
            fun fromExtension(ext: String): FileType = when (ext.lowercase()) {
                "kt", "kts" -> KOTLIN
                "java" -> JAVA
                "xml" -> XML
                "gradle", "gradle.kts" -> GRADLE
                "json" -> JSON
                "yaml", "yml" -> YAML
                "md", "mdx" -> MARKDOWN
                "png", "jpg", "jpeg", "gif", "webp", "svg" -> IMAGE
                "zip", "tar", "gz", "7z", "jar", "aar" -> ARCHIVE
                else -> UNKNOWN
            }
        }
    }
}
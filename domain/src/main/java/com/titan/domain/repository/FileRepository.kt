package com.titan.domain.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.FileNode
import com.titan.domain.model.Project
import java.io.File

interface FileRepository {
    suspend fun getFileTree(rootPath: String, showHidden: Boolean = false): Result<List<FileNode>>
    suspend fun readFile(path: String, encoding: String = "UTF-8"): Result<String>
    suspend fun writeFile(path: String, content: String, encoding: String = "UTF-8"): Result<Unit>
    suspend fun createFile(path: String): Result<Unit>
    suspend fun createDirectory(path: String): Result<Unit>
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun renameFile(oldPath: String, newPath: String): Result<Unit>
    suspend fun copyFile(sourcePath: String, destinationPath: String): Result<Unit>
    suspend fun moveFile(sourcePath: String, destinationPath: String): Result<Unit>
    suspend fun getFileInfo(path: String): Result<FileNode>
    suspend fun searchFiles(rootPath: String, query: String): Result<List<FileNode>>
    suspend fun extractZip(zipPath: String, destinationPath: String): Result<Unit>
    suspend fun compressToZip(sourcePath: String, outputPath: String): Result<String>
    suspend fun exists(path: String): Boolean
    suspend fun isDirectory(path: String): Boolean
    suspend fun getStorageInfo(): Result<StorageInfo>
}

data class StorageInfo(
    val totalSpace: Long = 0L,
    val freeSpace: Long = 0L,
    val usedSpace: Long = 0L,
) {
    val formattedTotal: String get() = Project.formatFileSize(totalSpace)
    val formattedFree: String get() = Project.formatFileSize(freeSpace)
    val formattedUsed: String get() = Project.formatFileSize(usedSpace)
    val usagePercentage: Double get() = if (totalSpace > 0) (usedSpace.toDouble() / totalSpace) * 100 else 0.0
}
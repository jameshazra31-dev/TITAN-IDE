package com.titan.data.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.FileNode
import com.titan.domain.repository.FileRepository
import com.titan.domain.repository.StorageInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor() : FileRepository {

    override suspend fun getFileTree(rootPath: String, showHidden: Boolean): Result<List<FileNode>> {
        return try {
            val root = File(rootPath)
            if (!root.exists() || !root.isDirectory) return Result.error(IllegalArgumentException("Invalid directory: $rootPath"))
            val nodes = buildFileTree(root, showHidden, 0)
            Result.success(nodes)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun buildFileTree(dir: File, showHidden: Boolean, depth: Int): List<FileNode> {
        if (depth > 20) return emptyList()
        return dir.listFiles()
            ?.filter { showHidden || !it.name.startsWith(".") }
            ?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?.map { file ->
                FileNode(
                    path = file.absolutePath,
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0L,
                    lastModified = file.lastModified(),
                    isHidden = file.isHidden,
                    isReadable = file.canRead(),
                    isWritable = file.canWrite(),
                    extension = if (file.isFile) file.extension else "",
                    children = if (file.isDirectory) buildFileTree(file, showHidden, depth + 1) else emptyList(),
                    depth = depth,
                )
            } ?: emptyList()
    }

    override suspend fun readFile(path: String, encoding: String): Result<String> {
        return try {
            val content = File(path).readText(Charset.forName(encoding))
            Result.success(content)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun writeFile(path: String, content: String, encoding: String): Result<Unit> {
        return try {
            File(path).writeText(content, Charset.forName(encoding))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun createFile(path: String): Result<Unit> {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.createNewFile()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun createDirectory(path: String): Result<Unit> {
        return try {
            File(path).mkdirs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            val file = File(path)
            if (file.isDirectory) file.deleteRecursively() else file.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun renameFile(oldPath: String, newPath: String): Result<Unit> {
        return try {
            File(oldPath).renameTo(File(newPath))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String): Result<Unit> {
        return try {
            val source = File(sourcePath)
            val dest = File(destinationPath)
            if (source.isDirectory) {
                source.copyRecursively(dest, overwrite = true)
            } else {
                source.copyTo(dest, overwrite = true)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String): Result<Unit> {
        return try {
            val source = File(sourcePath)
            val dest = File(destinationPath)
            dest.parentFile?.mkdirs()
            if (source.renameTo(dest)) {
                Result.success(Unit)
            } else {
                source.copyTo(dest, overwrite = true)
                source.deleteRecursively()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getFileInfo(path: String): Result<FileNode> {
        return try {
            val file = File(path)
            Result.success(FileNode(
                path = file.absolutePath, name = file.name,
                isDirectory = file.isDirectory, size = file.length(),
                lastModified = file.lastModified(), isHidden = file.isHidden,
                isReadable = file.canRead(), isWritable = file.canWrite(),
                extension = if (file.isFile) file.extension else "",
            ))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun searchFiles(rootPath: String, query: String): Result<List<FileNode>> {
        return try {
            val root = File(rootPath)
            val results = mutableListOf<FileNode>()
            root.walkTopDown().filter { it.isFile && it.name.contains(query, ignoreCase = true) }.forEach { file ->
                results.add(FileNode(
                    path = file.absolutePath, name = file.name,
                    isDirectory = false, size = file.length(),
                    lastModified = file.lastModified(), extension = file.extension,
                ))
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun extractZip(zipPath: String, destinationPath: String): Result<Unit> {
        return try {
            val destDir = File(destinationPath)
            destDir.mkdirs()
            ZipInputStream(FileInputStream(zipPath)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val file = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { out -> zis.copyTo(out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun compressToZip(sourcePath: String, outputPath: String): Result<String> {
        return try {
            val sourceDir = File(sourcePath)
            ZipOutputStream(FileOutputStream(outputPath)).use { zos ->
                sourceDir.walkTopDown().forEach { file ->
                    val entryName = file.relativeTo(sourceDir).path
                    if (file.isFile) {
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    } else if (file.isDirectory && entryName.isNotEmpty()) {
                        zos.putNextEntry(ZipEntry("$entryName/"))
                        zos.closeEntry()
                    }
                }
            }
            Result.success(outputPath)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun exists(path: String): Boolean = File(path).exists()

    override suspend fun isDirectory(path: String): Boolean = File(path).isDirectory

    override suspend fun getStorageInfo(): Result<StorageInfo> {
        return try {
            val externalDirs = android.os.Environment.getExternalStorageDirectory()
            val stat = android.os.StatFs(externalDirs.path)
            val total = stat.blockSizeLong * stat.blockCountLong
            val free = stat.blockSizeLong * stat.availableBlocksLong
            Result.success(StorageInfo(totalSpace = total, freeSpace = free, usedSpace = total - free))
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
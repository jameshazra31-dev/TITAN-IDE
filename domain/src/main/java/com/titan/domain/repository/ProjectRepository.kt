package com.titan.domain.repository

import com.titan.core.common.base.Result
import com.titan.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    fun getRecentProjects(limit: Int = 10): Flow<List<Project>>
    fun getPinnedProjects(): Flow<List<Project>>
    fun getProjectById(id: String): Flow<Project?>
    fun getProjectByPath(path: String): Flow<Project?>
    suspend fun createProject(project: Project): Result<Project>
    suspend fun updateProject(project: Project): Result<Project>
    suspend fun deleteProject(id: String): Result<Unit>
    suspend fun renameProject(id: String, newName: String): Result<Project>
    suspend fun duplicateProject(id: String): Result<Project>
    suspend fun togglePinProject(id: String): Result<Unit>
    suspend fun importProjectFromZip(zipPath: String, destinationPath: String): Result<Project>
    suspend fun exportProjectToZip(projectId: String, outputPath: String): Result<String>
    suspend fun cloneGitRepository(url: String, destinationPath: String, branch: String? = null): Result<Project>
    suspend fun getProjectStatistics(id: String): Result<ProjectStatistics>
}

data class ProjectStatistics(
    val totalFiles: Int = 0,
    val totalLines: Int = 0,
    val totalSize: Long = 0L,
    val kotlinFiles: Int = 0,
    val javaFiles: Int = 0,
    val xmlFiles: Int = 0,
    val gradleFiles: Int = 0,
    val otherFiles: Int = 0,
)
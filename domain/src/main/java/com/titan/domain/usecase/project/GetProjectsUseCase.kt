package com.titan.domain.usecase.project

import com.titan.domain.model.Project
import com.titan.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
) {
    operator fun invoke(): Flow<List<Project>> = projectRepository.getAllProjects()

    fun getRecent(limit: Int = 10): Flow<List<Project>> = projectRepository.getRecentProjects(limit)

    fun getPinned(): Flow<List<Project>> = projectRepository.getPinnedProjects()
}
package com.titan.domain.usecase.project

import com.titan.core.common.base.Result
import com.titan.domain.model.Project
import com.titan.domain.model.TemplateType
import com.titan.domain.repository.ProjectRepository
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(project: Project): Result<Project> = projectRepository.createProject(project)

    suspend fun fromTemplate(name: String, path: String, template: TemplateType): Result<Project> {
        val project = Project(
            name = name,
            path = path,
            templateType = template,
            packageName = "com.example.${name.lowercase().replace(' ', '_')}",
        )
        return projectRepository.createProject(project)
    }
}
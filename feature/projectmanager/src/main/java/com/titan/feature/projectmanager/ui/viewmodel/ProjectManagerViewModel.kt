package com.titan.feature.projectmanager.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.titan.core.common.base.BaseViewModel
import com.titan.core.common.base.UiAction
import com.titan.core.common.base.UiEvent
import com.titan.core.common.base.UiState
import com.titan.domain.model.BuildSystem
import com.titan.domain.model.ProgrammingLanguage
import com.titan.domain.model.Project
import com.titan.domain.model.TemplateType
import com.titan.domain.usecase.project.CreateProjectUseCase
import com.titan.domain.usecase.project.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectManagerUiState(
    val projects: List<Project> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val isGridView: Boolean = false,
    val searchQuery: String = "",
) : UiState

sealed class ProjectManagerEvent : UiEvent {
    data class SelectTab(val index: Int) : ProjectManagerEvent()
    data class DeleteProject(val id: String) : ProjectManagerEvent()
    data class TogglePin(val id: String) : ProjectManagerEvent()
    data class DuplicateProject(val id: String) : ProjectManagerEvent()
    data class RenameProject(val id: String, val newName: String) : ProjectManagerEvent()
    data class SearchChanged(val query: String) : ProjectManagerEvent()
    data object ToggleViewMode : ProjectManagerEvent()
    data object ShowImportOptions : ProjectManagerEvent()
}

sealed class ProjectManagerAction : UiAction {
    data class ShowToast(val message: String) : ProjectManagerAction()
    data class NavigateToProject(val projectId: String) : ProjectManagerAction()
}

@HiltViewModel
class ProjectManagerViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
) : BaseViewModel<ProjectManagerUiState, ProjectManagerEvent, ProjectManagerAction>() {

    private val _allProjects = MutableStateFlow<List<Project>>(emptyList())

    init {
        viewModelScope.launch {
            combine(
                getProjectsUseCase(),
                getProjectsUseCase.getPinned(),
            ) { all, pinned -> Pair(all, pinned) }.collect { (all, pinned) ->
                _allProjects.value = all
                val filtered = when (state.value.selectedTab) {
                    0 -> all
                    1 -> all.sortedByDescending { it.lastOpenedAt }.take(20)
                    2 -> pinned
                    else -> all
                }
                setState { copy(projects = filtered, isLoading = false) }
            }
        }
    }

    override fun initialState(): ProjectManagerUiState = ProjectManagerUiState()

    override fun onEvent(event: ProjectManagerEvent) {
        when (event) {
            is ProjectManagerEvent.SelectTab -> {
                val filtered = when (event.index) {
                    0 -> _allProjects.value
                    1 -> _allProjects.value
                        .sortedByDescending { it.lastOpenedAt }
                        .take(20)

                    2 -> _allProjects.value.filter { it.isPinned }
                    else -> _allProjects.value
                }
                setState {
                    copy(
                        selectedTab = event.index,
                        projects = filtered,
                    )
                }
            }

            is ProjectManagerEvent.DeleteProject -> {
                viewModelScope.launch {
                    sendAction(ProjectManagerAction.ShowToast("Project deleted"))
                }
            }

            is ProjectManagerEvent.TogglePin -> {
                viewModelScope.launch {
                    sendAction(ProjectManagerAction.ShowToast("Project pinned toggled"))
                }
            }

            is ProjectManagerEvent.DuplicateProject -> {
                viewModelScope.launch {
                    sendAction(ProjectManagerAction.ShowToast("Project duplicated"))
                }
            }

            is ProjectManagerEvent.RenameProject -> {
                viewModelScope.launch {
                    sendAction(ProjectManagerAction.ShowToast("Project renamed"))
                }
            }

            is ProjectManagerEvent.SearchChanged -> {
                setState { copy(searchQuery = event.query) }
            }

            is ProjectManagerEvent.ToggleViewMode -> {
                setState { copy(isGridView = !state.value.isGridView) }
            }

            is ProjectManagerEvent.ShowImportOptions -> {
                // Import options will be handled by the navigation layer
            }
        }
    }

    fun createProject(
        name: String,
        path: String,
        template: TemplateType,
        language: ProgrammingLanguage,
        buildSystem: BuildSystem,
        packageName: String,
        minSdk: Int,
    ) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                path = path,
                templateType = template,
                language = language,
                buildSystem = buildSystem,
                packageName = packageName,
                minSdk = minSdk,
            )
            createProjectUseCase(project)
        }
    }

    fun deleteProject(id: String) = onEvent(ProjectManagerEvent.DeleteProject(id))

    fun duplicateProject(id: String) = onEvent(ProjectManagerEvent.DuplicateProject(id))

    fun togglePin(id: String) = onEvent(ProjectManagerEvent.TogglePin(id))

    fun renameProject(id: String, newName: String) =
        onEvent(ProjectManagerEvent.RenameProject(id, newName))

    fun selectTab(index: Int) = onEvent(ProjectManagerEvent.SelectTab(index))

    fun toggleViewMode() = onEvent(ProjectManagerEvent.ToggleViewMode)

    fun showImportOptions() = onEvent(ProjectManagerEvent.ShowImportOptions)
}
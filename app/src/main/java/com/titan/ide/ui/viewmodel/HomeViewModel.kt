package com.titan.ide.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.titan.core.common.base.BaseViewModel
import com.titan.core.common.base.UiAction
import com.titan.core.common.base.UiEvent
import com.titan.core.common.base.UiState
import com.titan.domain.model.Project
import com.titan.domain.usecase.project.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentProjects: List<Project> = emptyList(),
    val pinnedProjects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val showCreateDialog: Boolean = false,
) : UiState

sealed class HomeEvent : UiEvent {
    data object CreateProjectClicked : HomeEvent()
    data object OpenProjectClicked : HomeEvent()
    data object CloneGitClicked : HomeEvent()
    data object SearchClicked : HomeEvent()
    data class SearchQueryChanged(val query: String) : HomeEvent()
    data class ProjectClicked(val projectId: String) : HomeEvent()
    data class DeleteProject(val projectId: String) : HomeEvent()
}

sealed class HomeAction : UiAction {
    data class ShowToast(val message: String) : HomeAction()
    data class NavigateToProject(val projectId: String) : HomeAction()
    data object NavigateToCreateProject : HomeAction()
    data object NavigateToOpenProject : HomeAction()
    data object NavigateToCloneGit : HomeAction()
    data object NavigateToSearch : HomeAction()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
) : BaseViewModel<HomeUiState, HomeEvent, HomeAction>() {

    init {
        viewModelScope.launch {
            combine(
                getProjectsUseCase.getRecent(limit = 20),
                getProjectsUseCase.getPinned(),
            ) { recent, pinned -> Pair(recent, pinned) }.collect { (recent, pinned) ->
                setState {
                    copy(
                        recentProjects = recent.filterNot { p ->
                            pinned.any { it.id == p.id }
                        },
                        pinnedProjects = pinned,
                        isLoading = false,
                    )
                }
            }
        }
    }

    override fun initialState(): HomeUiState = HomeUiState()

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.CreateProjectClicked -> sendAction(HomeAction.NavigateToCreateProject)
            is HomeEvent.OpenProjectClicked -> sendAction(HomeAction.NavigateToOpenProject)
            is HomeEvent.CloneGitClicked -> sendAction(HomeAction.NavigateToCloneGit)
            is HomeEvent.SearchClicked -> sendAction(HomeAction.NavigateToSearch)
            is HomeEvent.SearchQueryChanged -> setState { copy(searchQuery = event.query) }
            is HomeEvent.ProjectClicked -> sendAction(HomeAction.NavigateToProject(event.projectId))
            is HomeEvent.DeleteProject -> {
                viewModelScope.launch {
                    sendAction(HomeAction.ShowToast("Project deleted"))
                }
            }
        }
    }
}
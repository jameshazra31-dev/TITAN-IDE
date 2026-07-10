package com.titan.feature.editor.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.titan.core.common.base.BaseViewModel
import com.titan.domain.model.FileNode
import com.titan.domain.repository.FileRepository
import com.titan.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditorUiState(
    val projectRoot: String = "",
    val fileTree: List<FileNode> = emptyList(),
    val expandedDirectories: Set<String> = emptySet(),
    val openTabs: List<com.titan.domain.model.EditorTab> = emptyList(),
    val activeTabId: String = "",
    val activeContent: String = "",
    val activeLanguage: String = "",
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val wordWrap: Boolean = false,
    val lineNumbers: Boolean = true,
    val bracketMatching: Boolean = true,
    val codeFolding: Boolean = true,
    val isModified: Boolean = false,
    val readOnly: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val matchCount: Int = 0,
    val currentMatchIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
) : com.titan.core.common.base.UiState

sealed class EditorEvent : com.titan.core.common.base.UiEvent {
    data class OpenFile(val path: String) : EditorEvent()
    data class CloseTab(val tabId: String) : EditorEvent()
    data class SwitchTab(val tabId: String) : EditorEvent()
    data class TogglePinTab(val tabId: String) : EditorEvent()
    data class UpdateContent(val content: String) : EditorEvent()
    data class UpdateCursorPosition(val line: Int, val column: Int) : EditorEvent()
    data class ToggleDirectoryExpand(val path: String) : EditorEvent()
    data class CreateNewFile(val parentPath: String) : EditorEvent()
    data class CreateNewDirectory(val parentPath: String) : EditorEvent()
    data object Undo : EditorEvent()
    data object Redo : EditorEvent()
    data object SaveFile : EditorEvent()
    data object ToggleWordWrap : EditorEvent()
    data object ToggleLineNumbers : EditorEvent()
    data class FindQueryChanged(val query: String) : EditorEvent()
    data class ReplaceQueryChanged(val query: String) : EditorEvent()
    data object FindNext : EditorEvent()
    data object FindPrevious : EditorEvent()
    data object ReplaceNext : EditorEvent()
    data object ReplaceAll : EditorEvent()
}

sealed class EditorAction : com.titan.core.common.base.UiAction {
    data class ShowToast(val message: String) : EditorAction()
    data class ShowError(val error: String) : EditorAction()
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val projectRepository: ProjectRepository,
) : BaseViewModel<EditorUiState, EditorEvent, EditorAction>() {

    private var autoSaveJob: Job? = null

    override fun initialState() = EditorUiState()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.getProjectById(projectId).collect { project ->
                if (project != null) {
                    setState { copy(projectRoot = project.path) }
                    loadFileTree(project.path)
                }
            }
        }
    }

    fun openFile(path: String) {
        viewModelScope.launch {
            val existingTab = state.value.openTabs.find { it.filePath == path }
            if (existingTab != null) {
                switchTab(existingTab.id)
                return@launch
            }

            setState { copy(isLoading = true) }
            val result = fileRepository.readFile(path)
            when {
                result.isSuccess -> {
                    val content = result.getOrNull() ?: ""
                    val fileName = path.substringAfterLast("/")
                    val ext = fileName.substringAfterLast(".", "")
                    val language = com.titan.core.common.util.FileExtensions.getLanguageFromExtension(ext) ?: "text"
                    val tab = com.titan.domain.model.EditorTab(
                        filePath = path, fileName = fileName, content = content, language = language,
                    )
                    val newTabs = state.value.openTabs + tab
                    setState {
                        copy(
                            openTabs = newTabs, activeTabId = tab.id,
                            activeContent = content, activeLanguage = language,
                            isLoading = false, isModified = false,
                        )
                    }
                }
                result.isError -> {
                    setState { copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                    sendAction(EditorAction.ShowError("Failed to open file: ${result.exceptionOrNull()?.message}"))
                }
            }
        }
    }

    private fun loadFileTree(rootPath: String) {
        viewModelScope.launch {
            val result = fileRepository.getFileTree(rootPath)
            if (result.isSuccess) {
                val tree = result.getOrNull() ?: emptyList()
                val defaultExpanded = setOf(rootPath) + tree.filter { it.isDirectory }.take(3).map { it.path }
                setState { copy(fileTree = tree, expandedDirectories = defaultExpanded) }
            }
        }
    }

    override fun onEvent(event: EditorEvent) {
        when (event) {
            is EditorEvent.OpenFile -> openFile(event.path)
            is EditorEvent.CloseTab -> closeTab(event.tabId)
            is EditorEvent.SwitchTab -> switchTab(event.tabId)
            is EditorEvent.TogglePinTab -> togglePinTab(event.tabId)
            is EditorEvent.UpdateContent -> updateContent(event.content)
            is EditorEvent.UpdateCursorPosition -> setState { copy(cursorLine = event.line, cursorColumn = event.column) }
            is EditorEvent.ToggleDirectoryExpand -> toggleDirectory(event.path)
            is EditorEvent.CreateNewFile -> { }
            is EditorEvent.CreateNewDirectory -> { }
            is EditorEvent.Undo -> { }
            is EditorEvent.Redo -> { }
            is EditorEvent.SaveFile -> saveCurrentFile()
            is EditorEvent.ToggleWordWrap -> setState { copy(wordWrap = !wordWrap) }
            is EditorEvent.ToggleLineNumbers -> setState { copy(lineNumbers = !lineNumbers) }
            is EditorEvent.FindQueryChanged -> performSearch(event.query)
            is EditorEvent.ReplaceQueryChanged -> setState { copy(replaceQuery = event.query) }
            is EditorEvent.FindNext -> { }
            is EditorEvent.FindPrevious -> { }
            is EditorEvent.ReplaceNext -> { }
            is EditorEvent.ReplaceAll -> { }
        }
    }

    private fun switchTab(tabId: String) {
        val tab = state.value.openTabs.find { it.id == tabId } ?: return
        setState {
            copy(
                activeTabId = tabId,
                activeContent = tab.content,
                activeLanguage = tab.language,
                isModified = tab.isModified,
                readOnly = tab.readOnly,
                cursorLine = tab.cursorLine,
                cursorColumn = tab.cursorColumn,
            )
        }
    }

    private fun closeTab(tabId: String) {
        val tabs = state.value.openTabs.filter { it.id != tabId }.toMutableList()
        if (state.value.activeTabId == tabId) {
            val newActive = tabs.lastOrNull()?.id ?: ""
            switchTab(newActive)
        }
        setState { copy(openTabs = tabs) }
    }

    private fun togglePinTab(tabId: String) {
        val tabs = state.value.openTabs.map {
            if (it.id == tabId) it.copy(isPinned = !it.isPinned) else it
        }
        setState { copy(openTabs = tabs) }
    }

    private fun updateContent(content: String) {
        setState { copy(activeContent = content, isModified = true) }
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(3000)
            saveCurrentFile()
        }
    }

    private fun saveCurrentFile() {
        val tab = state.value.openTabs.find { it.id == state.value.activeTabId } ?: return
        viewModelScope.launch {
            fileRepository.writeFile(tab.filePath, state.value.activeContent)
            setState { copy(isModified = false) }
            sendAction(EditorAction.ShowToast("File saved"))
        }
    }

    private fun toggleDirectory(path: String) {
        val expanded = state.value.expandedDirectories.toMutableSet()
        if (expanded.contains(path)) expanded.remove(path) else expanded.add(path)
        setState { copy(expandedDirectories = expanded) }
    }

    private fun performSearch(query: String) {
        setState { copy(findQuery = query) }
        if (query.isNotBlank()) {
            val content = state.value.activeContent
            val matches = mutableListOf<Int>()
            var index = 0
            while (true) {
                val found = content.indexOf(query, index, ignoreCase = true)
                if (found == -1) break
                matches.add(found)
                index = found + 1
            }
            setState { copy(matchCount = matches.size, currentMatchIndex = if (matches.isNotEmpty()) 0 else -1) }
        } else {
            setState { copy(matchCount = 0, currentMatchIndex = -1) }
        }
    }

    fun saveFile() = onEvent(EditorEvent.SaveFile)
    fun undo() = onEvent(EditorEvent.Undo)
    fun redo() = onEvent(EditorEvent.Redo)
    fun updateContent(content: String) = onEvent(EditorEvent.UpdateContent(content))
    fun updateCursorPosition(line: Int, column: Int) = onEvent(EditorEvent.UpdateCursorPosition(line, column))
    fun switchTab(id: String) = onEvent(EditorEvent.SwitchTab(id))
    fun closeTab(id: String) = onEvent(EditorEvent.CloseTab(id))
    fun togglePinTab(id: String) = onEvent(EditorEvent.TogglePinTab(id))
    fun toggleDirectoryExpand(path: String) = onEvent(EditorEvent.ToggleDirectoryExpand(path))
    fun createNewFile(parentPath: String) = onEvent(EditorEvent.CreateNewFile(parentPath))
    fun createNewDirectory(parentPath: String) = onEvent(EditorEvent.CreateNewDirectory(parentPath))
    fun updateFindQuery(query: String) = onEvent(EditorEvent.FindQueryChanged(query))
    fun updateReplaceQuery(query: String) = onEvent(EditorEvent.ReplaceQueryChanged(query))
    fun findNext() = onEvent(EditorEvent.FindNext)
    fun findPrevious() = onEvent(EditorEvent.FindPrevious)
    fun replaceNext() = onEvent(EditorEvent.ReplaceNext)
    fun replaceAll() = onEvent(EditorEvent.ReplaceAll)
    fun toggleWordWrap() = onEvent(EditorEvent.ToggleWordWrap)
    fun toggleLineNumbers() = onEvent(EditorEvent.ToggleLineNumbers)
}
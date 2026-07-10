package com.titan.feature.filemanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titan.domain.model.FileNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    onNavigateBack: () -> Unit,
    onFileSelected: (String) -> Unit,
    viewModel: FileManagerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadDefaultPath() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateUp() }) {
                        Icon(if (state.canNavigateUp) Icons.Default.ArrowBack else Icons.Default.Menu, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = { viewModel.toggleHiddenFiles() }) { Icon(Icons.Default.VisibilityOff, contentDescription = "Toggle hidden") }
                    IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
                    IconButton(onClick = { viewModel.showSortOptions() }) { Icon(Icons.Default.Sort, contentDescription = "Sort") }
                },
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(onClick = { viewModel.showCreateFileDialog() }) { Icon(Icons.Default.NoteAdd, contentDescription = "New File") }
                SmallFloatingActionButton(onClick = { viewModel.showCreateDirectoryDialog() }) { Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder") }
                FloatingActionButton(onClick = { viewModel.showMoreOptions() }) { Icon(Icons.Default.Add, contentDescription = "More") }
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (showSearch) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder = { Text("Search files...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true,
                    trailingIcon = { if (state.searchQuery.isNotBlank()) IconButton(onClick = { viewModel.search("") }) { Icon(Icons.Default.Close, contentDescription = "Clear") } },
                )
            }
            Text(
                state.currentPath,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(state.files, key = { it.path }) { file ->
                        FileItemRow(
                            file = file,
                            onClick = {
                                if (file.isDirectory) viewModel.navigateTo(file.path)
                                else onFileSelected(file.path)
                            },
                            onLongClick = { viewModel.showFileContextMenu(file) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItemRow(file: FileNode, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!file.isDirectory) {
                    Text("${file.formattedSize} - ${file.formattedDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data class FileManagerUiState(
    val files: List<FileNode> = emptyList(),
    val currentPath: String = "",
    val canNavigateUp: Boolean = false,
    val showHiddenFiles: Boolean = false,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val bookmarks: List<String> = emptyList(),
) : com.titan.core.common.base.UiState

sealed class FileManagerEvent : com.titan.core.common.base.UiEvent {
    data class NavigateTo(val path: String) : FileManagerEvent()
    data object NavigateUp : FileManagerEvent()
    data class Search(val query: String) : FileManagerEvent()
    data object ToggleHiddenFiles : FileManagerEvent()
    data object Refresh : FileManagerEvent()
    data class ShowFileContextMenu(val file: FileNode) : FileManagerEvent()
    data object ShowCreateFileDialog : FileManagerEvent()
    data object ShowCreateDirectoryDialog : FileManagerEvent()
    data object ShowSortOptions : FileManagerEvent()
    data object ShowMoreOptions : FileManagerEvent()
    data object LoadDefaultPath : FileManagerEvent()
}

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val fileRepository: com.titan.domain.repository.FileRepository,
) : com.titan.core.common.base.BaseViewModel<FileManagerUiState, FileManagerEvent, com.titan.core.common.base.UiAction>() {

    private val pathHistory = mutableListOf<String>()

    override fun initialState() = FileManagerUiState()

    fun loadDefaultPath() {
        viewModelScope.launch {
            val externalStorage = android.os.Environment.getExternalStorageDirectory().absolutePath
            navigateTo(externalStorage, addToHistory = false)
        }
    }

    override fun onEvent(event: FileManagerEvent) {
        when (event) {
            is FileManagerEvent.NavigateTo -> navigateTo(event.path)
            is FileManagerEvent.NavigateUp -> navigateUp()
            is FileManagerEvent.Search -> setState { copy(searchQuery = event.query) }
            is FileManagerEvent.ToggleHiddenFiles -> {
                val newShow = !state.value.showHiddenFiles
                setState { copy(showHiddenFiles = newShow) }
                loadDirectory(state.value.currentPath)
            }
            is FileManagerEvent.Refresh -> loadDirectory(state.value.currentPath)
            is FileManagerEvent.ShowFileContextMenu -> { }
            is FileManagerEvent.ShowCreateFileDialog -> { }
            is FileManagerEvent.ShowCreateDirectoryDialog -> { }
            is FileManagerEvent.ShowSortOptions -> { }
            is FileManagerEvent.ShowMoreOptions -> { }
            is FileManagerEvent.LoadDefaultPath -> loadDefaultPath()
        }
    }

    private fun navigateTo(path: String, addToHistory: Boolean = true) {
        if (addToHistory && state.value.currentPath.isNotBlank()) {
            pathHistory.add(state.value.currentPath)
        }
        setState { copy(currentPath = path, canNavigateUp = pathHistory.isNotEmpty()) }
        loadDirectory(path)
    }

    fun navigateUp() {
        if (pathHistory.isNotEmpty()) {
            val parent = pathHistory.removeAt(pathHistory.lastIndex)
            navigateTo(parent, addToHistory = false)
        } else {
            val parent = java.io.File(state.value.currentPath).parentFile?.absolutePath ?: return
            navigateTo(parent, addToHistory = false)
        }
    }

    private fun loadDirectory(path: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = fileRepository.getFileTree(path, state.value.showHiddenFiles)
            if (result.isSuccess) {
                val sorted = (result.getOrNull() ?: emptyList()).sortedWith(compareByDescending<FileNode> { it.isDirectory }.thenBy { it.name.lowercase() })
                setState { copy(files = sorted, isLoading = false) }
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    fun navigateTo(path: String) = onEvent(FileManagerEvent.NavigateTo(path))
    fun toggleHiddenFiles() = onEvent(FileManagerEvent.ToggleHiddenFiles)
    fun refresh() = onEvent(FileManagerEvent.Refresh)
    fun search(query: String) = onEvent(FileManagerEvent.Search(query))
    fun showSortOptions() = onEvent(FileManagerEvent.ShowSortOptions)
    fun showMoreOptions() = onEvent(FileManagerEvent.ShowMoreOptions)
    fun showCreateFileDialog() = onEvent(FileManagerEvent.ShowCreateFileDialog)
    fun showCreateDirectoryDialog() = onEvent(FileManagerEvent.ShowCreateDirectoryDialog)
    fun showFileContextMenu(file: FileNode) = onEvent(FileManagerEvent.ShowFileContextMenu(file))
}
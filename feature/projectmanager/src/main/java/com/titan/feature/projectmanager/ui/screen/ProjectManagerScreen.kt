package com.titan.feature.projectmanager.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.folder.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titan.domain.model.BuildSystem
import com.titan.domain.model.ProgrammingLanguage
import com.titan.domain.model.Project
import com.titan.domain.model.TemplateType
import com.titan.feature.projectmanager.ui.viewmodel.ProjectManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagerScreen(
    onNavigateBack: () -> Unit,
    onProjectSelected: (String) -> Unit,
    viewModel: ProjectManagerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, path, template, language, buildSystem, packageName, minSdk ->
                viewModel.createProject(
                    name = name,
                    path = path,
                    template = template,
                    language = language,
                    buildSystem = buildSystem,
                    packageName = packageName,
                    minSdk = minSdk,
                )
                showCreateDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Project Manager",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            if (state.isGridView) Icons.Default.ViewList
                            else Icons.Default.GridView,
                            contentDescription = "Toggle view",
                        )
                    }
                    IconButton(onClick = { viewModel.showImportOptions() }) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Import",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = { Text("New Project") },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                ProjectTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(tab.title) },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                            )
                        },
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.projects.isEmpty()) {
                EmptyProjectState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.projects,
                        key = { it.id },
                    ) { project ->
                        ProjectManagerCard(
                            project = project,
                            onClick = { onProjectSelected(project.id) },
                            onPin = { viewModel.togglePin(project.id) },
                            onDelete = { viewModel.deleteProject(project.id) },
                            onDuplicate = { viewModel.duplicateProject(project.id) },
                            onRename = { newName ->
                                viewModel.renameProject(project.id, newName)
                            },
                        )
                    }
                }
            }
        }
    }
}

enum class ProjectTab(val title: String, val icon: ImageVector) {
    ALL("All", Icons.Default.Folder),
    RECENT("Recent", Icons.Default.History),
    PINNED("Pinned", Icons.Default.PushPin),
}

@Composable
fun EmptyProjectState(onCreateClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.FolderOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No projects found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a new project to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Project")
            }
        }
    }
}

@Composable
fun ProjectManagerCard(
    project: Project,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(project.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank() && newName != project.name) {
                        onRename(newName)
                    }
                    showRenameDialog = false
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Project") },
            text = {
                Text(
                    "Are you sure you want to delete \"${project.name}\"? " +
                        "This action cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = when (project.templateType) {
                    TemplateType.COMPOSE, TemplateType.MVVM ->
                        MaterialTheme.colorScheme.tertiaryContainer
                    TemplateType.JAVA ->
                        MaterialTheme.colorScheme.secondaryContainer
                    else ->
                        MaterialTheme.colorScheme.primaryContainer
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (project.templateType) {
                            TemplateType.COMPOSE,
                            TemplateType.MVVM,
                            TemplateType.NAVIGATION -> Icons.Default.Brush

                            TemplateType.CLEAN_ARCHITECTURE -> Icons.Default.AccountTree
                            TemplateType.ROOM -> Icons.Default.Storage
                            TemplateType.RETROFIT -> Icons.Default.Cloud
                            TemplateType.HILT -> Icons.Default.Hardware
                            else -> Icons.Default.Android
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (project.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = project.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = project.templateType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = project.language.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = project.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = {
                            onClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (project.isPinned) "Unpin" else "Pin",
                            )
                        },
                        onClick = {
                            onPin()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (project.isPinned) Icons.Default.PushPin
                                else Icons.Outlined.PushPin,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showRenameDialog = true
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            onDuplicate()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showDeleteDialog = true
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        path: String,
        template: TemplateType,
        language: ProgrammingLanguage,
        buildSystem: BuildSystem,
        packageName: String,
        minSdk: Int,
    ) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(TemplateType.COMPOSE) }
    var selectedLanguage by remember { mutableStateOf(ProgrammingLanguage.KOTLIN) }
    var selectedBuildSystem by remember { mutableStateOf(BuildSystem.GRADLE_KTS) }
    var packageName by remember { mutableStateOf("") }
    var minSdk by remember { mutableStateOf("29") }
    var expandedTemplate by remember { mutableStateOf(false) }
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedBuildSystem by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Project",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "Browse",
                            )
                        }
                    },
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTemplate,
                    onExpandedChange = { expandedTemplate = it },
                ) {
                    OutlinedTextField(
                        value = selectedTemplate.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Template") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedTemplate,
                            )
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTemplate,
                        onDismissRequest = { expandedTemplate = false },
                    ) {
                        TemplateType.entries.forEach { template ->
                            DropdownMenuItem(
                                text = { Text(template.displayName) },
                                onClick = {
                                    selectedTemplate = template
                                    expandedTemplate = false
                                },
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedLanguage,
                    onExpandedChange = { expandedLanguage = it },
                ) {
                    OutlinedTextField(
                        value = selectedLanguage.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Language") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedLanguage,
                            )
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLanguage,
                        onDismissRequest = { expandedLanguage = false },
                    ) {
                        ProgrammingLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    selectedLanguage = lang
                                    expandedLanguage = false
                                },
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedBuildSystem,
                    onExpandedChange = { expandedBuildSystem = it },
                ) {
                    OutlinedTextField(
                        value = selectedBuildSystem.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Build System") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedBuildSystem,
                            )
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBuildSystem,
                        onDismissRequest = { expandedBuildSystem = false },
                    ) {
                        BuildSystem.entries.forEach { system ->
                            DropdownMenuItem(
                                text = { Text(system.displayName) },
                                onClick = {
                                    selectedBuildSystem = system
                                    expandedBuildSystem = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("com.example.myproject")
                    },
                )
                OutlinedTextField(
                    value = minSdk,
                    onValueChange = { newValue ->
                        minSdk = newValue.filter { c -> c.isDigit() }
                    },
                    label = { Text("Min SDK") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && path.isNotBlank()) {
                        onCreate(
                            name,
                            path,
                            selectedTemplate,
                            selectedLanguage,
                            selectedBuildSystem,
                            packageName,
                            minSdk.toIntOrNull() ?: 29,
                        )
                    }
                },
                enabled = name.isNotBlank() && path.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier.fillMaxWidth(0.95f),
    )
}
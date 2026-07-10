package com.titan.feature.editor.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titan.domain.model.EditorTab
import com.titan.ide.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: String = "",
    filePath: String = "",
    onNavigateBack: () -> Unit = {},
    onNavigateToTerminal: () -> Unit = {},
    onNavigateToBuild: () -> Unit = {},
    onNavigateToAI: () -> Unit = {},
    onNavigateToGit: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onFileSelected: (String) -> Unit = {},
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFindReplace by remember { mutableStateOf(false) }
    var showFileTree by remember { mutableStateOf(true) }

    LaunchedEffect(projectId) {
        if (projectId.isNotBlank()) viewModel.loadProject(projectId)
    }

    LaunchedEffect(filePath) {
        if (filePath.isNotBlank()) viewModel.openFile(filePath)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (showFileTree) {
            FileTreeSidePanel(
                state = state,
                onFileClick = { viewModel.openFile(it) },
                onFileLongClick = {},
                onToggleExpand = { viewModel.toggleDirectoryExpand(it) },
                onCreateFile = { viewModel.createNewFile(it) },
                onCreateDirectory = { viewModel.createNewDirectory(it) },
                onClose = { showFileTree = false },
            )
            HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
        }

        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            EditorTabBar(
                tabs = state.openTabs,
                activeTabId = state.activeTabId,
                onTabSelected = { viewModel.switchTab(it) },
                onTabClosed = { viewModel.closeTab(it) },
                onTabPinToggle = { viewModel.togglePinTab(it) },
            )

            if (showFindReplace) {
                FindReplaceBar(
                    query = state.findQuery,
                    replaceQuery = state.replaceQuery,
                    onQueryChanged = { viewModel.updateFindQuery(it) },
                    onReplaceQueryChanged = { viewModel.updateReplaceQuery(it) },
                    onFindNext = { viewModel.findNext() },
                    onFindPrevious = { viewModel.findPrevious() },
                    onReplace = { viewModel.replaceNext() },
                    onReplaceAll = { viewModel.replaceAll() },
                    onClose = { showFindReplace = false },
                    matchCount = state.matchCount,
                    currentIndex = state.currentMatchIndex,
                )
            }

            TopAppBar(
                title = {
                    Column {
                        val activeTab = state.openTabs.find { it.id == state.activeTabId }
                        Text(
                            text = activeTab?.displayName ?: "Titan IDE Editor",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        BreadcrumbNavigation(
                            path = activeTab?.filePath ?: "",
                            onSegmentClick = {},
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showFileTree = !showFileTree }) {
                        Icon(if (showFileTree) Icons.Default.ViewSidebar else Icons.Outlined.ViewSidebar, contentDescription = "File Tree")
                    }
                    IconButton(onClick = { viewModel.undo() }) { Icon(Icons.Default.Undo, contentDescription = "Undo") }
                    IconButton(onClick = { viewModel.redo() }) { Icon(Icons.Default.Redo, contentDescription = "Redo") }
                    IconButton(onClick = { viewModel.saveFile() }) { Icon(Icons.Default.Save, contentDescription = "Save") }
                    IconButton(onClick = { showFindReplace = !showFindReplace }) { Icon(Icons.Default.Search, contentDescription = "Find & Replace") }
                    IconButton(onClick = onNavigateToAI) { Icon(Icons.Default.SmartToy, contentDescription = "AI") }
                    IconButton(onClick = onNavigateToTerminal) { Icon(Icons.Default.Terminal, contentDescription = "Terminal") }
                    IconButton(onClick = onNavigateToBuild) { Icon(Icons.Default.PlayArrow, contentDescription = "Build") }
                },
            )

            CodeEditorArea(
                content = state.activeContent,
                language = state.activeLanguage,
                fontSize = state.fontSize,
                wordWrap = state.wordWrap,
                lineNumbers = state.lineNumbers,
                bracketMatching = state.bracketMatching,
                codeFolding = state.codeFolding,
                isModified = state.isModified,
                onContentChanged = { viewModel.updateContent(it) },
                onCursorChanged = { line, column -> viewModel.updateCursorPosition(line, column) },
                modifier = Modifier.weight(1f),
            )

            EditorStatusBar(
                line = state.cursorLine,
                column = state.cursorColumn,
                language = state.activeLanguage,
                encoding = "UTF-8",
                tabSize = state.tabSize,
                lineEnding = "LF",
                isModified = state.isModified,
                readOnly = state.readOnly,
                onToggleWordWrap = { viewModel.toggleWordWrap() },
                onToggleLineNumbers = { viewModel.toggleLineNumbers() },
            )
        }
    }
}

@Composable
fun EditorTabBar(
    tabs: List<EditorTab>,
    activeTabId: String,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onTabPinToggle: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        ) {
            tabs.forEach { tab ->
                val isActive = tab.id == activeTabId
                Surface(
                    modifier = Modifier.widthIn(min = 120.dp, max = 200.dp).clickable { onTabSelected(tab.id) },
                    color = if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent,
                    contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            if (tab.isPinned) Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(12.dp))
                            Text(tab.fileName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
                            if (tab.isModified) Text("*", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { onTabClosed(tab.id) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbNavigation(path: String, onSegmentClick: (String) -> Unit) {
    if (path.isBlank()) return
    val segments = path.split("/").filter { it.isNotBlank() }
    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        segments.forEachIndexed { index, segment ->
            if (index > 0) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                segment,
                style = MaterialTheme.typography.labelSmall,
                color = if (index == segments.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.clickable { onSegmentClick(segment) },
            )
        }
    }
}

@Composable
fun CodeEditorArea(
    content: String,
    language: String,
    fontSize: Int,
    wordWrap: Boolean,
    lineNumbers: Boolean,
    bracketMatching: Boolean,
    codeFolding: Boolean,
    isModified: Boolean,
    onContentChanged: (String) -> Unit,
    onCursorChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = content.lines()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFFFFFFF),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
        ) {
            if (lineNumbers) {
                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    lines.forEachIndexed { index, _ ->
                        Text(
                            "${index + 1}",
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF858585),
                            lineHeight = (fontSize + 6).sp,
                        )
                    }
                    if (lines.isEmpty() || lines.size == 1 && lines[0].isEmpty()) {
                        Text("1", fontSize = fontSize.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF858585))
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFF3C3C3C))
            }
            Column(modifier = Modifier.weight(1f)) {
                lines.forEachIndexed { _, line ->
                    Text(
                        text = line,
                        fontSize = fontSize.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSystemInDarkTheme()) Color(0xFFD4D4D4) else Color(0xFF1E1E1E),
                        lineHeight = (fontSize + 6).sp,
                        softWrap = wordWrap,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun FindReplaceBar(
    query: String,
    replaceQuery: String,
    onQueryChanged: (String) -> Unit,
    onReplaceQueryChanged: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    matchCount: Int,
    currentIndex: Int,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = query, onValueChange = onQueryChanged,
                    placeholder = { Text("Find") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(36.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                if (matchCount > 0) {
                    Text("${currentIndex + 1}/$matchCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onFindPrevious, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onFindNext, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = replaceQuery, onValueChange = onReplaceQueryChanged,
                    placeholder = { Text("Replace") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(36.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = onReplace, modifier = Modifier.height(32.dp)) { Text("Replace", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = onReplaceAll, modifier = Modifier.height(32.dp)) { Text("All", style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    line: Int,
    column: Int,
    language: String,
    encoding: String,
    tabSize: Int,
    lineEnding: String,
    isModified: Boolean,
    readOnly: Boolean,
    onToggleWordWrap: () -> Unit,
    onToggleLineNumbers: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ln $line, Col $column", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
            Text(language.ifEmpty { "Plain Text" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(encoding, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Spaces: $tabSize", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(lineEnding, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isModified) Text("Modified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            if (readOnly) Text("Read Only", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FileTreeSidePanel(
    state: EditorUiState,
    onFileClick: (String) -> Unit,
    onFileLongClick: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateDirectory: (String) -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(280.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Files", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { onCreateFile(state.projectRoot) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.NoteAdd, contentDescription = "New File", modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = { onCreateDirectory(state.projectRoot) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp)) }
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 4.dp),
            ) {
                state.fileTree.forEach { node ->
                    FileTreeItem(
                        node = node,
                        depth = 0,
                        expandedDirs = state.expandedDirectories,
                        onFileClick = onFileClick,
                        onToggleExpand = onToggleExpand,
                    )
                }
            }
        }
    }
}

@Composable
fun FileTreeItem(
    node: com.titan.domain.model.FileNode,
    depth: Int,
    expandedDirs: Set<String>,
    onFileClick: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
) {
    val isExpanded = expandedDirs.contains(node.path)
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable {
                if (node.isDirectory) onToggleExpand(node.path)
                else onFileClick(node.path)
            }
            .padding(start = (depth * 16 + 8).dp, top = 2.dp, bottom = 2.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (node.isDirectory) (if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight) else Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = when {
                node.isDirectory -> MaterialTheme.colorScheme.primary
                node.extension in listOf("kt", "kts") -> SyntaxKeyword
                node.extension in listOf("java") -> SyntaxAnnotation
                node.extension in listOf("xml") -> SyntaxTag
                node.extension in listOf("gradle", "gradle.kts") -> SyntaxFunction
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(node.name, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
    if (node.isDirectory && isExpanded) {
        node.children.forEach { child ->
            FileTreeItem(child, depth + 1, expandedDirs, onFileClick, onToggleExpand)
        }
    }
}

@Composable
private fun isSystemInDarkTheme(): Boolean {
    return true // Will be replaced by actual theme detection
}
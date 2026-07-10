package com.titan.feature.terminal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var commandInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.startSession() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.createNewSession() }) { Icon(Icons.Default.Add, contentDescription = "New Session") }
                    IconButton(onClick = { viewModel.clearScreen() }) { Icon(Icons.Default.DeleteSweep, contentDescription = "Clear") }
                    IconButton(onClick = { viewModel.toggleFullscreen() }) { Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen") }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFF1E1E1E)),
        ) {
            if (state.sessions.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = state.activeSessionIndex,
                    containerColor = Color(0xFF2D2D2D),
                    contentColor = Color(0xFFD4D4D4),
                    edgePadding = 8.dp,
                ) {
                    state.sessions.forEachIndexed { index, session ->
                        Tab(
                            selected = state.activeSessionIndex == index,
                            onClick = { viewModel.switchSession(index) },
                            text = { Text(session.title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selectedContentColor = Color(0xFF569CD6),
                            unselectedContentColor = Color(0xFF808080),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(8.dp),
            ) {
                state.outputLines.forEach { line ->
                    Text(
                        text = line,
                        color = when {
                            line.contains("error", ignoreCase = true) || line.contains("ERROR") -> Color(0xFFF44747)
                            line.contains("warning", ignoreCase = true) || line.contains("WARNING") -> Color(0xFFCCA700)
                            line.contains("success", ignoreCase = true) -> Color(0xFF6A9955)
                            line.startsWith("$ ") || line.startsWith("# ") -> Color(0xFF569CD6)
                            else -> Color(0xFFD4D4D4)
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        softWrap = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${state.workingDirectory.substringAfterLast('/')}$ ",
                    color = Color(0xFF569CD6),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                )
                Text(
                    text = commandInput,
                    color = Color(0xFFD4D4D4),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFD4D4D4),
                    unfocusedTextColor = Color(0xFFD4D4D4),
                    cursorColor = Color(0xFF569CD6),
                    focusedBorderColor = Color(0xFF569CD6),
                    unfocusedBorderColor = Color(0xFF3C3C3C),
                    backgroundColor = Color(0xFF1E1E1E),
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                placeholder = { Text("Enter command...", color = Color(0xFF6A6A6A), fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
                singleLine = false,
                maxLines = 3,
                trailingIcon = {
                    IconButton(onClick = {
                        if (commandInput.isNotBlank()) {
                            viewModel.executeCommand(commandInput)
                            commandInput = ""
                        }
                    }) { Icon(Icons.Default.Send, contentDescription = "Execute", tint = Color(0xFF569CD6)) }
                },
            )
        }
    }
}

data class TerminalUiState(
    val outputLines: List<String> = listOf("Welcome to Titan IDE Terminal", "Type 'help' for available commands.", ""),
    val workingDirectory: String = "/sdcard",
    val sessions: List<com.titan.domain.model.TerminalSession> = emptyList(),
    val activeSessionIndex: Int = 0,
    val isRunning: Boolean = false,
    val commandHistory: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val isFullscreen: Boolean = false,
) : com.titan.core.common.base.UiState

sealed class TerminalEvent : com.titan.core.common.base.UiEvent {
    data class ExecuteCommand(val command: String) : TerminalEvent()
    data object ClearScreen : TerminalEvent()
    data object CreateNewSession : TerminalEvent()
    data class SwitchSession(val index: Int) : TerminalEvent()
    data object ToggleFullscreen : TerminalEvent()
    data object StartSession : TerminalEvent()
}

@HiltViewModel
class TerminalViewModel @Inject constructor() : com.titan.core.common.base.BaseViewModel<TerminalUiState, TerminalEvent, com.titan.core.common.base.UiAction>() {

    override fun initialState() = TerminalUiState()

    override fun onEvent(event: TerminalEvent) {
        when (event) {
            is TerminalEvent.ExecuteCommand -> executeCommand(event.command)
            is TerminalEvent.ClearScreen -> setState { copy(outputLines = listOf("Terminal cleared.", "")) }
            is TerminalEvent.CreateNewSession -> {
                val session = com.titan.domain.model.TerminalSession(title = "Terminal ${state.value.sessions.size + 1}")
                setState { copy(sessions = sessions + session, activeSessionIndex = sessions.size) }
            }
            is TerminalEvent.SwitchSession -> setState { copy(activeSessionIndex = event.index) }
            is TerminalEvent.ToggleFullscreen -> setState { copy(isFullscreen = !isFullscreen) }
            is TerminalEvent.StartSession -> {
                val defaultSession = com.titan.domain.model.TerminalSession(title = "Terminal 1")
                setState { copy(sessions = listOf(defaultSession)) }
            }
        }
    }

    private fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return
        val newLines = state.value.outputLines.toMutableList()
        newLines.add("${state.value.workingDirectory.substringAfterLast('/')}$ $trimmed")
        when (trimmed.lowercase()) {
            "clear", "cls" -> { newLines.clear(); newLines.add("") }
            "pwd" -> newLines.add(state.value.workingDirectory)
            "whoami" -> newLines.add("titan_user")
            "date" -> newLines.add(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
            "uname", "uname -a" -> newLines.add("Titan IDE Terminal - Android Linux")
            "help" -> newLines.addAll(listOf(
                "Available commands:",
                "  clear/cls  - Clear the screen",
                "  pwd        - Print working directory",
                "  ls         - List files",
                "  cd <dir>   - Change directory",
                "  cat <file> - Display file contents",
                "  echo <text>- Print text",
                "  date       - Show current date/time",
                "  whoami     - Show current user",
                "  help       - Show this help message",
                "  git        - Git commands",
                "  gradle     - Gradle commands",
                "  java       - Java version",
                "  python     - Python REPL",
                "  exit       - Close terminal",
            ))
            "ls" -> {
                val dir = java.io.File(state.value.workingDirectory)
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.sorted()?.forEach { file ->
                        val prefix = if (file.isDirectory) "d " else "f "
                        val size = if (file.isFile) " (${com.titan.domain.model.Project.formatFileSize(file.length())})" else ""
                        newLines.add("$prefix${file.name}$size")
                    } ?: newLines.add("Directory is empty or not accessible")
                } else newLines.add("Directory not found")
            }
            "java", "java -version" -> newLines.add("java version \"17.0.9\" 2023-10-17 LTS")
            "gradle", "gradle -v" -> newLines.add("Gradle 8.10")
            else -> {
                newLines.add("Command executed: $trimmed")
            }
        }
        newLines.add("")
        setState { copy(outputLines = newLines) }
    }

    fun startSession() = onEvent(TerminalEvent.StartSession)
    fun executeCommand(command: String) = onEvent(TerminalEvent.ExecuteCommand(command))
    fun clearScreen() = onEvent(TerminalEvent.ClearScreen)
    fun createNewSession() = onEvent(TerminalEvent.CreateNewSession)
    fun switchSession(index: Int) = onEvent(TerminalEvent.SwitchSession(index))
    fun toggleFullscreen() = onEvent(TerminalEvent.ToggleFullscreen)
}
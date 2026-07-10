package com.titan.feature.settings.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SettingsCategoryHeader("Editor")
            SettingsSliderItem("Font Size", "${state.fontSize}sp", state.fontSize.toFloat(), 8f, 32f) { viewModel.setFontSize(it.toInt()) }
            SettingsDropdownItem("Font Family", state.fontFamily, listOf("JetBrains Mono", "Fira Code", "Source Code Pro", "Inconsolata", "Monospace")) { viewModel.setFontFamily(it) }
            SettingsSliderItem("Tab Size", "${state.tabSize} spaces", state.tabSize.toFloat(), 2f, 8f) { viewModel.setTabSize(it.toInt()) }
            SettingsSwitchItem("Word Wrap", state.wordWrap) { viewModel.setWordWrap(it) }
            SettingsSwitchItem("Line Numbers", state.lineNumbers) { viewModel.setLineNumbers(it) }
            SettingsSwitchItem("Minimap", state.minimap) { viewModel.setMinimap(it) }
            SettingsSwitchItem("Bracket Matching", state.bracketMatching) { viewModel.setBracketMatching(it) }
            SettingsSwitchItem("Code Folding", state.codeFolding) { viewModel.setCodeFolding(it) }
            SettingsSwitchItem("Auto Save", state.autoSave) { viewModel.setAutoSave(it) }
            SettingsSwitchItem("Vim Mode", state.vimMode) { viewModel.setVimMode(it) }
            SettingsSwitchItem("AI Suggestions", state.suggestionsOn) { viewModel.setSuggestionsOn(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("Appearance")
            SettingsDropdownItem("Theme", state.themeMode, listOf("System", "Light", "Dark", "AMOLED")) { viewModel.setThemeMode(it) }
            SettingsSwitchItem("Dynamic Colors", state.dynamicColors) { viewModel.setDynamicColors(it) }
            SettingsDropdownItem("Language", state.language, listOf("English", "Spanish", "French", "German", "Japanese", "Chinese", "Hindi", "Korean")) { viewModel.setLanguage(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("Terminal")
            SettingsDropdownItem("Shell", state.terminalShell, listOf("/system/bin/sh", "bash", "zsh", "fish")) { viewModel.setTerminalShell(it) }
            SettingsSliderItem("Terminal Font Size", "${state.terminalFontSize}sp", state.terminalFontSize.toFloat(), 8f, 24f) { viewModel.setTerminalFontSize(it.toInt()) }
            SettingsDropdownItem("Terminal Theme", state.terminalTheme, listOf("Dark", "Light", "Solarized Dark", "Dracula", "Monokai")) { viewModel.setTerminalTheme(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("AI Providers")
            SettingsNavigationItem("Configure AI Providers", "Manage API keys, models, and providers", Icons.Default.SmartToy) { }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("Git")
            SettingsTextItem("User Name", state.gitUserName, Icons.Default.Person) { viewModel.setGitUserName(it) }
            SettingsTextItem("User Email", state.gitUserEmail, Icons.Default.Email) { viewModel.setGitUserEmail(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("Security")
            SettingsSwitchItem("Biometric Lock", state.biometricLock) { viewModel.setBiometricLock(it) }
            SettingsSwitchItem("Crash Reporting", state.crashReporting) { viewModel.setCrashReporting(it) }
            SettingsSwitchItem("Analytics", state.analytics) { viewModel.setAnalytics(it) }
            SettingsSwitchItem("Experimental Features", state.experimentalFeatures) { viewModel.setExperimentalFeatures(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("Data")
            SettingsNavigationItem("Backup Data", "Export settings and data", Icons.Default.Backup) { }
            SettingsNavigationItem("Restore Data", "Import settings and data", Icons.Default.Restore) { }
            SettingsNavigationItem("Clear Cache", "Clear temporary files and cache", Icons.Default.CleaningServices) { }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategoryHeader("About")
            SettingsInfoItem("Version", "1.0.0")
            SettingsInfoItem("Build", "2024.07.10")
            SettingsInfoItem("Android SDK", "35")
            SettingsInfoItem("Min SDK", "29 (Android 10)")
            SettingsInfoItem("Kotlin", "1.9.24")
            SettingsNavigationItem("Open Source Licenses", "View library licenses", Icons.Default.Description) { }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
}

@Composable
fun SettingsSwitchItem(title: String, value: Boolean, onValueChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onValueChanged(!value) }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge) }
        Switch(checked = value, onCheckedChange = onValueChanged)
    }
}

@Composable
fun SettingsSliderItem(title: String, subtitle: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = min..max, steps = ((max - min).toInt() - 1))
    }
}

@Composable
fun SettingsDropdownItem(title: String, currentValue: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(currentValue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(option)
                        if (option == currentValue) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                onClick = { onOptionSelected(option); expanded = false },
            )
        }
    }
}

@Composable
fun SettingsTextItem(title: String, value: String, icon: ImageVector, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(value) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(icon, contentDescription = null) },
                )
            },
            confirmButton = { TextButton(onClick = { onValueChange(editValue); showDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { showDialog = true; editValue = value }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(value.ifEmpty { "Not set" }, style = MaterialTheme.typography.bodySmall, color = if (value.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsNavigationItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

data class SettingsUiState(
    val fontSize: Int = 14,
    val fontFamily: String = "JetBrains Mono",
    val tabSize: Int = 4,
    val wordWrap: Boolean = false,
    val lineNumbers: Boolean = true,
    val minimap: Boolean = true,
    val bracketMatching: Boolean = true,
    val codeFolding: Boolean = true,
    val autoSave: Boolean = true,
    val vimMode: Boolean = false,
    val suggestionsOn: Boolean = true,
    val themeMode: String = "System",
    val dynamicColors: Boolean = true,
    val language: String = "English",
    val terminalShell: String = "/system/bin/sh",
    val terminalFontSize: Int = 12,
    val terminalTheme: String = "Dark",
    val gitUserName: String = "",
    val gitUserEmail: String = "",
    val biometricLock: Boolean = false,
    val crashReporting: Boolean = true,
    val analytics: Boolean = true,
    val experimentalFeatures: Boolean = false,
) : com.titan.core.common.base.UiState

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: com.titan.core.datastore.UserPreferencesRepository,
) : com.titan.core.common.base.BaseViewModel<SettingsUiState, com.titan.core.common.base.UiEvent, com.titan.core.common.base.UiAction>() {

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userPreferencesRepository.fontSize,
                userPreferencesRepository.fontFamily,
                userPreferencesRepository.tabSize,
                userPreferencesRepository.wordWrap,
                userPreferencesRepository.lineNumbers,
                userPreferencesRepository.minimap,
                userPreferencesRepository.bracketMatching,
                userPreferencesRepository.codeFolding,
                userPreferencesRepository.autoSave,
                userPreferencesRepository.vimMode,
                userPreferencesRepository.suggestionsOn,
                userPreferencesRepository.themeMode,
                userPreferencesRepository.dynamicColors,
                userPreferencesRepository.language,
                userPreferencesRepository.terminalShell,
                userPreferencesRepository.terminalFontSize,
                userPreferencesRepository.terminalTheme,
                userPreferencesRepository.gitUserName,
                userPreferencesRepository.gitUserEmail,
                userPreferencesRepository.experimentalFeatures,
            ) { args -> SettingsUiState(
                fontSize = args[0] as Int,
                fontFamily = args[1] as String,
                tabSize = args[2] as Int,
                wordWrap = args[3] as Boolean,
                lineNumbers = args[4] as Boolean,
                minimap = args[5] as Boolean,
                bracketMatching = args[6] as Boolean,
                codeFolding = args[7] as Boolean,
                autoSave = args[8] as Boolean,
                vimMode = args[9] as Boolean,
                suggestionsOn = args[10] as Boolean,
                themeMode = (args[11] as String).replaceFirstChar { it.uppercase() },
                dynamicColors = args[12] as Boolean,
                language = (args[13] as String).replaceFirstChar { it.uppercase() },
                terminalShell = args[14] as String,
                terminalFontSize = args[15] as Int,
                terminalTheme = (args[16] as String).replaceFirstChar { it.uppercase() },
                gitUserName = args[17] as String,
                gitUserEmail = args[18] as String,
                experimentalFeatures = args[19] as Boolean,
            )}.collect { setState { it } }
        }
    }

    override fun initialState() = SettingsUiState()
    override fun onEvent(event: com.titan.core.common.base.UiEvent) {}

    fun setFontSize(size: Int) = viewModelScope.launch { userPreferencesRepository.setFontSize(size) }
    fun setFontFamily(family: String) = viewModelScope.launch { userPreferencesRepository.setFontFamily(family) }
    fun setTabSize(size: Int) = viewModelScope.launch { userPreferencesRepository.setTabSize(size) }
    fun setWordWrap(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setWordWrap(enabled) }
    fun setLineNumbers(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setLineNumbers(enabled) }
    fun setMinimap(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setMinimap(enabled) }
    fun setBracketMatching(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setBracketMatching(enabled) }
    fun setCodeFolding(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setCodeFolding(enabled) }
    fun setAutoSave(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setAutoSave(enabled) }
    fun setVimMode(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setVimMode(enabled) }
    fun setSuggestionsOn(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setSuggestionsOn(enabled) }
    fun setThemeMode(mode: String) = viewModelScope.launch { userPreferencesRepository.setThemeMode(mode.lowercase()) }
    fun setDynamicColors(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setDynamicColors(enabled) }
    fun setLanguage(lang: String) = viewModelScope.launch { userPreferencesRepository.setLanguage(lang.lowercase()) }
    fun setTerminalShell(shell: String) = viewModelScope.launch { userPreferencesRepository.setTerminalShell(shell) }
    fun setTerminalFontSize(size: Int) = viewModelScope.launch { userPreferencesRepository.setTerminalFontSize(size) }
    fun setTerminalTheme(theme: String) = viewModelScope.launch { userPreferencesRepository.setTerminalTheme(theme.lowercase()) }
    fun setGitUserName(name: String) = viewModelScope.launch { userPreferencesRepository.setGitUserName(name) }
    fun setGitUserEmail(email: String) = viewModelScope.launch { userPreferencesRepository.setGitUserEmail(email) }
    fun setBiometricLock(enabled: Boolean) { }
    fun setCrashReporting(enabled: Boolean) { }
    fun setAnalytics(enabled: Boolean) { }
    fun setExperimentalFeatures(enabled: Boolean) = viewModelScope.launch { userPreferencesRepository.setExperimentalFeatures(enabled) }
}
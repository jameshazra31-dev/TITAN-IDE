package com.titan.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<String> = dataStore.data.map { it[PreferencesKeys.THEME_MODE] ?: "system" }
    val dynamicColors: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.DYNAMIC_COLORS] ?: true }
    val fontSize: Flow<Int> = dataStore.data.map { it[PreferencesKeys.FONT_SIZE] ?: 14 }
    val fontFamily: Flow<String> = dataStore.data.map { it[PreferencesKeys.FONT_FAMILY] ?: "JetBrains Mono" }
    val tabSize: Flow<Int> = dataStore.data.map { it[PreferencesKeys.TAB_SIZE] ?: 4 }
    val wordWrap: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.WORD_WRAP] ?: false }
    val lineNumbers: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.LINE_NUMBERS] ?: true }
    val minimap: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.MINIMAP] ?: true }
    val autoSave: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.AUTO_SAVE] ?: true }
    val autoSaveInterval: Flow<Int> = dataStore.data.map { it[PreferencesKeys.AUTO_SAVE_INTERVAL] ?: 3000 }
    val bracketMatching: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.BRACKET_MATCHING] ?: true }
    val codeFolding: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.CODE_FOLDING] ?: true }
    val vimMode: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.VIM_MODE] ?: false }
    val suggestionsOn: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.SUGGESTIONS_ON] ?: true }
    val terminalShell: Flow<String> = dataStore.data.map { it[PreferencesKeys.TERMINAL_SHELL] ?: "/system/bin/sh" }
    val terminalFontSize: Flow<Int> = dataStore.data.map { it[PreferencesKeys.TERMINAL_FONT_SIZE] ?: 12 }
    val terminalTheme: Flow<String> = dataStore.data.map { it[PreferencesKeys.TERMINAL_THEME] ?: "dark" }
    val gitUserName: Flow<String> = dataStore.data.map { it[PreferencesKeys.GIT_USER_NAME] ?: "" }
    val gitUserEmail: Flow<String> = dataStore.data.map { it[PreferencesKeys.GIT_USER_EMAIL] ?: "" }
    val activeAIProviderId: Flow<String> = dataStore.data.map { it[PreferencesKeys.ACTIVE_AI_PROVIDER_ID] ?: "" }
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.FIRST_LAUNCH] ?: true }
    val experimentalFeatures: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.EXPERIMENTAL_FEATURES] ?: false }
    val language: Flow<String> = dataStore.data.map { it[PreferencesKeys.LANGUAGE] ?: "en" }
    val lastOpenedProjectId: Flow<String> = dataStore.data.map { it[PreferencesKeys.LAST_OPENED_PROJECT_ID] ?: "" }
    val sidebarPosition: Flow<String> = dataStore.data.map { it[PreferencesKeys.SIDEBAR_POSITION] ?: "left" }
    val consolePosition: Flow<String> = dataStore.data.map { it[PreferencesKeys.CONSOLE_POSITION] ?: "bottom" }

    suspend fun setThemeMode(mode: String) = edit { it[PreferencesKeys.THEME_MODE] = mode }
    suspend fun setDynamicColors(enabled: Boolean) = edit { it[PreferencesKeys.DYNAMIC_COLORS] = enabled }
    suspend fun setFontSize(size: Int) = edit { it[PreferencesKeys.FONT_SIZE] = size }
    suspend fun setFontFamily(family: String) = edit { it[PreferencesKeys.FONT_FAMILY] = family }
    suspend fun setTabSize(size: Int) = edit { it[PreferencesKeys.TAB_SIZE] = size }
    suspend fun setWordWrap(enabled: Boolean) = edit { it[PreferencesKeys.WORD_WRAP] = enabled }
    suspend fun setLineNumbers(enabled: Boolean) = edit { it[PreferencesKeys.LINE_NUMBERS] = enabled }
    suspend fun setMinimap(enabled: Boolean) = edit { it[PreferencesKeys.MINIMAP] = enabled }
    suspend fun setAutoSave(enabled: Boolean) = edit { it[PreferencesKeys.AUTO_SAVE] = enabled }
    suspend fun setAutoSaveInterval(interval: Int) = edit { it[PreferencesKeys.AUTO_SAVE_INTERVAL] = interval }
    suspend fun setBracketMatching(enabled: Boolean) = edit { it[PreferencesKeys.BRACKET_MATCHING] = enabled }
    suspend fun setCodeFolding(enabled: Boolean) = edit { it[PreferencesKeys.CODE_FOLDING] = enabled }
    suspend fun setVimMode(enabled: Boolean) = edit { it[PreferencesKeys.VIM_MODE] = enabled }
    suspend fun setSuggestionsOn(enabled: Boolean) = edit { it[PreferencesKeys.SUGGESTIONS_ON] = enabled }
    suspend fun setTerminalShell(shell: String) = edit { it[PreferencesKeys.TERMINAL_SHELL] = shell }
    suspend fun setTerminalFontSize(size: Int) = edit { it[PreferencesKeys.TERMINAL_FONT_SIZE] = size }
    suspend fun setTerminalTheme(theme: String) = edit { it[PreferencesKeys.TERMINAL_THEME] = theme }
    suspend fun setGitUserName(name: String) = edit { it[PreferencesKeys.GIT_USER_NAME] = name }
    suspend fun setGitUserEmail(email: String) = edit { it[PreferencesKeys.GIT_USER_EMAIL] = email }
    suspend fun setActiveAIProviderId(id: String) = edit { it[PreferencesKeys.ACTIVE_AI_PROVIDER_ID] = id }
    suspend fun setFirstLaunch(launched: Boolean) = edit { it[PreferencesKeys.FIRST_LAUNCH] = launched }
    suspend fun setExperimentalFeatures(enabled: Boolean) = edit { it[PreferencesKeys.EXPERIMENTAL_FEATURES] = enabled }
    suspend fun setLanguage(lang: String) = edit { it[PreferencesKeys.LANGUAGE] = lang }
    suspend fun setLastOpenedProjectId(id: String) = edit { it[PreferencesKeys.LAST_OPENED_PROJECT_ID] = id }
    suspend fun setSidebarPosition(position: String) = edit { it[PreferencesKeys.SIDEBAR_POSITION] = position }
    suspend fun setConsolePosition(position: String) = edit { it[PreferencesKeys.CONSOLE_POSITION] = position }

    private suspend fun edit(transform: suspend (MutablePreferences) -> Unit) {
        dataStore.edit(transform)
    }
}
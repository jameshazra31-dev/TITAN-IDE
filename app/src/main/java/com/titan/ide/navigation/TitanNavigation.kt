package com.titan.ide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.titan.ide.ui.screens.HomeScreen

object TitanRoutes {
    const val HOME = "home"
    const val PROJECT_MANAGER = "project_manager"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    const val EDITOR = "editor/{filePath}"
    const val FILE_MANAGER = "file_manager"
    const val TERMINAL = "terminal"
    const val AI_CHAT = "ai_chat"
    const val SETTINGS = "settings"
    const val GIT = "git"
    const val BUILD = "build"
    const val LOGCAT = "logcat"
    const val DEBUGGER = "debugger"
    const val APK_TOOLS = "apk_tools"
    const val XML_DESIGNER = "xml_designer"
    const val PLUGINS = "plugins"
    const val TOOLS = "tools"

    fun projectDetail(projectId: String) = "project_detail/$projectId"
    fun editor(filePath: String) = "editor/$filePath"
}

@Composable
fun TitanNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = TitanRoutes.HOME) {
        composable(TitanRoutes.HOME) {
            HomeScreen(
                onNavigateToProject = { projectId -> navController.navigate(TitanRoutes.projectDetail(projectId)) },
                onNavigateToSettings = { navController.navigate(TitanRoutes.SETTINGS) },
                onNavigateToAIChat = { navController.navigate(TitanRoutes.AI_CHAT) },
                onNavigateToPlugins = { navController.navigate(TitanRoutes.PLUGINS) },
            )
        }
        composable(TitanRoutes.PROJECT_MANAGER) {
            com.titan.feature.projectmanager.ui.screen.ProjectManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onProjectSelected = { projectId -> navController.navigate(TitanRoutes.projectDetail(projectId)) },
            )
        }
        composable(TitanRoutes.PROJECT_DETAIL) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            com.titan.feature.editor.ui.screen.EditorScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTerminal = { navController.navigate(TitanRoutes.TERMINAL) },
                onNavigateToBuild = { navController.navigate(TitanRoutes.BUILD) },
                onNavigateToAI = { navController.navigate(TitanRoutes.AI_CHAT) },
                onNavigateToGit = { navController.navigate(TitanRoutes.GIT) },
                onNavigateToSettings = { navController.navigate(TitanRoutes.SETTINGS) },
                onFileSelected = { filePath -> navController.navigate(TitanRoutes.editor(filePath)) },
            )
        }
        composable(TitanRoutes.EDITOR) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            com.titan.feature.editor.ui.screen.EditorScreen(
                filePath = filePath,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.FILE_MANAGER) {
            com.titan.feature.filemanager.ui.screen.FileManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onFileSelected = { filePath -> navController.navigate(TitanRoutes.editor(filePath)) },
            )
        }
        composable(TitanRoutes.TERMINAL) {
            com.titan.feature.terminal.ui.screen.TerminalScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.AI_CHAT) {
            com.titan.feature.ai.ui.screen.AIChatScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.SETTINGS) {
            com.titan.feature.settings.ui.screen.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.GIT) {
            com.titan.feature.git.ui.screen.GitScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.BUILD) {
            com.titan.feature.buildsystem.ui.screen.BuildScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.LOGCAT) {
            com.titan.feature.logcat.ui.screen.LogcatScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.DEBUGGER) {
            com.titan.feature.debugger.ui.screen.DebuggerScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.APK_TOOLS) {
            com.titan.feature.apktools.ui.screen.ApkToolsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.XML_DESIGNER) {
            com.titan.feature.xmldesigner.ui.screen.XmlDesignerScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.PLUGINS) {
            com.titan.feature.plugins.ui.screen.PluginsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(TitanRoutes.TOOLS) {
            com.titan.feature.tools.ui.screen.ToolsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
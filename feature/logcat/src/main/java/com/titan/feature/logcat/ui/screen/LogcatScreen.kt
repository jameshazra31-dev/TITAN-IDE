package com.titan.feature.logcat.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(onNavigateBack: () -> Unit) {
    var filterLevel by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val levels = listOf("All", "Verbose", "Debug", "Info", "Warning", "Error", "Assert")

    val sampleLogs = listOf(
        Triple("D/Activity", "onCreate() called", "Debug"),
        Triple("I/Choreographer", "Skipped 31 frames! The application may be doing too much work on its main thread.", "Info"),
        Triple("W/ResourceType", "No package identifier when getting value for resource number 0x00000000", "Warning"),
        Triple("E/AndroidRuntime", "FATAL EXCEPTION: main\njava.lang.NullPointerException: Attempt to invoke virtual method on a null object reference", "Error"),
        Triple("D/Network", "Request completed in 245ms", "Debug"),
        Triple("I/TitanIDE", "Project loaded successfully", "Info"),
        Triple("D/Git", "Clone completed: 42 objects", "Debug"),
    )

    val filteredLogs = sampleLogs.filter { (tag, _, level) ->
        (filterLevel == "All" || level == filterLevel) &&
            (searchQuery.isBlank() || tag.contains(searchQuery, ignoreCase = true) || level.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logcat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter logs...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                FilterChip(
                    selected = filterLevel == "All",
                    onClick = { filterLevel = "All" },
                    label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                )
                FilterChip(
                    selected = filterLevel == "Error",
                    onClick = { filterLevel = "Error" },
                    label = { Text("Error", style = MaterialTheme.typography.labelSmall) },
                )
                FilterChip(
                    selected = filterLevel == "Warning",
                    onClick = { filterLevel = "Warning" },
                    label = { Text("Warn", style = MaterialTheme.typography.labelSmall) },
                )
            }

            ScrollableTabRow(
                selectedTabIndex = levels.indexOf(filterLevel).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
            ) {
                levels.forEach { level ->
                    Tab(
                        selected = filterLevel == level,
                        onClick = { filterLevel = level },
                        text = { Text(level, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color(0xFF1E1E1E),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    filteredLogs.forEach { (tag, message, level) ->
                        val color = when (level) {
                            "Error" -> Color(0xFFF44747)
                            "Warning" -> Color(0xFFCCA700)
                            "Info" -> Color(0xFF569CD6)
                            "Debug" -> Color(0xFFD4D4D4)
                            else -> Color(0xFF808080)
                        }
                        Text(
                            "$tag: $message",
                            color = color,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
        }
    }
}
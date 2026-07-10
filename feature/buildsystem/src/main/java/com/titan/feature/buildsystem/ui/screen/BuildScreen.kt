package com.titan.feature.buildsystem.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
    onNavigateBack: () -> Unit,
) {
    var isBuilding by remember { mutableStateOf(false) }
    var buildProgress by remember { mutableFloatStateOf(0f) }
    var buildOutput by remember {
        mutableStateOf(
            listOf(
                "> Task :app:preBuild UP-TO-DATE",
                "> Task :app:preDebugBuild UP-TO-DATE",
                "> Task :app:mergeDebugNativeLibs NO-SOURCE",
                "> Task :app:compileDebugKotlin",
                "w: Some input files use or override a deprecated API.",
                "> Task :app:compileDebugJavaWithJavac NO-SOURCE",
                "> Task :app:mergeDebugShaders",
                "> Task :app:mergeDebugAssets",
                "> Task :app:processDebugManifest",
                "> Task :app:mergeDebugResources",
                "> Task :app:packageDebug",
                "> BUILD SUCCESSFUL in 45s",
                "",
                "BUILD SUCCESSFUL in 45s",
                "23 actionable tasks: 23 executed",
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isBuilding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clean")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            BuildActionRow(
                isBuilding = isBuilding,
                onStartBuild = {
                    isBuilding = true
                    buildProgress = 0f
                },
            )

            if (isBuilding) {
                LinearProgressIndicator(
                    progress = { buildProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    buildOutput.forEach { line ->
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = when {
                                line.contains("BUILD SUCCESSFUL") -> Color(0xFF6A9955)
                                line.contains("FAILED") || line.contains("ERROR") -> Color(0xFFF44747)
                                line.startsWith("w:") -> Color(0xFFCCA700)
                                line.startsWith(">") -> Color(0xFF569CD6)
                                else -> Color(0xFFD4D4D4)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuildActionRow(isBuilding: Boolean, onStartBuild: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilledTonalButton(
            onClick = onStartBuild,
            enabled = !isBuilding,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = if (isBuilding) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isBuilding) "Building..." else "Build Debug APK")
        }

        OutlinedButton(
            onClick = {},
            enabled = !isBuilding,
        ) {
            Text("Release")
        }

        OutlinedButton(
            onClick = {},
            enabled = !isBuilding,
        ) {
            Text("Bundle")
        }
    }
}
// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 DevTools Contributors

package org.devtools.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.devtools.app.ui.screens.ToolScreen

data class ToolItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevToolsApp() {
    var selectedTool by remember { mutableStateOf<ToolItem?>(null) }

    val tools = listOf(
        ToolItem("Base64", "Encode & decode Base64", Icons.Filled.Code, "base64"),
        ToolItem("URL Encode", "Percent-encode & decode URLs", Icons.Filled.Link, "url"),
        ToolItem("HTML Entities", "Encode & decode HTML entities", Icons.Filled.Language, "html"),
        ToolItem("Hex ↔ Text", "Convert between hex and text", Icons.Filled.Memory, "hex"),
        ToolItem("Binary ↔ Text", "Convert between binary and text", Icons.Filled.DataObject, "binary"),
        ToolItem("Hash Generator", "MD5, SHA-1, SHA-256, SHA-512", Icons.Filled.Fingerprint, "hash"),
        ToolItem("UUID Generator", "Generate random UUIDs", Icons.Filled.Badge, "uuid"),
        ToolItem("JWT Decoder", "Decode JWT token payloads", Icons.Filled.Key, "jwt"),
        ToolItem("Unix Timestamp", "Convert timestamps to dates", Icons.Filled.Schedule, "timestamp"),
        ToolItem("JSON Formatter", "Pretty-print & validate JSON", Icons.Filled.DataArray, "json"),
        ToolItem("Text Stats", "Count characters, words, lines", Icons.Filled.TextFields, "textstats"),
    )

    if (selectedTool == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("DevTools", fontWeight = FontWeight.Bold)
                            Text(
                                "Offline Encoder/Decoder & Utilities",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tools) { tool ->
                    ToolCard(tool = tool, onClick = { selectedTool = tool })
                }
            }
        }
    } else {
        ToolScreen(
            tool = selectedTool!!,
            onBack = { selectedTool = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

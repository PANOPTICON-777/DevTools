// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 DevTools Contributors

package org.devtools.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.devtools.app.tools.ToolEngine
import org.devtools.app.ui.ToolItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(tool: ToolItem, onBack: () -> Unit) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (tool.route) {
                "base64" -> EncoderDecoderTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onEncode = { output = ToolEngine.base64Encode(input) },
                    onDecode = { output = ToolEngine.base64Decode(input) },
                    context = context
                )
                "url" -> EncoderDecoderTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onEncode = { output = ToolEngine.urlEncode(input) },
                    onDecode = { output = ToolEngine.urlDecode(input) },
                    context = context
                )
                "html" -> EncoderDecoderTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onEncode = { output = ToolEngine.htmlEncode(input) },
                    onDecode = { output = ToolEngine.htmlDecode(input) },
                    context = context
                )
                "hex" -> EncoderDecoderTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onEncode = { output = ToolEngine.textToHex(input) },
                    onDecode = { output = ToolEngine.hexToText(input) },
                    encodeLabel = "Text → Hex",
                    decodeLabel = "Hex → Text",
                    context = context
                )
                "binary" -> EncoderDecoderTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onEncode = { output = ToolEngine.textToBinary(input) },
                    onDecode = { output = ToolEngine.binaryToText(input) },
                    encodeLabel = "Text → Binary",
                    decodeLabel = "Binary → Text",
                    context = context
                )
                "hash" -> HashTool(input = input, onInputChange = { input = it }, context = context)
                "uuid" -> UuidTool(context = context)
                "jwt" -> SingleInputTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onProcess = { output = ToolEngine.decodeJwt(input) },
                    buttonLabel = "Decode JWT",
                    placeholder = "Paste JWT token here...",
                    context = context
                )
                "timestamp" -> TimestampTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    context = context
                )
                "json" -> SingleInputTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onProcess = { output = ToolEngine.formatJson(input) },
                    buttonLabel = "Format JSON",
                    placeholder = "Paste JSON here...",
                    context = context
                )
                "textstats" -> SingleInputTool(
                    input = input,
                    output = output,
                    onInputChange = { input = it },
                    onProcess = { output = ToolEngine.countChars(input) },
                    buttonLabel = "Count",
                    placeholder = "Type or paste text here...",
                    context = context
                )
            }
        }
    }
}

@Composable
fun EncoderDecoderTool(
    input: String,
    output: String,
    onInputChange: (String) -> Unit,
    onEncode: () -> Unit,
    onDecode: () -> Unit,
    encodeLabel: String = "Encode",
    decodeLabel: String = "Decode",
    context: Context
) {
    OutlinedTextField(
        value = input,
        onValueChange = onInputChange,
        label = { Text("Input") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        maxLines = 10
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onEncode,
            modifier = Modifier.weight(1f)
        ) {
            Text(encodeLabel)
        }
        OutlinedButton(
            onClick = onDecode,
            modifier = Modifier.weight(1f)
        ) {
            Text(decodeLabel)
        }
    }

    OutputCard(output = output, context = context)
}

@Composable
fun HashTool(input: String, onInputChange: (String) -> Unit, context: Context) {
    OutlinedTextField(
        value = input,
        onValueChange = onInputChange,
        label = { Text("Input text") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        maxLines = 8
    )

    if (input.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        HashResultRow("MD5", ToolEngine.md5(input), context)
        HashResultRow("SHA-1", ToolEngine.sha1(input), context)
        HashResultRow("SHA-256", ToolEngine.sha256(input), context)
        HashResultRow("SHA-512", ToolEngine.sha512(input), context)
    } else {
        Text(
            "Type text above to see hash values",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HashResultRow(label: String, value: String, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { copyToClipboard(context, value) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun UuidTool(context: Context) {
    var uuids by remember { mutableStateOf(listOf(ToolEngine.generateUUID())) }

    Button(
        onClick = { uuids = listOf(ToolEngine.generateUUID()) + uuids.take(9) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Generate New UUID")
    }

    uuids.forEach { uuid ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uuid,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { copyToClipboard(context, uuid) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun TimestampTool(
    input: String,
    output: String,
    onInputChange: (String) -> Unit,
    context: Context
) {
    var currentTs by remember { mutableStateOf(ToolEngine.currentTimestamp()) }
    var result by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current Unix Timestamp", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTs,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { currentTs = ToolEngine.currentTimestamp() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { copyToClipboard(context, currentTs) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = input,
        onValueChange = onInputChange,
        label = { Text("Enter Unix timestamp") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("e.g. 1700000000") }
    )

    Button(
        onClick = { result = ToolEngine.timestampToDate(input) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Convert to Date")
    }

    if (result.isNotEmpty()) {
        OutputCard(output = result, context = context)
    }
}

@Composable
fun SingleInputTool(
    input: String,
    output: String,
    onInputChange: (String) -> Unit,
    onProcess: () -> Unit,
    buttonLabel: String,
    placeholder: String,
    context: Context
) {
    OutlinedTextField(
        value = input,
        onValueChange = onInputChange,
        label = { Text("Input") },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        maxLines = 15
    )

    Button(
        onClick = onProcess,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(buttonLabel)
    }

    OutputCard(output = output, context = context)
}

@Composable
fun OutputCard(output: String, context: Context) {
    if (output.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Output",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { copyToClipboard(context, output) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy output",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = output,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        content()
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("DevTools Output", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

package com.aicoding.assistant.ui.screens.providers

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicoding.assistant.domain.model.ApiKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeysScreen(
    providerId: Long,
    onBack: () -> Unit,
    viewModel: KeysViewModel = hiltViewModel(),
) {
    val keys by viewModel.keys.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = { Text("API Keys") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add key")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Keys rotate automatically on rate-limit errors. Higher priority keys are tried first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (keys.isEmpty()) {
                item {
                    Text("No keys yet. Add one to start chatting.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(keys, key = { it.id }) { key ->
                KeyRow(
                    key = key,
                    onToggle = { viewModel.toggle(key.id, it) },
                    onUp = { viewModel.movePriority(key.id, -1) },
                    onDown = { viewModel.movePriority(key.id, 1) },
                    onDelete = {
                        viewModel.delete(key.id)
                        Toast.makeText(context, "Key deleted", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add API key") },
            text = {
                OutlinedTextField(
                    value = newKey,
                    onValueChange = { newKey = it },
                    placeholder = { Text("sk-...") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newKey.isNotBlank()) {
                        viewModel.add(newKey.trim())
                        newKey = ""
                        Toast.makeText(context, "Key added", Toast.LENGTH_SHORT).show()
                    }
                    showAdd = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun KeyRow(
    key: ApiKey,
    onToggle: (Boolean) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (visible) key.value else key.value.take(6) + "•".repeat(key.value.length.coerceAtMost(24)),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.width(16.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (key.enabled) "Active" else "Disabled",
                style = MaterialTheme.typography.labelSmall,
                color = if (key.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Used ${key.usageCount}x",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Switch(checked = key.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onUp) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Higher priority", modifier = Modifier.width(16.dp))
            }
            IconButton(onClick = onDown) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Lower priority", modifier = Modifier.width(16.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.width(16.dp))
            }
        }
    }
}
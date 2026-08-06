package com.aicoding.assistant.ui.screens.providers

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicoding.assistant.domain.model.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onKeys: (Long) -> Unit,
    viewModel: ProvidersViewModel = hiltViewModel(),
) {
    val providers by viewModel.providers.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var deleteTarget by remember { mutableStateOf<Provider?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = { Text("AI Providers") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add provider")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        ) {
            items(providers, key = { it.id }) { provider ->
                ProviderRow(
                    provider = provider,
                    onToggle = { enabled -> viewModel.toggleEnabled(provider.id, enabled) },
                    onEdit = { onEdit(provider.id) },
                    onKeys = { onKeys(provider.id) },
                    onDelete = { deleteTarget = provider },
                )
            }
        }
    }

    deleteTarget?.let { provider ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete provider?") },
            text = { Text("All its API keys will also be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(provider.id)
                    deleteTarget = null
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ProviderRow(
    provider: Provider,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onKeys: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(provider.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                provider.baseUrl,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onKeys) {
            Icon(Icons.Default.Key, contentDescription = "Keys", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        Switch(checked = provider.enabled, onCheckedChange = onToggle)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}
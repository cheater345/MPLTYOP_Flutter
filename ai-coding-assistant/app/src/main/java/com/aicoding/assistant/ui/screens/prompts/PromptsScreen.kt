package com.aicoding.assistant.ui.screens.prompts

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicoding.assistant.domain.model.Prompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptsScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onUse: (String) -> Unit,
    viewModel: PromptsViewModel = hiltViewModel(),
) {
    val prompts by viewModel.prompts.collectAsState()
    val query by viewModel.query.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Search prompts...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEdit(0L) }) {
                Icon(Icons.Default.Add, contentDescription = "New prompt")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(prompts, key = { it.id }) { prompt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUse(prompt.title) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prompt.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            prompt.content.take(80),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (prompt.category.isNotBlank()) {
                            Text(
                                prompt.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleFavorite(prompt.id, !prompt.favorite) }) {
                        Icon(
                            if (prompt.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (prompt.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onEdit(prompt.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        viewModel.delete(prompt.id)
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
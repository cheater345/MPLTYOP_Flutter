package com.aicoding.assistant.ui.screens.prompts

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditScreen(
    promptId: Long,
    onBack: () -> Unit,
    viewModel: PromptEditViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isEmpty = promptId == 0L

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var favorite by remember { mutableStateOf(false) }

    if (!isEmpty) {
        LaunchedEffect(promptId) {
            viewModel.load { p ->
                title = p.title
                content = p.content
                category = p.category
                favorite = p.favorite
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = { Text(if (isEmpty) "New prompt" else "Edit prompt") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Label("Title")
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))
            Label("Category")
            OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), placeholder = { Text("e.g. code-gen, debug, review") })

            Spacer(Modifier.height(12.dp))
            Label("Prompt")
            OutlinedTextField(
                content, { content = it }, Modifier.fillMaxWidth(),
                minLines = 6,
                placeholder = { Text("Write the prompt template...") },
            )

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Favorite", modifier = Modifier.weight(1f))
                Switch(checked = favorite, onCheckedChange = { favorite = it })
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (content.isBlank()) {
                        Toast.makeText(context, "Prompt content required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.save(title, content, category, favorite) { onBack() }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}
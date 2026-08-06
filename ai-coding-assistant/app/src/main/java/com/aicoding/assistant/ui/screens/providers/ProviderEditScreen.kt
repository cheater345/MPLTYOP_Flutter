package com.aicoding.assistant.ui.screens.providers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderEditScreen(
    providerId: Long,
    onBack: () -> Unit,
    viewModel: ProviderEditViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isEmpty = providerId == 0L

    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(ProviderKind.OPENAI_COMPAT.name) }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf(0.7f) }
    var topP by remember { mutableStateOf(1f) }
    var maxTokens by remember { mutableStateOf(2048) }
    var timeout by remember { mutableStateOf(120) }
    var headerJson by remember { mutableStateOf("") }
    var stream by remember { mutableStateOf(true) }

    if (!isEmpty) {
        LaunchedEffect(providerId) {
            viewModel.load(providerId) { p ->
                name = p.name
                kind = p.kind.name
                baseUrl = p.baseUrl
                apiKey = p.apiKey
                systemPrompt = p.systemPrompt
                temperature = p.temperature
                topP = p.topP
                maxTokens = p.maxTokens
                timeout = p.timeoutSeconds
                headerJson = p.headerJson
                stream = p.stream
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = { Text(if (isEmpty) "Add Provider" else "Edit Provider") },
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
            Field("Provider name")
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth())

            Field("Provider type")
            ProviderKind.entries.forEach { k ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = kind == k.name,
                        onClick = { kind = k.name },
                    )
                    Text(k.name, modifier = Modifier.padding(top = 8.dp))
                }
            }

            Field("Base URL")
            OutlinedTextField(baseUrl, { baseUrl = it }, Modifier.fillMaxWidth(), placeholder = { Text("https://api.openai.com/v1") })

            Field("API key")
            OutlinedTextField(apiKey, { apiKey = it }, Modifier.fillMaxWidth())

            Field("System prompt")
            OutlinedTextField(systemPrompt, { systemPrompt = it }, Modifier.fillMaxWidth(), minLines = 2)

            Field("Temperature: ${temperature}")
            androidx.compose.material3.Slider(value = temperature, onValueChange = { temperature = it }, valueRange = 0f..2f)

            Field("Top P: ${topP}")
            androidx.compose.material3.Slider(value = topP, onValueChange = { topP = it }, valueRange = 0f..1f)

            Field("Max tokens")
            OutlinedTextField(maxTokens.toString(), { s -> maxTokens = s.toIntOrNull() ?: 2048 }, Modifier.fillMaxWidth())

            Field("Timeout seconds")
            OutlinedTextField(timeout.toString(), { s -> timeout = s.toIntOrNull() ?: 120 }, Modifier.fillMaxWidth())

            Field("Custom headers (JSON)")
            OutlinedTextField(headerJson, { headerJson = it }, Modifier.fillMaxWidth(), minLines = 2)

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Streaming", modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = stream, onCheckedChange = { stream = it })
            }

            Button(
                onClick = {
                    if (name.isBlank() || baseUrl.isBlank()) {
                        Toast.makeText(context, "Name and URL required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.save(
                        Provider(
                            id = providerId, name = name, kind = ProviderKind.valueOf(kind),
                            baseUrl = baseUrl, apiKey = apiKey, temperature = temperature,
                            topP = topP, maxTokens = maxTokens, stream = stream,
                            timeoutSeconds = timeout, headerJson = headerJson,
                            enabled = true, systemPrompt = systemPrompt,
                        )
                    ) { onBack() }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
            Spacer(Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun Field(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}
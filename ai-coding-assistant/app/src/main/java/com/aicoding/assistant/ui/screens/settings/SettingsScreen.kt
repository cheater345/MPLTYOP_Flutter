package com.aicoding.assistant.ui.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProviders: () -> Unit,
    onPrompts: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                title = { Text("Settings") },
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
            Section("Appearance")
            Option(label = "Theme", value = settings.themeMode, onClick = { viewModel.cycleTheme() })
            SwitchRow("Dynamic color", settings.dynamicColor) { viewModel.setDynamicColor(it) }
            SliderRow("Font size", settings.fontSizeScale) { viewModel.setFontScale(it) }
            SliderRow("Animation speed", settings.animationSpeed) { viewModel.setAnimationSpeed(it) }

            Section("Data")
            SettingsLink(onClick = onProviders, title = "AI Providers", icon = Icons.Default.Dns)
            SettingsLink(onClick = onPrompts, title = "Prompt Library", icon = Icons.Default.Bookmarks)
            SwitchRow("Streaming responses", settings.streamingEnabled) { viewModel.setStreaming(it) }
            SwitchRow("Syntax highlighting", settings.highlightEnabled) { viewModel.setHighlight(it) }
            SwitchRow("Markdown tables", settings.markdownTablesEnabled) { viewModel.setTables(it) }
            SwitchRow("Background notifications", settings.notificationsEnabled) { viewModel.setNotifications(it) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { viewModel.exportData() }) {
                    Text("Export data (JSON)")
                }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun Option(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(label: String, enabled: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!enabled) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = enabled, onCheckedChange = onChecked)
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ${String.format("%.2f", value)}", style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0.6f..1.6f)
    }
}

@Composable
private fun SettingsLink(onClick: () -> Unit, title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
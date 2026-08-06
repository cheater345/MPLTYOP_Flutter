package com.aicoding.assistant.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicoding.assistant.domain.model.MessageRole
import com.aicoding.assistant.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsState()
    val error by viewModel.error.collectAsState()
    val composerText by viewModel.composerText.collectAsState()
    val sendState by viewModel.sendState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val streaming = sendState is ChatViewModel.SendState.Streaming

    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.chatTitle(messages),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            ChatInputBar(
                text = composerText,
                onTextChange = { viewModel.onComposerChange(it) },
                onSend = { viewModel.send(it) },
                streaming = streaming,
                onStop = { viewModel.stop() },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
        ) {
            if (messages.isEmpty()) {
                ChatEmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isStreaming = streaming && message.role == MessageRole.ASSISTANT,
                            onCopy = { copyTextToClipboard(context, it) },
                        )
                    }
                    item {
                        if (streaming) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "生成中...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    streaming: Boolean,
    onStop: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask anything or describe code to generate...") },
            maxLines = 6,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        Spacer(Modifier.width(8.dp))
        if (streaming) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        } else {
            IconButton(
                onClick = {
                    onSend(text)
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ChatEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("AI Code", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Generate code, explain bugs, refactor, write tests, debug errors, and more.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun MessageBubble(
    message: com.aicoding.assistant.domain.model.Message,
    isStreaming: Boolean,
    onCopy: (String) -> Unit,
) {
    val isUser = message.role == MessageRole.USER
    val background = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 1f)
                .background(background, MaterialTheme.shapes.medium)
                .padding(12.dp)
        ) {
MarkdownText(
                markdown = message.content,
                onCopy = onCopy,
            )
            if (message.content.isEmpty()) {
                Text("…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (isStreaming) {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
            }
        }
    }
}

private fun copyTextToClipboard(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("code", text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
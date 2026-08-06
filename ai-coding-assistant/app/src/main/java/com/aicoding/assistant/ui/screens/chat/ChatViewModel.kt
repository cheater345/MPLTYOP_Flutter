package com.aicoding.assistant.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.repository.ChatRepository
import com.aicoding.assistant.data.repository.ProviderRepository
import com.aicoding.assistant.domain.model.Message
import com.aicoding.assistant.domain.model.MessageRole
import com.aicoding.assistant.domain.model.Prompt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val chatId: Long = checkNotNull(savedStateHandle["chatId"])

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val messages: StateFlow<List<Message>> = chatRepository.observeMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers = providerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState

    private val _composerText = MutableStateFlow("")
    val composerText: StateFlow<String> = _composerText

    sealed interface SendState {
        object Idle : SendState
        data object Streaming : SendState
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onComposerChange(text: String) {
        _composerText.value = text
    }

    fun send(text: String, model: String? = null) {
        if (text.isBlank()) return
        _composerText.value = ""
        _sendState.value = SendState.Streaming
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatId = chatId,
                text = text,
                modelOverride = model,
                onError = { msg ->
                    _error.value = msg
                    _sendState.value = SendState.Idle
                },
                onFinished = { _, _ ->
                    _sendState.value = SendState.Idle
                }
            )
        }
    }

    fun sendPrompt(prompt: Prompt) {
        if (prompt.content.isBlank()) return
        val value = _composerText.value.trim()
        val combined = if (value.isEmpty()) prompt.content else "$value\n\n${prompt.content}"
        onComposerChange("")
        send(combined)
    }

    fun stop() {
        chatRepository.stopGeneration(chatId)
        _sendState.value = SendState.Idle
    }

    fun regenerate(modelOverride: String? = null) {
        viewModelScope.launch {
            val current = messages.value
            val lastUser = current.lastOrNull { it.role == MessageRole.USER } ?: return@launch
            _sendState.value = SendState.Streaming
            chatRepository.sendMessage(
                chatId = chatId,
                text = lastUser.content,
                modelOverride = modelOverride,
                onError = { msg ->
                    _error.value = msg
                    _sendState.value = SendState.Idle
                },
                onFinished = { _, _ ->
                    _sendState.value = SendState.Idle
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun rename(title: String) {
        viewModelScope.launch { chatRepository.renameChat(chatId, title) }
    }

    fun togglePin(pinned: Boolean) {
        viewModelScope.launch { chatRepository.setPinned(chatId, pinned) }
    }

    fun deleteChat(onDone: () -> Unit) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            onDone()
        }
    }

    fun copyMessage(message: Message): String = message.content

    fun isStreaming(): Boolean = _sendState.value is SendState.Streaming

    fun chatTitle(messages: List<Message>): String {
        val first = messages.firstOrNull()?.content?.trim()
        return if (!first.isNullOrEmpty()) first.take(24) else "New Chat"
    }
}
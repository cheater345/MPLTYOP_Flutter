package com.aicoding.assistant.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.remote.ModelFetcher
import com.aicoding.assistant.data.repository.ChatRepository
import com.aicoding.assistant.data.repository.ProviderRepository
import com.aicoding.assistant.domain.model.Attachment
import com.aicoding.assistant.domain.model.Message
import com.aicoding.assistant.domain.model.MessageRole
import com.aicoding.assistant.domain.model.ModelInfo
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
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
    private val modelFetcher: ModelFetcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val chatId: Long = checkNotNull(savedStateHandle["chatId"])

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val messages: StateFlow<List<Message>> = chatRepository.observeMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers = providerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProviderId = MutableStateFlow<Long?>(null)
    val selectedProviderId: StateFlow<Long?> = _selectedProviderId

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel

    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState

    private val _composerText = MutableStateFlow("")
    val composerText: StateFlow<String> = _composerText

    private val _attachments = MutableStateFlow<List<Attachment>>(emptyList())
    val attachments: StateFlow<List<Attachment>> = _attachments

    sealed interface SendState {
        object Idle : SendState
        data object Streaming : SendState
    }

    init {
        viewModelScope.launch {
            val enabled = providerRepository.getAllEnabled()
            val preferred = enabled.firstOrNull()
            if (preferred != null) {
                selectProvider(preferred)
            }
        }
    }

    fun selectProvider(provider: Provider) {
        _selectedProviderId.value = provider.id
        _selectedModel.value = null
        viewModelScope.launch {
            _modelsLoading.value = true
            _models.value = modelFetcher.fetch(provider)
            _modelsLoading.value = false
            if (_models.value.isEmpty()) {
                _models.value = fallbackModels(provider.kind)
            }
            _selectedModel.value = _models.value.firstOrNull()?.id
        }
    }

    fun selectModel(modelId: String) {
        _selectedModel.value = modelId
    }

    private fun fallbackModels(kind: ProviderKind): List<ModelInfo> = when (kind) {
        ProviderKind.OPENROUTER -> listOf(
            ModelInfo("meta-llama/llama-3.1-8b-instruct:free", "Llama 3.1 8B (free)"),
            ModelInfo("deepseek/deepseek-chat", "DeepSeek Chat"),
            ModelInfo("openai/gpt-4o-mini", "GPT-4o Mini"),
            ModelInfo("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash (free)"),
            ModelInfo("qwen/qwen-2.5-72b-instruct", "Qwen 2.5 72B"),
        )
        ProviderKind.GEMINI -> listOf(
            ModelInfo("gemini-1.5-flash", "Gemini 1.5 Flash"),
            ModelInfo("gemini-1.5-pro", "Gemini 1.5 Pro"),
            ModelInfo("gemini-2.0-flash", "Gemini 2.0 Flash"),
        )
        ProviderKind.GROQ -> listOf(
            ModelInfo("llama-3.3-70b-versatile", "Llama 3.3 70B"),
            ModelInfo("llama-3.1-8b-instant", "Llama 3.1 8B (fast)"),
            ModelInfo("gemma2-9b-it", "Gemma 2 9B"),
        )
        ProviderKind.MISTRAL -> listOf(
            ModelInfo("open-mistral-nemo", "Mistral Nemo"),
            ModelInfo("mistral-small-latest", "Mistral Small"),
            ModelInfo("codestral-latest", "Codestral"),
        )
        ProviderKind.CEREBRAS -> listOf(
            ModelInfo("llama-3.3-70b", "Llama 3.3 70B"),
            ModelInfo("llama-3.1-8b", "Llama 3.1 8B"),
        )
        ProviderKind.OLLAMA, ProviderKind.LM_STUDIO, ProviderKind.LOCAL -> listOf(
            ModelInfo("llama3.2", "Llama 3.2"),
            ModelInfo("qwen2.5:7b", "Qwen 2.5 7B"),
            ModelInfo("mistral", "Mistral"),
        )
        else -> listOf(
            ModelInfo("gpt-4o-mini", "GPT-4o Mini"),
            ModelInfo("gpt-4o", "GPT-4o"),
        )
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onComposerChange(text: String) {
        _composerText.value = text
    }

    fun addAttachment(attachment: Attachment) {
        val current = _attachments.value
        if (current.size >= 4) return
        if (current.any { it.uri == attachment.uri }) return
        _attachments.value = current + attachment
    }

    fun removeAttachment(uri: String) {
        _attachments.value = _attachments.value.filterNot { it.uri == uri }
    }

    fun clearAttachments() {
        _attachments.value = emptyList()
    }

    fun send(text: String, model: String? = null) {
        if (text.isBlank() && _attachments.value.isEmpty()) return
        val attachments = _attachments.value
        _composerText.value = ""
        clearAttachments()
        _sendState.value = SendState.Streaming
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatId = chatId,
                text = text,
                providerId = _selectedProviderId.value,
                modelOverride = model ?: _selectedModel.value,
                attachments = attachments,
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
                providerId = _selectedProviderId.value,
                modelOverride = modelOverride ?: _selectedModel.value,
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
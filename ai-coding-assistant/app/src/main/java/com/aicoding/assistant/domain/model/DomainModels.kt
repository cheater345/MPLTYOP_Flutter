package com.aicoding.assistant.domain.model

data class Chat(
    val id: Long,
    val title: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class Message(
    val id: Long,
    val chatId: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: Long,
    val attachmentPaths: List<String> = emptyList(),
    val error: String? = null,
    val model: String? = null,
    val providerId: Long? = null,
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }

enum class ProviderKind {
    OPENAI_COMPAT,
    GEMINI,
    OLLAMA,
    OPENROUTER,
    LM_STUDIO,
    CUSTOM,
    OPENCODE,
    ZEN,
    LOCAL,
    GROQ,
    MISTRAL,
    CEREBRAS,
}

data class Provider(
    val id: Long,
    val name: String,
    val kind: ProviderKind,
    val baseUrl: String,
    val apiKey: String,
    val temperature: Float,
    val topP: Float,
    val maxTokens: Int,
    val stream: Boolean,
    val timeoutSeconds: Int,
    val headerJson: String,
    val enabled: Boolean,
    val systemPrompt: String,
)

data class ApiKey(
    val id: Long,
    val providerId: Long,
    val value: String,
    val priority: Int,
    val enabled: Boolean,
    val usageCount: Long,
    val lastUsedAt: Long?,
    val consecutiveFailures: Int,
)

data class Prompt(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val favorite: Boolean,
    val createdAt: Long,
)

data class ModelInfo(
    val id: String,
    val name: String,
)

data class StreamChunk(
    val delta: String,
    val finished: Boolean = false,
    val error: String? = null,
)

data class Attachment(
    val uri: String,
    val mimeType: String,
    val displayName: String,
    val sizeBytes: Long,
)
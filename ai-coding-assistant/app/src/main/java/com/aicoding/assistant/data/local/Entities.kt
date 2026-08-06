package com.aicoding.assistant.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats", indices = [Index("updatedAt"), Index("pinned")])
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "messages", indices = [Index("chatId"), Index("role")])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String,
    val content: String,
    val createdAt: Long,
    val attachmentPaths: List<String> = emptyList(),
    val error: String? = null,
    val model: String? = null,
    val providerId: Long? = null,
)

@Entity(tableName = "prompts", indices = [Index("category"), Index("favorite")])
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,
    val favorite: Boolean,
    val createdAt: Long,
)

@Entity(tableName = "providers", indices = [Index("kind")])
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val temperature: Float,
    val topP: Float,
    val maxTokens: Int,
    val stream: Boolean,
    val timeoutSeconds: Int,
    val headerJson: String,
    val enabled: Boolean,
    val systemPrompt: String,
)

@Entity(tableName = "api_keys", indices = [Index("providerId"), Index("enabled"), Index("priority")])
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val valueEncrypted: String,
    val priority: Int,
    val enabled: Boolean,
    val usageCount: Long = 0,
    val lastUsedAt: Long? = null,
    val consecutiveFailures: Int = 0,
)
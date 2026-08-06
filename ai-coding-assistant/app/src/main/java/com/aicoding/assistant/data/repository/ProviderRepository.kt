package com.aicoding.assistant.data.repository

import android.content.Context
import com.aicoding.assistant.data.local.ProviderEntity
import com.aicoding.assistant.data.local.ProviderDao
import com.aicoding.assistant.data.security.CryptoManager
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProviderRepository(private val dao: ProviderDao, private val context: Context) {

    fun observeAll(): Flow<List<Provider>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getAllEnabled(): List<Provider> = withContext(Dispatchers.IO) {
        dao.getAllEnabled().map { it.toDomain() }
    }

    suspend fun getById(id: Long): Provider? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    suspend fun upsert(provider: Provider) = withContext(Dispatchers.IO) {
        val entity = ProviderEntity(
            id = provider.id,
            name = provider.name,
            kind = provider.kind.name,
            baseUrl = provider.baseUrl,
            apiKeyEncrypted = if (provider.apiKey.isBlank()) {
                dao.getById(provider.id)?.apiKeyEncrypted ?: ""
            } else {
                CryptoManager.encrypt(provider.apiKey)
            },
            temperature = provider.temperature,
            topP = provider.topP,
            maxTokens = provider.maxTokens,
            stream = provider.stream,
            timeoutSeconds = provider.timeoutSeconds,
            headerJson = provider.headerJson,
            enabled = provider.enabled,
            systemPrompt = provider.systemPrompt,
        )
        if (entity.id == 0L) dao.insert(entity) else dao.update(entity)
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.setEnabled(id, enabled)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }

    fun ProviderEntity.toDomainKey(): String = CryptoManager.tryDecrypt(apiKeyEncrypted) ?: ""

    private fun ProviderEntity.toDomain(): Provider = Provider(
        id = id,
        name = name,
        kind = ProviderKind.valueOf(kind),
        baseUrl = baseUrl,
        apiKey = CryptoManager.tryDecrypt(apiKeyEncrypted) ?: "",
        temperature = temperature,
        topP = topP,
        maxTokens = maxTokens,
        stream = stream,
        timeoutSeconds = timeoutSeconds,
        headerJson = headerJson,
        enabled = enabled,
        systemPrompt = systemPrompt,
    )

    companion object {
        fun defaultProviders(context: Context): List<Provider> = listOf(
            Provider(
                id = 0, name = "OpenRouter", kind = ProviderKind.OPENROUTER,
                baseUrl = "https://openrouter.ai/api/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Google Gemini", kind = ProviderKind.GEMINI,
                baseUrl = "https://generativelanguage.googleapis.com", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Groq (free)", kind = ProviderKind.GROQ,
                baseUrl = "https://api.groq.com/openai/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Mistral (free)", kind = ProviderKind.MISTRAL,
                baseUrl = "https://api.mistral.ai/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Cerebras (fast)", kind = ProviderKind.CEREBRAS,
                baseUrl = "https://api.cerebras.ai/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Ollama", kind = ProviderKind.OLLAMA,
                baseUrl = "http://10.0.2.2:11434/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 180, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "OpenAI Compatible", kind = ProviderKind.OPENAI_COMPAT,
                baseUrl = "https://api.openai.com/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "LM Studio", kind = ProviderKind.LM_STUDIO,
                baseUrl = "http://10.0.2.2:1234/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 180, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "OpenCode", kind = ProviderKind.OPENCODE,
                baseUrl = "http://10.0.2.2:8080/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
            Provider(
                id = 0, name = "Zen", kind = ProviderKind.ZEN,
                baseUrl = "http://10.0.2.2:8081/v1", apiKey = "",
                temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                timeoutSeconds = 120, headerJson = "", enabled = true,
                systemPrompt = "You are a helpful coding assistant."
            ),
        )
    }
}
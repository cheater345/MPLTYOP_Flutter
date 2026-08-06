package com.aicoding.assistant.util

import android.content.Context
import com.aicoding.assistant.data.local.ProviderDao
import com.aicoding.assistant.data.local.ProviderEntity
import com.aicoding.assistant.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedDefaults {

    suspend fun seedProviders(context: Context, dao: ProviderDao) {
        withContext(Dispatchers.IO) {
            if (dao.getAllEnabled().isNotEmpty()) return@withContext
            val defaults = listOf(
                ProviderEntity(
                    name = "OpenRouter", kind = "OPENROUTER",
                    baseUrl = "https://openrouter.ai/api/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 120, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "Google Gemini", kind = "GEMINI",
                    baseUrl = "https://generativelanguage.googleapis.com", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 120, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "Ollama", kind = "OLLAMA",
                    baseUrl = "http://10.0.2.2:11434/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 180, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "OpenAI Compatible", kind = "OPENAI_COMPAT",
                    baseUrl = "https://api.openai.com/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 120, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "LM Studio", kind = "LM_STUDIO",
                    baseUrl = "http://10.0.2.2:1234/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 180, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "OpenCode", kind = "OPENCODE",
                    baseUrl = "http://10.0.2.2:8080/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 120, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
                ProviderEntity(
                    name = "Zen", kind = "ZEN",
                    baseUrl = "http://10.0.2.2:8081/v1", apiKeyEncrypted = "",
                    temperature = 0.7f, topP = 1f, maxTokens = 4096, stream = true,
                    timeoutSeconds = 120, headerJson = "", enabled = true,
                    systemPrompt = "You are a helpful coding assistant.",
                ),
            )
            defaults.forEach { dao.insert(it) }
        }
    }
}
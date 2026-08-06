package com.aicoding.assistant.data.remote

import com.aicoding.assistant.domain.model.ModelInfo
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ModelFetcher(private val client: OkHttpClient) {

    suspend fun fetch(provider: Provider): List<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            when (provider.kind) {
                ProviderKind.GEMINI -> fetchGemini(provider)
                else -> fetchOpenAi(provider)
            }
        } catch (e: Exception) {
            listOf(
                ModelInfo("gpt-4o-mini", "gpt-4o-mini"),
                ModelInfo("meta-llama/llama-3.1-8b-instruct:free", "llama-3.1-8b"),
            )
        }
    }

    private fun fetchOpenAi(provider: Provider): List<ModelInfo> {
        val url = provider.baseUrl.trimEnd('/') + "/models"
        val builder = Request.Builder().url(url)
        provider.apiKey.takeIf { it.isNotBlank() }?.let {
            builder.header("Authorization", "Bearer $it")
        }
        SseStreamEngine.applyCustomHeaders(builder, provider.headerJson)
        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val arr = json.optJSONArray("data") ?: JSONArray()
            val out = mutableListOf<ModelInfo>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id")
                if (id.isNotEmpty()) out += ModelInfo(id, id)
            }
            return out
        }
    }

    private fun fetchGemini(provider: Provider): List<ModelInfo> {
        val base = provider.baseUrl.trimEnd('/')
        val url = if (base.contains("v1beta")) "$base/models" else "$base/v1beta/models"
        val builder = Request.Builder().url(url)
        provider.apiKey.takeIf { it.isNotBlank() }?.let {
            builder.header("x-goog-api-key", it)
        }
        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val arr = json.optJSONArray("models") ?: JSONArray()
            val out = mutableListOf<ModelInfo>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val name = item.optString("name")
                val short = name.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: name
                if (name.isNotEmpty()) out += ModelInfo(short, name)
            }
            return out
        }
    }
}
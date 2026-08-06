package com.aicoding.assistant.data.remote

import android.util.Base64
import com.aicoding.assistant.domain.model.ProviderKind
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class ImagePayload(
    val mimeType: String,
    val base64: String,
)

data class ChatRequestData(
    val messages: List<Pair<String, String>>,
    val model: String,
    val temperature: Float,
    val topP: Float,
    val maxTokens: Int,
    val stream: Boolean,
    val systemPrompt: String?,
    val images: List<ImagePayload> = emptyList(),
)

data class AbortRef(var aborted: Boolean = false)

class SseStreamEngine(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val apiKey: String?,
    private val kind: ProviderKind,
    private val headerJson: String,
) {

    fun stream(
        request: ChatRequestData,
        abortRef: AbortRef,
        onFinish: ((truncated: Boolean) -> Unit)? = null,
    ): Flow<String> = callbackFlow {
        try {
            val reqBuilder: Request.Builder
            when (kind) {
                ProviderKind.GEMINI -> {
                    val contents = JSONArray()
                    request.messages.forEach { (role, content) ->
                        val parts = JSONArray().put(JSONObject().put("text", content))
                        contents.put(
                            JSONObject()
                                .put("role", if (role == "assistant") "model" else "user")
                                .put("parts", parts)
                        )
                    }
                    if (request.images.isNotEmpty()) {
                        val lastUser = contents.getJSONObject(contents.length() - 1)
                        val parts = lastUser.getJSONArray("parts")
                        request.images.forEach { img ->
                            parts.put(
                                JSONObject().put(
                                    "inline_data",
                                    JSONObject()
                                        .put("mime_type", img.mimeType)
                                        .put("data", img.base64)
                                )
                            )
                        }
                    }
                    val payload = JSONObject()
                        .put("contents", contents)
                        .put(
                            "generationConfig",
                            JSONObject()
                                .put("temperature", request.temperature)
                                .put("topP", request.topP)
                                .put("maxOutputTokens", request.maxTokens)
                        )
                        .toString()
                    val url = buildGeminiUrl(request.model)
                    reqBuilder = Request.Builder()
                        .url(url)
                        .post(payload.toRequestBody("application/json".toMediaType()))
                    apiKey?.let { reqBuilder.header("x-goog-api-key", it) }
                }
                else -> {
                    val messages = JSONArray()
                    request.systemPrompt?.let {
                        messages.put(JSONObject().put("role", "system").put("content", it))
                    }
                    request.messages.forEachIndexed { idx, (role, content) ->
                        val contentJson: Any = if (request.images.isNotEmpty() && idx == request.messages.lastIndex) {
                            val parts = JSONArray()
                                .put(JSONObject().put("type", "text").put("text", content))
                            request.images.forEach { img ->
                                parts.put(
                                    JSONObject().put("type", "image_url").put(
                                        "image_url",
                                        JSONObject().put("url", "data:${img.mimeType};base64,${img.base64}")
                                    )
                                )
                            }
                            parts
                        } else {
                            content
                        }
                        messages.put(JSONObject().put("role", role).put("content", contentJson))
                    }
                    val payload = JSONObject()
                        .put("model", request.model)
                        .put("messages", messages)
                        .put("temperature", request.temperature)
                        .put("top_p", request.topP)
                        .put("max_tokens", request.maxTokens)
                        .put("stream", request.stream)
                        .toString()
                    val url = baseUrl.trimEnd('/') + "/chat/completions"
                    reqBuilder = Request.Builder()
                        .url(url)
                        .post(payload.toRequestBody("application/json".toMediaType()))
                    apiKey?.let { reqBuilder.header("Authorization", "Bearer $it") }
                }
            }
            applyCustomHeaders(reqBuilder, headerJson)

            val httpRequest = reqBuilder.build()
            var truncated = false
            var finishReported = false
            fun reportFinish() {
                if (finishReported) return
                finishReported = true
                onFinish?.invoke(truncated)
            }
            EventSources.createFactory(client).newEventSource(
                httpRequest,
                object : EventSourceListener() {
                    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                        if (abortRef.aborted) {
                            eventSource.cancel()
                            return
                        }
                        when (data.trim()) {
                            "", "[DONE]" -> {
                                eventSource.cancel()
                                reportFinish()
                                close()
                            }
                            else -> {
                                try {
                                    val json = JSONObject(data)
                                    val delta = when (kind) {
                                        ProviderKind.GEMINI -> json.optJSONArray("candidates")
                                            ?.optJSONObject(0)
                                            ?.optJSONObject("content")
                                            ?.optJSONArray("parts")
                                            ?.optJSONObject(0)
                                            ?.optString("text", null)
                                        else -> json.optJSONArray("choices")
                                            ?.optJSONObject(0)
                                            ?.optJSONObject("delta")
                                            ?.optString("content", null)
                                            ?: json.optJSONArray("choices")
                                                ?.optJSONObject(0)
                                                ?.optJSONObject("message")
                                                ?.optString("content", null)
                                    }
                                    if (!delta.isNullOrEmpty()) trySend(delta)
                                    val finishReason = when (kind) {
                                        ProviderKind.GEMINI -> json.optJSONArray("candidates")
                                            ?.optJSONObject(0)
                                            ?.optString("finishReason", null)
                                        else -> json.optJSONArray("choices")
                                            ?.optJSONObject(0)
                                            ?.optString("finish_reason", null)
                                    }
                                    if (finishReason == "length" || finishReason == "MAX_TOKENS" || finishReason == "context_length_exceeded") {
                                        truncated = true
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }

                    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                        if (!abortRef.aborted) {
                            reportFinish()
                            close(t ?: IOException("Stream failed"))
                        }
                    }

                    override fun onClosed(eventSource: EventSource) {
                        if (!abortRef.aborted) {
                            reportFinish()
                            close()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { abortRef.aborted = true }
    }.flowOn(Dispatchers.IO)

    private fun buildGeminiUrl(model: String): String {
        val base = baseUrl.trimEnd('/')
        return if (base.contains("v1beta")) {
            "$base/models/$model:streamGenerateContent"
        } else {
            "$base/v1beta/models/$model:streamGenerateContent"
        }
    }

    companion object {
        fun applyCustomHeaders(builder: Request.Builder, headerJson: String) {
            if (headerJson.isBlank()) return
            try {
                val json = JSONObject(headerJson)
                json.keys().forEach { key ->
                    builder.header(key, json.optString(key))
                }
            } catch (_: Exception) {
            }
        }
    }
}
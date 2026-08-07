package com.aicoding.assistant.data.remote

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class LocalLlmEngine(
    private val context: Context,
) {

    data class State(
        val exists: Boolean = false,
        val downloading: Boolean = false,
        val progress: Float = 0f,
        val error: String? = null,
    )

    companion object {
        const val MODEL_URL =
            "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv2048.task"
        const val MODEL_FILE = "megumi-gemma3-1b.task"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    @Volatile
    private var engine: LlmInference? = null
    private val generating = AtomicBoolean(false)

    private fun modelFile(): File = File(File(context.filesDir, "megumi_local"), MODEL_FILE)

    fun refresh() {
        _state.value = _state.value.copy(exists = modelFile().exists())
    }

    suspend fun download() {
        if (_state.value.downloading) return
        _state.value = _state.value.copy(downloading = true, progress = 0f, error = null)
        try {
            withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "megumi_local").apply { mkdirs() }
                val tmp = File(dir, "model.part")
                tmp.delete()
                val request = Request.Builder().url(MODEL_URL).get().build()
                client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "HTTP ${response.code}" }
                    val total = response.body!!.contentLength()
                    val body = response.body!!.byteStream()
                    val out = tmp.outputStream()
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    var done = 0L
                    while (body.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                        done += read
                        if (total > 0) {
                            _state.value = _state.value.copy(progress = done.toFloat() / total)
                        }
                    }
                    out.flush()
                    out.close()
                    body.close()
                }
                tmp.renameTo(modelFile())
            }
            _state.value = _state.value.copy(downloading = false, progress = 1f, exists = true, error = null)
        } catch (e: Exception) {
            _state.value = _state.value.copy(downloading = false, error = e.message ?: "Download failed")
        }
    }

    suspend fun generate(
        prompt: String,
        onDelta: suspend (String) -> Unit,
    ): Boolean {
        if (generating.getAndSet(true)) return false
        return try {
            withContext(Dispatchers.Default) {
                val llm = engine ?: createEngine() ?: return@withContext false
                val session = LlmInferenceSession.createFromOptions(
                    llm,
                    LlmInferenceSession.LlmInferenceSessionOptions.builder()
                        .setTopK(40)
                        .setTopP(0.95f)
                        .setTemperature(0.7f)
                        .build(),
                )
                try {
                    val channel = Channel<Pair<String, Boolean>>(Channel.UNLIMITED)
                    suspendCancellableCoroutine<Unit> { cont ->
                        try {
                            session.generateResponseAsync { partialResult, done ->
                                val text = partialResult ?: ""
                                runCatching { channel.trySend(text to done) }
                                if (done) {
                                    runCatching { channel.close() }
                                    if (cont.isActive) cont.resume(Unit)
                                }
                            }
                        } catch (e: Exception) {
                            channel.close()
                            if (cont.isActive) cont.resume(Unit)
                        }
                    }
                    var last = ""
                    for ((text, _) in channel) {
                        val delta = if (text.startsWith(last)) text.removePrefix(last) else text
                        if (last != text) last = text
                        if (delta.isNotEmpty() && delta.trim().isNotEmpty()) {
                            onDelta(delta)
                        }
                    }
                } finally {
                    runCatching { session.close() }
                }
                true
            }
        } catch (e: Exception) {
            closeEngine()
            false
        } finally {
            generating.set(false)
        }
    }

    private fun createEngine(): LlmInference? {
        val file = modelFile()
        if (!file.exists()) {
            _state.value = _state.value.copy(exists = false)
            return null
        }
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(file.absolutePath)
            .setMaxTokens(2048)
            .build()
        return LlmInference.createFromOptions(context, options).also {
            engine = it
        }
    }

    private fun closeEngine() {
        engine?.close()
        engine = null
    }
}
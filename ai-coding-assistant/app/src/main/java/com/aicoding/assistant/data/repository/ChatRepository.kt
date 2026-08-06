package com.aicoding.assistant.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.aicoding.assistant.data.local.ChatDao
import com.aicoding.assistant.data.local.ChatEntity
import com.aicoding.assistant.data.local.MessageDao
import com.aicoding.assistant.data.local.MessageEntity
import com.aicoding.assistant.data.remote.AbortRef
import com.aicoding.assistant.data.remote.ChatRequestData
import com.aicoding.assistant.data.remote.ImagePayload
import com.aicoding.assistant.data.remote.SseStreamEngine
import com.aicoding.assistant.domain.model.ApiKey
import com.aicoding.assistant.domain.model.Attachment
import com.aicoding.assistant.domain.model.Chat
import com.aicoding.assistant.domain.model.Message
import com.aicoding.assistant.domain.model.MessageRole
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val providerRepository: ProviderRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val context: Context,
) {

    private val activeJobs = ConcurrentHashMap<Long, Job>()

    private val sharedClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun observeChats(): Flow<List<Chat>> =
        chatDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun searchChats(q: String): Flow<List<Chat>> =
        if (q.isBlank()) observeChats() else chatDao.search(q).map { list -> list.map { it.toDomain() } }

    fun observeMessages(chatId: Long): Flow<List<Message>> =
        messageDao.observeForChat(chatId).map { list -> list.map { it.toDomain() } }

    suspend fun createChat(title: String? = null): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        chatDao.insert(ChatEntity(title = title ?: "New Chat", pinned = false, createdAt = now, updatedAt = now))
    }

    suspend fun renameChat(chatId: Long, title: String) = withContext(Dispatchers.IO) {
        chatDao.rename(chatId, title)
        chatDao.touch(chatId, System.currentTimeMillis())
    }

    suspend fun setPinned(chatId: Long, pinned: Boolean) = withContext(Dispatchers.IO) {
        chatDao.setPinned(chatId, pinned)
        chatDao.touch(chatId, System.currentTimeMillis())
    }

    suspend fun deleteChat(chatId: Long) = withContext(Dispatchers.IO) {
        messageDao.deleteForChat(chatId)
        chatDao.delete(chatId)
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        messageDao.delete(messageId)
    }

    suspend fun getMessage(messageId: Long): Message? = withContext(Dispatchers.IO) {
        messageDao.getById(messageId)?.toDomain()
    }

    fun stopGeneration(chatId: Long) {
        activeJobs.remove(chatId)?.cancel()
    }

    fun isGenerating(chatId: Long): Boolean = activeJobs[chatId]?.isActive == true

    fun sendMessage(
        chatId: Long,
        text: String,
        providerId: Long? = null,
        modelOverride: String? = null,
        attachments: List<Attachment> = emptyList(),
        onError: (String) -> Unit = {},
        onFinished: (Long, String) -> Unit = { _, _ -> },
    ): Job = GlobalScope.launch(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val chat = chatDao.getById(chatId)
            val imagePayloads = prepareImages(attachments)
            val userMessageId = messageDao.insert(
                MessageEntity(
                    chatId = chatId, role = MessageRole.USER.name, content = text, createdAt = now,
                    attachmentPaths = attachments.map { it.uri }.map { saveAttachment(context, Uri.parse(it)) },
                )
            )
            if (chat != null && (chat.title.isBlank() || chat.title == "New Chat")) {
                chatDao.rename(chatId, text.take(40))
            }

            val assistantId = messageDao.insert(
                MessageEntity(chatId = chatId, role = MessageRole.ASSISTANT.name, content = "", createdAt = System.currentTimeMillis() + 1)
            )

            val provider = if (providerId != null) {
                providerRepository.getById(providerId)
            } else {
                null
            } ?: providerRepository.getAllEnabled().firstOrNull()
            if (provider == null) {
                messageDao.delete(assistantId)
                onError("No enabled provider configured. Open Settings to add one.")
                return@launch
            }

            val model = modelOverride ?: defaultModel(provider)
            val keys = apiKeyRepository.getEnabledForProvider(provider.id)

            val history = messageDao.observeForChat(chatId).first()
                .filter { it.id != assistantId && it.id != userMessageId }
                .filter { it.role == "user" || it.role == "assistant" }
                .map { it.role to it.content }
                .takeLast(10)

            val request = ChatRequestData(
                messages = history + listOf("user" to text),
                model = model,
                temperature = provider.temperature,
                topP = provider.topP,
                maxTokens = provider.maxTokens,
                stream = provider.stream,
                systemPrompt = provider.systemPrompt.takeIf { it.isNotBlank() },
                images = imagePayloads,
            )

            val ok = deliver(
                provider = provider,
                keys = keys,
                request = request,
                onDelta = { delta ->
                    val current = messageDao.getById(assistantId)?.content ?: ""
                    messageDao.updateContent(assistantId, current + delta)
                },
            )
            if (!ok) {
                messageDao.delete(assistantId)
                onError("All API keys failed or request returned no content.")
                return@launch
            }

            chatDao.touch(chatId, System.currentTimeMillis())
            val finalText = messageDao.getById(assistantId)?.content ?: ""
            onFinished(assistantId, finalText)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError(e.message ?: "Failed to send message")
        }
    }

    private suspend fun deliver(
        provider: Provider,
        keys: List<ApiKey>,
        request: ChatRequestData,
        onDelta: suspend (String) -> Unit,
    ): Boolean {
        val abort = AbortRef()
        if (keys.isEmpty()) {
            return tryStream(provider, null, request, abort, onDelta)
        }
                for (key in keys) {
            if (abort.aborted) return true
            try {
                val ok = tryStream(provider, key, request, abort, onDelta)
                if (ok) {
                    apiKeyRepository.recordUsage(key.id)
                    return true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                apiKeyRepository.recordRotation(key.id)
                if (!isRateLimit(e)) break
            }
        }
        return false
    }

    private suspend fun tryStream(
        provider: Provider,
        key: ApiKey?,
        request: ChatRequestData,
        abort: AbortRef,
        onDelta: suspend (String) -> Unit,
    ): Boolean {
        val engine = buildEngine(provider, key)
        try {
            engine.stream(request, abort).collect { delta ->
                if (delta.isNotEmpty() && !abort.aborted) onDelta(delta)
            }
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (abort.aborted) return true
            throw e
        }
    }

    private fun buildEngine(provider: Provider, key: ApiKey?): SseStreamEngine {
        return SseStreamEngine(
            client = sharedClient,
            baseUrl = provider.baseUrl,
            apiKey = key?.value ?: provider.apiKey,
            kind = provider.kind,
            headerJson = provider.headerJson,
        )
    }

    private fun prepareImages(attachments: List<Attachment>): List<ImagePayload> {
        if (attachments.isEmpty()) return emptyList()
        val out = mutableListOf<ImagePayload>()
        for (attachment in attachments) {
            try {
                val uri = Uri.parse(attachment.uri)
                val stream = context.contentResolver.openInputStream(uri) ?: continue
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                if (bitmap == null) continue
                val scaled = scaleDown(bitmap, 1568)
                if (scaled !== bitmap) bitmap.recycle()
                val outStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                scaled.recycle()
                val data = Base64.encodeToString(outStream.toByteArray(), Base64.NO_WRAP)
                outStream.close()
                out += ImagePayload("image/jpeg", data)
            } catch (_: Exception) {
                // skip broken image
            }
        }
        return out
    }

    private fun scaleDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val largest = maxOf(w, h)
        if (largest <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / largest
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }

    private fun saveAttachment(context: Context, uri: Uri): String {
        try {
            val stream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
            val dir = File(context.filesDir, "megumi_attachments").apply { mkdirs() }
            val file = File(dir, "img_${System.currentTimeMillis()}.bin")
            FileOutputStream(file).use { out -> stream.copyTo(out) }
            return file.absolutePath
        } catch (_: Exception) {
            return uri.toString()
        }
    }

    private fun defaultModel(provider: Provider): String = when (provider.kind) {
        ProviderKind.OPENROUTER -> "meta-llama/llama-3.1-8b-instruct:free"
        ProviderKind.OLLAMA, ProviderKind.LM_STUDIO, ProviderKind.LOCAL -> "llama3.2"
        ProviderKind.GEMINI -> "gemini-1.5-flash"
        else -> "gpt-4o-mini"
    }

    private fun isRateLimit(e: Throwable): Boolean {
        val msg = e.message?.lowercase() ?: return false
        return msg.contains("429") || msg.contains("rate limit") || msg.contains("quota") ||
            msg.contains("too many") || msg.contains("insufficient_quota")
    }

    private fun ChatEntity.toDomain(): Chat = Chat(id, title, pinned, createdAt, updatedAt)

    private fun MessageEntity.toDomain(): Message = Message(
        id = id, chatId = chatId, role = MessageRole.valueOf(role), content = content,
        createdAt = createdAt, attachmentPaths = attachmentPaths, error = error,
        model = model, providerId = providerId,
    )
}
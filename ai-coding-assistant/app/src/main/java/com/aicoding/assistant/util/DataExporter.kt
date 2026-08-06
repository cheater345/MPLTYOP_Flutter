package com.aicoding.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.aicoding.assistant.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DataExporter {

    suspend fun export(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val chats = db.chatDao().observeAll().first()
            val prompts = db.promptDao().observeAll().first()

            val root = JSONObject()
            root.put("app", "AICodingAssistant")
            root.put("version", 1)

            val chatArr = JSONArray()
            chats.forEach { c ->
                val messages = db.messageDao().observeForChat(c.id).first()
                val msgArr = JSONArray()
                messages.forEach { m ->
                    msgArr.put(
                        JSONObject()
                            .put("role", m.role)
                            .put("content", m.content)
                            .put("createdAt", m.createdAt)
                    )
                }
                chatArr.put(
                    JSONObject()
                        .put("id", c.id)
                        .put("title", c.title)
                        .put("pinned", c.pinned)
                        .put("createdAt", c.createdAt)
                        .put("updatedAt", c.updatedAt)
                        .put("messages", msgArr)
                )
            }
            root.put("chats", chatArr)

            val promptArr = JSONArray()
            prompts.forEach { p ->
                promptArr.put(
                    JSONObject()
                        .put("title", p.title)
                        .put("content", p.content)
                        .put("category", p.category)
                        .put("favorite", p.favorite)
                        .put("createdAt", p.createdAt)
                )
            }
            root.put("prompts", promptArr)

            val file = File(context.getExternalFilesDir(null), "aicoding_backup_${System.currentTimeMillis()}.json")
            file.writeText(root.toString(2))
            shareFile(context, file)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export backup"))
    }
}
package com.aicoding.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.aicoding.assistant.data.local.AppDatabase
import com.aicoding.assistant.util.SeedDefaults
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AiCodingApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        seedDefaults()
    }

    private fun seedDefaults() {
        appScope.launch {
            SeedDefaults.seedProviders(applicationContext, AppDatabase.getInstance(applicationContext).providerDao())
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "assistant_stream",
            "AI Streaming",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Streaming responses in the background" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
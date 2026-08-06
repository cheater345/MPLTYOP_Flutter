package com.aicoding.assistant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

data class AppSettings(
    val themeMode: String = ThemeMode.SYSTEM.name,
    val dynamicColor: Boolean = true,
    val fontSizeScale: Float = 1f,
    val animationSpeed: Float = 1f,
    val defaultProviderId: Long = -1,
    val defaultModel: String = "",
    val language: String = "en",
    val notificationsEnabled: Boolean = false,
    val proxyUrl: String = "",
    val streamingEnabled: Boolean = true,
    val highlightEnabled: Boolean = true,
    val markdownTablesEnabled: Boolean = true,
)

object SettingsStore {

    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC = booleanPreferencesKey("dynamic_color")
    private val KEY_FONT = floatPreferencesKey("font_scale")
    private val KEY_ANIM = floatPreferencesKey("animation_speed")
    private val KEY_DEFAULT_PROVIDER = longPreferencesKey("default_provider")
    private val KEY_MODEL = stringPreferencesKey("default_model")
    private val KEY_LANG = stringPreferencesKey("language")
    private val KEY_NOTIF = booleanPreferencesKey("notifications_enabled")
    private val KEY_PROXY = stringPreferencesKey("proxy_url")
    private val KEY_STREAM = booleanPreferencesKey("streaming_enabled")
    private val KEY_HIGHLIGHT = booleanPreferencesKey("highlight_enabled")
    private val KEY_TABLES = booleanPreferencesKey("markdown_tables")

    fun flow(context: Context): Flow<AppSettings> =
        context.dataStore.data.map { p ->
            AppSettings(
                themeMode = p[KEY_THEME] ?: ThemeMode.SYSTEM.name,
                dynamicColor = p[KEY_DYNAMIC] ?: true,
                fontSizeScale = p[KEY_FONT] ?: 1f,
                animationSpeed = p[KEY_ANIM] ?: 1f,
                defaultProviderId = p[KEY_DEFAULT_PROVIDER] ?: -1,
                defaultModel = p[KEY_MODEL] ?: "",
                language = p[KEY_LANG] ?: "en",
                notificationsEnabled = p[KEY_NOTIF] ?: false,
                proxyUrl = p[KEY_PROXY] ?: "",
                streamingEnabled = p[KEY_STREAM] ?: true,
                highlightEnabled = p[KEY_HIGHLIGHT] ?: true,
                markdownTablesEnabled = p[KEY_TABLES] ?: true,
            )
        }

    suspend fun setTheme(context: Context, value: String) =
        context.dataStore.edit { it[KEY_THEME] = value }

    suspend fun setDynamicColor(context: Context, value: Boolean) =
        context.dataStore.edit { it[KEY_DYNAMIC] = value }

    suspend fun setFontScale(context: Context, value: Float) =
        context.dataStore.edit { it[KEY_FONT] = value }

    suspend fun setAnimationSpeed(context: Context, value: Float) =
        context.dataStore.edit { it[KEY_ANIM] = value }

    suspend fun setDefaultProvider(context: Context, value: Long) =
        context.dataStore.edit { it[KEY_DEFAULT_PROVIDER] = value }

    suspend fun setDefaultModel(context: Context, value: String) =
        context.dataStore.edit { it[KEY_MODEL] = value }

    suspend fun setLanguage(context: Context, value: String) =
        context.dataStore.edit { it[KEY_LANG] = value }

    suspend fun setNotifications(context: Context, value: Boolean) =
        context.dataStore.edit { it[KEY_NOTIF] = value }

    suspend fun setProxy(context: Context, value: String) =
        context.dataStore.edit { it[KEY_PROXY] = value }

    suspend fun setStreaming(context: Context, value: Boolean) =
        context.dataStore.edit { it[KEY_STREAM] = value }

    suspend fun setHighlight(context: Context, value: Boolean) =
        context.dataStore.edit { it[KEY_HIGHLIGHT] = value }

    suspend fun setTables(context: Context, value: Boolean) =
        context.dataStore.edit { it[KEY_TABLES] = value }
}
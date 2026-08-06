package com.aicoding.assistant.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.local.SettingsStore
import com.aicoding.assistant.data.local.ThemeMode
import com.aicoding.assistant.data.local.AppSettings
import com.aicoding.assistant.util.DataExporter
import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        SettingsStore.flow(app)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun cycleTheme() {
        val current = ThemeMode.valueOf(settings.value.themeMode)
        val next = ThemeMode.entries[(current.ordinal + 1) % ThemeMode.entries.size]
        viewModelScope.launch { SettingsStore.setTheme(app, next.name) }
    }

    fun setDynamicColor(value: Boolean) = viewModelScope.launch { SettingsStore.setDynamicColor(app, value) }
    fun setFontScale(value: Float) = viewModelScope.launch { SettingsStore.setFontScale(app, value) }
    fun setAnimationSpeed(value: Float) = viewModelScope.launch { SettingsStore.setAnimationSpeed(app, value) }
    fun setStreaming(value: Boolean) = viewModelScope.launch { SettingsStore.setStreaming(app, value) }
    fun setHighlight(value: Boolean) = viewModelScope.launch { SettingsStore.setHighlight(app, value) }
    fun setTables(value: Boolean) = viewModelScope.launch { SettingsStore.setTables(app, value) }
    fun setNotifications(value: Boolean) = viewModelScope.launch { SettingsStore.setNotifications(app, value) }

    fun exportData() {
        viewModelScope.launch {
            DataExporter.export(app)
        }
    }
}
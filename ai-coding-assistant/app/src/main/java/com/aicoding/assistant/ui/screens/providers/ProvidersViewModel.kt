package com.aicoding.assistant.ui.screens.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.repository.ProviderRepository
import com.aicoding.assistant.domain.model.Provider
import com.aicoding.assistant.domain.model.ProviderKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProvidersViewModel @Inject constructor(
    private val repository: ProviderRepository,
) : ViewModel() {

    val providers: StateFlow<List<Provider>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(id, enabled) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}

@HiltViewModel
class ProviderEditViewModel @Inject constructor(
    private val repository: ProviderRepository,
) : ViewModel() {

    fun load(id: Long, onLoaded: (Provider) -> Unit) {
        viewModelScope.launch {
            repository.getById(id)?.let(onLoaded)
        }
    }

    fun save(provider: Provider, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.upsert(provider)
            onDone()
        }
    }

    fun factory(): Provider = Provider(
        id = 0, name = "", kind = ProviderKind.OPENAI_COMPAT, baseUrl = "", apiKey = "",
        temperature = 0.7f, topP = 1f, maxTokens = 2048, stream = true,
        timeoutSeconds = 120, headerJson = "", enabled = true, systemPrompt = ""
    )
}
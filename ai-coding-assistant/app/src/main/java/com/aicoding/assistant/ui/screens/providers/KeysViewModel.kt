package com.aicoding.assistant.ui.screens.providers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.repository.ApiKeyRepository
import com.aicoding.assistant.domain.model.ApiKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeysViewModel @Inject constructor(
    private val repository: ApiKeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val providerId: Long = checkNotNull(savedStateHandle["providerId"])

    private val _keys = MutableStateFlow<List<ApiKey>>(emptyList())
    val keys: StateFlow<List<ApiKey>> = _keys

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _keys.value = repository.getForProvider(providerId).sortedByDescending { it.priority }
        }
    }

    fun add(value: String, priority: Int = 0) {
        viewModelScope.launch {
            repository.add(providerId, value, priority)
            reload()
        }
    }

    fun toggle(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(id, enabled)
            reload()
        }
    }

    fun movePriority(id: Long, delta: Int) {
        viewModelScope.launch {
            val keys = repository.getForProvider(providerId).sortedByDescending { it.priority }
            val idx = keys.indexOfFirst { it.id == id }
            if (idx == -1) return@launch
            val other = keys.getOrNull(idx + delta) ?: return@launch
            repository.setPriority(id, other.priority)
            repository.setPriority(other.id, keys[idx].priority)
            reload()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            reload()
        }
    }
}
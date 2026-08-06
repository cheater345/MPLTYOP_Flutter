package com.aicoding.assistant.ui.screens.prompts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.repository.PromptRepository
import com.aicoding.assistant.domain.model.Prompt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromptsViewModel @Inject constructor(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val prompts: StateFlow<List<Prompt>> = _query
        .flatMapLatest { q -> repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun toggleFavorite(id: Long, fav: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(id, fav) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}

@HiltViewModel
class PromptEditViewModel @Inject constructor(
    private val repository: PromptRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val promptId: Long = savedStateHandle["id"] ?: 0L

    fun load(onLoaded: (Prompt) -> Unit) {
        viewModelScope.launch {
            repository.getById(promptId)?.let(onLoaded)
        }
    }

    fun save(title: String, content: String, category: String, favorite: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val existing = if (promptId != 0L) repository.getById(promptId) else null
            repository.upsert(
                Prompt(
                    id = promptId, title = title, content = content, category = category,
                    favorite = favorite,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                )
            )
            onDone()
        }
    }
}
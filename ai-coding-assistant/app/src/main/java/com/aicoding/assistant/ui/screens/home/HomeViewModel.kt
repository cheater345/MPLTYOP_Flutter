package com.aicoding.assistant.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicoding.assistant.data.repository.ChatRepository
import com.aicoding.assistant.domain.model.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val chats: StateFlow<List<Chat>> = _query
        .flatMapLatest { q -> chatRepository.searchChats(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun newChat(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            onCreated(chatRepository.createChat())
        }
    }

    fun deleteChat(id: Long) {
        viewModelScope.launch { chatRepository.deleteChat(id) }
    }
}
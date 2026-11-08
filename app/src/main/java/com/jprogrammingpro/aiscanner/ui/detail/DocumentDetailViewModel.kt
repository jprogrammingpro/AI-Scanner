package com.jprogrammingpro.aiscanner.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jprogrammingpro.aiscanner.data.DocumentRepository
import com.jprogrammingpro.aiscanner.data.DocumentWithPages
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailState {
    data object Loading : DetailState
    data object Missing : DetailState
    data class Ready(val doc: DocumentWithPages) : DetailState
}

class DocumentDetailViewModel(
    private val documentId: Long,
    private val repository: DocumentRepository,
) : ViewModel() {
    val state: StateFlow<DetailState> = repository.observe(documentId)
        .map { if (it == null) DetailState.Missing else DetailState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState.Loading)

    fun rename(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.rename(documentId, title.trim()) }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(documentId)
            onDone()
        }
    }
}

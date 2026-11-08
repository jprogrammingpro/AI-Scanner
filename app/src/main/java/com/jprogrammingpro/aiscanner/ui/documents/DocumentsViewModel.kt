package com.jprogrammingpro.aiscanner.ui.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jprogrammingpro.aiscanner.data.DocumentRepository
import com.jprogrammingpro.aiscanner.data.DocumentWithPages
import com.jprogrammingpro.aiscanner.data.PageStorage
import com.jprogrammingpro.aiscanner.domain.ScanMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** null documents = still loading (skeletons are shown). */
data class DocumentsUiState(
    val documents: List<DocumentWithPages>? = null,
    val query: String = "",
)

class DocumentsViewModel(
    private val repository: DocumentRepository,
    private val storage: PageStorage,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val state: StateFlow<DocumentsUiState> =
        combine(repository.observeAll(), query) { docs, q ->
            val filtered = if (q.isBlank()) docs else docs.filter { it.document.title.contains(q.trim(), ignoreCase = true) }
            DocumentsUiState(filtered, q)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentsUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun import(uris: List<Uri>, title: String, onCreated: (Long) -> Unit) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val paths = uris.mapNotNull { storage.importFrom(it)?.absolutePath }
            if (paths.isNotEmpty()) onCreated(repository.create(title, ScanMode.PHOTO.name, paths))
        }
    }
}

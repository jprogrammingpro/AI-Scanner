package com.jprogrammingpro.aiscanner.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jprogrammingpro.aiscanner.data.DocumentRepository
import com.jprogrammingpro.aiscanner.data.PageStorage
import com.jprogrammingpro.aiscanner.data.SettingsRepository
import com.jprogrammingpro.aiscanner.domain.ScanMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class CameraUiState(
    val mode: ScanMode = ScanMode.DOCUMENT,
    val pages: List<String> = emptyList(),
    val flashOn: Boolean = false,
    val capturing: Boolean = false,
    val saving: Boolean = false,
)

class CameraViewModel(
    private val documents: DocumentRepository,
    private val storage: PageStorage,
    settings: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val start = settings.settings.first().defaultMode
            _state.update { it.copy(mode = start) }
        }
    }

    fun selectMode(mode: ScanMode) = _state.update { it.copy(mode = mode) }

    fun toggleFlash() = _state.update { it.copy(flashOn = !it.flashOn) }

    /** Returns the file the camera should write into, or null if busy. */
    fun beginCapture(): File? {
        if (_state.value.capturing || _state.value.saving) return null
        _state.update { it.copy(capturing = true) }
        return storage.newPageFile()
    }

    fun captureSucceeded(file: File) =
        _state.update { it.copy(capturing = false, pages = it.pages + file.absolutePath) }

    fun captureFailed(file: File) {
        file.delete()
        _state.update { it.copy(capturing = false) }
    }

    fun discardPages() {
        val paths = _state.value.pages
        viewModelScope.launch { storage.delete(paths) }
        _state.update { it.copy(pages = emptyList()) }
    }

    fun finish(title: String, onCreated: (Long) -> Unit) {
        val current = _state.value
        if (current.pages.isEmpty() || current.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val id = documents.create(title, current.mode.name, current.pages)
            _state.update { it.copy(saving = false, pages = emptyList()) }
            onCreated(id)
        }
    }
}

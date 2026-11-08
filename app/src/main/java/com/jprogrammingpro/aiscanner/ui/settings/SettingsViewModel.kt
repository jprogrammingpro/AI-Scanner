package com.jprogrammingpro.aiscanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jprogrammingpro.aiscanner.data.AppSettings
import com.jprogrammingpro.aiscanner.data.SettingsRepository
import com.jprogrammingpro.aiscanner.domain.ScanMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setAutoCapture(value: Boolean) = viewModelScope.launch { repository.setAutoCapture(value) }
    fun setHaptics(value: Boolean) = viewModelScope.launch { repository.setHaptics(value) }
    fun setDefaultMode(mode: ScanMode) = viewModelScope.launch { repository.setDefaultMode(mode) }
}

package com.jprogrammingpro.aiscanner.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jprogrammingpro.aiscanner.domain.ScanMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val autoCapture: Boolean = true,
    val haptics: Boolean = true,
    val defaultMode: ScanMode = ScanMode.DOCUMENT,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val autoCapture = booleanPreferencesKey("auto_capture")
        val haptics = booleanPreferencesKey("haptics")
        val defaultMode = stringPreferencesKey("default_mode")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            autoCapture = prefs[Keys.autoCapture] ?: true,
            haptics = prefs[Keys.haptics] ?: true,
            defaultMode = ScanMode.fromName(prefs[Keys.defaultMode]),
        )
    }

    suspend fun setAutoCapture(value: Boolean) {
        context.dataStore.edit { it[Keys.autoCapture] = value }
    }

    suspend fun setHaptics(value: Boolean) {
        context.dataStore.edit { it[Keys.haptics] = value }
    }

    suspend fun setDefaultMode(mode: ScanMode) {
        context.dataStore.edit { it[Keys.defaultMode] = mode.name }
    }
}

package com.jprogrammingpro.aiscanner.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jprogrammingpro.aiscanner.BuildConfig
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.domain.ScanMode
import com.jprogrammingpro.aiscanner.ui.components.ScreenTitle
import com.jprogrammingpro.aiscanner.ui.components.SoonBadge

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, contentPadding: PaddingValues) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        ScreenTitle(stringResource(R.string.settings_title), Modifier.padding(top = 12.dp))

        Section(stringResource(R.string.settings_section_scan)) {
            SwitchRow(
                title = stringResource(R.string.settings_auto_capture),
                body = stringResource(R.string.settings_auto_capture_body),
                checked = settings.autoCapture,
                onChange = { viewModel.setAutoCapture(it) },
                soon = true,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 18.dp))
            ModeRow(settings.defaultMode) { viewModel.setDefaultMode(it) }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 18.dp))
            SwitchRow(
                title = stringResource(R.string.settings_haptics),
                body = stringResource(R.string.settings_haptics_body),
                checked = settings.haptics,
                onChange = { viewModel.setHaptics(it) },
            )
        }

        Section(stringResource(R.string.settings_section_pro), highlighted = true) {
            Text(
                stringResource(R.string.settings_pro_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(18.dp),
            )
        }

        Section(stringResource(R.string.settings_section_about)) {
            Column(Modifier.padding(18.dp)) {
                Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.settings_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, highlighted: Boolean = false, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp),
    )
    Surface(
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun SwitchRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit, soon: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (soon) {
                    Spacer(Modifier.width(8.dp))
                    SoonBadge()
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ModeRow(current: ScanMode, onSelect: (ScanMode) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.settings_default_mode), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(current.label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ScanMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(stringResource(mode.label)) },
                    onClick = { onSelect(mode); open = false },
                )
            }
        }
    }
}

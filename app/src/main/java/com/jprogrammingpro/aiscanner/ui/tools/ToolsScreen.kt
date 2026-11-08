package com.jprogrammingpro.aiscanner.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.ui.components.BentoCard
import com.jprogrammingpro.aiscanner.ui.components.ScreenTitle

@Composable
fun ToolsScreen(contentPadding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        ScreenTitle(stringResource(R.string.tools_title), Modifier.padding(top = 12.dp))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoCard(
                title = stringResource(R.string.tools_translate),
                body = stringResource(R.string.tools_translate_body),
                icon = Icons.Outlined.Translate,
                highlighted = true,
                minHeight = 150.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BentoCard(
                    title = stringResource(R.string.tools_summarize),
                    body = stringResource(R.string.tools_summarize_body),
                    icon = Icons.Outlined.Summarize,
                    minHeight = 170.dp,
                    modifier = Modifier.weight(1f),
                )
                BentoCard(
                    title = stringResource(R.string.tools_read_aloud),
                    body = stringResource(R.string.tools_read_aloud_body),
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    minHeight = 170.dp,
                    modifier = Modifier.weight(1f),
                )
            }
            BentoCard(
                title = stringResource(R.string.tools_merge),
                body = stringResource(R.string.tools_merge_body),
                icon = Icons.Outlined.CallMerge,
                minHeight = 120.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BentoCard(
                    title = stringResource(R.string.tools_signature),
                    body = stringResource(R.string.tools_signature_body),
                    icon = Icons.Outlined.Draw,
                    minHeight = 170.dp,
                    modifier = Modifier.weight(1f),
                )
                BentoCard(
                    title = stringResource(R.string.tools_qr),
                    body = stringResource(R.string.tools_qr_body),
                    icon = Icons.Outlined.QrCodeScanner,
                    minHeight = 170.dp,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

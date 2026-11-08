package com.jprogrammingpro.aiscanner.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.data.DocumentWithPages
import com.jprogrammingpro.aiscanner.ui.components.SkeletonBox
import com.jprogrammingpro.aiscanner.ui.components.SoonBadge
import com.jprogrammingpro.aiscanner.ui.components.rememberHaptic
import java.io.File

private enum class DetailTab(val label: Int, val icon: ImageVector?, val soonText: Int?) {
    PAGES(R.string.detail_tab_pages, null, null),
    TEXT(R.string.detail_tab_text, Icons.AutoMirrored.Outlined.TextSnippet, R.string.detail_text_soon),
    DATA(R.string.detail_tab_data, Icons.Outlined.DataObject, R.string.detail_data_soon),
    AI(R.string.detail_tab_ai, Icons.Outlined.AutoAwesome, R.string.detail_ai_soon),
    EXPORT(R.string.detail_tab_export, Icons.Outlined.FileDownload, R.string.detail_export_soon),
}

@Composable
fun DocumentDetailScreen(viewModel: DocumentDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        DetailState.Loading -> Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            SkeletonBox(Modifier.fillMaxWidth().height(48.dp))
            Spacer(Modifier.height(16.dp))
            SkeletonBox(Modifier.fillMaxWidth().weight(1f))
        }
        DetailState.Missing -> {
            // deleted or unknown id
            androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }
        }
        is DetailState.Ready -> DetailContent(s.doc, viewModel, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(doc: DocumentWithPages, viewModel: DocumentDetailViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = rememberHaptic()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val pages = doc.sortedPages

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
            Text(
                doc.document.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showRename = true }) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.detail_rename))
            }
            IconButton(onClick = { haptic(); sharePages(context, pages.map { it.imagePath }) }) {
                Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.detail_share))
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.detail_delete))
            }
        }

        PrimaryScrollableTabRow(
            selectedTabIndex = tab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            divider = {},
        ) {
            DetailTab.entries.forEachIndexed { index, t ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(stringResource(t.label)) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedContent(
            targetState = DetailTab.entries[tab],
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.weight(1f),
            label = "detailTab",
        ) { current ->
            if (current == DetailTab.PAGES) {
                PagesPager(pages.map { it.imagePath })
            } else {
                ComingSoon(current.icon!!, stringResource(current.soonText!!))
            }
        }
    }

    if (showRename) {
        var text by remember { mutableStateOf(doc.document.title) }
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(stringResource(R.string.detail_rename)) },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = { viewModel.rename(text); showRename = false }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.detail_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; haptic(); viewModel.delete(onBack) }) {
                    Text(stringResource(R.string.detail_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun PagesPager(paths: List<String>) {
    val pagerState = rememberPagerState { paths.size }
    Column(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp, vertical = 16.dp),
            modifier = Modifier.weight(1f),
        ) { page ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxSize(),
            ) {
                AsyncImage(
                    model = File(paths[page]),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp).clip(MaterialTheme.shapes.small),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(paths.size) { index ->
                val active = index == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ComingSoon(icon: ImageVector, text: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(18.dp))
        }
        Spacer(Modifier.height(16.dp))
        SoonBadge()
        Spacer(Modifier.height(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun sharePages(context: Context, paths: List<String>) {
    if (paths.isEmpty()) return
    val authority = "${context.packageName}.files"
    val uris = ArrayList<Uri>(paths.map { FileProvider.getUriForFile(context, authority, File(it)) })
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/jpeg"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

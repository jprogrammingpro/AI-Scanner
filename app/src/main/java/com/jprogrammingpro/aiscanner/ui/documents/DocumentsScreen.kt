package com.jprogrammingpro.aiscanner.ui.documents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.data.DocumentWithPages
import com.jprogrammingpro.aiscanner.ui.components.SkeletonBox
import com.jprogrammingpro.aiscanner.ui.components.pressScale
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    contentPadding: PaddingValues,
    onOpenDocument: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importTitle = stringResource(
        R.string.document_import_title,
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
    )
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        viewModel.import(uris, importTitle, onOpenDocument)
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.documents_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                IconButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = stringResource(R.string.documents_import))
                }
            }
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            SearchField(state.query, viewModel::setQuery)
        }

        val docs = state.documents
        when {
            docs == null -> items(6) { index ->
                SkeletonBox(Modifier.fillMaxWidth().height(if (index % 3 == 0) 230.dp else 190.dp))
            }
            docs.isEmpty() && state.query.isNotBlank() -> item(span = StaggeredGridItemSpan.FullLine) {
                EmptyState(stringResource(R.string.documents_no_results, state.query), null)
            }
            docs.isEmpty() -> item(span = StaggeredGridItemSpan.FullLine) {
                EmptyState(stringResource(R.string.documents_empty_title), stringResource(R.string.documents_empty_body))
            }
            else -> items(docs, key = { it.document.id }) { doc ->
                DocumentCard(doc, onClick = { onOpenDocument(doc.document.id) })
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.documents_search)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    )
}

@Composable
private fun EmptyState(title: String, body: String?) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 64.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(20.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (body != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DocumentCard(doc: DocumentWithPages, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val first = doc.sortedPages.firstOrNull()
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(MaterialTheme.shapes.medium)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (doc.document.id % 3 == 0L) 0.72f else 0.9f)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.small),
            ) {
                if (first != null) {
                    AsyncImage(
                        model = File(first.imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    SkeletonBox(Modifier.fillMaxSize())
                }
            }
            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 2.dp)) {
                Text(
                    doc.document.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(doc.document.updatedAt)) +
                        " · " + pluralStringResource(R.plurals.page_count, doc.pages.size, doc.pages.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

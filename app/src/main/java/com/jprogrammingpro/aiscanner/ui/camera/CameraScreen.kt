package com.jprogrammingpro.aiscanner.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.domain.ScanMode
import com.jprogrammingpro.aiscanner.ui.components.pressScale
import com.jprogrammingpro.aiscanner.ui.components.rememberHaptic
import com.jprogrammingpro.aiscanner.ui.theme.Orange
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onOpenDocuments: () -> Unit,
    onDocumentCreated: (Long) -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            CameraContent(viewModel, onOpenDocuments, onDocumentCreated)
        } else {
            PermissionPrompt(
                onAllow = { launcher.launch(Manifest.permission.CAMERA) },
                onOpenDocuments = onOpenDocuments,
            )
        }
    }
}

@Composable
private fun PermissionPrompt(onAllow: () -> Unit, onOpenDocuments: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.camera_permission_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.camera_permission_body),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onAllow, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
            Text(stringResource(R.string.camera_permission_button))
        }
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = onOpenDocuments) {
            Icon(Icons.Outlined.Folder, contentDescription = stringResource(R.string.camera_library), tint = Color.White)
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CameraViewModel,
    onOpenDocuments: () -> Unit,
    onDocumentCreated: (Long) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    val flashOverlay = remember { Animatable(0f) }
    var error by remember { mutableStateOf(false) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        }
    }
    LaunchedEffect(lifecycleOwner) { controller.bindToLifecycle(lifecycleOwner) }
    LaunchedEffect(state.flashOn) {
        controller.imageCaptureFlashMode = if (state.flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    val defaultTitle = stringResource(
        R.string.document_default_title,
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
    )

    fun capture() {
        val file: File = viewModel.beginCapture() ?: return
        haptic()
        scope.launch {
            flashOverlay.snapTo(0.8f)
            flashOverlay.animateTo(0f, tween(220))
        }
        controller.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    error = false
                    viewModel.captureSucceeded(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    error = true
                    viewModel.captureFailed(file)
                }
            },
        )
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (state.mode != ScanMode.PHOTO) {
            ScanGuide(
                aspect = when (state.mode) {
                    ScanMode.BUSINESS_CARD, ScanMode.ID_CARD -> 1.586f
                    ScanMode.QR -> 1f
                    ScanMode.BOOK, ScanMode.WHITEBOARD -> 1.414f
                    else -> 0.707f
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // white flash when a page is captured
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = flashOverlay.value }.background(Color.White))

        // Top bar
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                onClick = viewModel::toggleFlash,
                content = {
                    Icon(
                        if (state.flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = stringResource(R.string.camera_flash),
                        tint = if (state.flashOn) Orange else Color.White,
                    )
                },
            )
            Spacer(Modifier.width(12.dp))
            Surface(color = Color.Black.copy(alpha = 0.45f), shape = CircleShape) {
                Text(
                    text = stringResource(
                        when {
                            error -> R.string.camera_capture_failed
                            state.mode == ScanMode.PHOTO -> R.string.camera_hint_photo
                            !state.mode.tuned -> R.string.camera_hint_soon
                            else -> R.string.camera_hint_document
                        },
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        // Bottom controls
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .navigationBarsPadding()
                .padding(bottom = 16.dp, top = 12.dp),
        ) {
            ModeBar(selected = state.mode, onSelect = { haptic(); viewModel.selectMode(it) })
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    PageStack(
                        pages = state.pages,
                        onOpenDocuments = onOpenDocuments,
                        onDiscard = viewModel::discardPages,
                    )
                }
                Shutter(enabled = !state.capturing && !state.saving, onClick = ::capture)
                Box(Modifier.size(width = 96.dp, height = 64.dp), contentAlignment = Alignment.CenterEnd) {
                    // fully qualified: inside Row the RowScope overload would be picked otherwise
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.pages.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                    ) {
                        Button(
                            onClick = { haptic(); viewModel.finish(defaultTitle, onDocumentCreated) },
                            enabled = !state.saving,
                            colors = ButtonDefaults.buttonColors(containerColor = Orange),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            Text(stringResource(if (state.saving) R.string.camera_saving else R.string.camera_done))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ModeBar(selected: ScanMode, onSelect: (ScanMode) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(selected) {
        listState.animateScrollToItem((selected.ordinal - 1).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ScanMode.entries) { mode ->
            val isSelected = mode == selected
            Surface(
                color = if (isSelected) Orange else Color.White.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.clip(CircleShape).clickable { onSelect(mode) },
            ) {
                Text(
                    stringResource(mode.label),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Shutter(enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.camera_shutter)
    Box(
        Modifier
            .size(78.dp)
            .semantics { contentDescription = label }
            .pressScale(interaction, pressedScale = 0.88f)
            .border(BorderStroke(4.dp, Color.White), CircleShape)
            .padding(7.dp)
            .clip(CircleShape)
            .background(if (enabled) Orange else Orange.copy(alpha = 0.5f))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
    )
}

@Composable
private fun PageStack(pages: List<String>, onOpenDocuments: () -> Unit, onDiscard: () -> Unit) {
    if (pages.isEmpty()) {
        GlassIconButton(onClick = onOpenDocuments) {
            Icon(Icons.Outlined.Folder, contentDescription = stringResource(R.string.camera_library), tint = Color.White)
        }
        return
    }
    Box {
        AsyncImage(
            model = File(pages.last()),
            contentDescription = stringResource(R.string.camera_pages, pages.size),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
        )
        Surface(
            color = Orange,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp),
        ) {
            Text(
                pages.size.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = 8.dp)
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onDiscard),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.camera_discard),
                tint = Color.White,
                modifier = Modifier.padding(4.dp),
            )
        }
    }
}

/** Orange corner brackets that show where to place the document. */
@Composable
private fun ScanGuide(aspect: Float, modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(aspect),
    ) {
        val len = size.minDimension * 0.12f
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        val corners = listOf(
            Offset(0f, 0f) to Pair(Offset(len, 0f), Offset(0f, len)),
            Offset(w, 0f) to Pair(Offset(w - len, 0f), Offset(w, len)),
            Offset(0f, h) to Pair(Offset(len, h), Offset(0f, h - len)),
            Offset(w, h) to Pair(Offset(w - len, h), Offset(w, h - len)),
        )
        corners.forEach { (corner, ends) ->
            drawLine(Orange, corner, ends.first, strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(Orange, corner, ends.second, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

package com.jprogrammingpro.aiscanner.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jprogrammingpro.aiscanner.AppContainer
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.data.AppSettings
import com.jprogrammingpro.aiscanner.ui.camera.CameraScreen
import com.jprogrammingpro.aiscanner.ui.camera.CameraViewModel
import com.jprogrammingpro.aiscanner.ui.components.LocalHapticsEnabled
import com.jprogrammingpro.aiscanner.ui.components.pressScale
import com.jprogrammingpro.aiscanner.ui.components.rememberHaptic
import com.jprogrammingpro.aiscanner.ui.detail.DocumentDetailScreen
import com.jprogrammingpro.aiscanner.ui.detail.DocumentDetailViewModel
import com.jprogrammingpro.aiscanner.ui.documents.DocumentsScreen
import com.jprogrammingpro.aiscanner.ui.documents.DocumentsViewModel
import com.jprogrammingpro.aiscanner.ui.insights.InsightsScreen
import com.jprogrammingpro.aiscanner.ui.settings.SettingsScreen
import com.jprogrammingpro.aiscanner.ui.settings.SettingsViewModel
import com.jprogrammingpro.aiscanner.ui.tools.ToolsScreen

object Routes {
    const val CAMERA = "camera"
    const val DOCUMENTS = "documents"
    const val TOOLS = "tools"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val DETAIL = "document/{id}"
    fun detail(id: Long) = "document/$id"
}

private data class TabItem(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val leftTabs = listOf(
    TabItem(Routes.DOCUMENTS, R.string.nav_documents, Icons.Outlined.Folder, Icons.Filled.Folder),
    TabItem(Routes.TOOLS, R.string.nav_tools, Icons.Outlined.Handyman, Icons.Filled.Handyman),
)
private val rightTabs = listOf(
    TabItem(Routes.INSIGHTS, R.string.nav_insights, Icons.Outlined.Insights, Icons.Filled.Insights),
    TabItem(Routes.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings),
)
private val tabRoutes = (leftTabs + rightTabs).map { it.route }.toSet()

@Composable
private inline fun <reified VM : ViewModel> appViewModel(key: String? = null, crossinline create: () -> VM): VM =
    viewModel(key = key, factory = viewModelFactory { initializer { create() } })

@Composable
fun AiScannerRoot(container: AppContainer) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val bottomPadding = PaddingValues(bottom = 96.dp)

    CompositionLocalProvider(LocalHapticsEnabled provides settings.haptics) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NavHost(
                navController = navController,
                startDestination = Routes.CAMERA,
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(160)) },
            ) {
                composable(Routes.CAMERA) {
                    val vm = appViewModel { CameraViewModel(container.documents, container.pageStorage, container.settings) }
                    CameraScreen(
                        viewModel = vm,
                        onOpenDocuments = { navController.navigateTab(Routes.DOCUMENTS) },
                        onDocumentCreated = { id -> navController.navigate(Routes.detail(id)) },
                    )
                }
                composable(Routes.DOCUMENTS) {
                    val vm = appViewModel { DocumentsViewModel(container.documents, container.pageStorage) }
                    DocumentsScreen(vm, bottomPadding, onOpenDocument = { navController.navigate(Routes.detail(it)) })
                }
                composable(Routes.TOOLS) { ToolsScreen(bottomPadding) }
                composable(Routes.INSIGHTS) { InsightsScreen(bottomPadding) }
                composable(Routes.SETTINGS) {
                    val vm = appViewModel { SettingsViewModel(container.settings) }
                    SettingsScreen(vm, bottomPadding)
                }
                composable(
                    Routes.DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.LongType }),
                ) { entry ->
                    val id = entry.arguments?.getLong("id") ?: 0L
                    val vm = appViewModel(key = "detail-$id") { DocumentDetailViewModel(id, container.documents) }
                    DocumentDetailScreen(vm, onBack = { navController.popBackStack() })
                }
            }

            AnimatedVisibility(
                visible = route in tabRoutes,
                enter = slideInVertically(tween(240)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(150)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                BottomBar(
                    current = route,
                    onTab = { navController.navigateTab(it) },
                    onScan = { navController.navigateTab(Routes.CAMERA) },
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(current: String?, onTab: (String) -> Unit, onScan: () -> Unit) {
    val haptic = rememberHaptic()
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            // translucent "frosted" bar floating above content
            color = colors.surfaceContainerLow.copy(alpha = 0.92f),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                leftTabs.forEach { TabButton(it, current == it.route) { haptic(); onTab(it.route) } }
                Box(Modifier.size(64.dp))
                rightTabs.forEach { TabButton(it, current == it.route) { haptic(); onTab(it.route) } }
            }
        }
        ScanButton(onClick = { haptic(); onScan() })
    }
}

@Composable
private fun TabButton(item: TabItem, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .pressScale(interaction, 0.9f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            if (selected) item.selectedIcon else item.icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
        )
        Text(
            stringResource(item.label),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun ScanButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .pressScale(interaction, 0.9f)
            .size(64.dp)
            .shadow(12.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.DocumentScanner,
            contentDescription = stringResource(R.string.nav_scan),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}

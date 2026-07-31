package com.shinytracker.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.checklist.api.ChecklistRoute
import com.shinytracker.feature.checklist.impl.checklistNavGraph
import com.shinytracker.feature.scan.impl.BoxScanAccessibilityService
import com.shinytracker.feature.scan.impl.ScanWidgetOverlayService
import dagger.hilt.android.AndroidEntryPoint

private const val ONBOARDING_ROUTE = "onboarding"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isServiceEnabled by mutableStateOf(false)
    private var overlayPermissionGranted by mutableStateOf(false)
    private var isWidgetRunning by mutableStateOf(false)
    private var pendingSharedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSharedProfileUri = parseIncomingProfileIntent(intent)
        isServiceEnabled = isBoxScanServiceEnabled()
        overlayPermissionGranted = isOverlayPermissionGranted()
        setContent {
            val navController = rememberNavController()
            val startDestination =
                remember {
                    if (isServiceEnabled && overlayPermissionGranted) ChecklistRoute.OWNER else ONBOARDING_ROUTE
                }
            ShinyTheme {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable(ONBOARDING_ROUTE) {
                        OnboardingScreen(
                            accessibilityGranted = isServiceEnabled,
                            overlayGranted = overlayPermissionGranted,
                            onOpenAccessibilitySettings = { openAccessibilitySettings() },
                            onOpenOverlaySettings = { openOverlaySettings() },
                        )
                    }
                    checklistNavGraph(
                        onExportProfile = { uri -> shareProfileFile(uri) },
                        onToggleWidget = { onToggleWidget() },
                        isWidgetRunning = isWidgetRunning,
                    )
                }
                LaunchedEffect(pendingSharedProfileUri) {
                    pendingSharedProfileUri?.let { uri ->
                        navController.navigate(ChecklistRoute.sharedRoute(uri))
                        pendingSharedProfileUri = null
                    }
                }
                LaunchedEffect(isServiceEnabled, overlayPermissionGranted, pendingSharedProfileUri) {
                    if (pendingSharedProfileUri != null) return@LaunchedEffect
                    val current = navController.currentDestination?.route ?: return@LaunchedEffect
                    val bothGranted = isServiceEnabled && overlayPermissionGranted
                    when {
                        bothGranted && current == ONBOARDING_ROUTE -> {
                            navController.navigate(ChecklistRoute.OWNER) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }

                        !bothGranted && current != ONBOARDING_ROUTE && current != ChecklistRoute.SHARED_PATTERN -> {
                            navController.navigate(ONBOARDING_ROUTE) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIncomingProfileIntent(intent)?.let { pendingSharedProfileUri = it }
    }

    private fun shareProfileFile(uri: Uri) {
        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        startActivity(Intent.createChooser(sendIntent, "Share your shiny checklist"))
    }

    override fun onResume() {
        super.onResume()
        isServiceEnabled = isBoxScanServiceEnabled()
        overlayPermissionGranted = isOverlayPermissionGranted()
        isWidgetRunning = ScanWidgetOverlayService.isRunning
    }

    private fun isBoxScanServiceEnabled(): Boolean {
        val am = getSystemService(AccessibilityManager::class.java) ?: return false
        return am
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.id.contains(BoxScanAccessibilityService::class.java.simpleName) }
    }

    private fun isOverlayPermissionGranted(): Boolean = Settings.canDrawOverlays(this)

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    private fun openOverlaySettings() {
        val uri = Uri.parse("package:$packageName")
        startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
        )
    }

    private fun onToggleWidget() {
        if (!isOverlayPermissionGranted()) {
            openOverlaySettings()
            return
        }
        val intent = Intent(this, ScanWidgetOverlayService::class.java)
        if (isWidgetRunning) stopService(intent) else startService(intent)
        isWidgetRunning = !isWidgetRunning
    }
}

/** Parses an incoming shared-profile file Intent (ACTION_VIEW or ACTION_SEND, application/json). */
private fun parseIncomingProfileIntent(intent: Intent?): Uri? {
    if (intent == null) return null
    return when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        else -> null
    }
}

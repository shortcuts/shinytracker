package com.shinytracker.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.checklist.api.ChecklistRoute
import com.shinytracker.feature.checklist.impl.checklistNavGraph
import com.shinytracker.feature.scan.api.BoxScanBridge
import com.shinytracker.feature.scan.impl.BoxScanAccessibilityService
import com.shinytracker.feature.scan.impl.ScanOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

private const val SCAN_ROUTE = "scan"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var boxScanBridge: BoxScanBridge

    @Inject lateinit var scanOrchestrator: ScanOrchestrator

    private var isServiceEnabled by mutableStateOf(false)
    private var statusText by mutableStateOf("")
    private var pendingSharedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSharedProfileUri = parseIncomingProfileIntent(intent)
        setContent {
            val scanResults by scanOrchestrator.scanResults.collectAsStateWithLifecycle()
            val reviewQueue by scanOrchestrator.reviewQueue.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            ShinyTheme {
                NavHost(navController = navController, startDestination = SCAN_ROUTE) {
                    composable(SCAN_ROUTE) {
                        ShinyApp(
                            isServiceEnabled = isServiceEnabled,
                            statusText = statusText,
                            scanResults = scanResults,
                            reviewQueueSize = reviewQueue.size,
                            onOpenAccessibilitySettings = { openAccessibilitySettings() },
                            onCaptureScreenshot = {
                                lifecycleScope.launch { statusText = captureAndSaveScreenshot() }
                            },
                            onScrollBoxDown = {
                                lifecycleScope.launch {
                                    statusText = if (boxScanBridge.scrollBoxDown()) "Scroll dispatched" else "Scroll failed"
                                }
                            },
                            onRunFullScan = {
                                lifecycleScope.launch {
                                    statusText = "Scanning..."
                                    scanOrchestrator.runFullScan()
                                    statusText = "Scan complete"
                                }
                            },
                            onOpenChecklist = { navController.navigate(ChecklistRoute.OWNER) },
                        )
                    }
                    checklistNavGraph(onExportProfile = { uri -> shareProfileFile(uri) })
                }
                LaunchedEffect(pendingSharedProfileUri) {
                    pendingSharedProfileUri?.let { uri ->
                        navController.navigate(ChecklistRoute.sharedRoute(uri))
                        pendingSharedProfileUri = null
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
    }

    private fun isBoxScanServiceEnabled(): Boolean {
        val am = getSystemService(AccessibilityManager::class.java) ?: return false
        return am
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.id.contains(BoxScanAccessibilityService::class.java.simpleName) }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    private suspend fun captureAndSaveScreenshot(): String {
        val bitmap = boxScanBridge.captureScreenshot() ?: return "Capture failed"
        return try {
            val file = File(getExternalFilesDir(null), "scan_debug_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            "Saved: ${file.name}"
        } catch (e: IOException) {
            Log.e("MainActivity", "Failed to save screenshot", e)
            "Save failed"
        } finally {
            bitmap.recycle()
        }
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

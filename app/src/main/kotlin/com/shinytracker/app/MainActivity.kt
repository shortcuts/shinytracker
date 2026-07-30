package com.shinytracker.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.scan.api.BoxScanBridge
import com.shinytracker.feature.scan.impl.BoxScanAccessibilityService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var boxScanBridge: BoxScanBridge

    private var isServiceEnabled by mutableStateOf(false)
    private var statusText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShinyTheme {
                ShinyApp(
                    isServiceEnabled = isServiceEnabled,
                    statusText = statusText,
                    onOpenAccessibilitySettings = { openAccessibilitySettings() },
                    onCaptureScreenshot = {
                        lifecycleScope.launch { statusText = captureAndSaveScreenshot() }
                    },
                    onScrollBoxDown = {
                        lifecycleScope.launch {
                            statusText = if (boxScanBridge.scrollBoxDown()) "Scroll dispatched" else "Scroll failed"
                        }
                    },
                )
            }
        }
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

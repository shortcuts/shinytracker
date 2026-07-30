package com.shinytracker.feature.scan.impl

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.shinytracker.core.designsystem.ShinyTheme
import com.shinytracker.feature.scan.api.BoxScanBridge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Floating widget overlay: screenshot-detect button, automated-scan button, and the
 * confirm/reject validation panel for both. Requires SYSTEM_ALERT_WINDOW.
 */
@AndroidEntryPoint
class ScanWidgetOverlayService :
    Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    companion object {
        private const val TAG = "ScanWidgetOverlayService"
        var isRunning: Boolean = false
            private set
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val exceptionHandler = CoroutineExceptionHandler { _, t -> Log.e(TAG, "Coroutine crashed", t) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    @Inject lateinit var boxScanBridge: BoxScanBridge

    @Inject lateinit var scanOrchestrator: ScanOrchestrator

    @Inject lateinit var scanValidationPresenter: ScanValidationPresenter

    private lateinit var windowManager: WindowManager
    private var pillView: ComposeView? = null
    private var pillParams: WindowManager.LayoutParams? = null
    private var panelView: ComposeView? = null

    private val isScanning = MutableStateFlow(false)

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addPillView()
        isRunning = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        hideValidationPanel()
        pillView?.let { view ->
            view.disposeComposition()
            if (view.isAttachedToWindow) windowManager.removeViewImmediate(view)
        }
        pillView = null
        scanValidationPresenter.clear()
        super.onDestroy()
    }

    private fun pillLayoutParams(): WindowManager.LayoutParams =
        WindowManager
            .LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = resources.displayMetrics.heightPixels / 2
            }

    private fun addPillView() {
        val view =
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@ScanWidgetOverlayService)
                setViewTreeSavedStateRegistryOwner(this@ScanWidgetOverlayService)
                setContent {
                    val scanning by isScanning.collectAsStateWithLifecycle()
                    ShinyTheme {
                        ScanWidgetPill(
                            isScanning = scanning,
                            onScreenshot = ::onScreenshotClicked,
                            onAutomatedScan = ::onAutomatedScanClicked,
                            onClose = { stopSelf() },
                            onDrag = { dx, dy -> onPillDragged(dx, dy) },
                        )
                    }
                }
            }
        val params = pillLayoutParams()
        pillView = view
        pillParams = params
        windowManager.addView(view, params)
    }

    private fun onPillDragged(
        dx: Float,
        dy: Float,
    ) {
        val view = pillView ?: return
        val params = pillParams ?: return
        val metrics = windowManager.currentWindowMetrics
        params.x = (params.x + dx.toInt()).coerceIn(0, (metrics.bounds.width() - view.width).coerceAtLeast(0))
        params.y = (params.y + dy.toInt()).coerceIn(0, (metrics.bounds.height() - view.height).coerceAtLeast(0))
        windowManager.updateViewLayout(view, params)
    }

    private fun onScreenshotClicked() {
        serviceScope.launch {
            isScanning.value = true
            val results = scanOrchestrator.captureAndDetect()
            isScanning.value = false
            if (results.isEmpty()) {
                Toast.makeText(this@ScanWidgetOverlayService, "Nothing detected", Toast.LENGTH_SHORT).show()
            } else {
                scanValidationPresenter.show(results)
                showValidationPanel()
            }
        }
    }

    private fun onAutomatedScanClicked() {
        serviceScope.launch {
            isScanning.value = true
            scanOrchestrator.runFullScan()
            isScanning.value = false
            val remaining = scanOrchestrator.reviewQueue.value
            if (remaining.isEmpty()) {
                Toast.makeText(this@ScanWidgetOverlayService, "Scan complete, nothing to review", Toast.LENGTH_SHORT).show()
            } else {
                scanValidationPresenter.show(remaining)
                showValidationPanel()
            }
        }
    }

    private fun showValidationPanel() {
        hideValidationPanel()
        val view =
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@ScanWidgetOverlayService)
                setViewTreeSavedStateRegistryOwner(this@ScanWidgetOverlayService)
                setContent {
                    val entries by scanValidationPresenter.entries.collectAsStateWithLifecycle()
                    ShinyTheme {
                        ScanValidationScreen(
                            entries = entries,
                            onConfirm = { index, chosen -> serviceScope.launch { scanValidationPresenter.confirm(index, chosen) } },
                            onReject = { index -> scanValidationPresenter.reject(index) },
                            onDismiss = { hideValidationPanel() },
                        )
                    }
                }
            }
        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            )
        panelView = view
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show validation panel", e)
            panelView = null
        }
    }

    private fun hideValidationPanel() {
        panelView?.let { view ->
            try {
                if (view.isAttachedToWindow) windowManager.removeViewImmediate(view)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove validation panel", e)
            }
            view.disposeComposition()
        }
        panelView = null
    }
}

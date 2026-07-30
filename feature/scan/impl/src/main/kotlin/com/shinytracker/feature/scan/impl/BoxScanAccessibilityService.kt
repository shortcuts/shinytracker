package com.shinytracker.feature.scan.impl

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shinytracker.core.common.constants.AppConstants
import com.shinytracker.feature.scan.api.BoxScanAccessibilityServiceBridge
import com.shinytracker.feature.scan.api.BoxScanBridge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random

@AndroidEntryPoint
class BoxScanAccessibilityService :
    AccessibilityService(),
    BoxScanAccessibilityServiceBridge {
    companion object {
        private const val TAG = "BoxScanAccessibilitySvc"
    }

    @Inject lateinit var boxScanBridge: BoxScanBridge

    override fun onServiceConnected() {
        super.onServiceConnected()
        boxScanBridge.bind(this)
        showActiveNotification()
        Log.d(TAG, "Service connected — bound to BoxScanBridge")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        boxScanBridge.unbind()
        cancelActiveNotification()
        Log.d(TAG, "Service unbound")
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override suspend fun captureScreenshot(): Bitmap? =
        suspendCancellableCoroutine { cont ->
            try {
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshot: ScreenshotResult) {
                            val hardwareBuffer = screenshot.hardwareBuffer
                            val hardwareBitmap = hardwareBuffer?.let { Bitmap.wrapHardwareBuffer(it, null) }
                            hardwareBuffer?.close()
                            val softwareBitmap = hardwareBitmap?.copy(Bitmap.Config.ARGB_8888, false)
                            hardwareBitmap?.recycle()
                            cont.resume(softwareBitmap)
                        }

                        override fun onFailure(errorCode: Int) {
                            Log.w(TAG, "takeScreenshot failed with code $errorCode")
                            cont.resume(null)
                        }
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "takeScreenshot threw", e)
                cont.resume(null)
            }
        }

    override suspend fun scrollBoxDown(): Boolean {
        delay(
            Random.nextLong(
                AppConstants.ScanConstants.PRE_SCROLL_DELAY_MIN_MS,
                AppConstants.ScanConstants.PRE_SCROLL_DELAY_MAX_MS,
            ),
        )
        val metrics = resources.displayMetrics
        val x = metrics.widthPixels / 2f
        val startY = metrics.heightPixels * AppConstants.ScanConstants.SCROLL_START_Y_PCT
        val endY = metrics.heightPixels * AppConstants.ScanConstants.SCROLL_END_Y_PCT
        val path =
            Path().apply {
                moveTo(x, startY)
                lineTo(x, endY)
            }
        val duration =
            Random.nextLong(
                AppConstants.ScanConstants.SCROLL_STROKE_MIN_DURATION_MS,
                AppConstants.ScanConstants.SCROLL_STROKE_MAX_DURATION_MS,
            )
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return suspendCancellableCoroutine { cont ->
            val dispatched =
                dispatchGesture(
                    gesture,
                    object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            cont.resume(true)
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            cont.resume(false)
                        }
                    },
                    null,
                )
            if (!dispatched) cont.resume(false)
        }
    }

    override suspend fun getIconSlotBounds(): List<Rect> {
        val root = rootInActiveWindow ?: return emptyList()
        val bounds = mutableListOf<Rect>()
        collectImageNodeBounds(root, bounds)
        return bounds
    }

    private fun collectImageNodeBounds(
        node: AccessibilityNodeInfo,
        out: MutableList<Rect>,
    ) {
        if (node.className == "android.widget.ImageView") {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            out.add(rect)
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectImageNodeBounds(it, out) }
        }
    }

    private fun showActiveNotification() {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val hasPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return // no runtime request flow yet — best-effort status notification only
        val channel =
            NotificationChannel(
                AppConstants.ScanConstants.NOTIFICATION_CHANNEL_ID,
                "Box scan active",
                NotificationManager.IMPORTANCE_LOW,
            )
        nm.createNotificationChannel(channel)
        val notification =
            NotificationCompat
                .Builder(this, AppConstants.ScanConstants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Box scan running")
                .setContentText("shinytracker's accessibility service is active")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .setSilent(true)
                .build()
        nm.notify(AppConstants.ScanConstants.NOTIFICATION_ID, notification)
    }

    private fun cancelActiveNotification() {
        getSystemService(NotificationManager::class.java)?.cancel(AppConstants.ScanConstants.NOTIFICATION_ID)
    }
}

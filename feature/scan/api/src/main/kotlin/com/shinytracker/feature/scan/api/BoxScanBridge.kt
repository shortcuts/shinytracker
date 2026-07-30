package com.shinytracker.feature.scan.api

import android.graphics.Bitmap
import android.graphics.Rect
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge between [BoxScanAccessibilityServiceBridge] (in feature:scan:impl) and consumers
 * (e.g. the debug screen in :app, later ScanOrchestrator). The service binds/unbinds
 * itself; consumers call [captureScreenshot]/[scrollBoxDown] which delegate to the
 * live service instance.
 */
@Singleton
class BoxScanBridge
    @Inject
    constructor() {
        private var service: BoxScanAccessibilityServiceBridge? = null

        fun bind(svc: BoxScanAccessibilityServiceBridge) {
            service = svc
        }

        fun unbind() {
            service = null
        }

        val isAvailable: Boolean get() = service != null

        suspend fun captureScreenshot(): Bitmap? = service?.captureScreenshot()

        suspend fun scrollBoxDown(): Boolean = service?.scrollBoxDown() ?: false

        suspend fun getIconSlotBounds(): List<Rect> = service?.getIconSlotBounds() ?: emptyList()
    }

/** Minimal interface exposed by BoxScanAccessibilityService to avoid a circular module dependency. */
interface BoxScanAccessibilityServiceBridge {
    suspend fun captureScreenshot(): Bitmap?

    suspend fun scrollBoxDown(): Boolean

    suspend fun getIconSlotBounds(): List<Rect>
}

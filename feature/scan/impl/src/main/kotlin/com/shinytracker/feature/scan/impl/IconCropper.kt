package com.shinytracker.feature.scan.impl

import android.graphics.Bitmap
import android.graphics.Rect
import javax.inject.Inject

class IconCropper
    @Inject
    constructor() {
        fun crop(
            screenshot: Bitmap,
            bounds: List<Rect>,
        ): List<Bitmap> =
            bounds.mapNotNull { rect ->
                val left = rect.left.coerceIn(0, screenshot.width)
                val top = rect.top.coerceIn(0, screenshot.height)
                val right = rect.right.coerceIn(left, screenshot.width)
                val bottom = rect.bottom.coerceIn(top, screenshot.height)
                val width = right - left
                val height = bottom - top
                if (width <= 0 || height <= 0) null else Bitmap.createBitmap(screenshot, left, top, width, height)
            }
    }

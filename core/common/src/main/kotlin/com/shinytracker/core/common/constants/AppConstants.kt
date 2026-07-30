package com.shinytracker.core.common.constants

object AppConstants {
    object ScanConstants {
        const val NOTIFICATION_CHANNEL_ID = "box_scan_active"
        const val NOTIFICATION_ID = 1001
        const val PRE_SCROLL_DELAY_MIN_MS = 400L
        const val PRE_SCROLL_DELAY_MAX_MS = 900L
        const val SCROLL_STROKE_MIN_DURATION_MS = 220L
        const val SCROLL_STROKE_MAX_DURATION_MS = 420L
        const val SCROLL_START_Y_PCT = 0.8f
        const val SCROLL_END_Y_PCT = 0.3f
        const val MATCH_CONFIDENCE_THRESHOLD = 0.85f
        const val REVIEW_QUEUE_MAX_SIZE = 100
        const val MAX_SCROLL_ITERATIONS = 50
    }

    object DatabaseConstants {
        const val DATABASE_NAME = "shinytracker.db"
    }
}

package com.shinytracker.feature.scan.impl

import android.graphics.Bitmap
import com.shinytracker.core.common.constants.AppConstants
import com.shinytracker.core.data.CaughtRepository
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.MatchResult
import com.shinytracker.core.sprites.SpriteMatcher
import com.shinytracker.feature.scan.api.BoxScanBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class ScanSlotResult(
    val record: CaughtRecord,
    val isNew: Boolean,
)

/** [crop] is kept (not recycled) so the confirm/reject validation panel can display it. */
data class PendingReview(
    val crop: Bitmap,
    val candidates: List<MatchResult>,
)

@Singleton
class ScanOrchestrator
    @Inject
    constructor(
        private val boxScanBridge: BoxScanBridge,
        private val iconCropper: IconCropper,
        private val spriteMatcher: SpriteMatcher,
        private val caughtRepository: CaughtRepository,
    ) {
        private val _scanResults = MutableStateFlow<List<ScanSlotResult>>(emptyList())
        val scanResults: StateFlow<List<ScanSlotResult>> = _scanResults.asStateFlow()

        private val _reviewQueue = MutableStateFlow<List<PendingReview>>(emptyList())
        val reviewQueue: StateFlow<List<PendingReview>> = _reviewQueue.asStateFlow()

        suspend fun runFullScan() {
            _reviewQueue.value.forEach { it.crop.recycle() } // drop any previous run's unreviewed crops
            _scanResults.value = emptyList()
            _reviewQueue.value = emptyList()
            var previousBounds: List<android.graphics.Rect>? = null
            var iteration = 0

            while (iteration < AppConstants.ScanConstants.MAX_SCROLL_ITERATIONS) {
                iteration++
                val bounds = boxScanBridge.getIconSlotBounds()
                if (bounds.isEmpty() || bounds == previousBounds) break

                val screenshot = boxScanBridge.captureScreenshot() ?: break
                val crops = iconCropper.crop(screenshot, bounds)
                screenshot.recycle()

                crops.forEach { crop -> processCrop(crop) }

                previousBounds = bounds
                boxScanBridge.scrollBoxDown()
            }
        }

        /**
         * Screenshot-button flow: capture once, detect every slot on screen, and return every
         * crop's candidates for the validation panel. No confidence threshold, no auto-record.
         */
        suspend fun captureAndDetect(): List<PendingReview> {
            val bounds = boxScanBridge.getIconSlotBounds()
            if (bounds.isEmpty()) return emptyList()
            val screenshot = boxScanBridge.captureScreenshot() ?: return emptyList()
            val crops = iconCropper.crop(screenshot, bounds)
            screenshot.recycle()
            return crops.map { crop ->
                PendingReview(crop, spriteMatcher.matchCandidates(crop, AppConstants.ScanConstants.SCREENSHOT_CANDIDATE_TOP_N))
            }
        }

        private suspend fun processCrop(crop: Bitmap) {
            val candidates = spriteMatcher.matchCandidates(crop)
            val match = candidates.firstOrNull()
            if (match != null && match.confidence >= AppConstants.ScanConstants.MATCH_CONFIDENCE_THRESHOLD) {
                val record = CaughtRecord(match.dexEntry, match.shiny, System.currentTimeMillis())
                val isNew = caughtRepository.recordIfAbsent(record)
                _scanResults.update { it + ScanSlotResult(record, isNew) }
                crop.recycle()
            } else if (_reviewQueue.value.size < AppConstants.ScanConstants.REVIEW_QUEUE_MAX_SIZE) {
                _reviewQueue.update { it + PendingReview(crop, candidates) }
            } else {
                crop.recycle() // review queue full — drop rather than leak; M4's real review UI paginates instead
            }
        }
    }

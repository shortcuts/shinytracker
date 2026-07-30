package com.shinytracker.feature.scan.impl

import com.shinytracker.core.data.CaughtRepository
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.MatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Plain confirm/reject state holder for the validation panel — not a ViewModel, since the
 * hosting [ScanWidgetOverlayService] is not a ViewModelStoreOwner (see scan-widget-overlay plan
 * decision 5). One instance per service lifetime.
 */
class ScanValidationPresenter
    @Inject
    constructor(
        private val caughtRepository: CaughtRepository,
    ) {
        private val _entries = MutableStateFlow<List<PendingReview>>(emptyList())
        val entries: StateFlow<List<PendingReview>> = _entries.asStateFlow()

        fun show(reviews: List<PendingReview>) {
            _entries.value = reviews
        }

        suspend fun confirm(
            index: Int,
            chosen: MatchResult,
        ) {
            _entries.value.getOrNull(index) ?: return
            caughtRepository.recordIfAbsent(CaughtRecord(chosen.dexEntry, chosen.shiny, System.currentTimeMillis()))
            remove(index)
        }

        fun reject(index: Int) = remove(index)

        fun clear() {
            _entries.value.forEach { it.crop.recycle() }
            _entries.value = emptyList()
        }

        private fun remove(index: Int) {
            val current = _entries.value
            val entry = current.getOrNull(index) ?: return
            entry.crop.recycle()
            _entries.value = current.filterIndexed { i, _ -> i != index }
        }
    }

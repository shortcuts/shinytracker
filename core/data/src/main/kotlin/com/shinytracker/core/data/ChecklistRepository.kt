package com.shinytracker.core.data

import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.ChecklistEntry
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.Generation
import com.shinytracker.core.sprites.ShinyChecklistSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/** Combines the full eligible-species checklist with the owner's own caught records. */
@Singleton
class ChecklistRepository
    @Inject
    constructor(
        private val checklistSource: ShinyChecklistSource,
        private val caughtRepository: CaughtRepository,
    ) {
        fun observeChecklist(): Flow<List<ChecklistEntry>> =
            checklistSource.observeChecklist().combine(caughtRepository.observeCaught()) { species, caught ->
                species.map { it.toChecklistEntry(caught) }
            }

        private fun DexEntry.toChecklistEntry(caught: List<CaughtRecord>): ChecklistEntry {
            val match = caught.firstOrNull { it.dexEntry.dexId == dexId }
            return ChecklistEntry(
                dexEntry = this,
                caught = match != null,
                caughtAt = match?.caughtAt,
                generation = Generation.fromDexId(dexId),
            )
        }
    }

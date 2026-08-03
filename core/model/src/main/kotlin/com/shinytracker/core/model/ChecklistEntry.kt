package com.shinytracker.core.model

/** One eligible-shiny species cross-referenced against the owner's caught records. */
data class ChecklistEntry(
    val dexEntry: DexEntry,
    val caught: Boolean,
    val caughtAt: Long?,
    val generation: Generation,
)

/** Matches [species] against [caught] by dexId/formId/costumeId; shared by owner and shared-profile views. */
fun buildChecklistEntries(
    species: List<DexEntry>,
    caught: List<CaughtRecord>,
): List<ChecklistEntry> =
    species.map { dex ->
        val match =
            caught.firstOrNull {
                it.dexEntry.dexId == dex.dexId && it.dexEntry.formId == dex.formId && it.dexEntry.costumeId == dex.costumeId
            }
        ChecklistEntry(
            dexEntry = dex,
            caught = match != null,
            caughtAt = match?.caughtAt,
            generation = Generation.fromDexId(dex.dexId),
        )
    }

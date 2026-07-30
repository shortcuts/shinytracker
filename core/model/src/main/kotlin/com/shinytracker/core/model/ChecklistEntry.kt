package com.shinytracker.core.model

/** One eligible-shiny species cross-referenced against the owner's caught records. */
data class ChecklistEntry(
    val dexEntry: DexEntry,
    val caught: Boolean,
    val caughtAt: Long?,
    val generation: Generation,
)

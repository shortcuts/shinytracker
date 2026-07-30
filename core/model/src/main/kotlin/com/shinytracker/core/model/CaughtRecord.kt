package com.shinytracker.core.model

data class CaughtRecord(
    val dexEntry: DexEntry,
    val shiny: Boolean,
    val caughtAt: Long,
)

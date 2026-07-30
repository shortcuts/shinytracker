package com.shinytracker.core.model

data class MatchResult(
    val dexEntry: DexEntry,
    val shiny: Boolean,
    val confidence: Float,
)

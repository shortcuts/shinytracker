package com.shinytracker.core.model

data class DexEntry(
    val dexId: Int,
    val formId: Int,
    val costumeId: Int = 0,
    val name: String,
)

package com.shinytracker.core.model

data class DexEntry(
    val dexId: Int,
    val formId: Int,
    val costumeId: Int = 0,
    val name: String,
    // ponytail: no per-species type source wired up yet, always empty until checklist.json gains a `types` field
    val types: List<PokemonType> = emptyList(),
)

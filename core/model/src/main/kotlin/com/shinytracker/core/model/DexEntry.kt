package com.shinytracker.core.model

data class DexEntry(
    val dexId: Int,
    val formId: Int,
    val costumeId: Int = 0,
    val name: String,
    val types: List<PokemonType> = emptyList(),
    val localizedNames: Map<String, String> = emptyMap(),
    val species: String? = null,
    val evolvesFrom: Int? = null,
    val evolvesTo: List<Int> = emptyList(),
)

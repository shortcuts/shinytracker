package com.shinytracker.core.model

/** Pokemon generation, derived from dex id. Ranges per Bulbapedia's National Pokedex. */
enum class Generation(
    val dexRange: IntRange,
) {
    KANTO(1..151),
    JOHTO(152..251),
    HOENN(252..386),
    SINNOH(387..493),
    UNOVA(494..649),
    KALOS(650..721),
    ALOLA(722..809),
    GALAR(810..905),
    PALDEA(906..1025),
    ;

    companion object {
        fun fromDexId(dexId: Int): Generation = entries.firstOrNull { dexId in it.dexRange } ?: PALDEA
    }
}

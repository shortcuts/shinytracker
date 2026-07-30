package com.shinytracker.core.sprites

import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.ShinyRecord

/**
 * Fixed ~20-sprite starter catalog for M3. Vendored via
 * scripts/pull_reference_sprites.py into
 * core/sprites/src/main/assets/sprites/. Superseded by a full catalog once
 * M4's ShinyChecklistSource + scripts/sync_sprites.py land.
 */
object SpriteCatalog {
    val ENTRIES: List<ShinyRecord> =
        listOf(
            normalAndShiny(1, "Bulbasaur"),
            normalAndShiny(4, "Charmander"),
            normalAndShiny(7, "Squirtle"),
            normalAndShiny(19, "Rattata"),
            normalAndShiny(25, "Pikachu"),
            normalAndShiny(66, "Machop"),
            normalAndShiny(74, "Geodude"),
            normalAndShiny(129, "Magikarp"),
            normalAndShiny(133, "Eevee"),
        ).flatten() + ShinyRecord(DexEntry(25, 0, 1, "Pikachu"), shiny = false, assetPath = "pokemon_icon_025_00_01.png")

    private fun normalAndShiny(
        dexId: Int,
        name: String,
    ): List<ShinyRecord> {
        val id = "%03d".format(dexId)
        return listOf(
            ShinyRecord(DexEntry(dexId, 0, 0, name), shiny = false, assetPath = "pokemon_icon_${id}_00.png"),
            ShinyRecord(DexEntry(dexId, 0, 0, name), shiny = true, assetPath = "pokemon_icon_${id}_00_shiny.png"),
        )
    }
}

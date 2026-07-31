package com.shinytracker.core.sprites

import android.content.Context
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.ShinyRecord

private const val SPRITES_ASSET_DIR = "sprites"
private val FILENAME_REGEX = Regex("""pokemon_icon_(\d{3})_(\d{2})(?:_(\d{2}))?(_shiny)?\.png""")

/**
 * Lists every vendored sprite under assets/sprites/ at runtime and parses
 * each filename into a ShinyRecord. Reads whatever scripts/sync_sprites.py
 * (full catalog) or scripts/pull_reference_sprites.py (M3 starter set) has
 * vendored -- both write into the same directory.
 */
object SpriteCatalog {
    fun load(
        context: Context,
        dexEntryFor: (dexId: Int) -> DexEntry,
    ): List<ShinyRecord> =
        context.assets.list(SPRITES_ASSET_DIR).orEmpty().mapNotNull { filename ->
            val match = FILENAME_REGEX.matchEntire(filename) ?: return@mapNotNull null
            val (dexId, formId, costumeId, shinySuffix) = match.destructured
            ShinyRecord(
                dexEntry =
                    dexEntryFor(dexId.toInt()).copy(
                        formId = formId.toInt(),
                        costumeId = costumeId.toIntOrNull() ?: 0,
                    ),
                shiny = shinySuffix.isNotEmpty(),
                assetPath = filename,
            )
        }
}

package com.shinytracker.core.sprites

import android.content.Context
import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.SpriteDescriptor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val DESCRIPTORS_ASSET_PATH = "sprites/descriptors.json"

/**
 * Reads the precomputed descriptor catalog bundled by the offline
 * precompute tool (`:core:sprites:descriptors`'s `Main.kt`, run via
 * `make precompute-descriptors`). Keyed by assetPath so [SpriteMatcher] can
 * join it against [SpriteCatalog]'s filename-parsed ShinyRecords.
 */
object DescriptorCatalog {
    fun load(context: Context): Map<String, SpriteDescriptor> {
        val json = Json { ignoreUnknownKeys = true }
        val text =
            context.assets
                .open(DESCRIPTORS_ASSET_PATH)
                .bufferedReader()
                .use { it.readText() }
        return json.decodeFromString<List<DescriptorEntry>>(text).associate { it.assetPath to it.descriptor }
    }
}

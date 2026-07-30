package com.shinytracker.core.sprites

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

private const val SPRITES_DIR = "src/main/assets/sprites"

/**
 * Catches "added a sprite via scripts/sync_sprites.py, forgot to re-run
 * `make precompute-descriptors`" before it reaches a device: every vendored
 * PNG must have a matching descriptors.json entry, and vice versa.
 */
class DescriptorCatalogConsistencyTest {
    @Test
    fun `every vendored PNG has a descriptor and every descriptor has a vendored PNG`() {
        val pngFilenames =
            File(SPRITES_DIR)
                .listFiles { file -> file.extension == "png" }
                .orEmpty()
                .map { it.name }
                .toSet()

        val descriptorAssetPaths =
            Json { ignoreUnknownKeys = true }
                .decodeFromString<List<DescriptorEntry>>(File(SPRITES_DIR, "descriptors.json").readText())
                .map { it.assetPath }
                .toSet()

        assertEquals(pngFilenames, descriptorAssetPaths)
    }
}

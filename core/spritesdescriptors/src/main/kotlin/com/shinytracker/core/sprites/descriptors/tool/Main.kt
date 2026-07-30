/*
 * Offline precompute tool: reads every PNG under
 * core/sprites/src/main/assets/sprites/, computes a SpriteDescriptor for
 * each via javax.imageio (this module is real JVM, not android.jar-
 * constrained, so no new dependency is needed), and writes
 * core/sprites/src/main/assets/sprites/descriptors.json.
 *
 * Run after scripts/sync_sprites.py: `make precompute-descriptors`
 * (`./gradlew :core:sprites:descriptors:run`). Safe to re-run -- it fully
 * regenerates the file, no incremental logic.
 */
package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.computeDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.imageio.ImageIO

private val SPRITES_DIR = File("core/sprites/src/main/assets/sprites")
private val OUTPUT_FILE = File(SPRITES_DIR, "descriptors.json")
private val json = Json { prettyPrint = true }

fun main() {
    val entries = precompute(SPRITES_DIR)
    OUTPUT_FILE.writeText(json.encodeToString(entries))
    println("Wrote ${entries.size} descriptors to $OUTPUT_FILE")
}

fun precompute(spritesDir: File): List<DescriptorEntry> =
    spritesDir
        .listFiles { file -> file.extension == "png" }
        .orEmpty()
        .sortedBy { it.name }
        .map { file ->
            val image = ImageIO.read(file)
            val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
            val descriptor = computeDescriptor(pixels, image.width, image.height)
            DescriptorEntry(assetPath = file.name, descriptor = descriptor)
        }

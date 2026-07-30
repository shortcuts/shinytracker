package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.computeDescriptor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class MainTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun writeFixturePng(
        dir: File,
        name: String,
        argb: Int,
    ) {
        val image = BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until 4) {
            for (x in 0 until 4) {
                image.setRGB(x, y, argb)
            }
        }
        ImageIO.write(image, "png", File(dir, name))
    }

    @Test
    fun `precompute writes a descriptor per fixture PNG matching computeDescriptor directly`() {
        val dir = tempFolder.newFolder("sprites")
        writeFixturePng(dir, "pokemon_icon_001_00.png", 0xFF00FF00.toInt())
        writeFixturePng(dir, "pokemon_icon_001_00_shiny.png", 0xFFFF0000.toInt())
        writeFixturePng(dir, "pokemon_icon_004_00.png", 0xFF0000FF.toInt())

        val entries = precompute(dir)
        val json = Json { prettyPrint = true }
        val roundTripped = json.decodeFromString<List<DescriptorEntry>>(json.encodeToString(entries))

        assertEquals(
            listOf("pokemon_icon_001_00.png", "pokemon_icon_001_00_shiny.png", "pokemon_icon_004_00.png"),
            roundTripped.map { it.assetPath },
        )

        val expectedGreen = computeDescriptor(IntArray(16) { 0xFF00FF00.toInt() }, 4, 4)
        val actualGreen = roundTripped.first { it.assetPath == "pokemon_icon_001_00.png" }.descriptor
        assertEquals(expectedGreen, actualGreen)
    }
}

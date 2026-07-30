package com.shinytracker.core.sprites.descriptors

import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.ShinyRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

private const val ASSETS_DIR = "../sprites/src/main/assets/sprites"

private fun loadDescriptor(fileName: String): SpriteDescriptor {
    val image = ImageIO.read(File(ASSETS_DIR, fileName))
    val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
    return computeDescriptor(pixels, image.width, image.height)
}

private fun solidColorPixels(
    width: Int,
    height: Int,
    argb: Int,
): IntArray = IntArray(width * height) { argb }

class SpriteMatchingTest {
    private val pikachuNormal =
        ShinyRecord(DexEntry(25, 0, 0, "Pikachu"), shiny = false, assetPath = "pokemon_icon_025_00.png")
    private val pikachuShiny =
        ShinyRecord(DexEntry(25, 0, 0, "Pikachu"), shiny = true, assetPath = "pokemon_icon_025_00_shiny.png")
    private val pikachuCostume =
        ShinyRecord(DexEntry(25, 0, 1, "Pikachu"), shiny = false, assetPath = "pokemon_icon_025_00_01.png")

    private val catalog =
        listOf(
            pikachuNormal to loadDescriptor(pikachuNormal.assetPath),
            pikachuShiny to loadDescriptor(pikachuShiny.assetPath),
            pikachuCostume to loadDescriptor(pikachuCostume.assetPath),
        )

    @Test
    fun `shiny crop matches the shiny catalog entry, not the normal one`() {
        val crop = loadDescriptor(pikachuShiny.assetPath)
        val (matched, _) = pickBestMatch(crop, catalog)!!
        assertEquals(pikachuShiny, matched)
    }

    @Test
    fun `costume crop is not confused with the base form`() {
        val crop = loadDescriptor(pikachuCostume.assetPath)
        val (matched, _) = pickBestMatch(crop, catalog)!!
        assertEquals(pikachuCostume, matched)
    }

    @Test
    fun `pHash and dHash are close for near-identical images and far for very different ones`() {
        val red = solidColorPixels(16, 16, 0xFF000000.toInt() or 0xFF0000)
        val redShifted = IntArray(16 * 16) { i -> if (i == 0) red[1] else red[i] } // 1-pixel shift
        val blue = solidColorPixels(16, 16, 0xFF000000.toInt() or 0x0000FF)

        val descRed = computeDescriptor(red, 16, 16)
        val descRedShifted = computeDescriptor(redShifted, 16, 16)
        val descBlue = computeDescriptor(blue, 16, 16)

        val closeHamming = java.lang.Long.bitCount(descRed.pHash xor descRedShifted.pHash)
        val farHamming = java.lang.Long.bitCount(descRed.pHash xor descBlue.pHash)
        assertTrue("expected near-identical images to hash close: $closeHamming", closeHamming < farHamming)

        val closeDHamming = java.lang.Long.bitCount(descRed.dHash xor descRedShifted.dHash)
        val farDHamming = java.lang.Long.bitCount(descRed.dHash xor descBlue.dHash)
        assertTrue("expected near-identical images to dHash close: $closeDHamming", closeDHamming <= farDHamming)
    }

    @Test
    fun `hsvHistogram peaks in the expected bin for a solid color image`() {
        val red = solidColorPixels(8, 8, 0xFF000000.toInt() or 0xFF0000) // hue 0
        val descriptor = computeDescriptor(red, 8, 8)
        val peakBin = descriptor.hsvHistogram.indices.maxByOrNull { descriptor.hsvHistogram[it] }
        assertEquals(0, peakBin)
    }

    @Test
    fun `labHistogram has a single dominant bin for a solid color image`() {
        val green = solidColorPixels(8, 8, 0xFF000000.toInt() or 0x00FF00)
        val descriptor = computeDescriptor(green, 8, 8)
        val total = descriptor.labHistogram.sum()
        val peak = descriptor.labHistogram.max()
        assertTrue("expected one bin to dominate the histogram", peak / total > 0.9f)
    }

    @Test
    fun `dominantColors finds both colors in a two-color image`() {
        val pixels =
            IntArray(64) { i ->
                if (i < 32) (0xFF000000.toInt() or 0xFF0000) else (0xFF000000.toInt() or 0x0000FF)
            }
        val descriptor = computeDescriptor(pixels, 8, 8)
        val hasRedish = descriptor.dominantColors.any { (it ushr 16) and 0xFF > 200 }
        val hasBlueish = descriptor.dominantColors.any { it and 0xFF > 200 }
        assertTrue("expected a red-ish dominant color", hasRedish)
        assertTrue("expected a blue-ish dominant color", hasBlueish)
    }

    @Test
    fun `edgeSignature is near zero for a flat image and high for a checkerboard`() {
        val flat = solidColorPixels(16, 16, 0xFF000000.toInt() or 0x808080)
        val checkerboard =
            IntArray(16 * 16) { i ->
                val x = i % 16
                val y = i / 16
                if ((x + y) % 2 == 0) (0xFF000000.toInt()) else (0xFFFFFFFF.toInt())
            }
        val flatSignature = computeDescriptor(flat, 16, 16).edgeSignature
        val checkerSignature = computeDescriptor(checkerboard, 16, 16).edgeSignature

        assertTrue("expected flat image to have near-zero edges", flatSignature.average() < 0.05)
        assertTrue("expected checkerboard to have strong edges", checkerSignature.average() > flatSignature.average())
    }

    @Test
    fun `alphaMask and boundingBox exclude transparent padding`() {
        // 8x8 image, fully transparent left half, opaque right half.
        val pixels =
            IntArray(64) { i ->
                val x = i % 8
                if (x < 4) 0x00000000 else (0xFF000000.toInt() or 0xFF0000)
            }
        val descriptor = computeDescriptor(pixels, 8, 8)

        assertEquals(4, descriptor.boundingBox.minX)
        assertEquals(7, descriptor.boundingBox.maxX)

        // Left half's grid columns (0-3) should be unset in the alpha mask; right half's set.
        for (row in 0 until 8) {
            val leftColumnBit = row * 8 + 0
            val rightColumnBit = row * 8 + 7
            assertEquals(0L, descriptor.alphaMask and (1L shl leftColumnBit))
            assertTrue((descriptor.alphaMask and (1L shl rightColumnBit)) != 0L)
        }
    }
}

package com.shinytracker.core.sprites

import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.ShinyRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.zip.Inflater

private const val ASSETS_DIR = "src/main/assets/sprites"

private fun loadDescriptor(fileName: String): SpriteDescriptor {
    val (pixels, width, height) = decodePng(File(ASSETS_DIR, fileName).readBytes())
    return computeDescriptor(pixels, width, height)
}

/**
 * Minimal PNG decoder: only 8-bit RGBA, non-interlaced (matches every sprite
 * `scripts/pull_reference_sprites.py` vendors from PokeMiners/pogo_assets).
 * javax.imageio is unusable here — Kotlin's Android-library unit test
 * compile classpath is android.jar-based and doesn't declare java.awt/imageio
 * at all, so this test can't call it even though it runs on a real JVM.
 * ponytail: 8-bit RGBA / non-interlaced only, extend if a differently
 * encoded PNG is ever vendored.
 */
private fun decodePng(bytes: ByteArray): Triple<IntArray, Int, Int> {
    require(bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(PNG_SIGNATURE)) { "Not a PNG file" }

    var offset = 8
    var width = 0
    var height = 0
    val idat = java.io.ByteArrayOutputStream()
    while (offset < bytes.size) {
        val length = readInt32(bytes, offset)
        val type = String(bytes, offset + 4, 4, Charsets.US_ASCII)
        val dataStart = offset + 8
        when (type) {
            "IHDR" -> {
                width = readInt32(bytes, dataStart)
                height = readInt32(bytes, dataStart + 4)
                val bitDepth = bytes[dataStart + 8].toInt() and 0xFF
                val colorType = bytes[dataStart + 9].toInt() and 0xFF
                val interlace = bytes[dataStart + 12].toInt() and 0xFF
                require(bitDepth == 8 && colorType == 6 && interlace == 0) {
                    "Unsupported PNG encoding: bitDepth=$bitDepth colorType=$colorType interlace=$interlace"
                }
            }

            "IDAT" -> {
                idat.write(bytes, dataStart, length)
            }

            "IEND" -> {
                Unit
            }
        }
        offset = dataStart + length + 4 // skip data + trailing CRC
    }

    val bytesPerPixel = 4
    val stride = width * bytesPerPixel
    val raw = ByteArray((stride + 1) * height)
    val inflater = Inflater()
    inflater.setInput(idat.toByteArray())
    var written = 0
    while (written < raw.size && !inflater.finished()) {
        written += inflater.inflate(raw, written, raw.size - written)
    }
    inflater.end()

    val unfiltered = unfilter(raw, width, height, bytesPerPixel)
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val i = (y * width + x) * bytesPerPixel
            val r = unfiltered[i].toInt() and 0xFF
            val g = unfiltered[i + 1].toInt() and 0xFF
            val b = unfiltered[i + 2].toInt() and 0xFF
            val a = unfiltered[i + 3].toInt() and 0xFF
            pixels[y * width + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
    return Triple(pixels, width, height)
}

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

private fun readInt32(
    bytes: ByteArray,
    offset: Int,
): Int =
    ((bytes[offset].toInt() and 0xFF) shl 24) or
        ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
        ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
        (bytes[offset + 3].toInt() and 0xFF)

/** Reverses PNG's per-scanline filtering (spec section 9.3), producing raw pixel bytes. */
private fun unfilter(
    raw: ByteArray,
    width: Int,
    height: Int,
    bpp: Int,
): ByteArray {
    val stride = width * bpp
    val out = ByteArray(stride * height)
    var rawOffset = 0
    for (y in 0 until height) {
        val filterType = raw[rawOffset].toInt() and 0xFF
        rawOffset++
        val rowStart = y * stride
        val prevRowStart = (y - 1) * stride
        for (x in 0 until stride) {
            val raw8 = raw[rawOffset + x].toInt() and 0xFF
            val a = if (x >= bpp) out[rowStart + x - bpp].toInt() and 0xFF else 0
            val b = if (y > 0) out[prevRowStart + x].toInt() and 0xFF else 0
            val c = if (y > 0 && x >= bpp) out[prevRowStart + x - bpp].toInt() and 0xFF else 0
            val value =
                when (filterType) {
                    0 -> raw8
                    1 -> raw8 + a
                    2 -> raw8 + b
                    3 -> raw8 + (a + b) / 2
                    4 -> raw8 + paeth(a, b, c)
                    else -> error("Unsupported PNG filter type $filterType")
                }
            out[rowStart + x] = value.toByte()
        }
        rawOffset += stride
    }
    return out
}

private fun paeth(
    a: Int,
    b: Int,
    c: Int,
): Int {
    val p = a + b - c
    val pa = kotlin.math.abs(p - a)
    val pb = kotlin.math.abs(p - b)
    val pc = kotlin.math.abs(p - c)
    return if (pa <= pb && pa <= pc) {
        a
    } else if (pb <= pc) {
        b
    } else {
        c
    }
}

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
}

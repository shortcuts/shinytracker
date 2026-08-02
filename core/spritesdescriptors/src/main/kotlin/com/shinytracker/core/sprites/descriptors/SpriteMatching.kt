package com.shinytracker.core.sprites.descriptors

import com.shinytracker.core.model.ShinyRecord
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val GRID_SIZE = 8 // 8x8 cells, used by gridRgb + edgeSignature + alphaMask
private const val ALPHA_SKIP_THRESHOLD = 16 // ignore near-transparent background pixels
private const val PHASH_SOURCE_SIZE = 32 // grayscale downsample size feeding the 8x8 DCT
private const val DHASH_WIDTH = 9 // dHash compares 8 adjacent pairs per row
private const val DHASH_HEIGHT = 8
private const val HISTOGRAM_BINS = 16
private const val DOMINANT_COLOR_COUNT = 3
private const val KMEANS_ITERATIONS = 5

@Serializable
data class BoundingBox(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
)

@Serializable
data class SpriteDescriptor(
    val gridRgb: List<Float>,
    val pHash: Long,
    val dHash: Long,
    val hsvHistogram: List<Float>,
    val labHistogram: List<Float>,
    val dominantColors: List<Int>,
    val edgeSignature: List<Float>,
    val alphaMask: Long,
    val boundingBox: BoundingBox,
)

/** One catalog entry as written by the offline precompute tool / read by [DescriptorCatalog]. */
@Serializable
data class DescriptorEntry(
    val assetPath: String,
    val descriptor: SpriteDescriptor,
)

fun computeDescriptor(
    pixels: IntArray,
    width: Int,
    height: Int,
): SpriteDescriptor {
    val gray = toGrayscale(pixels)
    return SpriteDescriptor(
        gridRgb = computeGridRgb(pixels, width, height),
        pHash = computePHash(gray, width, height),
        dHash = computeDHash(gray, width, height),
        hsvHistogram = computeHsvHistogram(pixels),
        labHistogram = computeLabHistogram(pixels),
        dominantColors = computeDominantColors(pixels),
        edgeSignature = computeEdgeSignature(gray, width, height),
        alphaMask = computeAlphaMask(pixels, width, height),
        boundingBox = computeBoundingBox(pixels, width, height),
    )
}

/** Runtime-swappable view of the 8 ensemble weights, for [similarity]'s optional param.
 * Defaults to the tuned consts below -- production callers never pass this explicitly. */
data class EnsembleWeights(
    val gridRgb: Float = WEIGHT_GRID_RGB,
    val pHash: Float = WEIGHT_PHASH,
    val dHash: Float = WEIGHT_DHASH,
    val hsvHistogram: Float = WEIGHT_HSV_HISTOGRAM,
    val labHistogram: Float = WEIGHT_LAB_HISTOGRAM,
    val dominantColors: Float = WEIGHT_DOMINANT_COLORS,
    val edgeSignature: Float = WEIGHT_EDGE_SIGNATURE,
    val alphaMask: Float = WEIGHT_ALPHA_MASK,
)

/** 1 = identical, 0 = maximally different. Weighted ensemble across every descriptor field. */
fun similarity(
    a: SpriteDescriptor,
    b: SpriteDescriptor,
    weights: EnsembleWeights = EnsembleWeights(),
): Float {
    val score =
        weights.gridRgb * gridRgbSimilarity(a.gridRgb, b.gridRgb) +
            weights.pHash * hammingSimilarity(a.pHash, b.pHash) +
            weights.dHash * hammingSimilarity(a.dHash, b.dHash) +
            weights.hsvHistogram * histogramSimilarity(a.hsvHistogram, b.hsvHistogram) +
            weights.labHistogram * histogramSimilarity(a.labHistogram, b.labHistogram) +
            weights.dominantColors * dominantColorSimilarity(a.dominantColors, b.dominantColors) +
            weights.edgeSignature * edgeSignatureSimilarity(a.edgeSignature, b.edgeSignature) +
            weights.alphaMask * hammingSimilarity(a.alphaMask, b.alphaMask)
    return score.coerceIn(0f, 1f)
}

/** Highest-similarity catalog entry, or null if [catalog] is empty. */
fun pickBestMatch(
    descriptor: SpriteDescriptor,
    catalog: List<Pair<ShinyRecord, SpriteDescriptor>>,
): Pair<ShinyRecord, Float>? =
    catalog
        .map { (record, candidate) -> record to similarity(descriptor, candidate) }
        .maxByOrNull { (_, score) -> score }

/** Top [topN] candidates sorted by descending ensemble score. */
fun pickTopMatches(
    descriptor: SpriteDescriptor,
    catalog: List<Pair<ShinyRecord, SpriteDescriptor>>,
    topN: Int = 5,
): List<Pair<ShinyRecord, Float>> =
    catalog
        .map { (record, candidate) -> record to similarity(descriptor, candidate) }
        .sortedByDescending { (_, score) -> score }
        .take(topN)

// --- Ensemble weights (local consts: this module is pure JVM and can't depend on
// :core:common's AppConstants, see docs/constants.md's pure-JVM exception). Sum to 1.0. ---
private const val WEIGHT_GRID_RGB = 0.30f
private const val WEIGHT_PHASH = 0.15f
private const val WEIGHT_DHASH = 0.15f
private const val WEIGHT_HSV_HISTOGRAM = 0.15f
private const val WEIGHT_LAB_HISTOGRAM = 0.10f
private const val WEIGHT_DOMINANT_COLORS = 0.05f
private const val WEIGHT_EDGE_SIGNATURE = 0.05f
private const val WEIGHT_ALPHA_MASK = 0.05f

// --- gridRgb: proportional grid-cell average-RGB descriptor. Works on any crop resolution. ---

private fun computeGridRgb(
    pixels: IntArray,
    width: Int,
    height: Int,
): List<Float> {
    val buckets = FloatArray(GRID_SIZE * GRID_SIZE * 3)
    val counts = IntArray(GRID_SIZE * GRID_SIZE)
    forEachOpaquePixel(pixels, width, height) { x, y, pixel ->
        val cell = gridCell(x, y, width, height)
        buckets[cell * 3] += ((pixel ushr 16) and 0xFF).toFloat()
        buckets[cell * 3 + 1] += ((pixel ushr 8) and 0xFF).toFloat()
        buckets[cell * 3 + 2] += (pixel and 0xFF).toFloat()
        counts[cell]++
    }
    for (cell in 0 until GRID_SIZE * GRID_SIZE) {
        val count = counts[cell].coerceAtLeast(1)
        buckets[cell * 3] /= count
        buckets[cell * 3 + 1] /= count
        buckets[cell * 3 + 2] /= count
    }
    return buckets.toList()
}

private fun gridRgbSimilarity(
    a: List<Float>,
    b: List<Float>,
): Float {
    var sumSquares = 0.0
    for (i in a.indices) {
        val diff = (a[i] - b[i]).toDouble()
        sumSquares += diff * diff
    }
    val distance = sqrt(sumSquares)
    val maxDistance = sqrt(a.size * 255.0 * 255.0)
    return (1.0 - (distance / maxDistance)).toFloat().coerceIn(0f, 1f)
}

// --- pHash: 8x8 DCT-II low-frequency, median-thresholded. ---

private fun computePHash(
    gray: FloatArray,
    width: Int,
    height: Int,
): Long {
    val small = resizeGrayscale(gray, width, height, PHASH_SOURCE_SIZE, PHASH_SOURCE_SIZE)
    val cosTable =
        Array(PHASH_SOURCE_SIZE) { n ->
            FloatArray(8) { k -> cos(((2 * n + 1) * k * PI) / (2.0 * PHASH_SOURCE_SIZE)).toFloat() }
        }
    // Separable DCT-II: rows first, then columns, keeping only the low 8 frequencies each way.
    val rowPass = Array(PHASH_SOURCE_SIZE) { FloatArray(8) }
    for (x in 0 until PHASH_SOURCE_SIZE) {
        for (v in 0 until 8) {
            var sum = 0f
            for (y in 0 until PHASH_SOURCE_SIZE) {
                sum += small[y * PHASH_SOURCE_SIZE + x] * cosTable[y][v]
            }
            rowPass[x][v] = sum
        }
    }
    val dct = FloatArray(64)
    for (u in 0 until 8) {
        for (v in 0 until 8) {
            var sum = 0f
            for (x in 0 until PHASH_SOURCE_SIZE) {
                sum += rowPass[x][v] * cosTable[x][u]
            }
            dct[u * 8 + v] = sum
        }
    }
    val median = dct.sorted()[dct.size / 2]
    var hash = 0L
    for (i in dct.indices) {
        if (dct[i] > median) hash = hash or (1L shl i)
    }
    return hash
}

// --- dHash: 9x8 gradient hash. ---

private fun computeDHash(
    gray: FloatArray,
    width: Int,
    height: Int,
): Long {
    val small = resizeGrayscale(gray, width, height, DHASH_WIDTH, DHASH_HEIGHT)
    var hash = 0L
    var bit = 0
    for (y in 0 until DHASH_HEIGHT) {
        for (x in 0 until DHASH_HEIGHT) {
            val left = small[y * DHASH_WIDTH + x]
            val right = small[y * DHASH_WIDTH + x + 1]
            if (left < right) hash = hash or (1L shl bit)
            bit++
        }
    }
    return hash
}

private fun hammingSimilarity(
    a: Long,
    b: Long,
): Float {
    val distance = java.lang.Long.bitCount(a xor b)
    return 1f - (distance / 64f)
}

// --- hsvHistogram: 16-bin hue, weighted by saturation*value. ---

private fun computeHsvHistogram(pixels: IntArray): List<Float> {
    val bins = FloatArray(HISTOGRAM_BINS)
    var total = 0f
    forEachOpaquePixelValue(pixels) { pixel ->
        val r = ((pixel ushr 16) and 0xFF) / 255f
        val g = ((pixel ushr 8) and 0xFF) / 255f
        val b = (pixel and 0xFF) / 255f
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min
        val value = max
        val saturation = if (max == 0f) 0f else delta / max
        val hue =
            when {
                delta == 0f -> 0f
                max == r -> 60f * (((g - b) / delta) % 6f)
                max == g -> 60f * (((b - r) / delta) + 2f)
                else -> 60f * (((r - g) / delta) + 4f)
            }.let { if (it < 0f) it + 360f else it }
        val weight = saturation * value
        val bin = ((hue / 360f) * HISTOGRAM_BINS).toInt().coerceIn(0, HISTOGRAM_BINS - 1)
        bins[bin] += weight
        total += weight
    }
    return normalize(bins, total)
}

// --- labHistogram: 16-bin a*/b* chroma bins. ---

private fun computeLabHistogram(pixels: IntArray): List<Float> {
    val bins = FloatArray(HISTOGRAM_BINS)
    var total = 0f
    forEachOpaquePixelValue(pixels) { pixel ->
        val (a, bChannel) = rgbToLabAb(pixel)
        val chroma = sqrt((a * a + bChannel * bChannel).toDouble()).toFloat()
        val angle = (Math.toDegrees(atan2(bChannel.toDouble(), a.toDouble())).toFloat()).let { if (it < 0f) it + 360f else it }
        val bin = ((angle / 360f) * HISTOGRAM_BINS).toInt().coerceIn(0, HISTOGRAM_BINS - 1)
        bins[bin] += chroma
        total += chroma
    }
    return normalize(bins, total)
}

private fun histogramSimilarity(
    a: List<Float>,
    b: List<Float>,
): Float {
    var totalVariation = 0f
    for (i in a.indices) {
        totalVariation += abs(a[i] - b[i])
    }
    return (1f - totalVariation / 2f).coerceIn(0f, 1f)
}

private fun normalize(
    bins: FloatArray,
    total: Float,
): List<Float> {
    if (total <= 0f) return bins.toList()
    for (i in bins.indices) bins[i] /= total
    return bins.toList()
}

// --- dominantColors: k=3 k-means over ARGB, few fixed iterations. ---

private fun computeDominantColors(pixels: IntArray): List<Int> {
    val opaque = pixels.filter { ((it ushr 24) and 0xFF) >= ALPHA_SKIP_THRESHOLD }
    if (opaque.isEmpty()) return List(DOMINANT_COLOR_COUNT) { 0 }

    val step = max(1, opaque.size / DOMINANT_COLOR_COUNT)
    var centroids = (0 until DOMINANT_COLOR_COUNT).map { opaque[min(it * step, opaque.size - 1)] }.toMutableList()

    repeat(KMEANS_ITERATIONS) {
        val sums = Array(DOMINANT_COLOR_COUNT) { longArrayOf(0, 0, 0, 0) } // r, g, b, count
        for (pixel in opaque) {
            var bestIndex = 0
            var bestDistance = Int.MAX_VALUE
            for (i in centroids.indices) {
                val distance = colorDistanceSquared(pixel, centroids[i])
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestIndex = i
                }
            }
            sums[bestIndex][0] += (pixel ushr 16) and 0xFF
            sums[bestIndex][1] += (pixel ushr 8) and 0xFF
            sums[bestIndex][2] += pixel and 0xFF
            sums[bestIndex][3]++
        }
        centroids =
            centroids
                .mapIndexed { i, previous ->
                    val count = sums[i][3]
                    if (count == 0L) {
                        previous
                    } else {
                        val r = (sums[i][0] / count).toInt()
                        val g = (sums[i][1] / count).toInt()
                        val b = (sums[i][2] / count).toInt()
                        (r shl 16) or (g shl 8) or b
                    }
                }.toMutableList()
    }
    return centroids.map { it and 0xFFFFFF }
}

private fun colorDistanceSquared(
    a: Int,
    b: Int,
): Int {
    val dr = ((a ushr 16) and 0xFF) - ((b ushr 16) and 0xFF)
    val dg = ((a ushr 8) and 0xFF) - ((b ushr 8) and 0xFF)
    val db = (a and 0xFF) - (b and 0xFF)
    return dr * dr + dg * dg + db * db
}

private fun dominantColorSimilarity(
    a: List<Int>,
    b: List<Int>,
): Float {
    val maxDistance = sqrt(3.0 * 255 * 255).toFloat()
    val distances =
        a.map { colorA ->
            b.minOf { colorB -> sqrt(colorDistanceSquared(colorA, colorB).toDouble()).toFloat() }
        }
    val avgDistance = if (distances.isEmpty()) 0f else distances.average().toFloat()
    return (1f - avgDistance / maxDistance).coerceIn(0f, 1f)
}

// --- edgeSignature: 8x8 grid of average Sobel magnitude. ---

private fun computeEdgeSignature(
    gray: FloatArray,
    width: Int,
    height: Int,
): List<Float> {
    val sums = FloatArray(GRID_SIZE * GRID_SIZE)
    val counts = IntArray(GRID_SIZE * GRID_SIZE)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val gx = sobelAxis(gray, width, height, x, y, horizontal = true)
            val gy = sobelAxis(gray, width, height, x, y, horizontal = false)
            val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toFloat()
            val cell = gridCell(x, y, width, height)
            sums[cell] += magnitude
            counts[cell]++
        }
    }
    for (cell in sums.indices) {
        val count = counts[cell].coerceAtLeast(1)
        sums[cell] = (sums[cell] / count / MAX_SOBEL_MAGNITUDE).coerceIn(0f, 1f)
    }
    return sums.toList()
}

private const val MAX_SOBEL_MAGNITUDE = 1020f // 4 * 255, theoretical max |Gx| or |Gy| combined via sqrt(2)*4*255 upper bound

private fun sobelAxis(
    gray: FloatArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    horizontal: Boolean,
): Float {
    fun at(
        dx: Int,
        dy: Int,
    ): Float {
        val px = (x + dx).coerceIn(0, width - 1)
        val py = (y + dy).coerceIn(0, height - 1)
        return gray[py * width + px]
    }
    return if (horizontal) {
        (at(1, -1) + 2 * at(1, 0) + at(1, 1)) - (at(-1, -1) + 2 * at(-1, 0) + at(-1, 1))
    } else {
        (at(-1, 1) + 2 * at(0, 1) + at(1, 1)) - (at(-1, -1) + 2 * at(0, -1) + at(1, -1))
    }
}

private fun edgeSignatureSimilarity(
    a: List<Float>,
    b: List<Float>,
): Float {
    var sumSquares = 0.0
    for (i in a.indices) {
        val diff = (a[i] - b[i]).toDouble()
        sumSquares += diff * diff
    }
    val distance = sqrt(sumSquares)
    val maxDistance = sqrt(a.size * 1.0)
    return (1.0 - (distance / maxDistance)).toFloat().coerceIn(0f, 1f)
}

// --- alphaMask + boundingBox: icon silhouette, ignoring transparent padding. ---

private fun computeAlphaMask(
    pixels: IntArray,
    width: Int,
    height: Int,
): Long {
    val sums = FloatArray(GRID_SIZE * GRID_SIZE)
    val counts = IntArray(GRID_SIZE * GRID_SIZE)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val alpha = (pixels[y * width + x] ushr 24) and 0xFF
            val cell = gridCell(x, y, width, height)
            sums[cell] += alpha
            counts[cell]++
        }
    }
    var mask = 0L
    for (cell in sums.indices) {
        val average = sums[cell] / counts[cell].coerceAtLeast(1)
        if (average >= ALPHA_SKIP_THRESHOLD * 8) mask = mask or (1L shl cell)
    }
    return mask
}

private fun computeBoundingBox(
    pixels: IntArray,
    width: Int,
    height: Int,
): BoundingBox {
    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            val alpha = (pixels[y * width + x] ushr 24) and 0xFF
            if (alpha >= ALPHA_SKIP_THRESHOLD) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    return if (maxX < minX || maxY < minY) {
        BoundingBox(0, 0, width - 1, height - 1)
    } else {
        BoundingBox(minX, minY, maxX, maxY)
    }
}

// --- shared pixel helpers ---

private fun toGrayscale(pixels: IntArray): FloatArray =
    FloatArray(pixels.size) { i ->
        val pixel = pixels[i]
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        0.299f * r + 0.587f * g + 0.114f * b
    }

/** Box-average resize of a grayscale buffer to [targetW]x[targetH]. */
private fun resizeGrayscale(
    gray: FloatArray,
    width: Int,
    height: Int,
    targetW: Int,
    targetH: Int,
): FloatArray {
    val sums = FloatArray(targetW * targetH)
    val counts = IntArray(targetW * targetH)
    for (y in 0 until height) {
        val cellY = (y * targetH / height).coerceIn(0, targetH - 1)
        for (x in 0 until width) {
            val cellX = (x * targetW / width).coerceIn(0, targetW - 1)
            val cell = cellY * targetW + cellX
            sums[cell] += gray[y * width + x]
            counts[cell]++
        }
    }
    for (cell in sums.indices) {
        sums[cell] /= counts[cell].coerceAtLeast(1)
    }
    return sums
}

private fun gridCell(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
): Int {
    val cellX = (x * GRID_SIZE / width).coerceIn(0, GRID_SIZE - 1)
    val cellY = (y * GRID_SIZE / height).coerceIn(0, GRID_SIZE - 1)
    return cellY * GRID_SIZE + cellX
}

private inline fun forEachOpaquePixel(
    pixels: IntArray,
    width: Int,
    height: Int,
    action: (x: Int, y: Int, pixel: Int) -> Unit,
) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = pixels[y * width + x]
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha < ALPHA_SKIP_THRESHOLD) continue
            action(x, y, pixel)
        }
    }
}

private inline fun forEachOpaquePixelValue(
    pixels: IntArray,
    action: (pixel: Int) -> Unit,
) {
    for (pixel in pixels) {
        val alpha = (pixel ushr 24) and 0xFF
        if (alpha < ALPHA_SKIP_THRESHOLD) continue
        action(pixel)
    }
}

/** sRGB -> CIE Lab a/b chroma channels only (L discarded, unused by [computeLabHistogram]). */
private fun rgbToLabAb(pixel: Int): Pair<Float, Float> {
    fun linearize(channel: Int): Double {
        val c = channel / 255.0
        return if (c > 0.04045) Math.pow((c + 0.055) / 1.055, 2.4) else c / 12.92
    }
    val r = linearize((pixel ushr 16) and 0xFF)
    val g = linearize((pixel ushr 8) and 0xFF)
    val b = linearize(pixel and 0xFF)

    // sRGB D65 -> XYZ
    val x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047
    val y = (r * 0.2126 + g * 0.7152 + b * 0.0722)
    val z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883

    fun f(t: Double): Double = if (t > 0.008856) Math.cbrt(t) else (7.787 * t) + (16.0 / 116.0)

    val fx = f(x)
    val fy = f(y)
    val fz = f(z)
    val a = 500 * (fx - fy)
    val bLab = 200 * (fy - fz)
    return a.toFloat() to bLab.toFloat()
}

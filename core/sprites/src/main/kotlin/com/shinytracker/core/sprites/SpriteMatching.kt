package com.shinytracker.core.sprites

import com.shinytracker.core.model.ShinyRecord
import kotlin.math.sqrt

private const val GRID_SIZE = 8 // 8x8 cells, 3 channels each -> 192-length descriptor
private const val ALPHA_SKIP_THRESHOLD = 16 // ignore near-transparent background pixels

data class SpriteDescriptor(
    val buckets: FloatArray,
)

/** Proportional grid-cell average-RGB descriptor. Works on any crop resolution. */
fun computeDescriptor(
    pixels: IntArray,
    width: Int,
    height: Int,
): SpriteDescriptor {
    val buckets = FloatArray(GRID_SIZE * GRID_SIZE * 3)
    val counts = IntArray(GRID_SIZE * GRID_SIZE)
    for (y in 0 until height) {
        val cellY = (y * GRID_SIZE / height).coerceIn(0, GRID_SIZE - 1)
        for (x in 0 until width) {
            val pixel = pixels[y * width + x]
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha < ALPHA_SKIP_THRESHOLD) continue
            val cellX = (x * GRID_SIZE / width).coerceIn(0, GRID_SIZE - 1)
            val cell = cellY * GRID_SIZE + cellX
            buckets[cell * 3] += ((pixel ushr 16) and 0xFF).toFloat()
            buckets[cell * 3 + 1] += ((pixel ushr 8) and 0xFF).toFloat()
            buckets[cell * 3 + 2] += (pixel and 0xFF).toFloat()
            counts[cell]++
        }
    }
    for (cell in 0 until GRID_SIZE * GRID_SIZE) {
        val count = counts[cell].coerceAtLeast(1)
        buckets[cell * 3] /= count
        buckets[cell * 3 + 1] /= count
        buckets[cell * 3 + 2] /= count
    }
    return SpriteDescriptor(buckets)
}

/** 1 = identical, 0 = maximally different. */
fun similarity(
    a: SpriteDescriptor,
    b: SpriteDescriptor,
): Float {
    var sumSquares = 0.0
    for (i in a.buckets.indices) {
        val diff = (a.buckets[i] - b.buckets[i]).toDouble()
        sumSquares += diff * diff
    }
    val distance = sqrt(sumSquares)
    val maxDistance = sqrt(a.buckets.size * 255.0 * 255.0)
    return (1.0 - (distance / maxDistance)).toFloat().coerceIn(0f, 1f)
}

/** Highest-similarity catalog entry, or null if [catalog] is empty. */
fun pickBestMatch(
    descriptor: SpriteDescriptor,
    catalog: List<Pair<ShinyRecord, SpriteDescriptor>>,
): Pair<ShinyRecord, Float>? =
    catalog
        .map { (record, candidate) -> record to similarity(descriptor, candidate) }
        .maxByOrNull { (_, score) -> score }

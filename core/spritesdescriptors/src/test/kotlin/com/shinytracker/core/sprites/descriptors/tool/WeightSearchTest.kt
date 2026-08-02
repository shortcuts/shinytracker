package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.computeDescriptor
import org.junit.Assert.assertTrue
import org.junit.Test

private fun entry(
    assetPath: String,
    argb: Int,
) = DescriptorEntry(assetPath, computeDescriptor(IntArray(16) { argb }, 4, 4))

class WeightSearchTest {
    @Test
    fun `accuracy is 1 for an empty confusable list and less than 1 when pairs are flagged`() {
        val entries =
            listOf(
                entry("a.png", 0xFF000000.toInt() or 0xFF0000),
                entry("b.png", 0xFF000000.toInt() or 0x0000FF),
            )
        assertTrue(accuracy(entries, emptyList()) == 1f)
        val pair = findConfusablePairs(entries, threshold = 0f) // everything "confusable" at threshold 0
        assertTrue(accuracy(entries, pair) < 1f)
    }

    @Test
    fun `hill climb never returns a worse score than the baseline it started from`() {
        val red = entry("red.png", 0xFF000000.toInt() or 0xFF0000)
        val redAlmost = entry("red_almost.png", 0xFF000000.toInt() or 0xFE0000)
        val blue = entry("blue.png", 0xFF000000.toInt() or 0x0000FF)
        val entries = listOf(red, redAlmost, blue)
        val entriesByPath = entries.associateBy { it.assetPath }
        val targets = setOf("red.png", "red_almost.png")
        val shortlists = buildShortlists(entries, targets)

        val baselineScore = score(BASELINE, entriesByPath, shortlists)
        val tuned = hillClimb(entriesByPath, shortlists)
        val tunedScore = score(tuned, entriesByPath, shortlists)

        assertTrue(tunedScore >= baselineScore)
        assertTrue("weights should still sum to ~1.0", kotlin.math.abs(tuned.sum() - 1f) < 0.001f)
    }
}

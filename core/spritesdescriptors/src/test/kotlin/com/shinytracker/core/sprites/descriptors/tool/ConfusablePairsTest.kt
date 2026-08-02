package com.shinytracker.core.sprites.descriptors.tool

import com.shinytracker.core.sprites.descriptors.DescriptorEntry
import com.shinytracker.core.sprites.descriptors.computeDescriptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun entry(
    assetPath: String,
    argb: Int,
) = DescriptorEntry(assetPath, computeDescriptor(IntArray(16) { argb }, 4, 4))

class ConfusablePairsTest {
    @Test
    fun `findTopNonSelfMatches pairs each entry with its closest other entry, never itself`() {
        val red = entry("red.png", 0xFF000000.toInt() or 0xFF0000)
        val redAlmost = entry("red_almost.png", 0xFF000000.toInt() or 0xFE0000)
        val blue = entry("blue.png", 0xFF000000.toInt() or 0x0000FF)

        val matches = findTopNonSelfMatches(listOf(red, redAlmost, blue))

        val redMatch = matches.first { it.assetPathA == "red.png" }
        assertEquals("red_almost.png", redMatch.assetPathB)

        val redAlmostMatch = matches.first { it.assetPathA == "red_almost.png" }
        assertEquals("red.png", redAlmostMatch.assetPathB)
    }

    @Test
    fun `findConfusablePairs flags near-identical entries but not distinct ones`() {
        val red = entry("red.png", 0xFF000000.toInt() or 0xFF0000)
        val redAlmost = entry("red_almost.png", 0xFF000000.toInt() or 0xFE0000)
        val blue = entry("blue.png", 0xFF000000.toInt() or 0x0000FF)

        val confusable = findConfusablePairs(listOf(red, redAlmost, blue), threshold = 0.95f)

        assertEquals(2, confusable.size) // red<->red_almost reported from both directions
        assertTrue(confusable.all { it.score >= 0.95f })
        assertTrue(confusable.none { "blue.png" in setOf(it.assetPathA, it.assetPathB) })
    }

    @Test
    fun `findConfusablePairs sorts by descending score`() {
        val a = entry("a.png", 0xFF000000.toInt() or 0xFF0000)
        val b = entry("b.png", 0xFF000000.toInt() or 0xFE0000)
        val c = entry("c.png", 0xFF000000.toInt() or 0xFD0000)

        val confusable = findConfusablePairs(listOf(a, b, c), threshold = 0f)

        val scores = confusable.map { it.score }
        assertEquals(scores.sortedDescending(), scores)
    }
}

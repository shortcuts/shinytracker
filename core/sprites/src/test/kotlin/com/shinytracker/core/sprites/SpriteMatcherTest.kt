package com.shinytracker.core.sprites

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpriteMatcherTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val matcher = SpriteMatcher(context, ShinyChecklistSource(context, PokemonDexDataSource(context)))

    private fun loadCrop(assetPath: String) = context.assets.open("sprites/$assetPath").use { BitmapFactory.decodeStream(it) }

    @Test
    fun `match picks the shiny catalog entry for a shiny crop`() {
        val crop = loadCrop("pokemon_icon_001_00_shiny.png")

        val result = matcher.match(crop)!!

        assertEquals(1, result.dexEntry.dexId)
        assertTrue(result.shiny)
    }

    @Test
    fun `match picks the correct species, not a different one from the fixture catalog`() {
        val crop = loadCrop("pokemon_icon_004_00.png")

        val result = matcher.match(crop)!!

        assertEquals(4, result.dexEntry.dexId)
        assertTrue(!result.shiny)
    }

    @Test
    fun `matchCandidates returns the best match first`() {
        val crop = loadCrop("pokemon_icon_001_00.png")

        val candidates = matcher.matchCandidates(crop, topN = 3)

        assertTrue(candidates.isNotEmpty())
        assertEquals(1, candidates.first().dexEntry.dexId)
        assertTrue(!candidates.first().shiny)
        // scores are sorted descending
        for (i in 1 until candidates.size) {
            assertTrue(candidates[i - 1].confidence >= candidates[i].confidence)
        }
    }
}

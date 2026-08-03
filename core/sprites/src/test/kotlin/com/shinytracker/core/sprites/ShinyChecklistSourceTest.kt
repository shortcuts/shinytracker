package com.shinytracker.core.sprites

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.model.PokemonType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShinyChecklistSourceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val source = ShinyChecklistSource(context, PokemonDexDataSource(context))

    @Test
    fun `observeChecklist parses the bundled checklist asset`() =
        runTest {
            val checklist = source.observeChecklist().first()

            assertTrue(checklist.isNotEmpty())
            assertEquals("Bulbasaur", source.nameFor(1))
        }

    @Test
    fun `dexEntryFor merges bundled Pokemon dex data`() {
        assertEquals(listOf(PokemonType.GRASS, PokemonType.POISON), source.dexEntryFor(1).types)
    }

    @Test
    fun `observeChecklist represents costume and form variants as distinct entries, not just base species`() =
        runTest {
            val checklist = source.observeChecklist().first()
            val uniqueDexIds = checklist.map { it.dexId }.distinct().size
            val uniqueTriples = checklist.map { Triple(it.dexId, it.formId, it.costumeId) }.distinct().size

            assertTrue(checklist.size > uniqueDexIds)
            assertEquals(checklist.size, uniqueTriples)
        }

    @Test
    fun `observeChecklist and dexEntryFor carry family and releaseDate`() =
        runTest {
            val checklist = source.observeChecklist().first()
            val bulbasaur = checklist.first { it.dexId == 1 && it.formId == 0 && it.costumeId == 0 }

            assertEquals("Bulbasaur", bulbasaur.family)
            assertEquals("2018-03-25", bulbasaur.releaseDate)
            assertEquals("Bulbasaur", source.dexEntryFor(1).family)
            assertEquals("2018-03-25", source.dexEntryFor(1).releaseDate)

            // dexId 421 (Cherrim) has no plain pms.json row -- exercises
            // leekduck_species_meta's fallback-to-earliest-released_date path.
            val cherrim = checklist.first { it.dexId == 421 && it.formId == 11 }
            assertEquals("Cherubi", cherrim.family)
            assertEquals("2022-04-20", cherrim.releaseDate)
        }

    @Test
    fun `refresh failure leaves the previously loaded checklist untouched`() =
        runTest {
            val before = source.observeChecklist().first()

            val result = source.refreshFrom("http://127.0.0.1:1/unreachable")

            assertTrue(result.isFailure)
            assertEquals(before, source.observeChecklist().first())
        }
}

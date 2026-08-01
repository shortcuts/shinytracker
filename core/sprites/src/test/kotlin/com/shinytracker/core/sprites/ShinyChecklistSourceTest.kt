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
    fun `refresh failure leaves the previously loaded checklist untouched`() =
        runTest {
            val before = source.observeChecklist().first()

            val result = source.refreshFrom("http://127.0.0.1:1/unreachable")

            assertTrue(result.isFailure)
            assertEquals(before, source.observeChecklist().first())
        }
}

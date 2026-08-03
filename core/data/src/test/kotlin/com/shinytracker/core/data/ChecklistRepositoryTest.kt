package com.shinytracker.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.model.buildChecklistEntries
import com.shinytracker.core.sprites.PokemonDexDataSource
import com.shinytracker.core.sprites.ShinyChecklistSource
import com.shinytracker.core.testing.FakeCaughtDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChecklistRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val checklistSource = ShinyChecklistSource(context, PokemonDexDataSource(context))
    private val caughtRepository = CaughtRepository(FakeCaughtDao())
    private val repository = ChecklistRepository(checklistSource, caughtRepository)

    @Test
    fun `entries default to not caught`() =
        runTest {
            val entries = repository.observeChecklist().first()

            assertTrue(entries.isNotEmpty())
            assertTrue(entries.all { !it.caught })
        }

    @Test
    fun `a caught species is marked caught with its caughtAt timestamp`() =
        runTest {
            caughtRepository.recordIfAbsent(CaughtRecord(DexEntry(1, 0, 0, "Bulbasaur"), shiny = true, caughtAt = 1000L))

            val bulbasaur = repository.observeChecklist().first().first { it.dexEntry.dexId == 1 }

            assertEquals(true, bulbasaur.caught)
            assertEquals(1000L, bulbasaur.caughtAt)
        }

    @Test
    fun `toggleCaught marks an uncaught entry as caught`() =
        runTest {
            val entry = repository.observeChecklist().first().first { it.dexEntry.dexId == 1 }

            repository.toggleCaught(entry)

            val updated = repository.observeChecklist().first().first { it.dexEntry.dexId == 1 }
            assertTrue(updated.caught)
        }

    @Test
    fun `matches caught records by full dexId, formId, costumeId identity, not dexId alone`() =
        runTest {
            val base = DexEntry(37, 0, 0, "Vulpix")
            val alolan = DexEntry(37, 1, 0, "Vulpix (Alolan)")
            caughtRepository.recordIfAbsent(CaughtRecord(alolan, shiny = true, caughtAt = 2000L))

            val caught = caughtRepository.observeCaught().first()
            val baseEntry = buildChecklistEntries(listOf(base), caught).first()
            val alolanEntry = buildChecklistEntries(listOf(alolan), caught).first()

            assertFalse(baseEntry.caught)
            assertTrue(alolanEntry.caught)
            assertEquals(2000L, alolanEntry.caughtAt)
        }

    @Test
    fun `refresh delegates to the checklist source`() =
        runTest {
            val result = repository.refresh()

            // Robolectric tests have no real network access, so this always fails --
            // the point of this test is only that ChecklistRepository.refresh() reaches
            // ShinyChecklistSource.refresh() and returns its Result, not network mocking
            // (already covered at the source level by ShinyChecklistSourceTest).
            assertTrue(result.isFailure)
        }

    @Test
    fun `toggleCaught marks a caught entry as uncaught`() =
        runTest {
            caughtRepository.recordIfAbsent(CaughtRecord(DexEntry(1, 0, 0, "Bulbasaur"), shiny = true, caughtAt = 1000L))
            val entry = repository.observeChecklist().first().first { it.dexEntry.dexId == 1 }

            repository.toggleCaught(entry)

            val updated = repository.observeChecklist().first().first { it.dexEntry.dexId == 1 }
            assertFalse(updated.caught)
        }
}

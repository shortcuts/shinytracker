package com.shinytracker.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.sprites.ShinyChecklistSource
import com.shinytracker.core.testing.FakeCaughtDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChecklistRepositoryTest {
    private val checklistSource = ShinyChecklistSource(ApplicationProvider.getApplicationContext<Context>())
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
}

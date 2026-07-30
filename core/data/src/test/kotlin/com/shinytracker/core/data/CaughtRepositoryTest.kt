package com.shinytracker.core.data

import com.shinytracker.core.model.CaughtRecord
import com.shinytracker.core.model.DexEntry
import com.shinytracker.core.testing.FakeCaughtDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaughtRepositoryTest {
    private val fakeDao = FakeCaughtDao()
    private val repository = CaughtRepository(fakeDao)

    @Test
    fun `recordIfAbsent returns true for a new record, false for a duplicate`() =
        runTest {
            val record = CaughtRecord(DexEntry(25, 0, 0, "Pikachu"), shiny = true, caughtAt = 1000L)

            assertTrue(repository.recordIfAbsent(record))
            assertFalse(repository.recordIfAbsent(record))
        }

    @Test
    fun `observeCaught maps entities to domain records`() =
        runTest {
            val record = CaughtRecord(DexEntry(1, 0, 0, "Bulbasaur"), shiny = false, caughtAt = 500L)
            repository.recordIfAbsent(record)

            assertEquals(listOf(record), repository.observeCaught().first())
        }
}

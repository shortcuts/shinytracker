package com.shinytracker.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.database.entities.CaughtEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CaughtDaoTest {
    private lateinit var database: ShinyTrackerDatabase

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ShinyTrackerDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then query round-trips all fields`() =
        runTest {
            val entity = CaughtEntity(dexId = 25, formId = 0, costumeId = 0, shiny = true, name = "Pikachu", caughtAt = 1000L)
            database.caughtDao().insertIfAbsent(entity)

            val all = database.caughtDao().observeAll().first()

            assertEquals(listOf(entity), all)
        }

    @Test
    fun `insertIfAbsent returns -1 on duplicate primary key, does not duplicate row`() =
        runTest {
            val entity = CaughtEntity(dexId = 1, formId = 0, costumeId = 0, shiny = false, name = "Bulbasaur", caughtAt = 500L)
            val first = database.caughtDao().insertIfAbsent(entity)
            val second = database.caughtDao().insertIfAbsent(entity)

            assertEquals(-1L, second)
            assertEquals(
                1,
                database
                    .caughtDao()
                    .observeAll()
                    .first()
                    .size,
            )
            check(first != -1L)
        }
}

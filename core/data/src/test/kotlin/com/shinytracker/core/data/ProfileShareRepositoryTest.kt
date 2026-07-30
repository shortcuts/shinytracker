package com.shinytracker.core.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.shinytracker.core.testing.FakeCaughtDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * exportProfile()'s FileProvider hand-off needs the real <provider> manifest
 * entry from :app, so only the import side (parsing + error handling) is
 * covered here at the unit level -- import is the side that guards the
 * "never touch CaughtRepository" invariant, which is what matters most.
 */
@RunWith(RobolectricTestRunner::class)
class ProfileShareRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = ProfileShareRepository(context, CaughtRepository(FakeCaughtDao()))

    @Test
    fun `importProfile parses a well-formed shared profile file`() =
        runTest {
            val file =
                File(context.cacheDir, "incoming.json").apply {
                    writeText(
                        """{"schemaVersion":1,"entries":[
                        |{"dexId":1,"formId":0,"costumeId":0,"shiny":true,"name":"Bulbasaur","caughtAt":1000}
                        |]}
                        """.trimMargin(),
                    )
                }

            val result = repository.importProfile(Uri.fromFile(file))

            assertTrue(result.isSuccess)
            val entries = result.getOrThrow()
            assertEquals(1, entries.size)
            assertEquals("Bulbasaur", entries.first().dexEntry.name)
        }

    @Test
    fun `importProfile fails on malformed JSON`() =
        runTest {
            val file = File(context.cacheDir, "broken.json").apply { writeText("not json") }

            val result = repository.importProfile(Uri.fromFile(file))

            assertTrue(result.isFailure)
        }
}

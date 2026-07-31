package com.shinytracker.core.sprites

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PokemonDexDataSourceTest {
    private val source = PokemonDexDataSource(ApplicationProvider.getApplicationContext<Context>())

    @Test
    fun `get returns bundled species metadata`() {
        val data = source.get(1)

        assertNotNull(data)
        assertTrue(data!!.species.orEmpty().contains("Seed"))
        assertEquals(listOf(2), data.evolvesTo)
    }

    @Test
    fun `get returns null for a dex id outside the bundled source's coverage`() {
        assertNull(source.get(999_999))
    }
}

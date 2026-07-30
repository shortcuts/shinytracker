package com.shinytracker.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerationTest {
    @Test
    fun `boundary dex ids map to the expected generation`() {
        assertEquals(Generation.KANTO, Generation.fromDexId(1))
        assertEquals(Generation.KANTO, Generation.fromDexId(151))
        assertEquals(Generation.JOHTO, Generation.fromDexId(152))
        assertEquals(Generation.JOHTO, Generation.fromDexId(251))
        assertEquals(Generation.PALDEA, Generation.fromDexId(1025))
    }

    @Test
    fun `unknown dex id beyond the last generation falls back to the last generation`() {
        assertEquals(Generation.PALDEA, Generation.fromDexId(9999))
    }
}

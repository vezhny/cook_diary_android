package com.vezhny.cookdiary.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeTagsTest {
    @Test
    fun `trims, lowercases, drops empty and duplicate tags`() {
        assertEquals("суп,быстро", normalizeTags("Суп,  быстро , ,суп"))
    }

    @Test
    fun `blank input gives empty string`() {
        assertEquals("", normalizeTags("  , "))
    }

    @Test
    fun `list input is normalized the same way`() {
        assertEquals("суп,быстро", normalizeTags(listOf(" Суп", "", "БЫСТРО", "суп")))
    }
}

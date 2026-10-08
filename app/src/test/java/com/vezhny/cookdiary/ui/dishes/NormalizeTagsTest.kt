package com.vezhny.cookdiary.ui.dishes

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeTagsTest {
    @Test
    fun `trims, lowercases, drops empty and duplicate tags`() {
        assertEquals("суп,быстро", DishEditViewModel.normalizeTags("Суп,  быстро , ,суп"))
    }

    @Test
    fun `blank input gives empty string`() {
        assertEquals("", DishEditViewModel.normalizeTags("  , "))
    }
}

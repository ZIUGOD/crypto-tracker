package com.example.crypto_tracker.data

import com.example.crypto_tracker.data.preferences.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppThemeTest {
    @Test
    fun theLiteSlotComesFirstAndTheCurrentThemeIsTheSecondOption() {
        assertEquals(listOf(AppTheme.LITE, AppTheme.GLASS), AppTheme.entries)
        assertEquals(1, AppTheme.GLASS.ordinal)
    }

    @Test
    fun onlyBuiltThemesAreSelectable() {
        assertFalse(AppTheme.LITE.available)
        assertEquals(listOf(AppTheme.GLASS), AppTheme.selectable)
    }

    @Test
    fun storedValuesFallBackToTheDefault() {
        assertEquals(AppTheme.GLASS, AppTheme.fromStored(null))
        assertEquals(AppTheme.GLASS, AppTheme.fromStored("REMOVED_THEME"))
        assertEquals(AppTheme.GLASS, AppTheme.fromStored("LITE")) // reserved, not built yet
        assertEquals(AppTheme.GLASS, AppTheme.fromStored("GLASS"))
    }
}

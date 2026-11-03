package com.example.crypto_tracker.data

import com.example.crypto_tracker.data.preferences.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeTest {
    @Test
    fun liteIsTheFirstOptionAndGlassTheSecond() {
        assertEquals(listOf(AppTheme.LITE, AppTheme.GLASS), AppTheme.entries)
        assertEquals(1, AppTheme.GLASS.ordinal)
        assertEquals(AppTheme.entries, AppTheme.selectable)
    }

    @Test
    fun liteIsTheDefault() {
        assertEquals(AppTheme.LITE, AppTheme.Default)
        assertEquals(AppTheme.LITE, AppTheme.fromStored(null))
    }

    @Test
    fun storedChoiceIsKeptAndUnknownValuesFallBackToLite() {
        assertEquals(AppTheme.GLASS, AppTheme.fromStored("GLASS"))
        assertEquals(AppTheme.LITE, AppTheme.fromStored("LITE"))
        assertEquals(AppTheme.LITE, AppTheme.fromStored("REMOVED_THEME"))
    }
}

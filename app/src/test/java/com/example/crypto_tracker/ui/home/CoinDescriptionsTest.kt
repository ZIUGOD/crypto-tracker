package com.example.crypto_tracker.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CoinDescriptionsTest {

    @Test
    fun resolvesProviderAliases() {
        assertEquals(CoinDescriptions.resFor("ripple"), CoinDescriptions.resFor("xrp"))
        assertEquals(CoinDescriptions.resFor("binancecoin"), CoinDescriptions.resFor("BNB"))
    }

    @Test
    fun knownCoinsHaveASummary() {
        assertNotNull(CoinDescriptions.resFor("bitcoin"))
    }

    @Test
    fun unknownCoinsHaveNone() {
        assertNull(CoinDescriptions.resFor("unknown-coin"))
    }
}

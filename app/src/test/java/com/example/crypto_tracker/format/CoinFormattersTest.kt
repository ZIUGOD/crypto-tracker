package com.example.crypto_tracker.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CoinFormattersTest {

    private val ptBr = Locale.forLanguageTag("pt-BR")

    @Test
    fun compact_usesTrillionBillionMillionSuffixes() {
        assertEquals("$1.25T", formatCompactUsd(1_250_000_000_000.0))
        assertEquals("$3.40B", formatCompactUsd(3_400_000_000.0))
        assertEquals("$850.50M", formatCompactUsd(850_500_000.0))
    }

    @Test
    fun compact_belowAMillionIsPlain() {
        assertEquals("$999", formatCompactUsd(999.4))
    }

    @Test
    fun compact_followsLocaleAndSuffixes() {
        val suffixes = CompactSuffixes(trillion = " tri", billion = " bi", million = " mi")

        assertEquals("US$3,40 bi", formatCompactUsd(3_400_000_000.0, ptBr, suffixes))
    }

    @Test
    fun currency_formatsWithGroupingAndCents() {
        assertEquals("$60,000.50", formatCurrencyUsd(60000.5))
    }

    @Test
    fun currency_englishLocaleKeepsTheDollarSign() {
        assertEquals("$60,000.50", formatCurrencyUsd(60000.5, Locale.ENGLISH))
    }

    @Test
    fun currency_portugueseUsesBrazilianSeparatorsAndStaysInDollars() {
        assertEquals("US$60.000,50", formatCurrencyUsd(60000.5, ptBr))
    }
}

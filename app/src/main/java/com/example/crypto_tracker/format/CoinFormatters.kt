package com.example.crypto_tracker.format

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Unit suffixes for [formatCompactUsd]; translated per language (e.g. "tri", "bi", "mi"). */
data class CompactSuffixes(
    val trillion: String = "T",
    val billion: String = "B",
    val million: String = "M"
)

/**
 * Formats large values compactly (e.g. $1.25B, $850.50M; "US$1,25 bi" in pt-BR).
 * Separators and the currency symbol follow [locale]; [suffixes] carry the translated units.
 */
fun formatCompactUsd(
    value: Double,
    locale: Locale = Locale.US,
    suffixes: CompactSuffixes = CompactSuffixes()
): String {
    val (divisor, suffix, decimals) = when {
        value >= 1_000_000_000_000 -> Triple(1_000_000_000_000.0, suffixes.trillion, 2)
        value >= 1_000_000_000 -> Triple(1_000_000_000.0, suffixes.billion, 2)
        value >= 1_000_000 -> Triple(1_000_000.0, suffixes.million, 2)
        else -> Triple(1.0, "", 0)
    }
    return usdPrefix(locale) + String.format(locale, "%.${decimals}f", value / divisor) + suffix
}

/** US Dollar currency format with grouping and cents, using the separators and symbol of [locale]. */
fun formatCurrencyUsd(value: Double, locale: Locale = Locale.US): String =
    usdFormats.get()!!.getOrPut(locale) {
        NumberFormat.getCurrencyInstance(locale).apply { currency = USD }
    }.format(value).replace(NBSP, "")

/** "$" in English, "US$" in pt-BR. Spaces are dropped so wide prices still fit in the coin card. */
private fun usdPrefix(locale: Locale): String = USD.getSymbol(locale)

private const val NBSP = " "

private val USD: Currency = Currency.getInstance("USD")

// NumberFormat is costly to build and not thread-safe; the UI and the background worker both
// format prices, so each thread keeps its own instance per locale.
private val usdFormats = object : ThreadLocal<MutableMap<Locale, NumberFormat>>() {
    override fun initialValue(): MutableMap<Locale, NumberFormat> = HashMap()
}

package com.example.crypto_tracker.ui.home

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats large double currency values into compact string format (e.g. $1.25B, $850.5M).
 */
internal fun formatCompactUsd(value: Double): String {
    return when {
        value >= 1_000_000_000_000 -> String.format(Locale.US, "$%.2fT", value / 1_000_000_000_000)
        value >= 1_000_000_000 -> String.format(Locale.US, "$%.2fB", value / 1_000_000_000)
        value >= 1_000_000 -> String.format(Locale.US, "$%.2fM", value / 1_000_000)
        else -> String.format(Locale.US, "$%.0f", value)
    }
}

/**
 * Formats a raw numeric double value into US Dollar ($) currency format.
 */
internal fun formatCurrencyUsd(value: Double): String = usdFormat.format(value)

// Created once: NumberFormat is costly to build and this runs for every visible price.
// Not thread-safe, so only call the formatters from the main thread (UI code).
private val usdFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

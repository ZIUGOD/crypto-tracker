package com.example.crypto_tracker.ui.home

import androidx.annotation.StringRes
import com.example.crypto_tracker.R

/**
 * Maps well-known coins to their translated summary (`coin_desc_*` in strings.xml). To add a coin,
 * add one entry to [KNOWN] keyed by its CoinGecko id plus the string in every language;
 * unknown coins get the generic sentence (`coin_desc_generic`).
 */
object CoinDescriptions {

    private val KNOWN = mapOf(
        "bitcoin" to R.string.coin_desc_bitcoin,
        "ethereum" to R.string.coin_desc_ethereum,
        "tether" to R.string.coin_desc_tether,
        "binancecoin" to R.string.coin_desc_binancecoin,
        "solana" to R.string.coin_desc_solana,
        "ripple" to R.string.coin_desc_ripple,
        "usd-coin" to R.string.coin_desc_usd_coin,
        "cardano" to R.string.coin_desc_cardano,
        "dogecoin" to R.string.coin_desc_dogecoin,
        "avalanche-2" to R.string.coin_desc_avalanche
    )

    // Ids that different data providers use for the same coin.
    private val ALIASES = mapOf(
        "bnb" to "binancecoin",
        "xrp" to "ripple",
        "avalanche" to "avalanche-2"
    )

    /** String resource with the summary of [id], or null when the coin has no hand-written one. */
    @StringRes
    fun resFor(id: String): Int? = KNOWN[id.lowercase().let { ALIASES[it] ?: it }]
}

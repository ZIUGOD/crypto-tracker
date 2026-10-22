package com.example.crypto_tracker.data.model

/**
 * Data model representing a cryptocurrency item in the global market rankings.
 *
 * @property id Unique identifier string for the coin (e.g., "bitcoin", "ethereum").
 * @property rank Global market capitalization rank (e.g., 1 for #1 coin).
 * @property symbol Uppercase trading symbol (e.g., "BTC", "ETH").
 * @property name Display name of the cryptocurrency (e.g., "Bitcoin", "Ethereum").
 * @property currentPriceUsd Current spot price in USD ($).
 * @property priceChangePercentage24h Price percentage change over the last 24 hours.
 * @property imageUrl Image URL of the coin logo (e.g., "https://assets.coingecko.com/.../bitcoin.png").
 * @property high24hUsd Highest price reached in the last 24 hours in USD.
 * @property low24hUsd Lowest price reached in the last 24 hours in USD.
 * @property marketCapUsd Total market capitalization in USD.
 * @property totalVolumeUsd Total 24h trading volume in USD.
 * @property description Brief summary description of the cryptocurrency.
 */
data class CryptoCoin(
    val id: String,
    val rank: Int,
    val symbol: String,
    val name: String,
    val currentPriceUsd: Double,
    val priceChangePercentage24h: Double,
    val imageUrl: String? = null,
    val high24hUsd: Double? = null,
    val low24hUsd: Double? = null,
    val marketCapUsd: Double? = null,
    val totalVolumeUsd: Double? = null,
    val description: String? = null
)

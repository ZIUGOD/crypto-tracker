package com.example.crypto_tracker.data.repository

import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure JSON-to-model mapping for each supported provider. No I/O, so it is trivially unit-testable.
 * When a provider changes its payload, this is the only file that needs to change.
 */
object CoinParser {

    fun fromCoinGecko(json: String): List<CryptoCoin> {
        val array = JSONArray(json)
        return (0 until array.length())
            .map { index -> coinGeckoCoin(array.getJSONObject(index), fallbackRank = index + 1) }
            .sortedBy { it.rank }
    }

    fun fromCoinPaprika(json: String): List<CryptoCoin> {
        val array = JSONArray(json)
        return (0 until array.length())
            .map { index -> coinPaprikaCoin(array.getJSONObject(index), fallbackRank = index + 1) }
            .sortedBy { it.rank }
    }

    /** CoinPaprika ids are `<symbol>-<slug>`; the app keys coins by the CoinGecko id (pins, descriptions, links). */
    private val paprikaSlugToCoinGeckoId = mapOf(
        "binance-coin" to "binancecoin",
        "xrp" to "ripple"
    )

    private fun coinGeckoIdOf(paprikaId: String, symbol: String): String {
        val slug = paprikaId.removePrefix("${symbol.lowercase()}-")
        return paprikaSlugToCoinGeckoId[slug] ?: slug
    }

    private fun coinGeckoCoin(obj: JSONObject, fallbackRank: Int): CryptoCoin {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val symbol = obj.getString("symbol").uppercase()
        return CryptoCoin(
            id = id,
            rank = obj.optInt("market_cap_rank", fallbackRank),
            symbol = symbol,
            name = name,
            currentPriceUsd = obj.getDouble("current_price"),
            priceChangePercentage24h = obj.optDouble("price_change_percentage_24h", 0.0),
            imageUrl = obj.optString("image").takeIf { it.isNotBlank() },
            high24hUsd = obj.optDoubleOrNull("high_24h"),
            low24hUsd = obj.optDoubleOrNull("low_24h"),
            marketCapUsd = obj.optDoubleOrNull("market_cap"),
            totalVolumeUsd = obj.optDoubleOrNull("total_volume")
        )
    }

    private fun coinPaprikaCoin(obj: JSONObject, fallbackRank: Int): CryptoCoin {
        val paprikaId = obj.getString("id")
        val symbol = obj.getString("symbol").uppercase()
        val usd = obj.getJSONObject("quotes").getJSONObject("USD")
        return CryptoCoin(
            id = coinGeckoIdOf(paprikaId, symbol),
            rank = obj.optInt("rank", fallbackRank),
            symbol = symbol,
            name = obj.getString("name"),
            currentPriceUsd = usd.getDouble("price"),
            priceChangePercentage24h = usd.optDouble("percent_change_24h", 0.0),
            imageUrl = AppConfig.Network.coinpaprikaIconUrl(paprikaId),
            marketCapUsd = usd.optDoubleOrNull("market_cap"),
            totalVolumeUsd = usd.optDoubleOrNull("volume_24h")
        )
    }

    /** Null when the key is missing or JSON null (unlike [JSONObject.optDouble], which would yield 0.0/NaN). */
    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (has(key) && !isNull(key)) getDouble(key) else null
}

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

    fun fromCoinCap(json: String): List<CryptoCoin> {
        val array = JSONObject(json).getJSONArray("data")
        return (0 until array.length())
            .map { index -> coinCapCoin(array.getJSONObject(index), fallbackRank = index + 1) }
            .sortedBy { it.rank }
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

    private fun coinCapCoin(obj: JSONObject, fallbackRank: Int): CryptoCoin {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val symbol = obj.getString("symbol").uppercase()
        return CryptoCoin(
            id = id,
            rank = obj.optInt("rank", fallbackRank),
            symbol = symbol,
            name = name,
            currentPriceUsd = obj.getString("priceUsd").toDoubleOrNull() ?: 0.0,
            priceChangePercentage24h = obj.getString("changePercent24Hr").toDoubleOrNull() ?: 0.0,
            imageUrl = AppConfig.Network.coincapIconUrl(symbol.lowercase()),
            marketCapUsd = obj.getString("marketCapUsd").toDoubleOrNull(),
            totalVolumeUsd = obj.getString("volumeUsd24Hr").toDoubleOrNull()
        )
    }

    /** Null when the key is missing or JSON null (unlike [JSONObject.optDouble], which would yield 0.0/NaN). */
    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (has(key) && !isNull(key)) getDouble(key) else null
}

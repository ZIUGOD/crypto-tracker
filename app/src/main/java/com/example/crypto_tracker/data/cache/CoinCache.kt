package com.example.crypto_tracker.data.cache

import android.content.Context
import android.util.Log
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import org.json.JSONArray
import org.json.JSONObject

/** A market snapshot stored locally, with the time ([savedAtMillis]) it was fetched. */
data class CachedCoins(val coins: List<CryptoCoin>, val savedAtMillis: Long)

/** Contract for the last successfully fetched market list. */
interface CoinCache {
    fun load(): CachedCoins?
    fun save(coins: List<CryptoCoin>, savedAtMillis: Long = System.currentTimeMillis())

    /** Cache that stores nothing; the default for tests and previews. */
    object None : CoinCache {
        override fun load(): CachedCoins? = null
        override fun save(coins: List<CryptoCoin>, savedAtMillis: Long) = Unit
    }
}

/** SharedPreferences-backed [CoinCache]: the list is small (see [AppConfig.Market.TOP_COINS_COUNT]), so JSON is enough. */
class CoinCachePreferences(context: Context) : CoinCache {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_COIN_CACHE, Context.MODE_PRIVATE)

    override fun load(): CachedCoins? {
        val json = prefs.getString(KEY_COINS, null) ?: return null
        return try {
            val coins = CoinCacheJson.decode(json)
            if (coins.isEmpty()) null else CachedCoins(coins, prefs.getLong(KEY_SAVED_AT, 0L))
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring unreadable coin cache", e)
            null
        }
    }

    override fun save(coins: List<CryptoCoin>, savedAtMillis: Long) {
        if (coins.isEmpty()) return
        prefs.edit()
            .putString(KEY_COINS, CoinCacheJson.encode(coins))
            .putLong(KEY_SAVED_AT, savedAtMillis)
            .apply()
    }

    private companion object {
        const val TAG = "CoinCache"
        const val KEY_COINS = "coins_json"
        const val KEY_SAVED_AT = "saved_at"
    }
}

/** Pure (de)serialization of the cached list; no Android types, so it is unit-testable. */
internal object CoinCacheJson {

    fun encode(coins: List<CryptoCoin>): String = JSONArray().apply {
        coins.forEach { coin ->
            put(JSONObject().apply {
                put("id", coin.id)
                put("rank", coin.rank)
                put("symbol", coin.symbol)
                put("name", coin.name)
                put("price", coin.currentPriceUsd)
                put("change24h", coin.priceChangePercentage24h)
                put("image", coin.imageUrl ?: JSONObject.NULL)
                put("high24h", coin.high24hUsd ?: JSONObject.NULL)
                put("low24h", coin.low24hUsd ?: JSONObject.NULL)
                put("marketCap", coin.marketCapUsd ?: JSONObject.NULL)
                put("volume", coin.totalVolumeUsd ?: JSONObject.NULL)
            })
        }
    }.toString()

    fun decode(json: String): List<CryptoCoin> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            CryptoCoin(
                id = obj.getString("id"),
                rank = obj.getInt("rank"),
                symbol = obj.getString("symbol"),
                name = obj.getString("name"),
                currentPriceUsd = obj.getDouble("price"),
                priceChangePercentage24h = obj.getDouble("change24h"),
                imageUrl = obj.optStringOrNull("image"),
                high24hUsd = obj.optDoubleOrNull("high24h"),
                low24hUsd = obj.optDoubleOrNull("low24h"),
                marketCapUsd = obj.optDoubleOrNull("marketCap"),
                totalVolumeUsd = obj.optDoubleOrNull("volume")
            )
        }
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (isNull(key)) null else getDouble(key)
}

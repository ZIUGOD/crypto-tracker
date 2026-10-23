package com.example.crypto_tracker.data.repository

import com.example.crypto_tracker.data.cache.CoinCache
import com.example.crypto_tracker.data.model.CryptoCoin

/** Decorator that stores every successful fetch in [cache], so any caller keeps it up to date. */
class CachingCryptoRepository(
    private val delegate: ICryptoRepository,
    private val cache: CoinCache
) : ICryptoRepository {

    override suspend fun fetchTopCryptos(): Result<List<CryptoCoin>> =
        delegate.fetchTopCryptos().onSuccess { cache.save(it) }
}

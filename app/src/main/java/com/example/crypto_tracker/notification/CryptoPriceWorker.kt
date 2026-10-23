package com.example.crypto_tracker.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.crypto_tracker.data.cache.CoinCachePreferences
import com.example.crypto_tracker.data.repository.CachingCryptoRepository
import com.example.crypto_tracker.data.repository.CryptoRepository
import com.example.crypto_tracker.data.repository.ICryptoRepository

/**
 * Background worker running periodically to fetch top crypto prices and update the persistent notification.
 * A failed fetch is reported as [Result.retry] so WorkManager backs off and tries again.
 */
class CryptoPriceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository: ICryptoRepository =
        CachingCryptoRepository(CryptoRepository(), CoinCachePreferences(appContext))
    private val notifier: PriceNotifier = CryptoNotificationManager(appContext)
    private val pinnedStore: PinnedCoinsStore = NotificationPreferences(appContext)

    override suspend fun doWork(): Result =
        repository.fetchTopCryptos().fold(
            onSuccess = { coins ->
                if (coins.isNotEmpty()) {
                    notifier.showPortfolioNotification(coins, pinnedStore.ensureDefaultSelection(coins))
                }
                Result.success()
            },
            onFailure = { Result.retry() }
        )
}

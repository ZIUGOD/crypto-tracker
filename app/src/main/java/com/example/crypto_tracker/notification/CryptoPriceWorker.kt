package com.example.crypto_tracker.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.crypto_tracker.data.repository.CryptoRepository

/**
 * Background worker running periodically to fetch top crypto prices and update the persistent notification.
 */
class CryptoPriceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository = CryptoRepository()
    private val notificationManager = CryptoNotificationManager(appContext)
    private val prefs = NotificationPreferences(appContext)

    override suspend fun doWork(): Result {
        return try {
            val result = repository.fetchTop10Cryptos()
            result.onSuccess { coins ->
                if (coins.isNotEmpty()) {
                    val top1Coin = coins.firstOrNull { it.rank == 1 } ?: coins.first()
                    val selectedIds = prefs.ensureDefaultSelectionFromTop1(top1Coin.id)
                    notificationManager.showPortfolioNotification(coins, selectedIds)
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

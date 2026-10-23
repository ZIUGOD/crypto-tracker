package com.example.crypto_tracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.crypto_tracker.MainActivity
import com.example.crypto_tracker.R
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.preferences.AppLocale

/**
 * Posts one notification per pinned coin and cancels the ones that are no longer pinned.
 * Unchanged notifications are not re-posted (see [PostedNotificationCache]).
 */
class CryptoNotificationManager(baseContext: Context) : PriceNotifier {

    // Localized with the language chosen in Settings, also when running from the background worker.
    private val context: Context = AppLocale.wrap(baseContext)
    private val locale = context.resources.configuration.locales[0]

    private val systemManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val postedCache = PostedNotificationCache()

    init {
        createNotificationChannel()
    }

    override fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>) {
        coins.forEach { coin ->
            val id = notificationIdFor(coin)
            if (coin.id in selectedCoinIds) {
                post(id, coin)
            } else {
                postedCache.forget(id)
                systemManager.cancel(id)
            }
        }
    }

    private fun post(id: Int, coin: CryptoCoin) {
        val text = notificationPriceText(coin, locale)
        if (!postedCache.shouldPost(id, text)) return

        val notification = NotificationCompat.Builder(context, AppConfig.Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(coin.name)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setAutoCancel(false)
            .setContentIntent(openAppIntent(coin))
            .build()
        systemManager.notify(id, notification)
    }

    private fun openAppIntent(coin: CryptoCoin): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, coin.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
    }

    /** Stable per-coin ID inside [AppConfig.Notifications.NOTIFICATION_ID_RANGE]. */
    private fun notificationIdFor(coin: CryptoCoin): Int =
        AppConfig.Notifications.NOTIFICATION_BASE_ID +
            Math.floorMod(coin.id.hashCode(), AppConfig.Notifications.NOTIFICATION_ID_RANGE)

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AppConfig.Notifications.CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        systemManager.createNotificationChannel(channel)
    }
}

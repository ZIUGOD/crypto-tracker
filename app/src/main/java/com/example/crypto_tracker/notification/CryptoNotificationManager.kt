package com.example.crypto_tracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.crypto_tracker.MainActivity
import com.example.crypto_tracker.R
import com.example.crypto_tracker.data.model.CryptoCoin
import java.util.Locale

/**
 * Manages notification channels and individual independent notifications for each selected cryptocurrency.
 */
class CryptoNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "crypto_price_channel"
        private const val NOTIFICATION_BASE_ID = 2000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Crypto Live Prices"
        val descriptionText = "Persistent notifications keeping you updated with live crypto prices"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Posts an individual independent status bar notification for each selected cryptocurrency,
     * and cancels notifications for any coins that are unselected.
     */
    fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        coins.forEach { coin ->
            // Generate a unique and stable notification ID for each coin
            val notificationId = NOTIFICATION_BASE_ID + Math.abs(coin.id.hashCode() % 1000)

            if (selectedCoinIds.contains(coin.id)) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context, coin.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
                )

                val sign = if (coin.priceChangePercentage24h >= 0) "+" else ""
                val priceText = "$${String.format(Locale.US, "%,.2f", coin.currentPriceUsd)} ($sign${String.format(Locale.US, "%,.1f", coin.priceChangePercentage24h)}%)"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(coin.name)
                    .setContentText(priceText)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(false)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(notificationId, notification)
            } else {
                // Cancel notification if this coin is not currently selected
                notificationManager.cancel(notificationId)
            }
        }
    }
}

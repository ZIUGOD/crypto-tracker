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
 * Manages notification channels and persistent status bar notifications for crypto prices.
 */
class CryptoNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "crypto_price_channel"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Crypto Live Prices"
        val descriptionText = "Persistent notification keeping you updated with live crypto prices"
        val importance = NotificationManager.IMPORTANCE_LOW // Low priority so it sits quietly in the notification bar
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Updates the persistent notification with the prices of the user's selected cryptocurrencies.
     * Displays just the cryptocurrency name as title and price below it, without app name or ticker symbols.
     */
    fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>) {
        val selectedCoins = coins.filter { selectedCoinIds.contains(it.id) }
        if (selectedCoins.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false) // Can be swiped away
            .setAutoCancel(false) // Stays in notification shade
            .setContentIntent(pendingIntent)

        if (selectedCoins.size == 1) {
            // Super simple: Title = Crypto Name, Text = Price and 24h change (no ticker symbol, no app name)
            val coin = selectedCoins.first()
            val sign = if (coin.priceChangePercentage24h >= 0) "+" else ""
            val priceText = "$${String.format(Locale.US, "%,.2f", coin.currentPriceUsd)} ($sign${String.format(Locale.US, "%,.1f", coin.priceChangePercentage24h)}%)"

            notificationBuilder
                .setContentTitle(coin.name)
                .setContentText(priceText)
        } else {
            // Multiple coins: Title = first coin name, InboxStyle with name and price only (no tickers, no app name)
            val firstCoin = selectedCoins.first()
            val firstSign = if (firstCoin.priceChangePercentage24h >= 0) "+" else ""
            val firstPrice = "$${String.format(Locale.US, "%,.2f", firstCoin.currentPriceUsd)} ($firstSign${String.format(Locale.US, "%,.1f", firstCoin.priceChangePercentage24h)}%)"

            val inboxStyle = NotificationCompat.InboxStyle()
            selectedCoins.forEach { coin ->
                val sign = if (coin.priceChangePercentage24h >= 0) "+" else ""
                val line = "${coin.name}: $${String.format(Locale.US, "%,.2f", coin.currentPriceUsd)} ($sign${String.format(Locale.US, "%,.1f", coin.priceChangePercentage24h)}%)"
                inboxStyle.addLine(line)
            }
            inboxStyle.setSummaryText("+${selectedCoins.size - 1} more tracked")

            notificationBuilder
                .setContentTitle(firstCoin.name)
                .setContentText(firstPrice)
                .setStyle(inboxStyle)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}

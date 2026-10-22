package com.example.crypto_tracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.notification.CryptoNotificationManager
import com.example.crypto_tracker.notification.NotificationPreferences
import com.example.crypto_tracker.ui.components.ScreenHeader
import com.example.crypto_tracker.ui.viewmodel.CryptoViewModel
import com.example.crypto_tracker.ui.viewmodel.HomeUiState
import java.text.NumberFormat
import java.util.Locale

/**
 * Main Home Screen displaying the top 10 cryptocurrencies ranked dynamically by market cap.
 *
 * Consumes [CryptoViewModel] following the MVVM architectural pattern.
 * Uses semantic theme tokens for colors across components.
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 * @param viewModel [CryptoViewModel] instance providing UI state and actions.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: CryptoViewModel = viewModel()
) {
    val context = LocalContext.current
    val notificationPrefs = remember { NotificationPreferences(context) }
    val notificationManager = remember { CryptoNotificationManager(context) }

    val uiState by viewModel.uiState.collectAsState()
    var selectedNotifIds by remember { mutableStateOf(notificationPrefs.getSelectedCoinIds()) }

    // Dynamically ensure default notification selection from the #1 ranked coin when market data arrives
    val currentState = uiState
    if (currentState is HomeUiState.Success && currentState.coins.isNotEmpty()) {
        val top1Coin = currentState.coins.firstOrNull { it.rank == 1 } ?: currentState.coins.first()
        val effectiveNotifIds = remember(currentState.coins) {
            notificationPrefs.ensureDefaultSelectionFromTop1(top1Coin.id)
        }
        if (selectedNotifIds.isEmpty() && effectiveNotifIds.isNotEmpty()) {
            selectedNotifIds = effectiveNotifIds
        }
        remember(currentState.coins, effectiveNotifIds) {
            notificationManager.showPortfolioNotification(currentState.coins, effectiveNotifIds)
            true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Standardized Screen Header
        ScreenHeader(
            title = "Top 10 Cryptocurrencies 🏆",
            subtitle = "Pin up to ${NotificationPreferences.MAX_NOTIFIED_COINS} live prices to your notification bar"
        )

        // Status Bar (Loading Indicator / Refresh Note / Timestamp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState is HomeUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Updating rankings...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "🔄 Auto-refreshes every 1 min",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (currentState is HomeUiState.Success) {
                Text(
                    text = "Updated: ${currentState.lastUpdated}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State-driven UI rendering (MVVM pattern)
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading top 10 market rankings...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text(text = "Try Again")
                        }
                    }
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.coins) { coin ->
                        val isNotified = selectedNotifIds.contains(coin.id)
                        CryptoRankCard(
                            coin = coin,
                            isNotified = isNotified,
                            onToggleNotification = {
                                if (isNotified) {
                                    // Turn off notification
                                    selectedNotifIds = selectedNotifIds - coin.id
                                    notificationPrefs.setSelectedCoinIds(selectedNotifIds)
                                    if (state.coins.isNotEmpty()) {
                                        notificationManager.showPortfolioNotification(state.coins, selectedNotifIds)
                                    }
                                } else {
                                    // Turn on notification, checking maximum limit of 5
                                    if (selectedNotifIds.size >= NotificationPreferences.MAX_NOTIFIED_COINS) {
                                        Toast.makeText(
                                            context,
                                            "Maximum of ${NotificationPreferences.MAX_NOTIFIED_COINS} cryptocurrencies allowed for notifications",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        selectedNotifIds = selectedNotifIds + coin.id
                                        notificationPrefs.setSelectedCoinIds(selectedNotifIds)
                                        if (state.coins.isNotEmpty()) {
                                            notificationManager.showPortfolioNotification(state.coins, selectedNotifIds)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card composable representing a single cryptocurrency in the market ranking list.
 *
 * Uses semantic theme tokens for colors, borders, and surfaces, with fixed hexadecimal podium colors for Top 3.
 *
 * @param coin [CryptoCoin] model.
 * @param isNotified Whether this coin is currently pinned to the persistent notification.
 * @param onToggleNotification Callback invoked when the switch is toggled.
 */
@Composable
fun CryptoRankCard(
    coin: CryptoCoin,
    isNotified: Boolean,
    onToggleNotification: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Podium Badge (Top 3) or Discreet Rank + API Coin Logo Image + Name & Symbol
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fixed hexadecimal colors for Top 3 podium badges across all themes (Gold #1, Silver #2, Bronze #3)
                val podiumColors = when (coin.rank) {
                    1 -> Pair(Color(0xFFFFD700), Color(0xFF3E2723)) // Gold (Ouro)
                    2 -> Pair(Color(0xFFCFD8DC), Color(0xFF263238)) // Silver (Prata)
                    3 -> Pair(Color(0xFFD77A31), Color(0xFFFFFFFF)) // Bronze (Bronze)
                    else -> null
                }

                if (podiumColors != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(podiumColors.first, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.rank.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = podiumColors.second
                        )
                    }
                } else {
                    // Discreet rank number for ranks 4 to 10
                    Text(
                        text = "#${coin.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(26.dp)
                    )
                }

                // Coin Remote Logo Image via Coil
                if (!coin.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coin.imageUrl,
                        contentDescription = coin.name,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Fallback Circle if image URL is unavailable
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.symbol.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Name & Symbol
                Column {
                    Text(
                        text = coin.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = coin.symbol,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            // Right Side: Spot Price (with animation), 24h Change, and Polished Notification Switch Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    // AnimatedContent creates a smooth fade transition effect when price updates
                    AnimatedContent(
                        targetState = coin.currentPriceUsd,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "priceAnimation"
                    ) { price ->
                        Text(
                            text = formatCurrencyUsd(price),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val isPositive = coin.priceChangePercentage24h >= 0
                    // Use semantic tokens: primary (terminal green) for positive, error for negative
                    val changeColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val changeSign = if (isPositive) "+" else ""

                    Text(
                        text = "$changeSign${String.format(Locale.US, "%.2f", coin.priceChangePercentage24h)}%",
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Modern Material 3 Switch Toggle for Notifications
                Switch(
                    checked = isNotified,
                    onCheckedChange = { onToggleNotification() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Formats a raw numeric double value into US Dollar ($) currency format.
 */
private fun formatCurrencyUsd(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(value)
}

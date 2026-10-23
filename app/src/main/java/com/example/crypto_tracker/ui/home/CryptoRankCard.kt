package com.example.crypto_tracker.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.ui.components.GlassCard
import com.example.crypto_tracker.ui.components.GlassDefaults
import com.example.crypto_tracker.ui.components.glassInnerPanelColor
import com.example.crypto_tracker.ui.theme.FinancialText
import java.util.Locale

private const val NOT_AVAILABLE = "N/A"

/**
 * One row of the market ranking. Collapsed: rank, logo, name, price, 24h change and the pin switch.
 * Expanded: summary, market stats and a link to the coin's web page.
 *
 * @param isNotified Whether the coin is pinned to the notification bar.
 * @param onToggleExpand Called when the card is tapped.
 * @param onToggleNotification Called when the pin switch is toggled.
 */
@Composable
fun CryptoRankCard(
    coin: CryptoCoin,
    isNotified: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleNotification: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        strong = isExpanded,
        highlight = if (isExpanded) MaterialTheme.colorScheme.primary else null,
        onClick = onToggleExpand
    ) {
        CardSummary(coin, isNotified, isExpanded, onToggleNotification)
        if (isExpanded) {
            CardDetails(coin)
        }
    }
}

@Composable
private fun CardSummary(
    coin: CryptoCoin,
    isNotified: Boolean,
    isExpanded: Boolean,
    onToggleNotification: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RankBadge(coin.rank)
            CoinLogo(coin)
            CoinTitle(coin, isExpanded)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriceColumn(coin)
            PinSwitch(isNotified, onToggleNotification)
        }
    }
}

/** Same "#n" format for every rank; the top three are tinted gold, silver and bronze. */
@Composable
private fun RankBadge(rank: Int) {
    Text(
        text = "#$rank",
        style = FinancialText.Rank,
        color = rankColor(rank),
        modifier = Modifier.width(36.dp)
    )
}

// Light-theme tones are darker so the podium keeps enough contrast on light surfaces.
private val GoldDark = Color(0xFFFFC107)
private val SilverDark = Color(0xFFB0BEC5)
private val BronzeDark = Color(0xFFCD7F32)
private val GoldLight = Color(0xFFB88700)
private val SilverLight = Color(0xFF78909C)
private val BronzeLight = Color(0xFF9A5B1C)

@Composable
private fun rankColor(rank: Int): Color {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (rank) {
        1 -> if (dark) GoldDark else GoldLight
        2 -> if (dark) SilverDark else SilverLight
        3 -> if (dark) BronzeDark else BronzeLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/** Remote logo, or the first letter of the symbol when none is available. */
@Composable
private fun CoinLogo(coin: CryptoCoin) {
    if (!coin.imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
        )
        return
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = coin.symbol.take(1),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun CoinTitle(coin: CryptoCoin, isExpanded: Boolean) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = coin.symbol,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PriceColumn(coin: CryptoCoin) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedContent(
            targetState = coin.currentPriceUsd,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "priceAnimation"
        ) { price ->
            Text(
                text = formatCurrencyUsd(price),
                style = FinancialText.Price,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        ChangePill(coin.priceChangePercentage24h)
    }
}

/** 24h change. Direction is conveyed by arrow + sign, never by colour alone. */
@Composable
private fun ChangePill(percent: Double) {
    val isPositive = percent >= 0
    val sign = if (isPositive) "+" else ""
    val arrow = if (isPositive) "▲" else "▼"

    Surface(
        shape = CircleShape,
        color = if (isPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = "$arrow $sign${String.format(Locale.US, "%.2f", percent)}%",
            style = FinancialText.Change,
            color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PinSwitch(isNotified: Boolean, onToggle: () -> Unit) {
    // State is shown by a bell icon inside the thumb as well as by colour (on/off bell).
    Switch(
        checked = isNotified,
        onCheckedChange = { onToggle() },
        thumbContent = {
            Icon(
                imageVector = if (isNotified) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize)
            )
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            checkedIconColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedIconColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
            uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/** Expanded content: summary, four market stats and the web-details link. */
@Composable
private fun CardDetails(coin: CryptoCoin) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(12.dp))

        if (!coin.description.isNullOrBlank()) {
            Surface(
                shape = GlassDefaults.InnerShape,
                color = glassInnerPanelColor(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = coin.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        MarketStats(coin)
    }

    Spacer(modifier = Modifier.height(14.dp))
    WebDetailsLink(coin.id)
}

@Composable
private fun MarketStats(coin: CryptoCoin) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MarketStatItem("24h High", coin.high24hUsd?.let(::formatCurrencyUsd) ?: NOT_AVAILABLE)
        MarketStatItem("24h Low", coin.low24hUsd?.let(::formatCurrencyUsd) ?: NOT_AVAILABLE)
        MarketStatItem("Market Cap", coin.marketCapUsd?.let(::formatCompactUsd) ?: NOT_AVAILABLE)
        MarketStatItem("24h Volume", coin.totalVolumeUsd?.let(::formatCompactUsd) ?: NOT_AVAILABLE)
    }
}

@Composable
private fun MarketStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = FinancialText.Stat,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WebDetailsLink(coinId: String) {
    val uriHandler = LocalUriHandler.current

    Surface(
        onClick = { uriHandler.openUri(AppConfig.Network.COIN_DETAILS_PAGE_URL + coinId) },
        shape = RoundedCornerShape(bottomStart = GlassDefaults.RadiusCard, bottomEnd = GlassDefaults.RadiusCard),
        color = glassInnerPanelColor(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "View details on web ↗",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp)
        )
    }
}

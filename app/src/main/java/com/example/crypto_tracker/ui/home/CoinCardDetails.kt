package com.example.crypto_tracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.R
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.format.CompactSuffixes
import com.example.crypto_tracker.format.formatCompactUsd
import com.example.crypto_tracker.format.formatCurrencyUsd
import com.example.crypto_tracker.ui.components.GlassDefaults
import com.example.crypto_tracker.ui.components.appLocale
import com.example.crypto_tracker.ui.components.glassInnerPanelColor
import com.example.crypto_tracker.ui.theme.FinancialText

/** Expanded content: summary, four market stats and the web-details link. */
@Composable
internal fun CardDetails(coin: CryptoCoin) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = GlassDefaults.InnerShape,
            color = glassInnerPanelColor(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = coinDescription(coin),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        MarketStats(coin)
    }

    Spacer(modifier = Modifier.height(14.dp))
    WebDetailsLink(coin.id)
}

/** Hand-written summary for well-known coins, a generic sentence for the rest; always in the selected language. */
@Composable
private fun coinDescription(coin: CryptoCoin): String {
    val res = CoinDescriptions.resFor(coin.id)
    return if (res != null) stringResource(res) else stringResource(R.string.coin_desc_generic, coin.name, coin.symbol)
}

@Composable
private fun MarketStats(coin: CryptoCoin) {
    val locale = appLocale()
    val notAvailable = stringResource(R.string.coin_stat_not_available)
    val suffixes = CompactSuffixes(
        trillion = stringResource(R.string.compact_suffix_trillion),
        billion = stringResource(R.string.compact_suffix_billion),
        million = stringResource(R.string.compact_suffix_million)
    )
    // 2x2 grid: wider translated values ("US$ 1,63 tri") must not run into each other.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MarketStatItem(
                stringResource(R.string.coin_stat_high_24h),
                coin.high24hUsd?.let { formatCurrencyUsd(it, locale) } ?: notAvailable,
                Modifier.weight(1f)
            )
            MarketStatItem(
                stringResource(R.string.coin_stat_low_24h),
                coin.low24hUsd?.let { formatCurrencyUsd(it, locale) } ?: notAvailable,
                Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MarketStatItem(
                stringResource(R.string.coin_stat_market_cap),
                coin.marketCapUsd?.let { formatCompactUsd(it, locale, suffixes) } ?: notAvailable,
                Modifier.weight(1f)
            )
            MarketStatItem(
                stringResource(R.string.coin_stat_volume_24h),
                coin.totalVolumeUsd?.let { formatCompactUsd(it, locale, suffixes) } ?: notAvailable,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MarketStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
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
            text = stringResource(R.string.coin_view_details_web),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp)
        )
    }
}

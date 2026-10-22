package com.example.crypto_tracker.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Reusable Google AdMob Banner Ad composable using AndroidView wrapper with standard banner size (50dp height).
 * Includes a subtle container box so the ad slot is clearly visible even during network load times.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var adFailedToLoad by remember { mutableStateOf(false) }

    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            // Official Google test ad unit ID for development and testing
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adFailedToLoad = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w("BannerAdView", "Banner failed to load: ${adError.message}")
                    adFailedToLoad = true
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    // The slot keeps its height even when no ad is filled, so content below never jumps.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        if (!adFailedToLoad) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { adView }
            )
        }
    }
}

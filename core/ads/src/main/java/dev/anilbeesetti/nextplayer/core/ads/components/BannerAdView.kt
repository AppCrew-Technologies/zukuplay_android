package dev.anilbeesetti.nextplayer.core.ads.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement

@Composable
fun BannerAdView(
    placement: AdPlacement,
    adManager: AdManager,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null,
    onAdClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var adConfig by remember { mutableStateOf(adManager.getBannerAdConfig(placement)) }

    LaunchedEffect(placement) {
        adConfig = adManager.getBannerAdConfig(placement)
    }

    if (adConfig != null) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    // Use adaptive banner size for better performance
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, 320))

                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            onAdLoaded?.invoke()
                        }

                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(error)
                            onAdFailedToLoad?.invoke(error.message)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            adManager.trackAdClick(adConfig!!.id)
                            onAdClicked?.invoke()
                        }

                        override fun onAdOpened() {
                            super.onAdOpened()
                        }

                        override fun onAdClosed() {
                            super.onAdClosed()
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            update = { view ->
                // Update view if needed
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                adView?.destroy()
            }
        }
    }
}

@Composable
fun AdaptiveBannerAdView(
    placement: AdPlacement,
    adManager: AdManager,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null,
    onAdClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var adConfig by remember { mutableStateOf(adManager.getBannerAdConfig(placement)) }
    
    LaunchedEffect(placement) {
        adConfig = adManager.getBannerAdConfig(placement)
    }
    
    if (adConfig != null) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = adConfig!!.adUnitId
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, 320))
                    
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            onAdLoaded?.invoke()
                        }
                        
                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(error)
                            onAdFailedToLoad?.invoke(error.message)
                        }
                        
                        override fun onAdClicked() {
                            super.onAdClicked()
                            adManager.trackAdClick(adConfig!!.id)
                            onAdClicked?.invoke()
                        }
                    }
                    
                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            modifier = modifier.fillMaxWidth(),
            update = { view ->
                // Update view if needed
            }
        )
        
        DisposableEffect(Unit) {
            onDispose {
                adView?.destroy()
            }
        }
    }
}

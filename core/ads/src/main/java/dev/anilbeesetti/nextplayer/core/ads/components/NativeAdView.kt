package dev.anilbeesetti.nextplayer.core.ads.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import dev.anilbeesetti.nextplayer.core.ads.R
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement
import dev.anilbeesetti.nextplayer.core.ads.models.AdType

@Composable
fun NativeAdCard(
    placement: AdPlacement,
    adManager: AdManager,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null,
    onAdClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(placement) {
        val adConfig = adManager.getAdConfigs()
            .filter { it.type == AdType.NATIVE && it.placement == placement }
            .minByOrNull { it.priority }
        
        if (adConfig != null) {
            val adLoader = AdLoader.Builder(context, adConfig.adUnitId)
                .forNativeAd { ad ->
                    nativeAd = ad
                    isLoading = false
                    onAdLoaded?.invoke()
                }
                .withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        super.onAdFailedToLoad(error)
                        isLoading = false
                        onAdFailedToLoad?.invoke(error.message)
                    }
                    
                    override fun onAdClicked() {
                        super.onAdClicked()
                        adManager.trackAdClick(adConfig.id)
                        onAdClicked?.invoke()
                    }
                })
                .build()
            
            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            isLoading = false
        }
    }
    
    if (isLoading) {
        NativeAdPlaceholder(modifier = modifier)
    } else if (nativeAd != null) {
        NativeAdContent(
            nativeAd = nativeAd!!,
            modifier = modifier
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }
}

@Composable
private fun NativeAdPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading Ad...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NativeAdContent(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                nativeAd.performClick(android.os.Bundle())
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ad",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                
                Text(
                    text = nativeAd.advertiser ?: "Sponsored",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Ad Icon
                if (nativeAd.icon != null) {
                    AsyncImage(
                        model = nativeAd.icon?.uri,
                        contentDescription = "Ad Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Headline
                    Text(
                        text = nativeAd.headline ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Body
                    Text(
                        text = nativeAd.body ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Call to Action
                    if (nativeAd.callToAction != null) {
                        Text(
                            text = nativeAd.callToAction ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NativeAdWithMedia(
    placement: AdPlacement,
    adManager: AdManager,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailedToLoad: ((String) -> Unit)? = null,
    onAdClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(placement) {
        val adConfig = adManager.getAdConfigs()
            .filter { it.type == AdType.NATIVE && it.placement == placement }
            .minByOrNull { it.priority }
        
        if (adConfig != null) {
            val adLoader = AdLoader.Builder(context, adConfig.adUnitId)
                .forNativeAd { ad ->
                    nativeAd = ad
                    isLoading = false
                    onAdLoaded?.invoke()
                }
                .withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        super.onAdFailedToLoad(error)
                        isLoading = false
                        onAdFailedToLoad?.invoke(error.message)
                    }
                    
                    override fun onAdClicked() {
                        super.onAdClicked()
                        adManager.trackAdClick(adConfig.id)
                        onAdClicked?.invoke()
                    }
                })
                .build()
            
            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            isLoading = false
        }
    }
    
    if (isLoading) {
        NativeAdPlaceholder(modifier = modifier)
    } else if (nativeAd != null) {
            AndroidView(
                factory = { ctx ->
                    NativeAdView(ctx).apply {
                        val inflater = ctx.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
                        val adView = inflater.inflate(R.layout.native_ad_layout, this, false) as ViewGroup
                        addView(adView)
                        
                        // Populate the native ad view
                        nativeAd?.let { ad ->
                            populateNativeAdView(ad, adView)
                        }
                    }
                },
                modifier = modifier,
                update = { view ->
                    // Update view if needed
                }
            )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: ViewGroup) {
    // This would populate the native ad view with the ad content
    // Implementation depends on your native ad layout
}

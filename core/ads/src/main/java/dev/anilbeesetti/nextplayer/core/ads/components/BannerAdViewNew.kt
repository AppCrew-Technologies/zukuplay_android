package dev.anilbeesetti.nextplayer.core.ads.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


@Composable
fun BannerAdViewNew() {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            adUnitId = "ca-app-pub-3940256099942544/6300978111" // ✅ Test banner
        }
    }

    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            (context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density).toInt()
        )
        adView.setAdSize(AdSize.SMART_BANNER)
        adView.loadAd(adRequest)
    }

    AndroidView(
        factory = { adView },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

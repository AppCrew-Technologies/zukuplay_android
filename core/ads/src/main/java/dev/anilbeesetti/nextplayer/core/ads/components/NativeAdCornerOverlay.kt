package dev.anilbeesetti.nextplayer.core.ads.components



import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd





// ✅ 1. Helper function to load native ads (must be top-level)
private fun loadNativeAd(
    context: Context,
    adUnitId: String,
    onAdLoaded: (NativeAd) -> Unit,
    onAdFailed: () -> Unit
) {
    val adLoader = AdLoader.Builder(context, adUnitId)
        .forNativeAd { ad: NativeAd ->
            onAdLoaded(ad)
        }
        .withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onAdFailed()
            }
        })
        .build()

    adLoader.loadAd(AdRequest.Builder().build())
}

// ✅ 2. Main composable
@Composable
fun NativeAdCornerOverlay(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/2247696110" // Test ad ID
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isAdFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdVisible by remember { mutableStateOf(true) } // 👈 Used to hide ad

    LaunchedEffect(Unit) {
        loadNativeAd(
            context = context,
            adUnitId = adUnitId,
            onAdLoaded = { ad ->
                nativeAd = ad
                isLoading = false
                isAdFailed = false
            },
            onAdFailed = {
                isAdFailed = true
                isLoading = false
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    if (isAdVisible) {
        Box(modifier = modifier.wrapContentSize()) {
            when {
                isLoading -> {
                    Text(
                        text = "Loading ad...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                isAdFailed -> {
                    Text(
                        text = "Ad failed to load",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                nativeAd != null -> {
                    AdCard(
                        ad = nativeAd!!,
                        onCloseClick = {
                            nativeAd?.destroy()
                            nativeAd = null
                            isAdVisible = false
                        }
                    )
                }
            }
        }
    }
}

// ✅ 3. Ad card with close button
@Composable
private fun AdCard(ad: NativeAd, onCloseClick: () -> Unit) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp)
            .background(Color.Transparent)
    ) {
        Card(
            modifier = Modifier.wrapContentSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (ad.icon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(ad.icon!!.uri),
                        contentDescription = ad.headline,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ad.headline ?: "Sponsored App",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = ad.callToAction ?: "Learn More",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ❌ Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
                .clickable { onCloseClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // AD badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-4).dp, y = (-4).dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AD",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
//@Composable
//fun NativeAdCornerOverlay(
//    modifier: Modifier = Modifier,
//    imageRes: Int = dev.anilbeesetti.nextplayer.core.ads.R.drawable.gol_pouch // your gold coin pouch image
//) {
//    var isAdVisible by remember { mutableStateOf(true) }
//
//    if (isAdVisible) {
//        Box(
//            modifier = modifier
//                .wrapContentSize()
//                .padding(8.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            // 🟡 Show the ad image
//            Image(
//                painter = painterResource(id = imageRes),
//                contentDescription = "Gold Pouch Ad",
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(RoundedCornerShape(16.dp))
//                    .clickable {
//                        // TODO: Handle click action (like open link or reward user)
//                    },
//                contentScale = ContentScale.Crop
//            )
//
//            // ❌ Close button (top-right corner)
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .offset(x = 6.dp, y = (-6).dp)
//                    .size(20.dp)
//                    .clip(CircleShape)
//                    .background(Color.Black.copy(alpha = 0.7f))
//                    .clickable { isAdVisible = false },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "×",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            // Small AD label in top-left corner
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .offset(x = (-4).dp, y = (-4).dp)
//                    .background(Color(0xFFFFC107), RoundedCornerShape(4.dp))
//                    .padding(horizontal = 4.dp, vertical = 2.dp)
//            ) {
//                Text(
//                    text = "AD",
//                    color = Color.Black,
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
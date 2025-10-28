package dev.anilbeesetti.nextplayer.core.ads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import dev.anilbeesetti.nextplayer.core.ads.components.AdaptiveBannerAdView
import dev.anilbeesetti.nextplayer.core.ads.components.BannerAdView
import dev.anilbeesetti.nextplayer.core.ads.components.NativeAdCard
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement

@Composable
fun TestAdScreen(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInterstitial by remember { mutableStateOf(false) }
    var showRewarded by remember { mutableStateOf(false) }
    var showRewardedInterstitial by remember { mutableStateOf(false) }
    var showAppOpen by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🧪 AdMob Test Ads",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Test all AdMob ad types using official Google demo ad units",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider()

        // Banner Ads Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📱 Banner Ads",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Fixed Size Banner (320x50)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                BannerAdView(
                    placement = AdPlacement.HOME_SCREEN,
                    adManager = adManager,
                    onAdLoaded = { testResult = "✅ Fixed Banner Ad loaded successfully!" },
                    onAdFailedToLoad = { error -> testResult = "❌ Fixed Banner Ad failed: $error" }
                )
                
                Text(
                    text = "Adaptive Banner",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                AdaptiveBannerAdView(
                    placement = AdPlacement.HOME_SCREEN,
                    adManager = adManager,
                    onAdLoaded = { testResult = "✅ Adaptive Banner Ad loaded successfully!" },
                    onAdFailedToLoad = { error -> testResult = "❌ Adaptive Banner Ad failed: $error" }
                )
            }
        }

        // Native Ad Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎨 Native Ads",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                NativeAdCard(
                    placement = AdPlacement.IN_FEED,
                    adManager = adManager,
                    onAdLoaded = { testResult = "✅ Native Ad loaded successfully!" },
                    onAdFailedToLoad = { error -> testResult = "❌ Native Ad failed: $error" }
                )
            }
        }

        // Full-Screen Ads Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🖥️ Full-Screen Ads",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            adManager.showInterstitialAd(
                                context = context,
                                placement = AdPlacement.BEFORE_VIDEO,
                                onAdShown = { testResult = "✅ Interstitial Ad shown!" },
                                onAdDismissed = { testResult = "✅ Interstitial Ad dismissed!" },
                                onAdFailed = { error -> testResult = "❌ Interstitial Ad failed: $error" }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Interstitial")
                    }
                    
                    Button(
                        onClick = {
                            adManager.showRewardedAd(
                                context = context,
                                placement = AdPlacement.AFTER_VIDEO,
                                onUserEarnedReward = { rewardType, amount ->
                                    testResult = "✅ Rewarded Ad: $rewardType ($amount)"
                                },
                                onAdDismissed = { testResult = "✅ Rewarded Ad dismissed!" },
                                onAdFailed = { error -> testResult = "❌ Rewarded Ad failed: $error" }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rewarded")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Note: Rewarded Interstitial not implemented yet
                            testResult = "⚠️ Rewarded Interstitial not implemented yet"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rewarded Interstitial")
                    }
                    
                    Button(
                        onClick = {
                            adManager.showAppOpenAd(
                                context = context,
                                onAdDismissed = { testResult = "✅ App Open Ad dismissed!" },
                                onAdFailed = { error -> testResult = "❌ App Open Ad failed: $error" }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("App Open")
                    }
                }
            }
        }

        // Test Results Section
        if (testResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (testResult.startsWith("✅")) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = testResult,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (testResult.startsWith("✅")) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋 Testing Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "• All ads use official Google demo ad units",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Test ads are safe to click and won't affect your account",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Look for 'Test Ad' labels on the ads",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Check the test results above for status",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

package dev.anilbeesetti.nextplayer.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor() {

    companion object {
        private const val TAG = "AdMobManager"

        // Official Google Demo Ad Unit IDs for Testing
        // Source: https://developers.google.com/admob/android/test-ads
        const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
        const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
        const val TEST_FIXED_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
        const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val TEST_NATIVE_VIDEO_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"
    }

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "AdMob already initialized")
            return
        }

        try {
            Log.d(TAG, "Initializing AdMob...")
            MobileAds.initialize(context) { initializationStatus ->
                val statusMap = initializationStatus.adapterStatusMap
                for (adapterClass in statusMap.keys) {
                    val status = statusMap[adapterClass]
                    Log.d(TAG, "Adapter: $adapterClass, Status: ${status?.initializationState}")
                }
                isInitialized = true
                Log.d(TAG, "AdMob initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
        }
    }

    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    // Banner Ad
    fun loadBannerAd(adView: AdView, adUnitId: String = TEST_FIXED_BANNER_AD_UNIT_ID) {
        Log.d(TAG, "Loading banner ad with unit ID: $adUnitId")
        adView.loadAd(createAdRequest())
    }

    // Interstitial Ad
    fun loadInterstitialAd(
        context: Context,
        adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((String) -> Unit)? = null
    ) {
        Log.d(TAG, "Loading interstitial ad with unit ID: $adUnitId")

        InterstitialAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    ad.show(context as ComponentActivity)
                    interstitialAd = ad
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    onAdFailedToLoad?.invoke(error.message)
                }
            }
        )
    }

    fun showInterstitialAd(
        context: Context,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailedToShow: ((String) -> Unit)? = null
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                    interstitialAd = null
                    onAdFailedToShow?.invoke(error.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed full screen content")
                }
            }
            ad.show(context as androidx.activity.ComponentActivity)
        } ?: run {
            Log.w(TAG, "Interstitial ad not loaded")
            onAdFailedToShow?.invoke("Ad not loaded")
        }
    }

    // Rewarded Ad
    fun loadRewardedAd(
        context: Context,
        adUnitId: String = TEST_REWARDED_AD_UNIT_ID,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((String) -> Unit)? = null
    ) {
        Log.d(TAG, "Loading rewarded ad with unit ID: $adUnitId")

        RewardedAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {

                    Log.d(TAG, "Rewarded ad loaded successfully")
                    ad.show(context as ComponentActivity) { rewardItem ->
                        Log.d("ADS", "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                    }
                    rewardedAd = ad
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    onAdFailedToLoad?.invoke(error.message)
                }
            }
        )
    }

    fun showRewardedAd(
        context: Context,
        onUserEarnedReward: ((String, Int) -> Unit)? = null,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailedToShow: ((String) -> Unit)? = null
    ) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                    rewardedAd = null
                    onAdFailedToShow?.invoke(error.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed full screen content")
                }
            }

            ad.show(context as androidx.activity.ComponentActivity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.type} - ${rewardItem.amount}")
                onUserEarnedReward?.invoke(rewardItem.type, rewardItem.amount)
            }
        } ?: run {
            Log.w(TAG, "Rewarded ad not loaded")
            onAdFailedToShow?.invoke("Ad not loaded")
        }
    }

    // Rewarded Interstitial Ad
    fun loadRewardedInterstitialAd(
        context: Context,
        adUnitId: String = TEST_REWARDED_INTERSTITIAL_AD_UNIT_ID,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((String) -> Unit)? = null
    ) {
        Log.d(TAG, "Loading rewarded interstitial ad with unit ID: $adUnitId")

        RewardedInterstitialAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded interstitial ad loaded successfully")
                    rewardedInterstitialAd = ad
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded interstitial ad failed to load: ${error.message}")
                    rewardedInterstitialAd = null
                    onAdFailedToLoad?.invoke(error.message)
                }
            }
        )
    }

    fun showRewardedInterstitialAd(
        context: Context,
        onUserEarnedReward: ((String, Int) -> Unit)? = null,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailedToShow: ((String) -> Unit)? = null
    ) {
        rewardedInterstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded interstitial ad dismissed")
                    rewardedInterstitialAd = null
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded interstitial ad failed to show: ${error.message}")
                    rewardedInterstitialAd = null
                    onAdFailedToShow?.invoke(error.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded interstitial ad showed full screen content")
                }
            }

            ad.show(context as androidx.activity.ComponentActivity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.type} - ${rewardItem.amount}")
                onUserEarnedReward?.invoke(rewardItem.type, rewardItem.amount)
            }
        } ?: run {
            Log.w(TAG, "Rewarded interstitial ad not loaded")
            onAdFailedToShow?.invoke("Ad not loaded")
        }
    }

    // App Open Ad
    fun loadAppOpenAd(
        context: Context,
        adUnitId: String = TEST_APP_OPEN_AD_UNIT_ID,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((String) -> Unit)? = null
    ) {
        Log.d(TAG, "Loading app open ad with unit ID: $adUnitId")

        AppOpenAd.load(
            context,
            adUnitId,
            createAdRequest(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded successfully")
                    ad.show(context as ComponentActivity)
                    appOpenAd = ad
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "App open ad failed to load: ${error.message}")
                    appOpenAd = null
                    onAdFailedToLoad?.invoke(error.message)
                }
            }
        )
    }

    fun showAppOpenAd(
        context: Context,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailedToShow: ((String) -> Unit)? = null
    ) {
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed")
                    appOpenAd = null
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "App open ad failed to show: ${error.message}")
                    appOpenAd = null
                    onAdFailedToShow?.invoke(error.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App open ad showed full screen content")
                }
            }
            ad.show(context as androidx.activity.ComponentActivity)
        } ?: run {
            Log.w(TAG, "App open ad not loaded")
            onAdFailedToShow?.invoke("Ad not loaded")
        }
    }
    private var interstitialAd1: InterstitialAd? = null
    private var isAdShowing = false

    fun showInterstitialAdForOldActivity(activity: Activity) {
        // ✅ Don’t load or show if an ad is already showing
        if (isAdShowing) return

        InterstitialAd.load(
            activity,
            "ca-app-pub-3940256099942544/1033173712",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd1 = ad

                    // ✅ Set callbacks to handle ad lifecycle
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            isAdShowing = true
                        }

                        override fun onAdDismissedFullScreenContent() {
                            isAdShowing = false
                            interstitialAd1 = null // free reference
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isAdShowing = false
                            interstitialAd1 = null
                        }
                    }

                    // ✅ Show ad safely
                    if (!isAdShowing) {
                        ad.show(activity)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd1 = null
                    isAdShowing = false
                    // You can log error here if needed
                }
            }
        )
    }

    // Check if ads are loaded
    fun isInterstitialAdLoaded(): Boolean = interstitialAd != null
    fun isRewardedAdLoaded(): Boolean = rewardedAd != null
    fun isRewardedInterstitialAdLoaded(): Boolean = rewardedInterstitialAd != null
    fun isAppOpenAdLoaded(): Boolean = appOpenAd != null
}

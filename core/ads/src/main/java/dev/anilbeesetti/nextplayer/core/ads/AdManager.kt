package dev.anilbeesetti.nextplayer.core.ads

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dev.anilbeesetti.nextplayer.core.ads.models.AdConfig
import dev.anilbeesetti.nextplayer.core.ads.models.AdManagerConfig
import dev.anilbeesetti.nextplayer.core.ads.models.AdPerformance
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement
import dev.anilbeesetti.nextplayer.core.ads.models.AdType
import dev.anilbeesetti.nextplayer.core.ads.models.AdsData
import dev.anilbeesetti.nextplayer.core.ads.repository.AdRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    private val adRepository: AdRepository,
    private val adMobManager: AdMobManager
) {
    
    companion object {
        private const val TAG = "AdManager"
        private const val PREF_AD_COUNTER = "ad_counter"
        private const val PREF_LAST_AD_SHOWN = "last_ad_shown"
    }
    // Add this to track if we're observing
    private var isObservingAdsData = false
    private var adConfigs: List<AdConfig> = emptyList()
    private var managerConfig: AdManagerConfig? = null
    private var adCounters: MutableMap<String, Int> = mutableMapOf()
    
    suspend fun initialize(context: Context) {
        Log.d(TAG, "Initializing AdManager...")
        // Load and save AdsData to constants
        loadAdsDataToConstants()
        // Initialize AdMob
        adMobManager.initialize(context)
        
        // Load configurations
        loadAdConfigurations()
        
        // Preload ads
        preloadAds(context)
        // Start real-time listener
        //startAdsDataListener()
        Log.d(TAG, "AdManager initialized successfully")
    }
    
//    private suspend fun loadAdConfigurations() {
//        try {
//            adConfigs = adRepository.getActiveAds()
//            managerConfig = adRepository.getAdManagerConfig()
//            Log.d(TAG, "Loaded ${adConfigs.size} active ad configurations")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading ad configurations", e)
//        }
//    }
private suspend fun loadAdsDataToConstants() {
    try {
        // Get AdsData from repository
        val adsData = adRepository.getAdsData()

        // Save values to constants
        adsData?.let {
            AdConstants.FLOATING_AD = it.floating_ad
            AdConstants.NUMBER_OF_VIDEO_CLIPS_AFTER_EACH_AD = it.number_of_video_clips_after_each_ad
            AdConstants.ON_EXIT_POP_UP_AD = it.on_exit_pop_up_ad
            AdConstants.SHOW_AD_AFTER_VIDEO_ENDS = it.show_ad_after_video_ends
            AdConstants.SHOW_AD_BETWEEN_VIDEO_CLIPS_LIST = it.show_ad_between_video_clips_list
            AdConstants.SHOW_AD_ON_APP_STARTUP = it.show_ad_on_app_startup
            AdConstants.SHOW_AD_ON_PAUSE = it.show_ad_on_pause
            AdConstants.SHOW_BANNER_BOTTOM = it.show_banner_bottom
            AdConstants.SHOW_BANNER_TOP = it.show_banner_top
            AdConstants.SHOW_AD_ON_TAB_CHANGE = it.show_ad_on_tab_change
        }

        Log.d(TAG, "AdsData loaded and saved to constants: $adsData")
    } catch (e: Exception) {
        Log.e(TAG, "Error loading AdsData to constants", e)
    }
}
    private fun startAdsDataListener() {
        if (isObservingAdsData) return

        // Observe real-time changes
        adRepository.getAdsDataLiveData().observeForever { adsData ->
            updateAdConstants(adsData)
            Log.d(TAG, "Constants updated in real-time: $adsData")
        }

        isObservingAdsData = true
        Log.d(TAG, "Started real-time AdsData listener")
    }

    private fun updateAdConstants(adsData: AdsData?) {
        adsData?.let {
            AdConstants.FLOATING_AD = it.floating_ad
            AdConstants.NUMBER_OF_VIDEO_CLIPS_AFTER_EACH_AD = it.number_of_video_clips_after_each_ad
            AdConstants.ON_EXIT_POP_UP_AD = it.on_exit_pop_up_ad
            AdConstants.SHOW_AD_AFTER_VIDEO_ENDS = it.show_ad_after_video_ends
            AdConstants.SHOW_AD_BETWEEN_VIDEO_CLIPS_LIST = it.show_ad_between_video_clips_list
            AdConstants.SHOW_AD_ON_APP_STARTUP = it.show_ad_on_app_startup
            AdConstants.SHOW_AD_ON_PAUSE = it.show_ad_on_pause
            AdConstants.SHOW_BANNER_BOTTOM = it.show_banner_bottom
            AdConstants.SHOW_BANNER_TOP = it.show_banner_top
            AdConstants.SHOW_AD_ON_TAB_CHANGE = it.show_ad_on_tab_change
        }
    }
    private suspend fun loadAdConfigurations() {
        try {
            adConfigs = adRepository.getActiveAds()
            managerConfig = adRepository.getAdManagerConfig()

            // If no configs exist, hardcode default ones
            if (adConfigs.isEmpty()) {
                Log.w(TAG, "No ad configs found — using hardcoded defaults")

                val now = Date().toString()

                adConfigs = listOf(
                    // 🟩 Banner ad on Home Screen
                    AdConfig(
                        id = "banner_home",
                        name = "Home Banner",
                        type = AdType.BANNER,
                        placement = AdPlacement.HOME_SCREEN,
                        adUnitId = AdConstants.ADMOB_BANNER_AD_UNIT_ID, // Test banner
                        isActive = true,
                        priority = 1,
                        frequency = 1,
                        createdAt = now,
                        updatedAt = now
                    ),

                    // 🟦 Interstitial ad before video
                AdConfig(
                    id = "interstitial_before_video",
                    name = "Before Video Interstitial",
                    type = AdType.INTERSTITIAL,
                    placement = AdPlacement.BEFORE_VIDEO,
                    adUnitId = AdConstants.ADMOB_INTERSTITIAL_AD_UNIT_ID, // Test interstitial
                    isActive = true,
                    priority = 1,
                    frequency = 2,
                    createdAt = now,
                    updatedAt = now
                ),

                  //   🟨 Rewarded ad after video
                AdConfig(
                    id = "rewarded_after_video",
                    name = "After Video Rewarded",
                    type = AdType.REWARDED,
                    placement = AdPlacement.AFTER_VIDEO,
                    adUnitId = AdConstants.ADMOB_REWARDED_AD_UNIT_ID, // Test rewarded
                    isActive = true,
                    priority = 1,
                    frequency = 3,
                    createdAt = now,
                    updatedAt = now
                ),

                    //  🟧 App Open ad
                    AdConfig(
                        id = "app_open_default",
                        name = "App Open Ad",
                        type = AdType.APP_OPEN,
                        placement = AdPlacement.APP_EXIT,
                        adUnitId = AdConstants.ADMOB_REWARDED_AD_UNIT_ID, // Test app open ad
                        isActive = true,
                        priority = 1,
                        frequency = 1,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            // If manager config is null, set default
            if (managerConfig == null) {
                managerConfig = AdManagerConfig(
                    isAdMobEnabled = true,
                    testMode = true,
                    coppaCompliant = false,
                    maxAdFrequency = 3,
                    adLoadingTimeout = 8000,
                    retryAttempts = 2,
                    fallbackAdUnitIds = mapOf(
                        AdType.BANNER to "ca-app-pub-3940256099942544/6300978111",
                        AdType.INTERSTITIAL to "ca-app-pub-3940256099942544/1033173712",
                        AdType.REWARDED to "ca-app-pub-3940256099942544/5224354917"
                    )
                )
                Log.w(TAG, "No manager config found — using hardcoded default")
            }

            Log.d(TAG, "Loaded ${adConfigs.size} ad configs (from repo or defaults)")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad configurations", e)
        }
    }
    public fun preloadAds(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Preload interstitial ads
//                val interstitialConfigs = adConfigs.filter { it.type == AdType.INTERSTITIAL }
//                interstitialConfigs.forEach { config ->
//                    adMobManager.loadInterstitialAd(
//                        context = context,
//                        adUnitId = config.adUnitId,
//                        onAdLoaded = { Log.d(TAG, "Preloaded interstitial ad: ${config.name}") },
//                        onAdFailedToLoad = { error -> Log.w(TAG, "Failed to preload interstitial ad: ${config.name}, Error: $error") }
//                    )
//                }

                if (AdConstants.SHOW_AD_ON_APP_STARTUP){
                    // Preload rewarded ads
                    val rewardedConfigs = adConfigs.filter { it.type == AdType.REWARDED }
                    rewardedConfigs.forEach { config ->
                        adMobManager.loadRewardedAd(
                            context = context,
                            adUnitId = config.adUnitId,
                            onAdLoaded = { Log.d(TAG, "Preloaded rewarded ad: ${config.name}") },
                            onAdFailedToLoad = { error -> Log.w(TAG, "Failed to preload rewarded ad: ${config.name}, Error: $error") }
                        )
                    }
                }

                
                // Preload app open ads
//                val appOpenConfigs = adConfigs.filter { it.type == AdType.APP_OPEN }
//                appOpenConfigs.forEach { config ->
//                    adMobManager.loadAppOpenAd(
//                        context = context,
//                        adUnitId = config.adUnitId,
//                        onAdLoaded = { Log.d(TAG, "Preloaded app open ad: ${config.name}") },
//                        onAdFailedToLoad = { error -> Log.w(TAG, "Failed to preload app open ad: ${config.name}, Error: $error") }
//                    )
//                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading ads", e)
            }
        }
    }
    
    fun getBannerAdConfig(placement: AdPlacement): AdConfig? {
        return adConfigs
            .filter { it.type == AdType.BANNER && it.placement == placement }
            .minByOrNull { it.priority }
    }
    
    fun shouldShowInterstitialAd(placement: AdPlacement): Boolean {
        val config = getAdConfigForPlacement(AdType.INTERSTITIAL, placement) ?: return false
        return shouldShowAd(config)
    }
    
    fun shouldShowRewardedAd(placement: AdPlacement): Boolean {
        val config = getAdConfigForPlacement(AdType.REWARDED, placement) ?: return false
        return shouldShowAd(config)
    }
    
    fun shouldShowAppOpenAd(): Boolean {
        val config = adConfigs
            .filter { it.type == AdType.APP_OPEN }
            .minByOrNull { it.priority } ?: return false
        return shouldShowAd(config)
    }
    
    private fun getAdConfigForPlacement(type: AdType, placement: AdPlacement): AdConfig? {
        return adConfigs
            .filter { it.type == type && it.placement == placement }
            .minByOrNull { it.priority }
    }
    
    private fun shouldShowAd(config: AdConfig): Boolean {
        if (!config.isActive) return false
        
        // Check date range
        val now = Date()
        config.startDate?.let { startDate ->
            if (now.before(Date(startDate))) return false
        }
        config.endDate?.let { endDate ->
            if (now.after(Date(endDate))) return false
        }
        
        // Check frequency
        val counter = adCounters[config.id] ?: 0
        if (counter % config.frequency != 0) return false
        
        // Check max frequency from manager config
        managerConfig?.let { managerConfig ->
            if (counter >= managerConfig.maxAdFrequency) return false
        }
        
        return true
    }
    
    fun showInterstitialAd(
        context: Context,
        placement: AdPlacement,
        onAdShown: (() -> Unit)? = null,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        val config = getAdConfigForPlacement(AdType.INTERSTITIAL, placement)
        if (config == null) {
            onAdFailed?.invoke("No ad configuration found for placement: $placement")
            return
        }
        
        if (!shouldShowAd(config)) {
            onAdFailed?.invoke("Ad should not be shown based on frequency rules")
            return
        }
        
        adMobManager.showInterstitialAd(
            context = context,
            onAdDismissed = {
                incrementAdCounter(config.id)
                trackAdImpression(config.id)
                onAdDismissed?.invoke()
            },
            onAdFailedToShow = { error ->
                onAdFailed?.invoke(error)
            }
        )
        
        onAdShown?.invoke()
    }
    
    fun showRewardedAd(
        context: Context,
        placement: AdPlacement,
        onUserEarnedReward: ((String, Int) -> Unit)? = null,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        val config = getAdConfigForPlacement(AdType.REWARDED, placement)
        if (config == null) {
            onAdFailed?.invoke("No ad configuration found for placement: $placement")
            return
        }
        
        if (!shouldShowAd(config)) {
            onAdFailed?.invoke("Ad should not be shown based on frequency rules")
            return
        }
        
        adMobManager.showRewardedAd(
            context = context,
            onUserEarnedReward = { rewardType, amount ->
                incrementAdCounter(config.id)
                trackAdImpression(config.id)
                onUserEarnedReward?.invoke(rewardType, amount)
            },
            onAdDismissed = {
                onAdDismissed?.invoke()
            },
            onAdFailedToShow = { error ->
                onAdFailed?.invoke(error)
            }
        )
    }
    
    fun showAppOpenAd(
        context: Context,
        onAdDismissed: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        val config = adConfigs
            .filter { it.type == AdType.APP_OPEN }
            .minByOrNull { it.priority }
            
        if (config == null) {
            onAdFailed?.invoke("No app open ad configuration found")
            return
        }
        
        if (!shouldShowAd(config)) {
            onAdFailed?.invoke("App open ad should not be shown based on frequency rules")
            return
        }
        
        adMobManager.showAppOpenAd(
            context = context,
            onAdDismissed = {
                incrementAdCounter(config.id)
                trackAdImpression(config.id)
                onAdDismissed?.invoke()
            },
            onAdFailedToShow = { error ->
                onAdFailed?.invoke(error)
            }
        )
    }
    
    private fun incrementAdCounter(adConfigId: String) {
        val currentCount = adCounters[adConfigId] ?: 0
        adCounters[adConfigId] = currentCount + 1
    }
    
    private fun trackAdImpression(adConfigId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentPerformance = adRepository.getAdPerformance(adConfigId)
                val updatedPerformance = currentPerformance?.copy(
                    impressions = currentPerformance.impressions + 1,
                    lastUpdated = Date().toString()
                ) ?: AdPerformance(
                    adConfigId = adConfigId,
                    impressions = 1,
                    lastUpdated = Date().toString()
                )
                
                adRepository.updateAdPerformance(updatedPerformance)
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking ad impression", e)
            }
        }
    }
    
    fun trackAdClick(adConfigId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentPerformance = adRepository.getAdPerformance(adConfigId)
                val updatedPerformance = currentPerformance?.copy(
                    clicks = currentPerformance.clicks + 1,
                    ctr = if (currentPerformance.impressions > 0) {
                        (currentPerformance.clicks + 1).toDouble() / currentPerformance.impressions.toDouble()
                    } else 0.0,
                    lastUpdated = Date().toString()
                ) ?: AdPerformance(
                    adConfigId = adConfigId,
                    clicks = 1,
                    lastUpdated = Date().toString()
                )
                
                adRepository.updateAdPerformance(updatedPerformance)
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking ad click", e)
            }
        }
    }
    private var interstitialAd1: InterstitialAd? = null
    private var isAdShowing = false
    fun showInterstitialAdForTabsChangeComposeActivity(activity: ComponentActivity) {
        // ✅ Don’t load or show if an ad is already showing
        if (isAdShowing) return

        InterstitialAd.load(
            activity,
            AdConstants.ADMOB_INTERSTITIAL_AD_UNIT_ID,
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
    fun showRewardedAdForComposeActivity(activity: ComponentActivity) {
        var isAdShowing = false

        // ✅ Load and show rewarded ad
        com.google.android.gms.ads.rewarded.RewardedAd.load(
            activity,
            AdConstants.ADMOB_REWARDED_AD_UNIT_ID, // Test Reward Ad ID
            com.google.android.gms.ads.AdRequest.Builder().build(),
            object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {

                    ad.fullScreenContentCallback = object :
                        com.google.android.gms.ads.FullScreenContentCallback() {

                        override fun onAdShowedFullScreenContent() {
                            isAdShowing = true
                        }

                        override fun onAdDismissedFullScreenContent() {
                            isAdShowing = false
                            activity.finish() // ✅ Finish when ad closed
                        }

                        override fun onAdFailedToShowFullScreenContent(
                            adError: com.google.android.gms.ads.AdError
                        ) {
                            isAdShowing = false
                            activity.finish() // ✅ Finish if ad fails to show
                        }
                    }

                    // ✅ Show safely
                    if (!isAdShowing) {
                        ad.show(activity) { rewardItem ->
                            // Optional: handle reward
                            val rewardAmount = rewardItem.amount
                            val rewardType = rewardItem.type
                            // Example: give user reward here
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    isAdShowing = false
                    activity.finish() // ✅ Finish if ad fails to load
                }
            }
        )
    }

    suspend fun refreshAdConfigurations() {
        loadAdConfigurations()
        Log.d(TAG, "Ad configurations refreshed")
    }
    
    fun getAdConfigs(): List<AdConfig> = adConfigs
    fun getManagerConfig(): AdManagerConfig? = managerConfig
}

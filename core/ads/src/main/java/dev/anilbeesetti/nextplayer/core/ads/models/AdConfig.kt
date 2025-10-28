package dev.anilbeesetti.nextplayer.core.ads.models

import com.google.gson.annotations.SerializedName

data class AdConfig(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("type")
    val type: AdType,
    
    @SerializedName("placement")
    val placement: AdPlacement,
    
    @SerializedName("adUnitId")
    val adUnitId: String,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("priority")
    val priority: Int = 1,
    
    @SerializedName("frequency")
    val frequency: Int = 1, // Show every N times
    
    @SerializedName("startDate")
    val startDate: String? = null,
    
    @SerializedName("endDate")
    val endDate: String? = null,
    
    @SerializedName("targeting")
    val targeting: AdTargeting? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

enum class AdType(val value: String) {
    @SerializedName("banner")
    BANNER("banner"),
    
    @SerializedName("interstitial")
    INTERSTITIAL("interstitial"),
    
    @SerializedName("rewarded")
    REWARDED("rewarded"),
    
    @SerializedName("rewarded_interstitial")
    REWARDED_INTERSTITIAL("rewarded_interstitial"),
    
    @SerializedName("app_open")
    APP_OPEN("app_open"),
    
    @SerializedName("native")
    NATIVE("native")
}

enum class AdPlacement(val value: String) {
    @SerializedName("home_screen")
    HOME_SCREEN("home_screen"),
    
    @SerializedName("video_list")
    VIDEO_LIST("video_list"),
    
    @SerializedName("before_video")
    BEFORE_VIDEO("before_video"),
    
    @SerializedName("after_video")
    AFTER_VIDEO("after_video"),
    
    @SerializedName("app_exit")
    APP_EXIT("app_exit"),
    
    @SerializedName("settings")
    SETTINGS("settings"),
    
    @SerializedName("player_overlay")
    PLAYER_OVERLAY("player_overlay"),
    
    @SerializedName("in_feed")
    IN_FEED("in_feed")
}

data class AdTargeting(
    @SerializedName("userSegments")
    val userSegments: List<String>? = null,
    
    @SerializedName("deviceTypes")
    val deviceTypes: List<String>? = null,
    
    @SerializedName("countries")
    val countries: List<String>? = null,
    
    @SerializedName("ageGroups")
    val ageGroups: List<String>? = null
)

data class AdPerformance(
    @SerializedName("adConfigId")
    val adConfigId: String,
    
    @SerializedName("impressions")
    val impressions: Long = 0,
    
    @SerializedName("clicks")
    val clicks: Long = 0,
    
    @SerializedName("revenue")
    val revenue: Double = 0.0,
    
    @SerializedName("ctr")
    val ctr: Double = 0.0, // Click-through rate
    
    @SerializedName("ecpm")
    val ecpm: Double = 0.0, // Effective cost per mille
    
    @SerializedName("lastUpdated")
    val lastUpdated: String
)

data class AdManagerConfig(
    @SerializedName("isAdMobEnabled")
    val isAdMobEnabled: Boolean = true,
    
    @SerializedName("testMode")
    val testMode: Boolean = true,
    
    @SerializedName("coppaCompliant")
    val coppaCompliant: Boolean = false,
    
    @SerializedName("maxAdFrequency")
    val maxAdFrequency: Int = 3, // Max ads per session
    
    @SerializedName("adLoadingTimeout")
    val adLoadingTimeout: Long = 10000, // 10 seconds
    
    @SerializedName("retryAttempts")
    val retryAttempts: Int = 3,
    
    @SerializedName("fallbackAdUnitIds")
    val fallbackAdUnitIds: Map<AdType, String> = emptyMap()
)

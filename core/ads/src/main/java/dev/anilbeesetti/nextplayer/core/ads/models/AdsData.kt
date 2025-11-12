package dev.anilbeesetti.nextplayer.core.ads.models

data class AdsData(
    val floating_ad: Boolean = false,
    val number_of_video_clips_after_each_ad: Int = 5,
    val on_exit_pop_up_ad: Boolean = false,
    val show_ad_after_video_ends: Boolean = false,
    val show_ad_between_video_clips_list: Boolean = false,
    val show_ad_on_app_startup: Boolean = false,
    val show_ad_on_pause: Boolean = false,
    val show_banner_bottom: Boolean = false,
    val show_banner_top: Boolean = false,
    val show_ad_on_tab_change: Boolean = false
) {
    companion object {
        // Default values
        const val DEFAULT_FLOATING_AD = false
        const val DEFAULT_VIDEO_CLIPS_AFTER_AD = 5
        const val DEFAULT_ON_EXIT_POPUP_AD = false
        const val DEFAULT_SHOW_AD_AFTER_VIDEO_ENDS = false
        const val DEFAULT_SHOW_AD_BETWEEN_VIDEO_CLIPS = false
        const val DEFAULT_SHOW_AD_ON_APP_STARTUP = false
        const val DEFAULT_SHOW_AD_ON_PAUSE = false
        const val DEFAULT_SHOW_BANNER_BOTTOM = false
        const val DEFAULT_SHOW_BANNER_TOP = false
    }
}
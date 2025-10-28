package dev.anilbeesetti.nextplayer.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import dev.anilbeesetti.nextplayer.feature.videopicker.navigation.mediaPickerFolderScreen
import dev.anilbeesetti.nextplayer.feature.videopicker.navigation.mediaPickerNavigationRoute
import dev.anilbeesetti.nextplayer.feature.videopicker.navigation.mediaPickerScreen
import dev.anilbeesetti.nextplayer.feature.videopicker.navigation.navigateToMediaPickerFolderScreen
import dev.anilbeesetti.nextplayer.settings.navigation.navigateToSettings

// Import the route constant directly
import dev.anilbeesetti.nextplayer.navigation.MEDIA_ROUTE

fun NavGraphBuilder.mediaNavGraph(
    navGraphBuilder: NavGraphBuilder,
    navController: NavHostController,
    adBannerContent: @Composable (() -> Unit)? = null
) {
    navigation(
        startDestination = mediaPickerNavigationRoute,
        route = MEDIA_ROUTE,
    ) {
        mediaPickerScreen(
            onPlayVideo = navController.context::startPlayerActivity,
            onFolderClick = navController::navigateToMediaPickerFolderScreen,
            onSettingsClick = navController::navigateToSettings
        )
        mediaPickerFolderScreen(
            onNavigateUp = navController::navigateUp,
            onVideoClick = navController.context::startPlayerActivity,
            onFolderClick = navController::navigateToMediaPickerFolderScreen,
            adBannerContent = adBannerContent
        )
        // Removed notifications screen as it's now in MainActivity
    }
}

fun Context.startPlayerActivity(uri: Uri) {
    val intent = Intent().apply {
        setClassName(this@startPlayerActivity, "dev.anilbeesetti.nextplayer.feature.player.PlayerActivity")
        action = Intent.ACTION_VIEW
        data = uri
    }
    startActivity(intent)
}

fun Context.startAudioPlayerActivity(uri: Uri) {
    val intent = Intent().apply {
        setClassName(this@startAudioPlayerActivity, "dev.anilbeesetti.nextplayer.AudioPlayerActivity")
        action = Intent.ACTION_VIEW
        data = uri
    }
    startActivity(intent)
}

fun Context.startAudioPlayerActivity(uri: Uri, title: String?, artist: String?, album: String?) {
    val intent = Intent().apply {
        setClassName(this@startAudioPlayerActivity, "dev.anilbeesetti.nextplayer.AudioPlayerActivity")
        action = Intent.ACTION_VIEW
        data = uri
        putExtra("audio_title", title)
        putExtra("audio_artist", artist)
        putExtra("audio_album", album)
    }
    startActivity(intent)
}

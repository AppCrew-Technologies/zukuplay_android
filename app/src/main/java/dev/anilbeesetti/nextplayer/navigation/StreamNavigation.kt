package dev.anilbeesetti.nextplayer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.anilbeesetti.nextplayer.screens.StreamScreen
import dev.anilbeesetti.nextplayer.navigation.STREAM_ROUTE

fun NavGraphBuilder.streamNavGraph(
    onPlayVideo: (android.net.Uri) -> Unit
) {
    composable(route = STREAM_ROUTE) {
        StreamScreen(
            onPlayVideo = onPlayVideo
        )
    }
} 
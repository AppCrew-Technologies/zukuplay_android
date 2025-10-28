package dev.anilbeesetti.nextplayer.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.anilbeesetti.nextplayer.screens.AudioScreen
import dev.anilbeesetti.nextplayer.screens.AudioFile
import dev.anilbeesetti.nextplayer.navigation.MUSIC_ROUTE

fun NavGraphBuilder.musicNavGraph(
    onPlayAudio: (AudioFile) -> Unit
) {
    composable(route = MUSIC_ROUTE) {
        AudioScreen(
            onPlayAudio = { audioFile ->
                onPlayAudio(audioFile)
            }
        )
    }
} 
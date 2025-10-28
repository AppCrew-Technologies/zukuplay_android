package dev.anilbeesetti.nextplayer.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.music.AudioPlayerState
import javax.inject.Inject

@HiltViewModel
class AudioScreenViewModel @Inject constructor(
    val audioPlayerState: AudioPlayerState
) : ViewModel() 
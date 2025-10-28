package dev.anilbeesetti.nextplayer.core.common.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple coordinator to prevent audio/video player conflicts.
 * Uses basic state tracking without complex broadcast systems.
 */
@Singleton
class SimpleMediaCoordinator @Inject constructor() {
    
    enum class PlayerType {
        NONE,
        AUDIO,
        VIDEO
    }
    
    private val _activePlayer = MutableStateFlow(PlayerType.NONE)
    val activePlayer: StateFlow<PlayerType> = _activePlayer.asStateFlow()
    
    /**
     * Notify that audio player is starting
     */
    fun onAudioPlayerStarted() {
        _activePlayer.value = PlayerType.AUDIO
    }
    
    /**
     * Notify that video player is starting  
     */
    fun onVideoPlayerStarted() {
        _activePlayer.value = PlayerType.VIDEO
    }
    
    /**
     * Notify that current player stopped
     */
    fun onPlayerStopped() {
        _activePlayer.value = PlayerType.NONE
    }
    
    /**
     * Check if another type of player is active
     */
    fun isOtherPlayerActive(requestingPlayer: PlayerType): Boolean {
        return when (requestingPlayer) {
            PlayerType.AUDIO -> _activePlayer.value == PlayerType.VIDEO
            PlayerType.VIDEO -> _activePlayer.value == PlayerType.AUDIO
            PlayerType.NONE -> false
        }
    }
} 
package dev.anilbeesetti.nextplayer.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import android.app.Service
import dagger.hilt.android.AndroidEntryPoint
import dev.anilbeesetti.nextplayer.AudioPlayerActivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

// Global audio player state for equalizer animations and cross-app state
@Singleton
class AudioPlayerState @Inject constructor() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentAudioUri = MutableStateFlow<Uri?>(null)
    val currentAudioUri: StateFlow<Uri?> = _currentAudioUri.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _audioMetadata = MutableStateFlow(
        AudioMetadata(
            title = "Unknown Song",
            artist = "Unknown Artist", 
            album = "Unknown Album"
        )
    )
    val audioMetadata: StateFlow<AudioMetadata> = _audioMetadata.asStateFlow()
    
    fun updatePlaybackState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
    
    fun updateCurrentAudio(uri: Uri?) {
        _currentAudioUri.value = uri
    }
    
    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }
    
    fun updateDuration(duration: Long) {
        _duration.value = duration
    }
    
    fun updateMetadata(metadata: AudioMetadata) {
        _audioMetadata.value = metadata
    }
}

data class AudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: String? = null
)

@AndroidEntryPoint
class AudioPlayerService : Service() {

    @Inject
    lateinit var audioPlayerState: AudioPlayerState
    
    @Inject
    lateinit var mediaCoordinator: dev.anilbeesetti.nextplayer.core.common.media.SimpleMediaCoordinator
    
    private lateinit var player: ExoPlayer
    private val playbackStateListener: Player.Listener = playbackStateListener()
    
    // Service scope for coroutines
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null
    
    // Binder for local service connection
    private val binder = AudioPlayerBinder()
    
    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
        fun getPlayer(): ExoPlayer = player
        fun getAudioPlayerState(): AudioPlayerState = audioPlayerState
    }
    
    companion object {
        const val ACTION_PLAY = "dev.anilbeesetti.nextplayer.action.PLAY"
        const val ACTION_PAUSE = "dev.anilbeesetti.nextplayer.action.PAUSE"
        const val ACTION_STOP = "dev.anilbeesetti.nextplayer.action.STOP"
        const val ACTION_PREVIOUS = "dev.anilbeesetti.nextplayer.action.PREVIOUS"
        const val ACTION_NEXT = "dev.anilbeesetti.nextplayer.action.NEXT"
        const val EXTRA_AUDIO_URI = "dev.anilbeesetti.nextplayer.extra.AUDIO_URI"
        const val EXTRA_SEEK_POSITION = "dev.anilbeesetti.nextplayer.extra.SEEK_POSITION"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_player_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            android.util.Log.d("AudioPlayerService", "onCreate() started")
            createNotificationChannel()
            initializePlayer()
            
            // Start service in foreground immediately
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            android.util.Log.d("AudioPlayerService", "onCreate() completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayerService", "Error in onCreate()", e)
            // Don't throw exception, just log it to prevent crashes
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_PLAY -> {
                if (player.mediaItemCount > 0) {
                    resumeAudio()
                }
            }
            ACTION_PAUSE -> {
                pauseAudio()
            }
            ACTION_PREVIOUS -> {
                // Seek backward 10 seconds or restart song if at beginning
                val currentPos = getCurrentPosition()
                val newPosition = (currentPos - 10000).coerceAtLeast(0)
                seekTo(newPosition)
            }
            ACTION_NEXT -> {
                // Seek forward 10 seconds
                val currentPos = getCurrentPosition()
                val duration = getDuration()
                val newPosition = if (duration > 0) {
                    (currentPos + 10000).coerceAtMost(duration)
                } else {
                    currentPos + 10000
                }
                seekTo(newPosition)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
            
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(this)))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            
        player.addListener(playbackStateListener)
    }
    
    fun playAudio(uri: Uri, seekToPosition: Long = 0L) {
        playAudio(uri, null, seekToPosition)
    }
    
    fun playAudio(uri: Uri, metadata: AudioMetadata?, seekToPosition: Long = 0L) {
        try {
            android.util.Log.d("AudioPlayerService", "playAudio() started for URI: $uri with metadata: $metadata")
            
            // Request audio focus first - this will pause any other media players
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            
            android.util.Log.d("AudioPlayerService", "Audio focus result: $result")
            
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Notify coordinator that audio player is starting
                try {
                    mediaCoordinator.onAudioPlayerStarted()
                } catch (e: Exception) {
                    android.util.Log.e("AudioPlayerService", "Error notifying media coordinator", e)
                }
                
                val mediaItem = MediaItem.fromUri(uri)
                player.run {
                    setMediaItem(mediaItem)
                    prepare()
                    if (seekToPosition > 0) {
                        seekTo(seekToPosition)
                    }
                    playWhenReady = true // Ensure autoplay is enabled
                    play()
                }
                
                // Update state immediately to reflect that we're starting playback
                try {
                    audioPlayerState.updateCurrentAudio(uri)
                    audioPlayerState.updatePlaybackState(true)
                } catch (e: Exception) {
                    android.util.Log.e("AudioPlayerService", "Error updating audio state", e)
                }
                
                // Use provided metadata if available, otherwise extract from file
                if (metadata != null) {
                    android.util.Log.d("AudioPlayerService", "Using provided metadata: $metadata")
                    try {
                        audioPlayerState.updateMetadata(metadata)
                    } catch (e: Exception) {
                        android.util.Log.e("AudioPlayerService", "Error updating metadata", e)
                    }
                } else {
                    android.util.Log.d("AudioPlayerService", "No metadata provided, extracting from file")
                    extractAndUpdateMetadata(uri)
                }
                
                startPositionUpdates()
                android.util.Log.d("AudioPlayerService", "playAudio() completed successfully, auto-starting playback")
            } else {
                android.util.Log.w("AudioPlayerService", "Audio focus not granted, result: $result")
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayerService", "Error in playAudio()", e)
            // If there's an error, make sure we notify the coordinator and reset state
            try {
                mediaCoordinator.onPlayerStopped()
                audioPlayerState.updatePlaybackState(false)
            } catch (e2: Exception) {
                android.util.Log.e("AudioPlayerService", "Error resetting state", e2)
            }
        }
    }
    
    fun pauseAudio() {
        player.pause()
        mediaCoordinator.onPlayerStopped()
    }
    
    fun resumeAudio() {
        player.play()
        startPositionUpdates()
    }
    
    fun seekTo(position: Long) {
        player.seekTo(position)
    }
    
    fun getCurrentPosition(): Long = player.currentPosition
    
    fun getDuration(): Long = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
    
    fun isPlaying(): Boolean = player.isPlaying
    
    private fun extractAndUpdateMetadata(uri: Uri) {
        android.util.Log.d("AudioPlayerService", "Starting metadata extraction for URI: $uri")
        
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            
            // Extract metadata with detailed logging
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            
            android.util.Log.d("AudioPlayerService", "Extracted metadata - Title: '$title', Artist: '$artist', Album: '$album'")
            
            // Use fallback values only if metadata is null or empty
            val finalTitle = if (!title.isNullOrBlank()) title else "Unknown Song"
            val finalArtist = if (!artist.isNullOrBlank()) artist else "Unknown Artist"
            val finalAlbum = if (!album.isNullOrBlank()) album else "Unknown Album"
            
            val metadata = AudioMetadata(
                title = finalTitle,
                artist = finalArtist,
                album = finalAlbum
            )
            
            android.util.Log.d("AudioPlayerService", "Final metadata - Title: '${metadata.title}', Artist: '${metadata.artist}', Album: '${metadata.album}'")
            
            audioPlayerState.updateMetadata(metadata)
            retriever.release()
            
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("AudioPlayerService", "Invalid URI or data source: $uri", e)
            setDefaultMetadata()
        } catch (e: IllegalStateException) {
            android.util.Log.e("AudioPlayerService", "MediaMetadataRetriever is in invalid state", e)
            setDefaultMetadata()
        } catch (e: RuntimeException) {
            android.util.Log.e("AudioPlayerService", "Runtime error during metadata extraction", e)
            setDefaultMetadata()
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayerService", "Unexpected error during metadata extraction", e)
            setDefaultMetadata()
        }
    }
    
    private fun setDefaultMetadata() {
        android.util.Log.d("AudioPlayerService", "Setting default metadata values")
        audioPlayerState.updateMetadata(
            AudioMetadata(
                title = "Unknown Song",
                artist = "Unknown Artist",
                album = "Unknown Album"
            )
        )
    }
    
    private fun playbackStateListener() = object : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            android.util.Log.d("AudioPlayerService", "ExoPlayer metadata changed - Title: '${mediaMetadata.title}', Artist: '${mediaMetadata.artist}', Album: '${mediaMetadata.albumTitle}'")
            
            val currentMetadata = audioPlayerState.audioMetadata.value
            
            // Only update if ExoPlayer provides non-null metadata and current metadata is default/unknown
            val shouldUpdateTitle = !mediaMetadata.title.isNullOrBlank() && 
                (currentMetadata.title == "Unknown Song" || currentMetadata.title.isBlank())
            val shouldUpdateArtist = !mediaMetadata.artist.isNullOrBlank() && 
                (currentMetadata.artist == "Unknown Artist" || currentMetadata.artist.isBlank())
            val shouldUpdateAlbum = !mediaMetadata.albumTitle.isNullOrBlank() && 
                (currentMetadata.album == "Unknown Album" || currentMetadata.album.isBlank())
            
            if (shouldUpdateTitle || shouldUpdateArtist || shouldUpdateAlbum) {
                val updatedMetadata = currentMetadata.copy(
                    title = if (shouldUpdateTitle) mediaMetadata.title.toString() else currentMetadata.title,
                    artist = if (shouldUpdateArtist) mediaMetadata.artist.toString() else currentMetadata.artist,
                    album = if (shouldUpdateAlbum) mediaMetadata.albumTitle.toString() else currentMetadata.album
                )
                android.util.Log.d("AudioPlayerService", "Updating metadata from ExoPlayer - Title: '${updatedMetadata.title}', Artist: '${updatedMetadata.artist}', Album: '${updatedMetadata.album}'")
                audioPlayerState.updateMetadata(updatedMetadata)
            }
        }

        override fun onIsPlayingChanged(playerIsPlaying: Boolean) {
            super.onIsPlayingChanged(playerIsPlaying)
            audioPlayerState.updatePlaybackState(playerIsPlaying)
            if (playerIsPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    audioPlayerState.updatePlaybackState(false)
                    stopPositionUpdates()
                }
                Player.STATE_READY -> {
                    audioPlayerState.updateDuration(player.duration.takeIf { it != C.TIME_UNSET } ?: 0L)
                }
                else -> {}
            }
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            audioPlayerState.updatePosition(newPosition.positionMs)
        }
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Continue playing in background unless explicitly stopped
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopPositionUpdates()
        serviceScope.cancel()
        
        player.removeListener(playbackStateListener)
        player.release()
        audioPlayerState.updatePlaybackState(false)
        audioPlayerState.updateCurrentAudio(null)
        mediaCoordinator.onPlayerStopped()
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = serviceScope.launch {
            while (player.isPlaying) {
                audioPlayerState.updatePosition(getCurrentPosition())
                audioPlayerState.updateDuration(getDuration())
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback controls"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, AudioPlayerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Audio Player")
                .setContentText("Audio playback is active")
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Audio Player")
                .setContentText("Audio playback is active")
                .setContentIntent(pendingIntent)
                .build()
        }
    }
//

} 
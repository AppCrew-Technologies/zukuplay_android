package dev.anilbeesetti.nextplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.VipulPlayerTheme

import dev.anilbeesetti.nextplayer.music.AudioPlayerService
import dev.anilbeesetti.nextplayer.music.AudioPlayerState
import dev.anilbeesetti.nextplayer.music.AudioMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class AudioPlayerActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    
    @Inject
    lateinit var audioPlayerState: AudioPlayerState
    
    private var audioService: AudioPlayerService? = null
    private var serviceBound = false
    // ✅ ADD THIS BLOCK RIGHT HERE ↓↓↓
//    override fun onBackPressed() {
//        // Stop playback and release resources before finishing
//       // audioService?.stopPlaybackAndRelease()
//        super.onBackPressed() // Finish the activity normally
//    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            android.util.Log.d("AudioPlayerActivity", "Service connected")
            val binder = service as AudioPlayerService.AudioPlayerBinder
            audioService = binder.getService()
            serviceBound = true
            
            // If this is a new audio file, start playing automatically with a small delay
            intent.data?.let { uri ->
                android.util.Log.d("AudioPlayerActivity", "Auto-starting playback for URI: $uri")
                lifecycleScope.launch {
                    // Add a small delay to ensure service is fully initialized
                    delay(100)
                    handleAudioPlayback(uri)
                }
            }
            
            // Start position updates
            startPositionUpdates()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            android.util.Log.d("AudioPlayerActivity", "Service disconnected")
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Ensure screenshots are allowed
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        android.util.Log.d("AudioPlayerActivity", "onCreate() called with intent data: ${intent.data}")

        try {
            // Stop any running PlayerService first to prevent conflicts
            val playerServiceIntent = Intent().apply {
                setClassName(this@AudioPlayerActivity, "dev.anilbeesetti.nextplayer.feature.player.service.PlayerService")
            }
            stopService(playerServiceIntent)
            
            // Start and bind to the audio service
            val serviceIntent = Intent(this, AudioPlayerService::class.java)
            startForegroundService(serviceIntent)
            android.util.Log.d("AudioPlayerActivity", "AudioPlayerService started")
            
            val bindIntent = Intent(this, AudioPlayerService::class.java)
            val bindResult = bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            android.util.Log.d("AudioPlayerActivity", "Service bind result: $bindResult")
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayerActivity", "Error starting service", e)
            // Show error to user and finish activity
            android.widget.Toast.makeText(this, "Error starting audio player", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }

        setContent {
            val preferencesRepository = viewModel.preferencesRepository
            val preferences by preferencesRepository.applicationPreferences.collectAsStateWithLifecycle(
                initialValue = dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences()
            )
            
            // Collect state from the global audio player state
            val isPlaying by audioPlayerState.isPlaying.collectAsStateWithLifecycle()
            val currentPosition by audioPlayerState.currentPosition.collectAsStateWithLifecycle()
            val duration by audioPlayerState.duration.collectAsStateWithLifecycle()
            val audioMetadata by audioPlayerState.audioMetadata.collectAsStateWithLifecycle()
            
            // Apply theme
            AppCompatDelegate.setDefaultNightMode(
                when (preferences.themeConfig) {
                    ThemeConfig.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    ThemeConfig.OFF -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemeConfig.ON -> AppCompatDelegate.MODE_NIGHT_YES
                }
            )

            if (preferences.useDynamicColors) {
                DynamicColors.applyToActivityIfAvailable(this@AudioPlayerActivity)
            }
            
            VipulPlayerTheme(
                darkTheme = preferences.themeConfig == ThemeConfig.ON,
                dynamicColor = preferences.useDynamicColors
            ) {
                ProfessionalAudioPlayerScreen(
                    songTitle = audioMetadata.title,
                    artist = audioMetadata.artist,
                    album = audioMetadata.album,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    onBackPressed = {
                        audioService?.let { service ->
                          //  service.stopPlaybackAndRelease() // custom safe-stop method (see below)
                        }
                        // Don't stop the service, just finish the activity
                        // Music will continue playing in background
                        finish() 
                    },
                    onPlayPause = { 
                        audioService?.let { service ->
                            if (service.isPlaying()) {
                                service.pauseAudio()
                            } else {
                                service.resumeAudio()
                            }
                        }
                    },
                    onPrevious = { 
                        // Seek backward 10 seconds
                        audioService?.let { service ->
                            val currentPos = service.getCurrentPosition()
                            val newPosition = (currentPos - 10000).coerceAtLeast(0)
                            service.seekTo(newPosition)
                        }
                    },
                    onNext = { 
                        // Seek forward 10 seconds  
                        audioService?.let { service ->
                            val currentPos = service.getCurrentPosition()
                            val duration = service.getDuration()
                            val newPosition = if (duration > 0) {
                                (currentPos + 10000).coerceAtMost(duration)
                            } else {
                                currentPos + 10000
                            }
                            service.seekTo(newPosition)
                        }
                    },
                    onSeek = { position ->
                        audioService?.seekTo(position)
                    },
                )
            }
        }
    }
    
    private fun handleAudioPlayback(uri: Uri) {
        audioService?.let { service ->
            android.util.Log.d("AudioPlayerActivity", "handleAudioPlayback called for URI: $uri")
            val currentAudioUri = audioPlayerState.currentAudioUri.value
            
            if (currentAudioUri == uri && service.isPlaying()) {
                // Same audio file and already playing - do nothing
                android.util.Log.d("AudioPlayerActivity", "Same audio already playing")
            } else {
                // Different audio file OR same file but not playing - start/restart playing
                android.util.Log.d("AudioPlayerActivity", "Starting playback: currentUri=$currentAudioUri, newUri=$uri, isPlaying=${service.isPlaying()}")
                
                // Extract metadata from intent extras if available
                val title = intent.getStringExtra("audio_title")
                val artist = intent.getStringExtra("audio_artist")
                val album = intent.getStringExtra("audio_album")
                
                val metadata = if (!title.isNullOrBlank() || !artist.isNullOrBlank() || !album.isNullOrBlank()) {
                    AudioMetadata(
                        title = title?.takeIf { it.isNotBlank() } ?: "Unknown Song",
                        artist = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
                        album = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
                    )
                } else {
                    null
                }
                
                android.util.Log.d("AudioPlayerActivity", "Using metadata from intent: $metadata")
                service.playAudio(uri, metadata)
            }
        } ?: run {
            android.util.Log.w("AudioPlayerActivity", "audioService is null, cannot start playback")
        }
    }
    
    private fun startPositionUpdates() {
        lifecycleScope.launch {
            while (serviceBound) {
                audioService?.let { service ->
                    audioPlayerState.updatePosition(service.getCurrentPosition())
                    audioPlayerState.updateDuration(service.getDuration())
                }
                delay(1000) // Update every second
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (serviceBound) {
                unbindService(serviceConnection)
                serviceBound = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_SPACE -> {
                audioService?.let { service ->
                    if (service.isPlaying()) {
                        service.pauseAudio()
                    } else {
                        service.resumeAudio()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                // Seek forward 10 seconds
                audioService?.let { service ->
                    val currentPos = service.getCurrentPosition()
                    val duration = service.getDuration()
                    val newPosition = if (duration > 0) {
                        (currentPos + 10000).coerceAtMost(duration)
                    } else {
                        currentPos + 10000
                    }
                    service.seekTo(newPosition)
                }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                // Seek backward 10 seconds
                audioService?.let { service ->
                    val currentPos = service.getCurrentPosition()
                    val newPosition = (currentPos - 10000).coerceAtLeast(0)
                    service.seekTo(newPosition)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalAudioPlayerScreen(
    songTitle: String,
    artist: String,
    album: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onBackPressed: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    
    // Simple animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Simple vinyl rotation
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )

    LaunchedEffect(Unit) {
        delay(300)
        showControls = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0f0f23),
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            text = "♪ Now Playing",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(alpha = 0.15f)
                                )
                        ) {
                            Icon(
                                imageVector = NextIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Album Art Section
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl record background
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            rotationZ = if (isPlaying) vinylRotation else 0f
                        }
                        .background(Color.Black, CircleShape)
                        .shadow(24.dp, CircleShape)
                )
                
                // Simple album art placeholder
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            rotationZ = if (isPlaying) vinylRotation else 0f
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = NextIcons.Audio,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(120.dp)
                    )
                }
                
                // Center hole
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Song Information
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = album,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { progress ->
                            onSeek((progress * duration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Utils.formatDurationMillis(currentPosition),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        
                        Text(
                            text = Utils.formatDurationMillis(duration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Control Buttons
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button (Replay 10 seconds)
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = NextIcons.Replay,
                            contentDescription = "Replay 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Play/Pause Button
                    val playButtonScale by animateFloatAsState(
                        targetValue = if (isPlaying) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "playButtonScale"
                    )
                    
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(88.dp)
                            .scale(playButtonScale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .shadow(20.dp, CircleShape)
                    ) {
                        if (isPlaying) {
                            Icon(
                                painter = painterResource(id = dev.anilbeesetti.nextplayer.core.ui.R.drawable.ic_pause),
                                contentDescription = "Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(44.dp)
                            )
                        } else {
                            Icon(
                                imageVector = NextIcons.Play,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    
                    // Next Button (Fast Forward 10 seconds)
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = NextIcons.Fast,
                            contentDescription = "Fast forward 10 seconds", 
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
} 
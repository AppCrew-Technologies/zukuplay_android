package dev.anilbeesetti.nextplayer.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.AudioPlayerActivity
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.music.AudioPlayerState
import javax.inject.Inject
import coil.compose.AsyncImage

@Composable
fun MiniPlayer(
    audioPlayerState: AudioPlayerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Collect state from the audio player
    val isPlaying by audioPlayerState.isPlaying.collectAsStateWithLifecycle()
    val currentAudioUri by audioPlayerState.currentAudioUri.collectAsStateWithLifecycle()
    val currentPosition by audioPlayerState.currentPosition.collectAsStateWithLifecycle()
    val duration by audioPlayerState.duration.collectAsStateWithLifecycle()
    val audioMetadata by audioPlayerState.audioMetadata.collectAsStateWithLifecycle()
    
    // Only show mini player if there's an audio file loaded
    // For testing: always show the mini player
    val shouldShow = true // currentAudioUri != null
    
    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Open the full audio player activity
                    val intent = Intent(context, AudioPlayerActivity::class.java).apply {
                        data = currentAudioUri
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // Progress bar
                if (duration > 0) {
                    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art with fallback
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Always show the background icon first
                            Icon(
                                imageVector = NextIcons.Audio,
                                contentDescription = "Audio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            
                            // Try to overlay album art if available
                            if (!audioMetadata.albumArtUri.isNullOrBlank() && audioMetadata.albumArtUri != "null") {
                                AsyncImage(
                                    model = audioMetadata.albumArtUri,
                                    contentDescription = "Album Art",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Song info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (currentAudioUri != null) audioMetadata.title else "Test Song Title",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentAudioUri != null) audioMetadata.artist else "Test Artist",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous/Rewind button
                        IconButton(
                            onClick = {
                                // We'll implement this to send commands to the service
                                sendPlaybackCommand(context, "PREVIOUS")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = NextIcons.Replay,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Play/Pause button
                        IconButton(
                            onClick = {
                                val action = if (isPlaying) "PAUSE" else "PLAY"
                                sendPlaybackCommand(context, action)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            if (isPlaying) {
                                Icon(
                                    painter = painterResource(id = dev.anilbeesetti.nextplayer.core.ui.R.drawable.ic_pause),
                                    contentDescription = "Pause",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = NextIcons.Play,
                                    contentDescription = "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Next/Fast forward button
                        IconButton(
                            onClick = {
                                sendPlaybackCommand(context, "NEXT")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = NextIcons.Fast,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to send commands to the audio service
private fun sendPlaybackCommand(context: Context, action: String) {
    val serviceIntent = Intent().apply {
        setClassName(context, "dev.anilbeesetti.nextplayer.music.AudioPlayerService")
        this.action = when (action) {
            "PLAY" -> "dev.anilbeesetti.nextplayer.action.PLAY"
            "PAUSE" -> "dev.anilbeesetti.nextplayer.action.PAUSE"
            "PREVIOUS" -> "dev.anilbeesetti.nextplayer.action.PREVIOUS"
            "NEXT" -> "dev.anilbeesetti.nextplayer.action.NEXT"
            else -> "dev.anilbeesetti.nextplayer.action.PLAY"
        }
    }
    try {
        context.startService(serviceIntent)
    } catch (e: Exception) {
        // Handle exception silently for now
    }
} 
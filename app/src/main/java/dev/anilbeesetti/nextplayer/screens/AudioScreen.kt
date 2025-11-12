package dev.anilbeesetti.nextplayer.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.anilbeesetti.nextplayer.core.common.isAudioPermissionGranted
import com.google.accompanist.permissions.shouldShowRationale
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextCenterAlignedTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.ui.AudioVisualizerIcon
import dev.anilbeesetti.nextplayer.components.MiniPlayer
import dev.anilbeesetti.nextplayer.screens.AudioScreenViewModel
import javax.inject.Inject
import androidx.compose.ui.platform.LocalConfiguration

// Data models for audio files
data class AudioFile(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val uriString: String,
    val albumArtUri: String? = null,
    val size: Long = 0L,
    val timestamp: Long = 0L
)

data class AudioFolder(
    val name: String,
    val path: String,
    val audioCount: Int,
    val thumbnailUri: String? = null
)

// State classes
sealed class AudioState {
    object Loading : AudioState()
    object Error : AudioState()
    
    data class Success(
        val data: AudioData
    ) : AudioState()
    
    data class AudioData(
        val audioFiles: List<AudioFile> = emptyList(),
        val folders: List<AudioFolder> = emptyList(),
        val recentlyPlayedAudio: AudioFile? = null,
        val firstAudio: AudioFile? = null
    )
}

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AudioScreen(
    onPlayAudio: ((AudioFile) -> Unit)? = null,
    viewModel: AudioViewModel = hiltViewModel(),
    audioScreenViewModel: AudioScreenViewModel = hiltViewModel()
) {
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val audioPlayerState = audioScreenViewModel.audioPlayerState
    
    val context = LocalContext.current
    
    // Tab selection state
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    // Selected folder state
    var selectedFolder by remember { mutableStateOf<AudioFolder?>(null) }
    
    // Selected artist and album states
    var selectedArtist by remember { mutableStateOf<String?>(null) }
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    
    // ⭐ Use centralized permission system - no more individual requests!
    val isAudioPermissionGranted = context.isAudioPermissionGranted()
    
    // Refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Update pull-to-refresh state based on isRefreshing
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            // The state will automatically end when isRefreshing becomes false
        } else {
            // pullToRefreshState will automatically finish the refresh animation
        }
    }
    
    // Open audio file launcher
    val selectAudioFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                // Create a temporary AudioFile for external files
                val tempAudioFile = AudioFile(
                    id = "external_${System.currentTimeMillis()}",
                    title = "External Audio",
                    artist = "Unknown Artist",
                    album = "Unknown Album",
                    duration = 0L,
                    path = uri.toString(),
                    uriString = uri.toString(),
                    albumArtUri = null,
                    size = 0L,
                    timestamp = System.currentTimeMillis()
                )
                onPlayAudio?.invoke(tempAudioFile)
            }
        }
    )
    
    // Animated gradient background effect
    val infiniteTransition = rememberInfiniteTransition(label = "audioBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Show folder screen if a folder is selected
    if (selectedFolder != null && audioState is AudioState.Success) {
        val audioFiles = (audioState as AudioState.Success).data.audioFiles
        
        FolderAudioScreen(
            folderPath = selectedFolder!!.path,
            audioFiles = audioFiles,
            onBackClick = { selectedFolder = null },
            onAudioClick = { audioFile ->
                onPlayAudio?.invoke(audioFile)
            }
        )
        return
    }
    
    // Show artist screen if an artist is selected
    if (selectedArtist != null && audioState is AudioState.Success) {
        val audioFiles = (audioState as AudioState.Success).data.audioFiles
        
        ArtistAudioScreen(
            artist = selectedArtist!!,
            audioFiles = audioFiles,
            onBackClick = { selectedArtist = null },
            onAudioClick = { audioFile ->
                onPlayAudio?.invoke(audioFile)
            }
        )
        return
    }
    
    // Show album screen if an album is selected
    if (selectedAlbum != null && audioState is AudioState.Success) {
        val audioFiles = (audioState as AudioState.Success).data.audioFiles
        
        AlbumAudioScreen(
            album = selectedAlbum!!,
            audioFiles = audioFiles,
            onBackClick = { selectedAlbum = null },
            onAudioClick = { audioFile ->
                onPlayAudio?.invoke(audioFile)
            }
        )
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    start = Offset(0f, gradientShift * 1000),
                    end = Offset(1000f, (1f - gradientShift) * 1000)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Responsive top bar matching the screenshot exactly
            ResponsiveMusicTopBar(
                onRefresh = { viewModel.onRefresh() }
            )
            
            // Modern Section Tabs matching video section style - only show if permission is granted
            if (isAudioPermissionGranted) {
                ResponsiveMusicSectionTabs(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it }
                )
            }
            
            // Content
            Box(modifier = Modifier.weight(1f)) {
                Scaffold(
                    bottomBar = {
                        // Mini player at the bottom
                        val state = audioPlayerState.currentAudioUri.collectAsStateWithLifecycle()
                        if (state.value != null) {
                            MiniPlayer(
                                audioPlayerState = audioPlayerState,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
//                        MiniPlayer(
//                            audioPlayerState = audioPlayerState,
//                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
//                        )
                    },
                    floatingActionButton = {
                        if (isAudioPermissionGranted) {
                            FloatingActionButton(
                                onClick = { selectAudioFileLauncher.launch("audio/*") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                Icon(
                                    imageVector = NextIcons.FileOpen,
                                    contentDescription = "Open audio file"
                                )
                            }
                        }
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        onRefresh = { viewModel.onRefresh() },
                        isRefreshing = isRefreshing
                    ) {
                        // Check if we have permission (handled centrally in MainActivity)
                        if (!isAudioPermissionGranted) {
                            // Show a message that permissions are handled on app startup
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = NextIcons.Audio,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Audio Permission Required",
                                        style = MaterialTheme.typography.headlineSmall,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Please grant audio permission to view your music.\nPermissions are requested when the app starts.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Content based on audio state
                            when (val state = audioState) {
                                is AudioState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is AudioState.Error -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text("Error loading audio files")
                                            Button(onClick = { viewModel.onRefresh() }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                                is AudioState.Success -> {
                                    when (selectedTabIndex) {
                                        0 -> SongsTab(
                                            audioFiles = state.data.audioFiles,
                                            onAudioClick = { audioFile ->
                                                onPlayAudio?.invoke(audioFile)
                                            }
                                        )
                                        1 -> FoldersTab(
                                            folders = state.data.folders,
                                            onFolderClick = { folder -> selectedFolder = folder }
                                        )
                                        2 -> ArtistsTab(
                                            audioFiles = state.data.audioFiles,
                                            onArtistClick = { selectedArtist = it }
                                        )
                                        3 -> AlbumsTab(
                                            audioFiles = state.data.audioFiles,
                                            onAlbumClick = { selectedAlbum = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponsiveMusicTopBar(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side with icon and text - responsive layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Music icon with adaptive size
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 40.dp else 36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = NextIcons.Audio,
                        contentDescription = null,
                        modifier = Modifier.size(if (isTablet) 24.dp else 20.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Music text with responsive sizing
                Text(
                    text = "Music",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 20.sp else 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Right side with refresh button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(if (isTablet) 40.dp else 36.dp)
            ) {
                Icon(
                    imageVector = NextIcons.Replay,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(if (isTablet) 24.dp else 22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ResponsiveMusicSectionTabs(
    selectedTabIndex: Int, 
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    val isSmallScreen = screenWidth < 360.dp
    
    val tabs = listOf(
        Triple("Songs", NextIcons.Audio, 0),
        Triple("Folders", NextIcons.Folder, 1),
        Triple("Artists", NextIcons.Headset, 2),
        Triple("Albums", NextIcons.Movie, 3),
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        if (isSmallScreen) {
            // For very small screens, use a scrollable row
            LazyRow(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tabs) { (label, icon, index) ->
                    ResponsiveTabItem(
                        label = label,
                        icon = icon,
                        isSelected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        isCompact = true,
                        isTablet = isTablet
                    )
                }
            }
        } else {
            // For normal screens, use equal weight distribution
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tabs.forEach { (label, icon, index) ->
                    ResponsiveTabItem(
                        label = label,
                        icon = icon,
                        isSelected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        isCompact = false,
                        isTablet = isTablet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponsiveTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isCompact: Boolean,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    }
    
    val textStyle = if (isTablet) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodySmall
    }
    
    val iconSize = if (isTablet) 22.dp else if (isCompact) 18.dp else 20.dp
    val verticalPadding = if (isTablet) 14.dp else 12.dp
    val horizontalPadding = if (isCompact) 12.dp else 8.dp
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    ) {
        if (isCompact) {
            // Compact layout for small screens - icon above text
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = contentColor,
                    style = textStyle,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = if (isTablet) 12.sp else 10.sp
                )
            }
        } else {
            // Normal layout - icon and text side by side
            Row(
                modifier = Modifier
                    .padding(
                        vertical = verticalPadding,
                        horizontal = horizontalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = contentColor,
                    style = textStyle,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioPermissionRequest(
    permissionState: com.google.accompanist.permissions.PermissionState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = NextIcons.Audio,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Audio Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (permissionState.status.shouldShowRationale) {
                    "This permission is needed to access and play your audio files. Please grant permission to continue."
                } else {
                    "We need permission to access your audio files. This allows you to browse and play music from your device."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { permissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun SongsTab(
    audioFiles: List<AudioFile>,
    onAudioClick: (AudioFile) -> Unit
) {
    if (audioFiles.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No audio files found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp) // Extra space for potential mini player
        ) {
            items(audioFiles) { audioFile ->
                AudioItem(
                    audioFile = audioFile,
                    onClick = { onAudioClick(audioFile) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun AudioItem(
    audioFile: AudioFile,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Audio thumbnail
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
                if (!audioFile.albumArtUri.isNullOrBlank() && audioFile.albumArtUri != "null") {
                    AsyncImage(
                        model = audioFile.albumArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Song info column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = audioFile.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${audioFile.artist} • ${audioFile.album}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Duration
        Text(
            text = formatDuration(audioFile.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun FoldersTab(
    folders: List<AudioFolder>,
    onFolderClick: (AudioFolder) -> Unit
) {
    if (folders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No folders found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp) // Extra space for potential mini player
        ) {
            items(folders) { folder ->
                FolderItem(
                    folder = folder,
                    onClick = { onFolderClick(folder) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun FolderItem(
    folder: AudioFolder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Folder icon
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NextIcons.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${folder.audioCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ArtistsTab(
    audioFiles: List<AudioFile>,
    onArtistClick: (String) -> Unit
) {
    // Group by artist
    val artistGroups = audioFiles.groupBy { it.artist }
    
    if (artistGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No artists found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp) // Extra space for potential mini player
        ) {
            items(artistGroups.keys.toList().sorted()) { artist ->
                ArtistItem(
                    artist = artist,
                    songCount = artistGroups[artist]?.size ?: 0,
                    onClick = { onArtistClick(artist) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun ArtistItem(
    artist: String,
    songCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artist icon
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NextIcons.Headset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$songCount songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AlbumsTab(
    audioFiles: List<AudioFile>,
    onAlbumClick: (String) -> Unit
) {
    // Group by album
    val albumGroups = audioFiles.groupBy { it.album }
    
    if (albumGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No albums found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp) // Extra space for potential mini player
        ) {
            items(albumGroups.keys.toList().sorted()) { album ->
                AlbumItem(
                    album = album,
                    songCount = albumGroups[album]?.size ?: 0,
                    onClick = { onAlbumClick(album) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun AlbumItem(
    album: String,
    songCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album cover icon
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NextIcons.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$songCount songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper function to format duration
private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderAudioScreen(
    folderPath: String,
    audioFiles: List<AudioFile>,
    onBackClick: () -> Unit,
    onAudioClick: (AudioFile) -> Unit
) {
    val folderName = folderPath.substringAfterLast('/')
    val folderAudioFiles = audioFiles.filter { it.path.startsWith(folderPath) }
    
    Scaffold(
        topBar = {
            NextCenterAlignedTopAppBar(
                title = {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (folderAudioFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No audio files in this folder")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(folderAudioFiles) { audioFile ->
                    AudioItem(
                        audioFile = audioFile,
                        onClick = { onAudioClick(audioFile) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistAudioScreen(
    artist: String,
    audioFiles: List<AudioFile>,
    onBackClick: () -> Unit,
    onAudioClick: (AudioFile) -> Unit
) {
    val artistAudioFiles = audioFiles.filter { it.artist == artist }
    
    Scaffold(
        topBar = {
            NextCenterAlignedTopAppBar(
                title = {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (artistAudioFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No audio files for this artist")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(artistAudioFiles) { audioFile ->
                    AudioItem(
                        audioFile = audioFile,
                        onClick = { onAudioClick(audioFile) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumAudioScreen(
    album: String,
    audioFiles: List<AudioFile>,
    onBackClick: () -> Unit,
    onAudioClick: (AudioFile) -> Unit
) {
    val albumAudioFiles = audioFiles.filter { it.album == album }
    
    Scaffold(
        topBar = {
            NextCenterAlignedTopAppBar(
                title = {
                    Text(
                        text = album,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (albumAudioFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No audio files in this album")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(albumAudioFiles) { audioFile ->
                    AudioItem(
                        audioFile = audioFile,
                        onClick = { onAudioClick(audioFile) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
} 
package dev.anilbeesetti.nextplayer.settings.screens.medialibrary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.ClickablePreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.PreferenceSwitch
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaLibraryPreferencesScreen(
    onNavigateUp: () -> Unit,
    onFolderSettingClick: () -> Unit = {},
    onDisplayFieldsClick: () -> Unit = {},
    viewModel: MediaLibraryPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Professional animation states
    var showLibrarySection by remember { mutableStateOf(false) }
    var showDisplaySection by remember { mutableStateOf(false) }
    var showPlaybackSection by remember { mutableStateOf(false) }
    
    // Subtle background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "mediaLibraryBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        showLibrarySection = true
        delay(200)
        showDisplaySection = true
        delay(300)
        showPlaybackSection = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    start = Offset(0f, gradientShift * 300),
                    end = Offset(300f, (1f - gradientShift) * 300)
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                NextTopAppBar(
                    title = stringResource(id = R.string.media_library),
                    scrollBehavior = scrollBehaviour,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
                        ) {
                            Icon(
                                imageVector = NextIcons.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigate_up),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(state = rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Library Scanning Section
                AnimatedVisibility(
                    visible = showLibrarySection,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { -100 }
                    )
                ) {
                    ModernMediaLibrarySection(
                        title = "Library Scanning",
                        subtitle = "Control how your media files are discovered and indexed",
                        icon = NextIcons.Update
                    ) {
                        EnhancedMediaLibraryItem(
                            title = "Auto Scan Library",
                            description = "Automatically scan for new media files when app starts",
                            icon = NextIcons.Update,
                            isChecked = preferences.autoScanLibrary,
                            onClick = viewModel::toggleAutoScanLibrary,
                            index = 0
                        )
                        
                        EnhancedMediaLibraryItem(
                            title = "Show Hidden Files",
                            description = "Display hidden files and folders in media list",
                            icon = NextIcons.Visibility,
                            isChecked = preferences.showHiddenFiles,
                            onClick = viewModel::toggleShowHiddenFiles,
                            index = 1
                        )
                        
                        EnhancedMediaLibraryItem(
                            title = stringResource(id = R.string.manage_folders),
                            description = stringResource(id = R.string.manage_folders_desc),
                            icon = NextIcons.FolderOff,
                            onClick = onFolderSettingClick,
                            index = 2
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // View & Display Section
                AnimatedVisibility(
                    visible = showDisplaySection,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ModernMediaLibrarySection(
                        title = "View & Display",
                        subtitle = "Customize how your media library appears and behaves",
                        icon = NextIcons.Visibility
                    ) {
                        EnhancedMediaLibraryItem(
                            title = "Group by Folder",
                            description = "Organize media files by their parent folders",
                            icon = NextIcons.Folder,
                            isChecked = preferences.groupByFolder,
                            onClick = viewModel::toggleGroupByFolder,
                            index = 3
                        )
                        
                        EnhancedMediaLibraryItem(
                            title = "High Quality Thumbnails",
                            description = "Generate higher resolution thumbnails (uses more storage)",
                            icon = NextIcons.HighQuality,
                            isChecked = preferences.highQualityThumbnails,
                            onClick = viewModel::toggleHighQualityThumbnails,
                            index = 4
                        )
                        
                        EnhancedMediaLibraryItem(
                            title = "Display Fields",
                            description = "Customize which information to show for each media file",
                            icon = NextIcons.Settings,
                            onClick = onDisplayFieldsClick,
                            index = 5
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Playback & Interface Section
                AnimatedVisibility(
                    visible = showPlaybackSection,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ModernMediaLibrarySection(
                        title = "Playback & Interface",
                        subtitle = "Enhanced features for better media playback experience",
                        icon = NextIcons.Player
                    ) {
                        EnhancedMediaLibraryItem(
                            title = stringResource(id = R.string.mark_last_played_media),
                            description = stringResource(id = R.string.mark_last_played_media_desc),
                            icon = NextIcons.Check,
                            isChecked = preferences.markLastPlayedMedia,
                            onClick = viewModel::toggleMarkLastPlayedMedia,
                            index = 6
                        )
                        
                        EnhancedMediaLibraryItem(
                            title = stringResource(id = R.string.floating_play_button),
                            description = stringResource(id = R.string.floating_play_button_desc),
                            icon = NextIcons.SmartButton,
                            isChecked = preferences.showFloatingPlayButton,
                            onClick = viewModel::toggleShowFloatingPlayButton,
                            index = 7
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernMediaLibrarySection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 6.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section Content
            content()
        }
    }
}

@Composable
private fun EnhancedMediaLibraryItem(
    title: String,
    description: String,
    icon: ImageVector,
    index: Int,
    isChecked: Boolean? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 100L)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 50 }
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = enabled) { onClick() },
            color = MaterialTheme.colorScheme.surfaceContainer.copy(
                alpha = if (enabled) 1f else 0.6f
            ),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = if (enabled) 1f else 0.5f
                    ),
                    tonalElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (enabled) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        lineHeight = 18.sp
                    )
                }
                
                if (isChecked != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isChecked,
                        onCheckedChange = { onClick() },
                        enabled = enabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayFieldsPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: MediaLibraryPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Professional animation states
    var showFileInfoSection by remember { mutableStateOf(false) }
    var showVisualSection by remember { mutableStateOf(false) }
    
    // Subtle background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "displayFieldsBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        showFileInfoSection = true
        delay(200)
        showVisualSection = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    start = Offset(0f, gradientShift * 300),
                    end = Offset(300f, (1f - gradientShift) * 300)
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                NextTopAppBar(
                    title = "Display Fields",
                    scrollBehavior = scrollBehaviour,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
                        ) {
                            Icon(
                                imageVector = NextIcons.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigate_up),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(state = rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // File Information Section
                AnimatedVisibility(
                    visible = showFileInfoSection,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { -100 }
                    )
                ) {
                    ModernMediaLibrarySection(
                        title = "File Information",
                        subtitle = "Choose which technical details to display for each media file",
                        icon = NextIcons.Info
                    ) {
                                                 EnhancedMediaLibraryItem(
                             title = "Show Duration",
                             description = "Display video/audio duration in file list",
                             icon = NextIcons.Timer,
                             isChecked = preferences.showDurationField,
                             onClick = viewModel::toggleShowDurationField,
                             index = 0
                         )
                         
                         EnhancedMediaLibraryItem(
                             title = "Show File Size",
                             description = "Display file size information",
                             icon = NextIcons.Size,
                             isChecked = preferences.showSizeField,
                             onClick = viewModel::toggleShowSizeField,
                             index = 1
                         )
                         
                         EnhancedMediaLibraryItem(
                             title = "Show Resolution",
                             description = "Display video resolution (e.g., 1920x1080)",
                             icon = NextIcons.HighQuality,
                             isChecked = preferences.showResolutionField,
                             onClick = viewModel::toggleShowResolutionField,
                             index = 2
                         )
                         
                         EnhancedMediaLibraryItem(
                             title = "Show File Extension",
                             description = "Display file format extension",
                             icon = NextIcons.Title,
                             isChecked = preferences.showExtensionField,
                             onClick = viewModel::toggleShowExtensionField,
                             index = 3
                         )
                         
                         EnhancedMediaLibraryItem(
                             title = "Show File Path",
                             description = "Display full file path location",
                             icon = NextIcons.FolderOpen,
                             isChecked = preferences.showPathField,
                             onClick = viewModel::toggleShowPathField,
                             index = 4
                         )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Visual Elements Section
                AnimatedVisibility(
                    visible = showVisualSection,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ModernMediaLibrarySection(
                        title = "Visual Elements",
                        subtitle = "Control the visual appearance and interactive features",
                        icon = NextIcons.Video
                    ) {
                                                 EnhancedMediaLibraryItem(
                             title = "Show Thumbnails",
                             description = "Display video thumbnails in the library",
                             icon = NextIcons.Movie,
                             isChecked = preferences.showThumbnailField,
                             onClick = viewModel::toggleShowThumbnailField,
                             index = 5
                         )
                         
                         EnhancedMediaLibraryItem(
                             title = "Show Playback Progress",
                             description = "Display progress bar for partially watched videos",
                             icon = NextIcons.Player,
                             isChecked = preferences.showPlayedProgress,
                             onClick = viewModel::toggleShowPlayedProgress,
                             index = 6
                         )
                    }
                }
            }
        }
    }
}

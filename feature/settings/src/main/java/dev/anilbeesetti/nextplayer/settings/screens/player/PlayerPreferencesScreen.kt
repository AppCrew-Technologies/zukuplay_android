package dev.anilbeesetti.nextplayer.settings.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.common.extensions.round
import dev.anilbeesetti.nextplayer.core.model.*
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.*
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.settings.composables.*
import dev.anilbeesetti.nextplayer.settings.extensions.name
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: PlayerPreferencesViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Professional animation states
    var showHeader by remember { mutableStateOf(false) }
    var showSections by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        showHeader = true
        delay(400)
        showSections = true
    }

    // Subtle animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "playerBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
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
                    title = stringResource(id = R.string.player_name),
                    scrollBehavior = scrollBehaviour,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        ) {
                            Icon(
                                imageVector = NextIcons.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigate_up),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Professional Header
                item {
                    AnimatedVisibility(
                        visible = showHeader,
                        enter = fadeIn(tween(600)) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetY = { -150 }
                        )
                    ) {
                        PlayerHeader()
                    }
                }

                // Modern Sections with staggered animations
                val sections = listOf(
                    PlayerSection.CONTROLS,
                    PlayerSection.PLAYBACK,
                    PlayerSection.ADVANCED
                )

                itemsIndexed(sections) { index, section ->
                    val delay = (index + 1) * 150L
                    var showSection by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(showSections) {
                        if (showSections) {
                            delay(delay)
                            showSection = true
                        }
                    }

                    AnimatedVisibility(
                        visible = showSection,
                        enter = fadeIn(tween(500)) + 
                               slideInVertically(
                                   animationSpec = spring(
                                       dampingRatio = Spring.DampingRatioMediumBouncy,
                                       stiffness = Spring.StiffnessMediumLow
                                   ),
                                   initialOffsetY = { 100 }
                               ) + 
                               scaleIn(
                                   animationSpec = spring(
                                       dampingRatio = Spring.DampingRatioMediumBouncy,
                                       stiffness = Spring.StiffnessMediumLow
                                   ),
                                   initialScale = 0.8f
                               )
                    ) {
                        when (section) {
                            PlayerSection.CONTROLS -> {
                                ModernPlayerControlsSection(
                                    preferences = preferences,
                                    viewModel = viewModel
                                )
                            }
                            PlayerSection.PLAYBACK -> {
                                ModernPlaybackSection(
                                    preferences = preferences,
                                    viewModel = viewModel
                                )
                            }
                            PlayerSection.ADVANCED -> {
                                ModernAdvancedSection(
                                    preferences = preferences,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialogs remain the same but now in a cleaner context
    PlayerDialogs(
        uiState = uiState,
        preferences = preferences,
        viewModel = viewModel
    )
}

@Composable
private fun PlayerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = NextIcons.Player,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Player Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Customize your playback experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModernPlayerControlsSection(
    preferences: PlayerPreferences,
    viewModel: PlayerPreferencesViewModel
) {
    ModernSettingsSection(
        title = "Player Controls",
        subtitle = "Gestures and touch controls",
        icon = NextIcons.Player
    ) {
        PreferenceSwitch(
            title = stringResource(id = R.string.seek_gesture),
            description = stringResource(id = R.string.seek_gesture_description),
            icon = NextIcons.SwipeHorizontal,
            isChecked = preferences.useSeekControls,
            onClick = viewModel::toggleUseSeekControls
        )
        
        PreferenceSwitch(
            title = stringResource(id = R.string.swipe_gesture),
            description = stringResource(id = R.string.swipe_gesture_description),
            icon = NextIcons.SwipeVertical,
            isChecked = preferences.useSwipeControls,
            onClick = viewModel::toggleUseSwipeControls
        )
        
        PreferenceSwitch(
            title = stringResource(id = R.string.zoom_gesture),
            description = stringResource(id = R.string.zoom_gesture_description),
            icon = NextIcons.Pinch,
            isChecked = preferences.useZoomControls,
            onClick = viewModel::toggleUseZoomControls
        )

        PreferenceSwitchWithDivider(
            title = stringResource(id = R.string.double_tap),
            description = stringResource(id = R.string.double_tap_description),
            isChecked = (preferences.doubleTapGesture != DoubleTapGesture.NONE),
            onChecked = viewModel::toggleDoubleTapGesture,
            icon = NextIcons.DoubleTap,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.DoubleTapDialog) }
        )

        PreferenceSwitchWithDivider(
            title = stringResource(id = R.string.long_press_gesture),
            description = stringResource(id = R.string.long_press_gesture_desc, preferences.longPressControlsSpeed),
            isChecked = preferences.useLongPressControls,
            onChecked = viewModel::toggleUseLongPressControls,
            icon = NextIcons.Tap,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.LongPressControlsSpeedDialog) }
        )

        ClickablePreferenceItem(
            title = stringResource(R.string.seek_increment),
            description = stringResource(R.string.seconds, preferences.seekIncrement),
            icon = NextIcons.Replay,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.SeekIncrementDialog) }
        )

        ClickablePreferenceItem(
            title = stringResource(R.string.controller_timeout),
            description = stringResource(R.string.seconds, preferences.controllerAutoHideTimeout / 1000),
            icon = NextIcons.Timer,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.ControllerTimeoutDialog) }
        )

        ClickablePreferenceItem(
            title = stringResource(id = R.string.control_buttons_alignment),
            description = preferences.controlButtonsPosition.name,
            icon = NextIcons.ButtonsPosition,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.ControlButtonsDialog) }
        )
    }
}

@Composable
private fun ModernPlaybackSection(
    preferences: PlayerPreferences,
    viewModel: PlayerPreferencesViewModel
) {
    ModernSettingsSection(
        title = "Playback Settings",
        subtitle = "Speed, resume, and auto-play options",
        icon = NextIcons.Speed
    ) {
        ClickablePreferenceItem(
            title = stringResource(id = R.string.resume),
            description = preferences.resume.name,
            icon = NextIcons.Resume,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.ResumeDialog) }
        )
        
        ClickablePreferenceItem(
            title = stringResource(id = R.string.default_playback_speed),
            description = preferences.defaultPlaybackSpeed.toString(),
            icon = NextIcons.Speed,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.PlaybackSpeedDialog) }
        )
        
        PreferenceSwitch(
            title = stringResource(id = R.string.autoplay_settings),
            description = stringResource(id = R.string.autoplay_settings_description),
            icon = NextIcons.Player,
            isChecked = preferences.autoplay,
            onClick = viewModel::toggleAutoplay
        )
    }
}

@Composable
private fun ModernAdvancedSection(
    preferences: PlayerPreferences,
    viewModel: PlayerPreferencesViewModel
) {
    ModernSettingsSection(
        title = "Advanced Settings",
        subtitle = "Picture-in-picture and system integration",
        icon = NextIcons.Settings
    ) {
        PreferenceSwitch(
            title = stringResource(id = R.string.pip_settings),
            description = stringResource(id = R.string.pip_settings_description),
            icon = NextIcons.Pip,
            isChecked = preferences.autoPip,
            onClick = viewModel::toggleAutoPip
        )
        
        PreferenceSwitch(
            title = stringResource(id = R.string.background_play),
            description = stringResource(id = R.string.background_play_description),
            icon = NextIcons.Headset,
            isChecked = preferences.autoBackgroundPlay,
            onClick = viewModel::toggleAutoBackgroundPlay
        )
        
        ClickablePreferenceItem(
            title = stringResource(id = R.string.player_screen_orientation),
            description = preferences.playerScreenOrientation.name,
            icon = NextIcons.Rotation,
            onClick = { viewModel.showDialog(PlayerPreferenceDialog.PlayerScreenOrientationDialog) }
        )
    }
}

@Composable
private fun ModernSettingsSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sectionScale"
    )

    Column {
        // Modern section header using PreferenceSubtitle
        PreferenceSubtitle(
            text = title,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PlayerDialogs(
    uiState: PlayerPreferencesUIState,
    preferences: PlayerPreferences,
    viewModel: PlayerPreferencesViewModel
) {
    uiState.showDialog?.let { showDialog ->
        when (showDialog) {
            PlayerPreferenceDialog.ResumeDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.resume),
                    onDismissClick = viewModel::hideDialog
                ) {
                    items(Resume.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name,
                            selected = (it == preferences.resume),
                            onClick = {
                                viewModel.updatePlaybackResume(it)
                                viewModel.hideDialog()
                            }
                        )
                    }
                }
            }

            PlayerPreferenceDialog.DoubleTapDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.double_tap),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(DoubleTapGesture.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name,
                            selected = (it == preferences.doubleTapGesture),
                            onClick = {
                                viewModel.updateDoubleTapGesture(it)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            PlayerPreferenceDialog.FastSeekDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.fast_seek),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(FastSeek.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name,
                            selected = (it == preferences.fastSeek),
                            onClick = {
                                viewModel.updateFastSeek(it)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            PlayerPreferenceDialog.PlayerScreenOrientationDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.player_screen_orientation),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(ScreenOrientation.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name,
                            selected = it == preferences.playerScreenOrientation,
                            onClick = {
                                viewModel.updatePreferredPlayerOrientation(it)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            PlayerPreferenceDialog.ControlButtonsDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.control_buttons_alignment),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(ControlButtonsPosition.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name,
                            selected = it == preferences.controlButtonsPosition,
                            onClick = {
                                viewModel.updatePreferredControlButtonsPosition(it)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            PlayerPreferenceDialog.LongPressControlsSpeedDialog -> {
                var speedValue by remember { mutableFloatStateOf(preferences.longPressControlsSpeed) }

                NextDialogWithDoneAndCancelButtons(
                    title = stringResource(R.string.long_press_gesture),
                    onDoneClick = {
                        viewModel.updateLongPressControlsSpeed(speedValue)
                        viewModel.hideDialog()
                    },
                    onDismissClick = viewModel::hideDialog,
                    content = {
                        Text(
                            text = speedValue.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = speedValue,
                            onValueChange = { speedValue = it.round(1) },
                            valueRange = 0.2f..4.0f,
                        )
                    },
                )
            }

            PlayerPreferenceDialog.ControllerTimeoutDialog -> {
                var timeoutValue by remember {
                    mutableIntStateOf(preferences.controllerAutoHideTimeout / 1000)
                }

                NextDialogWithDoneAndCancelButtons(
                    title = stringResource(R.string.controller_timeout),
                    onDoneClick = {
                        viewModel.updateControlAutoHideTimeout(timeoutValue)
                        viewModel.hideDialog()
                    },
                    onDismissClick = viewModel::hideDialog,
                    content = {
                        Text(
                            text = stringResource(R.string.seconds, timeoutValue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = timeoutValue.toFloat(),
                            onValueChange = { timeoutValue = it.toInt() },
                            valueRange = 1.0f..10.0f,
                        )
                    },
                )
            }

            PlayerPreferenceDialog.SeekIncrementDialog -> {
                var incrementValue by remember {
                    mutableIntStateOf(preferences.seekIncrement)
                }

                NextDialogWithDoneAndCancelButtons(
                    title = stringResource(R.string.seek_increment),
                    onDoneClick = {
                        viewModel.updateSeekIncrement(incrementValue)
                        viewModel.hideDialog()
                    },
                    onDismissClick = viewModel::hideDialog,
                    content = {
                        Text(
                            text = stringResource(R.string.seconds, incrementValue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = incrementValue.toFloat(),
                            onValueChange = { incrementValue = it.toInt() },
                            valueRange = 1.0f..60.0f,
                        )
                    },
                )
            }

            PlayerPreferenceDialog.PlaybackSpeedDialog -> {
                var speedValue by remember {
                    mutableFloatStateOf(preferences.defaultPlaybackSpeed)
                }

                NextDialogWithDoneAndCancelButtons(
                    title = stringResource(R.string.default_playback_speed),
                    onDoneClick = {
                        viewModel.updateDefaultPlaybackSpeed(speedValue)
                        viewModel.hideDialog()
                    },
                    onDismissClick = viewModel::hideDialog,
                    content = {
                        Text(
                            text = speedValue.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = speedValue,
                            onValueChange = { speedValue = it.round(2) },
                            valueRange = 0.5f..2.0f,
                        )
                    },
                )
            }
        }
    }
}

// Enum for section organization
private enum class PlayerSection {
    CONTROLS,
    PLAYBACK,
    ADVANCED
}

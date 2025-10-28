package dev.anilbeesetti.nextplayer.settings.screens.audio

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.RadioTextButton
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.settings.composables.OptionsDialog
import dev.anilbeesetti.nextplayer.settings.utils.LocalesHelper
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: AudioPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languages = remember { listOf(Pair("None", "")) + LocalesHelper.getAvailableLocales() }

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.audio),
                scrollBehavior = scrollBehaviour,
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
                    ) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
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
            // Professional Audio Visualizer
            ProfessionalAudioVisualizer()
            
            // Sound Experience header
            Text(
                text = "Sound Experience",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Audio Behavior card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Audio Behavior",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    AudioFeatureItem(
                        title = "Audio Focus",
                        subtitle = "Pause when other apps play audio and vice versa",
                        icon = NextIcons.Focus,
                        isChecked = preferences.requireAudioFocus,
                        onClick = viewModel::toggleRequireAudioFocus
                    )
                    
                    AudioFeatureItem(
                        title = "Auto-Pause on",
                        subtitle = "Pause when headphones are unplugged",
                        icon = NextIcons.HeadsetOff,
                        isChecked = preferences.pauseOnHeadsetDisconnect,
                        onClick = viewModel::togglePauseOnHeadsetDisconnect
                    )
                    
                    AudioFeatureItem(
                        title = "System Volume",
                        subtitle = "Show system volume panel with headset buttons",
                        icon = NextIcons.Headset,
                        isChecked = preferences.showSystemVolumePanel,
                        onClick = viewModel::toggleShowSystemVolumePanel,
                        showDivider = false
                    )
                }
            }

            // Language & Volume settings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Language & Volume",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LanguagePreference(
                        currentLanguage = languages.find { it.second == preferences.preferredAudioLanguage }?.first ?: "None",
                        onClick = { viewModel.showDialog(AudioPreferenceDialog.AudioLanguageDialog) }
                    )
                    
                    AudioFeatureItem(
                        title = "Volume Boost",
                        subtitle = "Enhance volume up to 200% for quiet videos",
                        icon = NextIcons.VolumeUp,
                        isChecked = preferences.shouldUseVolumeBoost,
                        onClick = viewModel::toggleShouldUseVolumeBoost,
                        showDivider = false
                    )
                }
            }
        }

        uiState.showDialog?.let { showDialog ->
            when (showDialog) {
                AudioPreferenceDialog.AudioLanguageDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.preferred_audio_lang),
                        onDismissClick = viewModel::hideDialog,
                    ) {
                        items(languages) {
                            RadioTextButton(
                                text = it.first,
                                selected = it.second == preferences.preferredAudioLanguage,
                                onClick = {
                                    viewModel.updateAudioLanguage(it.second)
                                    viewModel.hideDialog()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioFeatureItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        
        // Text content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Switch
        Switch(
            checked = isChecked,
            onCheckedChange = { onClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
    
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun LanguagePreference(
    currentLanguage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NextIcons.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        
        // Text content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = "Preferred Language",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = currentLanguage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Icon(
            imageVector = NextIcons.ArrowBack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 8.dp)
        )
    }
    
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}

@Composable
private fun ProfessionalAudioVisualizer() {
    // State for controlling animation cycles
    var isAnimating by remember { mutableStateOf(false) }
    var currentCycle by remember { mutableStateOf(0) }
    
    // Animation values for each frequency band
    val bandCount = 32
    val bandAnimations = remember {
        (0 until bandCount).map { index ->
            Animatable(initialValue = 0.1f + (index % 4) * 0.15f)
        }
    }
    
    // Central speaker pulse animation
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakerPulse"
    )
    
    // Glow effect animation
    val glowAlpha by pulseAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Start the complex animation cycles
    LaunchedEffect(Unit) {
        isAnimating = true
        while (isAnimating) {
            // Create realistic audio patterns
            when (currentCycle % 6) {
                0 -> {
                    // Bass drop pattern
                    bandAnimations.forEachIndexed { index, animatable ->
                        val delay = (index * 20).toLong()
                        launch {
                            delay(delay)
                            val targetHeight = when {
                                index < 4 -> 0.9f + Random.nextFloat() * 0.1f
                                index < 8 -> 0.7f + Random.nextFloat() * 0.2f
                                index < 16 -> 0.4f + Random.nextFloat() * 0.3f
                                else -> 0.2f + Random.nextFloat() * 0.2f
                            }
                            animatable.animateTo(
                                targetValue = targetHeight,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                    delay(1200)
                }
                1 -> {
                    // High frequency sweep
                    bandAnimations.forEachIndexed { index, animatable ->
                        val delay = ((bandCount - index) * 15).toLong()
                        launch {
                            delay(delay)
                            val targetHeight = when {
                                index > 24 -> 0.8f + Random.nextFloat() * 0.2f
                                index > 16 -> 0.5f + Random.nextFloat() * 0.3f
                                index > 8 -> 0.3f + Random.nextFloat() * 0.2f
                                else -> 0.1f + Random.nextFloat() * 0.1f
                            }
                            animatable.animateTo(
                                targetValue = targetHeight,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessHigh
                                )
                            )
                        }
                    }
                    delay(1000)
                }
                2 -> {
                    // Rhythmic pattern
                    repeat(3) {
                        bandAnimations.forEachIndexed { index, animatable ->
                            launch {
                                val pattern = sin((index * 0.5f)) * 0.4f + 0.4f + Random.nextFloat() * 0.2f
                                animatable.animateTo(
                                    targetValue = pattern,
                                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                        delay(300)
                    }
                }
                3 -> {
                    // Random burst pattern
                    repeat(8) {
                        val randomIndices = (0 until bandCount).shuffled().take(bandCount / 2)
                        randomIndices.forEach { index ->
                            launch {
                                bandAnimations[index].animateTo(
                                    targetValue = 0.6f + Random.nextFloat() * 0.4f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioHighBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    )
                                )
                            }
                        }
                        delay(200)
                    }
                }
                4 -> {
                    // Build up pattern
                    for (wave in 0..3) {
                        bandAnimations.forEachIndexed { index, animatable ->
                            val delay = (index * 25).toLong()
                            launch {
                                delay(delay)
                                val buildHeight = (wave + 1) * 0.2f + Random.nextFloat() * 0.1f
                                animatable.animateTo(
                                    targetValue = buildHeight,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                        delay(400)
                    }
                }
                5 -> {
                    // Calm down to idle
                    bandAnimations.forEachIndexed { index, animatable ->
                        val delay = (index * 30).toLong()
                        launch {
                            delay(delay)
                            animatable.animateTo(
                                targetValue = 0.1f + (index % 3) * 0.1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                    delay(1500)
                }
            }
            currentCycle++
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color(0xFF16213e),
                            Color(0xFF0f3460),
                            Color(0xFF1a1a2e)
                        ),
                        center = androidx.compose.ui.geometry.Offset.Unspecified
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left frequency bands
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    for (i in 0 until bandCount / 2) {
                        val currentHeight by bandAnimations[i].asState()
                        FrequencyBar(
                            height = currentHeight,
                            index = i,
                            maxHeight = 80.dp,
                            isLeft = true
                        )
                    }
                }
                
                // Central speaker with premium effects
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(85.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.6f),
                                        Color.Transparent
                                    ),
                                    radius = 60f
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // Speaker container with gradient
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = NextIcons.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    shadowElevation = 8f
                                }
                        )
                    }
                }
                
                // Right frequency bands
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    for (i in (bandCount / 2) until bandCount) {
                        val currentHeight by bandAnimations[i].asState()
                        FrequencyBar(
                            height = currentHeight,
                            index = i,
                            maxHeight = 80.dp,
                            isLeft = false
                        )
                    }
                }
            }
            
            // Overlay text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Audio Engine Active",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Premium Sound Processing",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FrequencyBar(
    height: Float,
    index: Int,
    maxHeight: androidx.compose.ui.unit.Dp,
    isLeft: Boolean
) {
    val baseWidth = 4.dp
    val actualHeight = (maxHeight.value * height).dp
    
    // Create frequency-based color
    val barColor = when {
        index < 4 -> Color(0xFFFF6B6B) // Bass - Red
        index < 8 -> Color(0xFFFFE66D) // Low Mid - Yellow
        index < 12 -> Color(0xFF4ECDC4) // Mid - Cyan
        else -> Color(0xFF45B7D1) // High - Blue
    }
    
    Box(
        modifier = Modifier
            .width(baseWidth)
            .height(actualHeight.coerceAtLeast(2.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        barColor.copy(alpha = 0.9f),
                        barColor.copy(alpha = 0.6f),
                        barColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 2.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 1.dp
                )
            )
            .graphicsLayer {
                shadowElevation = 2f
            }
    )
}

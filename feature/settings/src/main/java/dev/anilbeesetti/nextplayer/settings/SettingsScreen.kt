package dev.anilbeesetti.nextplayer.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onItemClick: (Setting) -> Unit,
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var showContent by remember { mutableStateOf(false) }
    
    // Get settings items inside @Composable context
    val settingsItems = getSettingsItems()
    
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundAnimation")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientShift"
    )

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
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
                    start = androidx.compose.ui.geometry.Offset(0f, gradientShift * 1000),
                    end = androidx.compose.ui.geometry.Offset(1000f, (1f - gradientShift) * 1000)
                )
            )
    ) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            NextTopAppBar(
                title = "Settings",
                scrollBehavior = scrollBehaviour,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Start)
                        ),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top spacing
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Settings Grid
            itemsIndexed(settingsItems) { index, item ->
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 200 + (index * 100),
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 200 + (index * 100),
                            easing = FastOutSlowInEasing
                        ),
                        initialScale = 0.8f
                    ) + slideInVertically(
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 200 + (index * 100),
                            easing = FastOutSlowInEasing
                        ),
                        initialOffsetY = { it / 3 }
                    ),
                    exit = fadeOut() + scaleOut() + slideOutVertically()
                ) {
                    ModernSettingCard(
                        item = item,
                        onClick = { onItemClick(item.setting) },
                        index = index
                    )
                }
            }

            // --- Footer Version Text ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ZukuPlay_V1_Rc5",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
        }
    }




@Composable
private fun ModernSettingCard(
    item: SettingItem,
    onClick: () -> Unit,
    index: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardElevation"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with modern gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = getGradientColors(index)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 20.sp
                )
            }
            
            // Arrow indicator
            Icon(
                imageVector = NextIcons.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = 180f },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

private fun getGradientColors(index: Int): List<Color> {
    return when (index % 7) {
        0 -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        1 -> listOf(Color(0xFFf093fb), Color(0xFFf5576c))
        2 -> listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
        3 -> listOf(Color(0xFFa8edea), Color(0xFFfed6e3))
        4 -> listOf(Color(0xFFffecd2), Color(0xFFfcb69f))
        5 -> listOf(Color(0xFFa18cd1), Color(0xFFfbc2eb))
        6 -> listOf(Color(0xFFfad0c4), Color(0xFFffd1ff))
        else -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
    }
}

@Composable
private fun getSettingsItems(): List<SettingItem> {
    return listOf(
        SettingItem(
            title = stringResource(id = R.string.appearance_name),
            description = "Themes, colors, and visual customization",
            icon = NextIcons.Appearance,
            setting = Setting.APPEARANCE
        ),
        SettingItem(
            title = stringResource(id = R.string.media_library),
            description = "Video library management and folders",
            icon = NextIcons.Movie,
            setting = Setting.MEDIA_LIBRARY
        ),
        SettingItem(
            title = stringResource(id = R.string.player_name),
            description = "Playback controls and player behavior",
            icon = NextIcons.Player,
            setting = Setting.PLAYER
        ),
        SettingItem(
            title = stringResource(id = R.string.decoder),
            description = "Video decoding and performance settings",
            icon = NextIcons.Decoder,
            setting = Setting.DECODER
        ),
        SettingItem(
            title = stringResource(id = R.string.audio),
            description = "Audio output and volume management",
            icon = NextIcons.Audio,
            setting = Setting.AUDIO
        ),
        SettingItem(
            title = stringResource(id = R.string.subtitle),
            description = "Subtitle styling and language preferences",
            icon = NextIcons.Subtitle,
            setting = Setting.SUBTITLE
        ),
        SettingItem(
            title = stringResource(id = R.string.about_name),
            description = "App information and version details",
            icon = NextIcons.Info,
            setting = Setting.ABOUT
        )
    )
}

private data class SettingItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val setting: Setting
)

enum class Setting {
    APPEARANCE,
    MEDIA_LIBRARY,
    PLAYER,
    DECODER,
    AUDIO,
    SUBTITLE,
    ABOUT,
}

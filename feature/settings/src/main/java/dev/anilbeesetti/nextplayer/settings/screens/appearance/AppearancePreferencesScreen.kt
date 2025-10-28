package dev.anilbeesetti.nextplayer.settings.screens.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.model.ThemeConfig
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.RadioTextButton
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.supportsDynamicTheming
import dev.anilbeesetti.nextplayer.settings.composables.OptionsDialog
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: AppearancePreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Animation states
    var showHeader by remember { mutableStateOf(false) }
    var showCards by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showHeader = true
        delay(300)
        showCards = true
    }
    
    // Animated gradient
    val infiniteTransition = rememberInfiniteTransition(label = "gradientTransition")
    val gradientPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientPosition"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            NextTopAppBar(
                title = " Appearance ",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Animated Header
            item {
                AnimatedVisibility(
                    visible = showHeader,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { -200 }
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),  // Bright blue
                                            Color(0xFF1976D2),  // Darker blue
                                            Color(0xFF0D47A1),  // Deep blue
                                            Color(0xFF2196F3)   // Back to bright blue
                                        ),
                                        start = androidx.compose.ui.geometry.Offset(gradientPosition, 0f),
                                        end = androidx.compose.ui.geometry.Offset(gradientPosition + 500f, 500f)
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GLOW UP YOUR APP",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = buildAnnotatedString {
                                        append("Make it ")
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("yours")
                                        }
                                        append(" & express your style")
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ThemeIndicator(currentTheme = preferences.themeConfig)
                            }
                        }
                    }
                }
            }
            
            // Theme Settings
            item {
                AnimatedVisibility(
                    visible = showCards,
                    enter = fadeIn() + scaleIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    ThemeCard(
                        currentTheme = preferences.themeConfig,
                        onThemeClick = { viewModel.showDialog(AppearancePreferenceDialog.Theme) }
                    )
                }
            }
            
            // High Contrast Setting
            item {
                AnimatedVisibility(
                    visible = showCards,
                    enter = fadeIn() + scaleIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            delayMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    SettingCard(
                        title = "High Contrast Mode 👀",
                        description = "Deep black background for OLED screens - saves battery & looks sleek",
                        icon = NextIcons.Contrast,
                        isChecked = preferences.useHighContrastDarkTheme,
                        onClick = viewModel::toggleUseHighContrastDarkTheme,
                        backgroundColor = Color(0xFF0D47A1)  // Deep blue
                    )
                }
            }
            
            // Dynamic Colors Setting
            if (supportsDynamicTheming()) {
                item {
                    AnimatedVisibility(
                        visible = showCards,
                        enter = fadeIn() + scaleIn(
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        SettingCard(
                            title = "Vibe With Your Wallpaper 🎨",
                            description = "Colors adapt to your device wallpaper for personalized style that's totally you",
                            icon = NextIcons.Appearance,
                            isChecked = preferences.useDynamicColors,
                            onClick = viewModel::toggleUseDynamicColors,
                            backgroundColor = Color(0xFF1976D2)  // Darker blue
                        )
                    }
                }
            }
            
            // Quick Theme Selector
            item {
                AnimatedVisibility(
                    visible = showCards,
                    enter = fadeIn() + expandHorizontally(
                        animationSpec = tween(
                            durationMillis = 500,
                            delayMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    QuickThemeSelector(
                        currentTheme = preferences.themeConfig,
                        onThemeSelected = { viewModel.updateThemeConfig(it) }
                    )
                }
            }
            
            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        uiState.showDialog?.let { showDialog ->
            when (showDialog) {
                AppearancePreferenceDialog.Theme -> {
                    OptionsDialog(
                        text = "Choose Your Vibe",
                        onDismissClick = viewModel::hideDialog,
                    ) {
                        itemsIndexed(ThemeConfig.entries.toTypedArray()) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (item) {
                                                ThemeConfig.SYSTEM -> Color(0xFFE3F2FD)  // Light blue background
                                                ThemeConfig.OFF -> Color(0xFFFFFFFF)     // White background
                                                ThemeConfig.ON -> Color(0xFF000000)      // Black background
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item) {
                                            ThemeConfig.SYSTEM -> NextIcons.Settings
                                            ThemeConfig.OFF -> NextIcons.Brightness
                                            ThemeConfig.ON -> NextIcons.DarkMode
                                        },
                                        contentDescription = null,
                                        tint = when (item) {
                                            ThemeConfig.SYSTEM -> Color(0xFF1976D2)  // Darker blue
                                            ThemeConfig.OFF -> Color(0xFF000000)     // Black
                                            ThemeConfig.ON -> Color(0xFFFFFFFF)      // White
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = when (item) {
                                            ThemeConfig.SYSTEM -> "Match My Device"
                                            ThemeConfig.OFF -> "Light Mode"
                                            ThemeConfig.ON -> "Dark Mode"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    
                                    Text(
                                        text = when (item) {
                                            ThemeConfig.SYSTEM -> "Uses your device settings"
                                            ThemeConfig.OFF -> "Bright and clean vibes"
                                            ThemeConfig.ON -> "Easy on the eyes at night"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                RadioTextButton(
                                    text = "",
                                    selected = (item == preferences.themeConfig),
                                    onClick = {
                                        viewModel.updateThemeConfig(item)
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
}

@Composable
fun ThemeIndicator(currentTheme: ThemeConfig) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconTint = Color.White
        
        Icon(
            imageVector = when (currentTheme) {
                ThemeConfig.SYSTEM -> NextIcons.Settings
                ThemeConfig.OFF -> NextIcons.Brightness
                ThemeConfig.ON -> NextIcons.DarkMode
            },
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = when (currentTheme) {
                ThemeConfig.SYSTEM -> "Matching Device"
                ThemeConfig.OFF -> "Light Mode"
                ThemeConfig.ON -> "Dark Mode"
            },
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun ThemeCard(
    currentTheme: ThemeConfig,
    onThemeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF1976D2)  // Darker blue
            )
            .clickable { onThemeClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Icons
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1976D2),  // Darker blue
                                Color(0xFF2196F3)   // Bright blue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Icon(
                        imageVector = NextIcons.Brightness,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = (-4).dp)
                    )
                    
                    Icon(
                        imageVector = NextIcons.DarkMode,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Theme Mode 🌓",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "Currently: ${
                        when (currentTheme) {
                            ThemeConfig.SYSTEM -> "Matching Device"
                            ThemeConfig.OFF -> "Light Mode"
                            ThemeConfig.ON -> "Dark Mode"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = NextIcons.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .rotate(180f)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isChecked) 16.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = backgroundColor
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feature icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = { onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = backgroundColor,
                    checkedBorderColor = backgroundColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
                    uncheckedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun QuickThemeSelector(
    currentTheme: ThemeConfig,
    onThemeSelected: (ThemeConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Quick Switch 💫",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeConfig.entries.forEach { theme ->
                QuickThemeOption(
                    theme = theme,
                    isSelected = theme == currentTheme,
                    onSelected = { onThemeSelected(theme) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickThemeOption(
    theme: ThemeConfig,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelected() },
        shape = RoundedCornerShape(16.dp),
        color = when (theme) {
            ThemeConfig.SYSTEM -> Color(0xFFE3F2FD)  // Light blue background
            ThemeConfig.OFF -> Color(0xFFFFFFFF)     // White background
            ThemeConfig.ON -> Color(0xFF000000)      // Black background
        },
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = when (theme) {
                    ThemeConfig.SYSTEM -> NextIcons.Settings
                    ThemeConfig.OFF -> NextIcons.Brightness
                    ThemeConfig.ON -> NextIcons.DarkMode
                },
                contentDescription = null,
                tint = when (theme) {
                    ThemeConfig.SYSTEM -> Color(0xFF1976D2)  // Darker blue
                    ThemeConfig.OFF -> Color(0xFF000000)     // Black
                    ThemeConfig.ON -> Color(0xFFFFFFFF)      // White
                },
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (theme) {
                    ThemeConfig.SYSTEM -> "Auto"
                    ThemeConfig.OFF -> "Light"
                    ThemeConfig.ON -> "Dark"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = when (theme) {
                    ThemeConfig.SYSTEM -> Color(0xFF1976D2)  // Darker blue
                    ThemeConfig.OFF -> Color(0xFF000000)     // Black
                    ThemeConfig.ON -> Color(0xFFFFFFFF)      // White
                }
            )
        }
    }
}

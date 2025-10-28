package dev.anilbeesetti.nextplayer.settings.screens.subtitle

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
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
import dev.anilbeesetti.nextplayer.core.model.Font
import dev.anilbeesetti.nextplayer.core.model.PlayerPreferences
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.AnimatedClickablePreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.ClickablePreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.DoneButton
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.PreferenceSwitch
import dev.anilbeesetti.nextplayer.core.ui.components.PreferenceSwitchWithDivider
import dev.anilbeesetti.nextplayer.core.ui.components.RadioTextButton
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.settings.composables.OptionsDialog
import dev.anilbeesetti.nextplayer.settings.composables.PreferenceSubtitle
import dev.anilbeesetti.nextplayer.settings.extensions.name
import dev.anilbeesetti.nextplayer.settings.utils.LocalesHelper
import kotlinx.coroutines.delay
import java.nio.charset.Charset
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlePreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: SubtitlePreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languages = remember { listOf(Pair("None", "")) + LocalesHelper.getAvailableLocales() }
    val charsetResource = stringArrayResource(id = R.array.charsets_list)
    val context = LocalContext.current

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Professional animation states
    var showPreview by remember { mutableStateOf(false) }
    var showPlaybackSettings by remember { mutableStateOf(false) }
    var showAppearanceSettings by remember { mutableStateOf(false) }
    
    // Subtitle text preview animation
    val subtitleText = "Welcome to ZukuPlay! こんにちは! Bonjour!"
    var subtitleOffset by remember { mutableStateOf(0f) }
    
    // Subtle background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "subtitleBackground")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        showPreview = true
        delay(200)
        showPlaybackSettings = true
        delay(300)
        showAppearanceSettings = true
        
        // Subtitle animation effect
        while (true) {
            for (i in 0 until 20) {
                subtitleOffset = i * 0.05f
                delay(30)
            }
            for (i in 20 downTo 0) {
                subtitleOffset = i * 0.05f
                delay(30)
            }
            delay(1000)
        }
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
                    title = stringResource(id = R.string.subtitle),
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
                // Professional Subtitle Preview
                AnimatedVisibility(
                    visible = showPreview,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { -100 }
                    )
                ) {
                    ModernSubtitlePreview(
                        subtitleText = subtitleText,
                        subtitleOffset = subtitleOffset,
                        preferences = preferences
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Professional Playback Settings Section
                AnimatedVisibility(
                    visible = showPlaybackSettings,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ModernSubtitleSection(
                        title = "Subtitle Playback",
                        subtitle = "Language and encoding preferences",
                        icon = NextIcons.Subtitle
                    ) {
                        AnimatedSubtitleItem(
                            title = "Preferred Language",
                            description = languages.find { it.second == preferences.preferredSubtitleLanguage }?.first
                                ?: "Auto-select preferred subtitle language",
                            icon = NextIcons.Language,
                            onClick = { 
                                viewModel.showDialog(SubtitlePreferenceDialog.SubtitleLanguageDialog) 
                            },
                            index = 0
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Text Encoding",
                            description = "Current: ${
                                charsetResource.first { it.contains(preferences.subtitleTextEncoding) }
                            }",
                            icon = NextIcons.Subtitle,
                            onClick = { 
                                viewModel.showDialog(SubtitlePreferenceDialog.SubtitleEncodingDialog) 
                            },
                            index = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Professional Appearance Section
                AnimatedVisibility(
                    visible = showAppearanceSettings,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    ModernSubtitleSection(
                        title = "Subtitle Style",
                        subtitle = "Appearance and visual customization",
                        icon = NextIcons.Style
                    ) {
                        AnimatedSubtitleItem(
                            title = "Use System Caption Style",
                            description = "Use your Android system's subtitle settings",
                            icon = NextIcons.Settings,
                            isChecked = preferences.useSystemCaptionStyle,
                            onClick = { 
                                viewModel.toggleUseSystemCaptionStyle()
                                if (preferences.useSystemCaptionStyle) {
                                    context.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS))
                                }
                            },
                            index = 2
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Subtitle Font",
                            description = "Current: ${preferences.subtitleFont.name()}",
                            icon = NextIcons.Font,
                            onClick = { 
                                viewModel.showDialog(SubtitlePreferenceDialog.SubtitleFontDialog) 
                            },
                            enabled = !preferences.useSystemCaptionStyle,
                            index = 3
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Bold Text",
                            description = "Make subtitle text bold for better readability",
                            icon = NextIcons.Bold,
                            isChecked = preferences.subtitleTextBold,
                            onClick = viewModel::toggleSubtitleTextBold,
                            enabled = !preferences.useSystemCaptionStyle,
                            index = 4
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Text Size",
                            description = "Current: ${preferences.subtitleTextSize}sp",
                            icon = NextIcons.FontSize,
                            onClick = { 
                                viewModel.showDialog(SubtitlePreferenceDialog.SubtitleSizeDialog) 
                            },
                            enabled = !preferences.useSystemCaptionStyle,
                            index = 5
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Background Box",
                            description = "Add a background behind subtitle text",
                            icon = NextIcons.Background,
                            isChecked = preferences.subtitleBackground,
                            onClick = viewModel::toggleSubtitleBackground,
                            enabled = !preferences.useSystemCaptionStyle,
                            index = 6
                        )
                        
                        AnimatedSubtitleItem(
                            title = "Use Embedded Styles",
                            description = "Apply styling from the subtitle file itself",
                            icon = NextIcons.Style,
                            isChecked = preferences.applyEmbeddedStyles,
                            onClick = viewModel::toggleApplyEmbeddedStyles,
                            index = 7
                        )
                    }
                }
            }
        }
    }

    // Dialogs remain the same but now in cleaner context
    SubtitleDialogs(
        uiState = uiState,
        preferences = preferences,
        viewModel = viewModel,
        languages = languages,
        charsetResource = charsetResource
    )
}

@Composable
private fun ModernSubtitlePreview(
    subtitleText: String,
    subtitleOffset: Float,
    preferences: PlayerPreferences
) {
    Card(
        modifier = Modifier
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Video placeholder with gradient border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            ) {
                // Subtitle preview text
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .offset(y = (subtitleOffset * 8).dp)
                ) {
                    Surface(
                        color = if (preferences.subtitleBackground) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = subtitleText,
                            modifier = Modifier.padding(
                                horizontal = if (preferences.subtitleBackground) 12.dp else 0.dp,
                                vertical = if (preferences.subtitleBackground) 6.dp else 0.dp
                            ),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (preferences.subtitleTextBold) FontWeight.Bold else FontWeight.Normal,
                                fontSize = preferences.subtitleTextSize.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernSubtitleSection(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
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
        // Modern section header
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
                defaultElevation = 3.dp
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SubtitleDialogs(
    uiState: SubtitlePreferencesUIState,
    preferences: PlayerPreferences,
    viewModel: SubtitlePreferencesViewModel,
    languages: List<Pair<String, String>>,
    charsetResource: Array<String>
) {
    uiState.showDialog?.let { showDialog ->
        when (showDialog) {
            SubtitlePreferenceDialog.SubtitleLanguageDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.preferred_subtitle_lang),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(languages) {
                        RadioTextButton(
                            text = it.first,
                            selected = it.second == preferences.preferredSubtitleLanguage,
                            onClick = {
                                viewModel.updateSubtitleLanguage(it.second)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            SubtitlePreferenceDialog.SubtitleFontDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.subtitle_font),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(Font.entries.toTypedArray()) {
                        RadioTextButton(
                            text = it.name(),
                            selected = it == preferences.subtitleFont,
                            onClick = {
                                viewModel.updateSubtitleFont(it)
                                viewModel.hideDialog()
                            },
                        )
                    }
                }
            }

            SubtitlePreferenceDialog.SubtitleSizeDialog -> {
                var size by remember { mutableIntStateOf(preferences.subtitleTextSize) }

                NextDialog(
                    onDismissRequest = viewModel::hideDialog,
                    title = { Text(text = stringResource(id = R.string.subtitle_text_size)) },
                    content = {
                        // Show example subtitle text with the selected size
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = if (preferences.subtitleTextBold) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = size.sp
                                    )
                                ) {
                                    append("Subtitle Preview")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${size}sp",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Slider(
                            value = size.toFloat(),
                            onValueChange = { size = it.toInt() },
                            valueRange = 15f..60f,
                        )
                    },
                    confirmButton = {
                        DoneButton(onClick = {
                            viewModel.updateSubtitleFontSize(size)
                            viewModel.hideDialog()
                        })
                    },
                    dismissButton = { CancelButton(onClick = viewModel::hideDialog) },
                )
            }

            SubtitlePreferenceDialog.SubtitleEncodingDialog -> {
                OptionsDialog(
                    text = stringResource(id = R.string.subtitle_text_encoding),
                    onDismissClick = viewModel::hideDialog,
                ) {
                    items(charsetResource) {
                        val currentCharset = it.substringAfterLast("(", "").removeSuffix(")")
                        if (currentCharset.isEmpty() || Charset.isSupported(currentCharset)) {
                            RadioTextButton(
                                text = it,
                                selected = currentCharset == preferences.subtitleTextEncoding,
                                onClick = {
                                    viewModel.updateSubtitleEncoding(currentCharset)
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
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun AnimatedSubtitleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    index: Int,
    isChecked: Boolean? = null,
    enabled: Boolean = true
) {
    val staggerDelay = 100L * index
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(staggerDelay)
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut()
    ) {
        Column {
            if (isChecked != null) {
                PreferenceSwitch(
                    title = title,
                    description = description,
                    icon = icon,
                    isChecked = isChecked,
                    onClick = onClick,
                    enabled = enabled
                )
            } else {
                ClickablePreferenceItem(
                    title = title,
                    description = description,
                    icon = icon,
                    onClick = onClick,
                    enabled = enabled
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .height(0.5.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}

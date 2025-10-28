package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextCenterAlignedTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var showDebugText by remember { mutableStateOf(false) }
    
    // Multiple smooth infinite animations for premium effects
    val infiniteTransition = rememberInfiniteTransition(label = "premiumTopBarAnimations")
    
    // Primary gradient animation
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Secondary shimmer animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    // Floating glassmorphism animation
    val floatingEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Breathing animation for depth
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    Column(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        // Premium Animated Glassmorphism TopBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .graphicsLayer {
                    scaleX = breathingScale
                    scaleY = breathingScale
                }
        ) {
            // Base background with animated gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ),
                            start = Offset(gradientShift * 300, 0f),
                            end = Offset((1f - gradientShift) * 300, 100f)
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Animated glassmorphism overlay with floating effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()),
                                Color.White.copy(alpha = 0.08f + (sin(floatingEffect * Math.PI * 1.5) * 0.03f).toFloat()),
                                Color.Transparent
                            ),
                            center = Offset(
                                150f + (sin(floatingEffect * Math.PI * 2) * 50f).toFloat(),
                                36f + (sin(floatingEffect * Math.PI * 1.5) * 10f).toFloat()
                            ),
                            radius = 120f + (sin(floatingEffect * Math.PI) * 20f).toFloat()
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Shimmer effect overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerOffset - 50f, 0f),
                            end = Offset(shimmerOffset + 50f, 72f)
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Content layer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern Navigation Icon with animated container
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    radius = 25f + (sin(floatingEffect * Math.PI) * 5f).toFloat()
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        navigationIcon()
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Enhanced title with subtle glow effect
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            alpha = 0.95f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Modern Actions with animated container
                if (actions != {}) {
                    Surface(
                        modifier = Modifier.height(40.dp),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        radius = 25f + (sin(floatingEffect * Math.PI * 1.3) * 5f).toFloat()
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actions()
                        }
                    }
                }
            }
        }
        
        // Enhanced debug text with premium styling
        AnimatedVisibility(
            visible = showDebugText,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "🎯 Developer Mode",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextCenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var showDebugText by remember { mutableStateOf(false) }
    
    // Multiple smooth infinite animations for premium effects
    val infiniteTransition = rememberInfiniteTransition(label = "premiumTopBarAnimations")
    
    // Primary gradient animation
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Secondary shimmer animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    // Floating glassmorphism animation
    val floatingEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Breathing animation for depth
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    Column(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
    ) {
        // Premium Animated Glassmorphism TopBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .graphicsLayer {
                    scaleX = breathingScale
                    scaleY = breathingScale
                }
        ) {
            // Base background with animated gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ),
                            start = Offset(gradientShift * 300, 0f),
                            end = Offset((1f - gradientShift) * 300, 100f)
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Animated glassmorphism overlay with floating effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()),
                                Color.White.copy(alpha = 0.08f + (sin(floatingEffect * Math.PI * 1.5) * 0.03f).toFloat()),
                                Color.Transparent
                            ),
                            center = Offset(
                                150f + (sin(floatingEffect * Math.PI * 2) * 50f).toFloat(),
                                36f + (sin(floatingEffect * Math.PI * 1.5) * 10f).toFloat()
                            ),
                            radius = 120f + (sin(floatingEffect * Math.PI) * 20f).toFloat()
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Shimmer effect overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerOffset - 50f, 0f),
                            end = Offset(shimmerOffset + 50f, 72f)
                        )
                    )
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            
            // Content layer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern Navigation Icon with animated container
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    radius = 25f + (sin(floatingEffect * Math.PI) * 5f).toFloat()
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        navigationIcon()
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Custom title composable with subtle animation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            alpha = 0.95f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    title()
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Modern Actions with animated container
                if (actions != {}) {
                    Surface(
                        modifier = Modifier.height(40.dp),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        radius = 25f + (sin(floatingEffect * Math.PI * 1.3) * 5f).toFloat()
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actions()
                        }
                    }
                }
            }
        }
        
        // Enhanced debug text with premium styling
        AnimatedVisibility(
            visible = showDebugText,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "🎯 Developer Mode",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    // Multiple smooth infinite animations for premium effects
    val infiniteTransition = rememberInfiniteTransition(label = "premiumTopBarAnimations")
    
    // Primary gradient animation
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    
    // Secondary shimmer animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    // Floating glassmorphism animation
    val floatingEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Breathing animation for depth
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .height(72.dp)
            .graphicsLayer {
                scaleX = breathingScale
                scaleY = breathingScale
            }
    ) {
        // Base background with animated gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        start = Offset(gradientShift * 300, 0f),
                        end = Offset((1f - gradientShift) * 300, 100f)
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )
        
        // Animated glassmorphism overlay with floating effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()),
                            Color.White.copy(alpha = 0.08f + (sin(floatingEffect * Math.PI * 1.5) * 0.03f).toFloat()),
                            Color.Transparent
                        ),
                        center = Offset(
                            150f + (sin(floatingEffect * Math.PI * 2) * 50f).toFloat(),
                            36f + (sin(floatingEffect * Math.PI * 1.5) * 10f).toFloat()
                        ),
                        radius = 120f + (sin(floatingEffect * Math.PI) * 20f).toFloat()
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )
        
        // Shimmer effect overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset - 50f, 0f),
                        end = Offset(shimmerOffset + 50f, 72f)
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )
        
        // Content layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Modern Navigation Icon with animated container
            Surface(
                modifier = Modifier.size(40.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                radius = 25f + (sin(floatingEffect * Math.PI) * 5f).toFloat()
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    navigationIcon()
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Enhanced title with subtle glow effect
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = 0.95f + (sin(floatingEffect * Math.PI) * 0.05f).toFloat()
                    }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Modern Actions with animated container
            if (actions != {}) {
                Surface(
                    modifier = Modifier.height(40.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    radius = 25f + (sin(floatingEffect * Math.PI * 1.3) * 5f).toFloat()
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun NextPlayerMainTopAppBarPreview() {
    NextCenterAlignedTopAppBar(
        title = "ZukuPlay",
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More",
                )
            }
        },
    )
}

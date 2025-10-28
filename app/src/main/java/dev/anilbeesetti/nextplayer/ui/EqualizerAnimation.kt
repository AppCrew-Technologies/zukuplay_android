package dev.anilbeesetti.nextplayer.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerAnimation(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    spacing: Dp = 2.dp,
    maxHeight: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    // Create multiple bar animations with different delays and durations
    val barHeights = (0 until barCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (300..700).random(),
                    easing = FastOutSlowInEasing,
                    delayMillis = index * 100
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }
    
    Box(
        modifier = modifier
            .size(
                width = (barWidth * barCount) + (spacing * (barCount - 1)),
                height = maxHeight
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (isPlaying) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawEqualizer(
                    barHeights = barHeights.map { it.value },
                    barCount = barCount,
                    barWidth = barWidth.toPx(),
                    spacing = spacing.toPx(),
                    maxHeight = maxHeight.toPx(),
                    color = color
                )
            }
        } else {
            // Static bars when not playing
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawEqualizer(
                    barHeights = List(barCount) { 0.3f },
                    barCount = barCount,
                    barWidth = barWidth.toPx(),
                    spacing = spacing.toPx(),
                    maxHeight = maxHeight.toPx(),
                    color = color.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun DrawScope.drawEqualizer(
    barHeights: List<Float>,
    barCount: Int,
    barWidth: Float,
    spacing: Float,
    maxHeight: Float,
    color: Color
) {
    for (i in 0 until barCount) {
        val x = i * (barWidth + spacing)
        val barHeight = maxHeight * barHeights.getOrElse(i) { 0.3f }
        val y = maxHeight - barHeight
        
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
    }
}

@Composable
fun AudioVisualizerIcon(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        EqualizerAnimation(
            isPlaying = isPlaying,
            barCount = 4,
            barWidth = 2.dp,
            spacing = 1.5.dp,
            maxHeight = size * 0.7f,
            color = color
        )
    }
} 
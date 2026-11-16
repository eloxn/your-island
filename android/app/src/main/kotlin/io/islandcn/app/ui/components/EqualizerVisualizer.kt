package io.islandcn.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.islandcn.app.ui.theme.NeonGreen

@Composable
fun EqualizerVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 16.dp,
    minHeight: Dp = 4.dp,
    barColor: Color = NeonGreen,
    isPlaying: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val animations = (0 until barCount).map { index ->
        val duration = 400 + (index * 130) % 350
        val delay = index * 80
        val targetFactor = if (isPlaying) 1f else 0.2f

        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = targetFactor,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animations.forEach { anim ->
            val scale by anim
            val currentHeight = minHeight + (maxHeight - minHeight) * scale
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentHeight)
                    .background(barColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

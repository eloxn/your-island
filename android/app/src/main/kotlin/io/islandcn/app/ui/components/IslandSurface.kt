package io.islandcn.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.islandcn.app.data.CutoutPosition
import io.islandcn.app.data.IslandConfig
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandExpansion
import io.islandcn.app.data.IslandUiState
import io.islandcn.app.ui.theme.NeonAmber
import io.islandcn.app.ui.theme.NeonCyan
import io.islandcn.app.ui.theme.NeonGreen
import io.islandcn.app.ui.theme.NeonIndigo
import io.islandcn.app.ui.theme.NeonViolet
import io.islandcn.app.ui.theme.OledBlack

@Composable
fun IslandSurface(
    uiState: IslandUiState,
    modifier: Modifier = Modifier,
    onToggleExpand: () -> Unit = {},
    onDismissEvent: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val config = uiState.config
    val activeEvent = uiState.activeEvent
    val secondaryEvent = uiState.secondaryEvent
    val isExpanded = uiState.expansion == IslandExpansion.EXPANDED

    // Calculate dimensions based on state
    val targetWidth = when {
        isExpanded -> config.expandedWidth.dp
        activeEvent != null -> (config.idleWidth * 1.55f).dp
        else -> config.idleWidth.dp
    }

    val targetHeight = when {
        isExpanded -> config.expandedHeight.dp
        else -> config.idleHeight.dp
    }

    // Spring physics animation (tuned to match Apple Dynamic Island natural bounce)
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 380f
        ),
        label = "island_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 380f
        ),
        label = "island_height"
    )

    val cornerRadius = if (isExpanded) 32.dp else (config.idleHeight / 2).dp

    // Ambient glow color
    val glowColor = when (activeEvent) {
        is IslandEvent.Battery -> NeonGreen
        is IslandEvent.Media -> NeonViolet
        is IslandEvent.Notification -> NeonCyan
        is IslandEvent.Call -> NeonGreen
        is IslandEvent.Timer -> NeonAmber
        else -> NeonIndigo
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Island Pill Container
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Optional ambient back-glow
            if (config.ambientGlow && activeEvent != null) {
                Box(
                    modifier = Modifier
                        .size(width = animatedWidth + 12.dp, height = animatedHeight + 10.dp)
                        .clip(RoundedCornerShape(cornerRadius + 4.dp))
                        .background(glowColor.copy(alpha = if (isExpanded) 0.28f else 0.18f))
                        .blur(16.dp)
                )
            }

            // Solid Island Surface
            Box(
                modifier = Modifier
                    .size(width = animatedWidth, height = animatedHeight)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(OledBlack)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x35FFFFFF),
                                Color(0x08FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onToggleExpand() },
                            onLongPress = { onLongPress() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activeEvent,
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith
                                fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    },
                    label = "island_content"
                ) { event ->
                    when (event) {
                        is IslandEvent.Media -> MusicIslandView(
                            event = event,
                            expansion = uiState.expansion
                        )
                        is IslandEvent.Battery -> BatteryIslandView(
                            event = event,
                            expansion = uiState.expansion
                        )
                        is IslandEvent.Notification -> NotificationIslandView(
                            event = event,
                            expansion = uiState.expansion,
                            onDismiss = onDismissEvent
                        )
                        is IslandEvent.Call -> CallIslandView(
                            event = event,
                            expansion = uiState.expansion
                        )
                        is IslandEvent.Timer -> TimerIslandView(
                            event = event,
                            expansion = uiState.expansion
                        )
                        else -> {
                            // Idle resting notch placeholder (e.g. camera hole)
                            Box(
                                modifier = Modifier
                                    .size(width = 12.dp, height = 12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF161822))
                            )
                        }
                    }
                }
            }
        }

        // Secondary Split Island Bubble (for dual activities: e.g. Music + Timer)
        if (secondaryEvent != null && !isExpanded) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(config.idleHeight.dp)
                    .clip(CircleShape)
                    .background(OledBlack)
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = CircleShape
                    )
                    .clickable { onToggleExpand() },
                contentAlignment = Alignment.Center
            ) {
                when (secondaryEvent) {
                    is IslandEvent.Media -> {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    is IslandEvent.Timer -> {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                    }
                }
            }
        }
    }
}

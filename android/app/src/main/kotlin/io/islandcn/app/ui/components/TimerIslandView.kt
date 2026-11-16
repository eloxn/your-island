package io.islandcn.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandExpansion
import io.islandcn.app.ui.theme.NeonAmber
import io.islandcn.app.ui.theme.TextPrimary
import io.islandcn.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TimerIslandView(
    event: IslandEvent.Timer,
    expansion: IslandExpansion
) {
    val progress = if (event.totalSeconds > 0) {
        event.remainingSeconds.toFloat() / event.totalSeconds.toFloat()
    } else 0f

    val minutes = event.remainingSeconds / 60
    val seconds = event.remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    if (expansion == IslandExpansion.COMPACT) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.HourglassBottom,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = timeFormatted,
                color = NeonAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (event.isRunning) "Counting down..." else "Paused",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(50.dp),
                    color = NeonAmber,
                    trackColor = Color(0xFF332B1A),
                    strokeWidth = 4.dp
                )
                Text(
                    text = timeFormatted,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

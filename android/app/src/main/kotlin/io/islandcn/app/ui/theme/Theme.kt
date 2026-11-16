package io.islandcn.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IslandColorScheme = darkColorScheme(
    primary = NeonIndigo,
    primaryContainer = NeonViolet,
    secondary = NeonCyan,
    tertiary = NeonGreen,
    background = IslandBgDark,
    surface = IslandSurfaceDark,
    surfaceVariant = IslandCardDark,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun IslandCNTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IslandColorScheme,
        typography = Typography,
        content = content
    )
}

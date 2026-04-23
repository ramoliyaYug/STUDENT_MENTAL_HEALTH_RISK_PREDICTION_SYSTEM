package yug.ramoliya.ojtapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Single, always-light colour scheme ───────────────────────────────── //
private val AppColorScheme = lightColorScheme(
    primary          = BrandPurple,
    onPrimary        = Color.White,
    secondary        = BrandTeal,
    onSecondary      = Color.White,
    background       = LightBackground,
    onBackground     = LightTextMain,
    surface          = LightSurface,
    onSurface        = LightTextMain,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightTextMuted,
    outline          = LightBorder,
)

@Composable
fun OjtappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content,
    )
}
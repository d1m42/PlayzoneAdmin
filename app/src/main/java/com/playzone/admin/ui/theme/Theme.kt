package com.playzone.admin.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = PurplePrimary,
    onPrimary        = Color.White,
    primaryContainer = PurpleLight,
    secondary        = GoldAccent,
    onSecondary      = Color(0xFF1A1200),
    background       = DarkBackground,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = CardDark,
    error            = RedCancel,
    outline          = Color(0xFF4A4468)
)

@Composable
fun PlayzoneAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}

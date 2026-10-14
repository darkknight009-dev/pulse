package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PayPulseColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBg,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonTeal,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = NeonTeal,
    tertiary = NeonPurple,
    onTertiary = TextPrimary,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = NeonCoral,
    onError = TextPrimary
)

@Composable
fun PayPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PayPulseColorScheme,
        typography = AppTypography,
        content = content
    )
}

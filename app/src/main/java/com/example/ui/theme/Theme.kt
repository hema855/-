package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DaylightColorScheme = lightColorScheme(
    primary = VolcanicOrange,
    onPrimary = PureWhite,
    primaryContainer = VolcanicOrangeContainer,
    onPrimaryContainer = VolcanicOrangeDark,
    secondary = Slate700,
    onSecondary = PureWhite,
    secondaryContainer = Slate200,
    onSecondaryContainer = Slate900,
    tertiary = EmeraldSafe,
    onTertiary = PureWhite,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldDark,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = Slate50,
    surfaceContainer = Slate100,
    surfaceContainerHigh = Slate200,
    outline = Slate300,
    outlineVariant = Slate200,
    error = DangerRed,
    onError = PureWhite,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRedDark
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TashteebTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DaylightColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

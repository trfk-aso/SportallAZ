package com.sportall.az.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SportallDarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    secondary = LimeGreen,
    tertiary = Gold,
    background = DarkBlue,
    surface = SurfaceBlue,
    surfaceVariant = DrillCardBlue,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextOnBlue,
    onSurface = TextOnBlue,
    primaryContainer = DrillCardBlue,
    secondaryContainer = LimeGreen.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = LimeGreen
)

private val SportallLightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = LimeGreen,
    tertiary = Gold,
    background = DarkBlue,
    surface = SurfaceBlue,
    surfaceVariant = DrillCardBlue,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextOnBlue,
    onSurface = TextOnBlue,
    primaryContainer = DrillCardBlue,
    secondaryContainer = LimeGreen.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = LimeGreen
)

@Composable
fun SportallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> SportallDarkColorScheme
        else -> SportallLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

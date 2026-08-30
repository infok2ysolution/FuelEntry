package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FuelGreenPrimary,
    onPrimary = FuelGreenOnPrimary,
    primaryContainer = FuelGreenOnContainer,
    onPrimaryContainer = FuelGreenContainer,
    secondary = FuelGoldSecondary,
    onSecondary = FuelGoldOnSecondary,
    secondaryContainer = FuelGoldOnContainer,
    onSecondaryContainer = FuelGoldContainer,
    tertiary = FuelPetrolTertiary,
    onTertiary = FuelPetrolOnTertiary,
    background = FuelDarkBackground,
    surface = FuelDarkSurface,
    surfaceVariant = FuelDarkSurfaceVariant,
    onBackground = FuelDarkOnSurface,
    onSurface = FuelDarkOnSurface,
    outline = FuelDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = FuelGreenPrimary,
    onPrimary = FuelGreenOnPrimary,
    primaryContainer = FuelGreenContainer,
    onPrimaryContainer = FuelGreenOnContainer,
    secondary = FuelGoldSecondary,
    onSecondary = FuelGoldOnSecondary,
    secondaryContainer = FuelGoldContainer,
    onSecondaryContainer = FuelGoldOnContainer,
    tertiary = FuelPetrolTertiary,
    onTertiary = FuelPetrolOnTertiary,
    background = FuelLightBackground,
    surface = FuelLightSurface,
    surfaceVariant = FuelLightSurfaceVariant,
    onBackground = FuelLightOnSurface,
    onSurface = FuelLightOnSurface,
    outline = FuelLightOutline
)

@Composable
fun FuelRecordTheme(
    darkTheme: Boolean = false, // Default to clean white background theme requested
    dynamicColor: Boolean = false, // Use our tailored branded petroleum colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

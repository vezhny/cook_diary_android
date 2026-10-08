package com.vezhny.cookdiary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Palette from the launcher icon: green cookbook, wooden spoon, tomato, warm beige paper.

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6B45),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4F0C2),
    onPrimaryContainer = Color(0xFF00210E),
    inversePrimary = Color(0xFF98D6A8),
    secondary = Color(0xFF7A5734),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCBE),
    onSecondaryContainer = Color(0xFF2C1600),
    tertiary = Color(0xFFA63B2A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD3),
    onTertiaryContainer = Color(0xFF3E0500),
    background = Color(0xFFFFF8F1),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFF8F1),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFEFE0CF),
    onSurfaceVariant = Color(0xFF4F4539),
    surfaceTint = Color(0xFF2E6B45),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF2E8),
    surfaceContainer = Color(0xFFF5ECE2),
    surfaceContainerHigh = Color(0xFFEFE6DC),
    surfaceContainerHighest = Color(0xFFEAE1D7),
    inverseSurface = Color(0xFF353029),
    inverseOnSurface = Color(0xFFF9EFE7),
    outline = Color(0xFF817567),
    outlineVariant = Color(0xFFD3C4B4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF98D6A8),
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF12522F),
    onPrimaryContainer = Color(0xFFB4F0C2),
    inversePrimary = Color(0xFF2E6B45),
    secondary = Color(0xFFEBBE91),
    onSecondary = Color(0xFF452B0B),
    secondaryContainer = Color(0xFF5F4020),
    onSecondaryContainer = Color(0xFFFFDCBE),
    tertiary = Color(0xFFFFB4A6),
    onTertiary = Color(0xFF651108),
    tertiaryContainer = Color(0xFF842617),
    onTertiaryContainer = Color(0xFFFFDAD3),
    background = Color(0xFF17130E),
    onBackground = Color(0xFFECE1D6),
    surface = Color(0xFF17130E),
    onSurface = Color(0xFFECE1D6),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    surfaceTint = Color(0xFF98D6A8),
    surfaceContainerLowest = Color(0xFF120E09),
    surfaceContainerLow = Color(0xFF201B16),
    surfaceContainer = Color(0xFF241F1A),
    surfaceContainerHigh = Color(0xFF2F2924),
    surfaceContainerHighest = Color(0xFF3A342E),
    inverseSurface = Color(0xFFECE1D6),
    inverseOnSurface = Color(0xFF353029),
    outline = Color(0xFF9C8E80),
    outlineVariant = Color(0xFF4F4539),
)

/** Red of a dish cooked today in the "what to cook" list; fades as the dish cools down. */
val HotRed = Color(0xFFD32F2F)

@Composable
fun CookDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: the brand palette is part of the app's look.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

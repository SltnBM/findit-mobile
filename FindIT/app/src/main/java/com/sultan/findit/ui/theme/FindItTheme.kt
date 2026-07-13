package com.sultan.findit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FindItBlue = Color(0xFF2563EB)
val FindItBlueDark = Color(0xFF1E40AF)
val FindItSky = Color(0xFFEFF6FF)
val FindItSurface = Color(0xFFF8FAFC)
val FindItText = Color(0xFF0F172A)
val FindItMuted = Color(0xFF64748B)
val FindItSuccess = Color(0xFF16A34A)
val FindItWarning = Color(0xFFF59E0B)
val FindItDanger = Color(0xFFDC2626)
val FindItSurfaceDark = Color(0xFF0F172A)
val FindItCardDark = Color(0xFF1E293B)
val FindItTextDark = Color(0xFFF8FAFC)
val FindItMutedDark = Color(0xFF94A3B8)
val FindItBlueSoftDark = Color(0xFF3B82F6)
val FindItSkyDark = Color(0xFF1E3A8A)

private val LightColors: ColorScheme = lightColorScheme(
    primary = FindItBlue,
    onPrimary = Color.White,
    primaryContainer = FindItSky,
    onPrimaryContainer = FindItBlueDark,

    secondary = FindItBlueDark,
    onSecondary = Color.White,
    secondaryContainer = FindItSky,
    onSecondaryContainer = FindItBlueDark,

    tertiary = FindItBlue,
    onTertiary = Color.White,
    tertiaryContainer = FindItSky,
    onTertiaryContainer = FindItBlueDark,

    background = FindItSurface,
    onBackground = FindItText,

    surface = Color.White,
    onSurface = FindItText,
    surfaceVariant = FindItSurface,
    onSurfaceVariant = FindItMuted,

    outline = Color(0xFFE2E8F0),

    error = FindItDanger,
    onError = Color.White
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = FindItBlueSoftDark,
    onPrimary = Color.White,
    primaryContainer = FindItSkyDark,
    onPrimaryContainer = Color.White,

    secondary = FindItBlueSoftDark,
    onSecondary = Color.White,
    secondaryContainer = FindItSkyDark,
    onSecondaryContainer = Color.White,

    tertiary = FindItBlueSoftDark,
    onTertiary = Color.White,
    tertiaryContainer = FindItSkyDark,
    onTertiaryContainer = Color.White,

    background = FindItSurfaceDark,
    onBackground = FindItTextDark,

    surface = FindItCardDark,
    onSurface = FindItTextDark,
    surfaceVariant = FindItSurfaceDark,
    onSurfaceVariant = FindItMutedDark,

    outline = Color(0xFF334155),

    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun FindItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
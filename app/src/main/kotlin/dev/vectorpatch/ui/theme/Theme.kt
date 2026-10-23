package dev.vectorpatch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// VectorPatch brand palette: deep violet + electric blue
private val VectorViolet       = Color(0xFF6A35FF)
private val VectorVioletDark   = Color(0xFF4A15DF)
private val VectorBlue         = Color(0xFF00BFFF)
private val VectorBackground   = Color(0xFF0F0F1A)
private val VectorSurface      = Color(0xFF1A1A2E)
private val VectorOnSurface    = Color(0xFFE8E8FF)

private val DarkColorScheme = darkColorScheme(
    primary            = VectorViolet,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF3A1FAF),
    onPrimaryContainer = Color(0xFFE0DDFF),
    secondary          = VectorBlue,
    onSecondary        = Color.Black,
    secondaryContainer = Color(0xFF003A4F),
    onSecondaryContainer = Color(0xFFB8EEFF),
    tertiary           = Color(0xFFFF6B9D),
    background         = VectorBackground,
    onBackground       = VectorOnSurface,
    surface            = VectorSurface,
    onSurface          = VectorOnSurface,
    surfaceVariant     = Color(0xFF252540),
    outline            = Color(0xFF6060A0),
    error              = Color(0xFFFF5252),
    onError            = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary            = VectorVioletDark,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF1A0080),
    secondary          = Color(0xFF0086B3),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFCCF0FF),
    onSecondaryContainer = Color(0xFF001E2B),
    tertiary           = Color(0xFFBF0060),
    background         = Color(0xFFF8F8FF),
    onBackground       = Color(0xFF1A1A2E),
    surface            = Color.White,
    onSurface          = Color(0xFF1A1A2E),
    surfaceVariant     = Color(0xFFEEEEFF),
    outline            = Color(0xFF8080C0),
    error              = Color(0xFFB00020),
    onError            = Color.White
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun VectorPatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = VectorPatchTypography, content = content)
}

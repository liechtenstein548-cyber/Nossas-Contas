package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = OnElectricBlue,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = ElectricBlueDark,
    secondary = LightTextSecondary,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceSubtle,
    onSecondaryContainer = LightTextPrimary,
    tertiary = SuccessEmerald,
    onTertiary = OnElectricBlue,
    tertiaryContainer = SuccessContainer,
    onTertiaryContainer = SuccessEmerald,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSubtle,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.5f),
    error = RosePriority,
    onError = OnElectricBlue,
    errorContainer = RoseContainer,
    onErrorContainer = RosePriority
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = OnElectricBlue,
    primaryContainer = Color(0xFF142D59),
    onPrimaryContainer = ElectricBlueLight,
    secondary = DarkTextSecondary,
    onSecondary = DarkSurface,
    secondaryContainer = DarkSurfaceSubtle,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = SuccessEmerald,
    onTertiary = DarkBg,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = SuccessEmerald,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f),
    error = RosePriority,
    onError = DarkTextPrimary,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = RosePriority
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

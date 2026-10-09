package com.simonecompany.lavagna.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Palette identica a quella della web app (Material 3 blue + neutri Google).
val LavagnaBlue = Color(0xFF0B57D0)
val LavagnaBlueDark = Color(0xFF0842A0)
val LavagnaBlueLight = Color(0xFFC2E7FF)
val LavagnaOnBlueLight = Color(0xFF001D35)
val LavagnaSurface = Color(0xFFFFFFFF)
val LavagnaSurfaceDim = Color(0xFFF8F9FA)
val LavagnaOutline = Color(0xFFE0E0E0)
val LavagnaTextPrimary = Color(0xFF1F1F1F)
val LavagnaTextSecondary = Color(0xFF444746)
val LavagnaDanger = Color(0xFFB3261E)

private val LightScheme = lightColorScheme(
    primary = LavagnaBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = LavagnaBlue,
    onSecondary = Color.White,
    secondaryContainer = LavagnaBlueLight,
    onSecondaryContainer = LavagnaOnBlueLight,
    tertiary = Color(0xFF00639B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFE5FF),
    onTertiaryContainer = Color(0xFF001D31),
    background = LavagnaSurfaceDim,
    onBackground = LavagnaTextPrimary,
    surface = LavagnaSurface,
    onSurface = LavagnaTextPrimary,
    surfaceVariant = Color(0xFFF1F3F7),
    onSurfaceVariant = LavagnaTextSecondary,
    surfaceContainer = Color(0xFFF7F8FA),
    surfaceContainerHigh = Color(0xFFF1F2F4),
    outline = Color(0xFF74777F),
    outlineVariant = LavagnaOutline,
    error = LavagnaDanger,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFA8C7FF),
)

/** La web app e' sempre in tema chiaro: idem su Android. */
@Composable
fun LavagnaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(colorScheme = LightScheme, content = content)
}
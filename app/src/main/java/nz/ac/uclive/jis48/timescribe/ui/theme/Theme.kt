package nz.ac.uclive.jis48.timescribe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.screens.settings.SettingsScreen

@Composable
fun TimeScribeTheme(darkModeState: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val darkColorPalette = darkColors(
        primary = softBlue,
        primaryVariant = darkBlue,
        secondary = gentleOrange,
        background = black,
        surface = black,
        onPrimary = white,
        onSecondary = black
    )

    val lightColorPalette = lightColors(
        primary = lightPurple,
        primaryVariant = darkPurple,
        secondary = teal,
        background = white,
        surface = white,
        onPrimary = white,
        onSecondary = black
    )


    val colors = if (darkModeState) {
        darkColorPalette
    } else {
        lightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
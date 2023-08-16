package nz.ac.uclive.jis48.timescribe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun TimeScribeTheme(darkModeState: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val darkColorPalette = darkColors(
        primary = softBlue,
        primaryVariant = darkerBlue,
        secondary = yellow,
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
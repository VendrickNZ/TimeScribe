package nz.ac.uclive.jis48.timescribe.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun TimeScribeTheme(
    darkModeState: Boolean,
    content: @Composable () -> Unit
) {
    val darkColorPalette = darkColors(
        primary = DarkPrimary,
        primaryVariant = DarkPrimaryVariant,
        secondary = DarkSecondary,
        secondaryVariant = DarkSecondaryVariant,
        background = DarkBackground,
        surface = DarkSurface,
        error = ErrorColor,
        onPrimary = DarkOnPrimary,
        onSecondary = DarkOnSecondary,
        onBackground = DarkOnBackground,
        onSurface = DarkOnSurface,
        onError = OnErrorColor
    )

    val lightColorPalette = lightColors(
        primary = LightPrimary,
        primaryVariant = LightPrimaryVariant,
        secondary = LightSecondary,
        secondaryVariant = LightSecondaryVariant,
        background = LightBackground,
        surface = LightSurface,
        error = ErrorColor,
        onPrimary = LightOnPrimary,
        onSecondary = LightOnSecondary,
        onBackground = LightOnBackground,
        onSurface = LightOnSurface,
        onError = OnErrorColor
    )

    val colors = if (darkModeState) darkColorPalette else lightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

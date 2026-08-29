package sv.edu.utec.etps1.registroincidencias.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurple,
    onPrimary = ColorWhite,

    secondary = DarkPurpleLight,
    onSecondary = ColorWhite,

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,

    outline = DarkPurple
)

private val LightColorScheme = lightColorScheme(
    primary = LightPurple,
    onPrimary = ColorWhite,

    secondary = LightPurpleDark,
    onSecondary = ColorWhite,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    outline = LightPurple
)

@Composable
fun RegistroIncidenciasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
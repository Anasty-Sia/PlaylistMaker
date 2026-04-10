package com.example.playlistmaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.core.view.WindowCompat

val BlueSurface = Color(0xFF3772E7)
val LightGray = Color(0xFFE6E8EB)
val DarkGray = Color(0xFF4A4A4A)

val AlmostWhite = Color(0xFFFFFFFF)
val AlmostBlack = Color(0xFF1A1B22)

val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)


val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Composable
fun PlaylistMakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = BlueSurface,
            onPrimary = AlmostWhite,
            primaryContainer = BlueSurface.copy(alpha = 0.2f),
            onPrimaryContainer = AlmostWhite,
            secondary = PurpleGrey80,
            onSecondary = AlmostBlack,
            secondaryContainer = PurpleGrey80.copy(alpha = 0.2f),
            onSecondaryContainer = AlmostBlack,
            tertiary = Pink80,
            onTertiary = AlmostBlack,
            background = AlmostBlack,
            onBackground = AlmostWhite,
            surface = AlmostBlack,
            onSurface = AlmostWhite,
            surfaceVariant = DarkGray,
            onSurfaceVariant = LightGray,
            error = Color(0xFFCF6679),
            onError = AlmostBlack
        )
    } else {
        lightColorScheme(
            primary = BlueSurface,
            onPrimary = AlmostWhite,
            primaryContainer = BlueSurface.copy(alpha = 0.1f),
            onPrimaryContainer = BlueSurface,
            secondary = PurpleGrey40,
            onSecondary = AlmostWhite,
            secondaryContainer = PurpleGrey40.copy(alpha = 0.1f),
            onSecondaryContainer = PurpleGrey40,
            tertiary = Pink40,
            onTertiary = AlmostWhite,
            background = AlmostWhite,
            onBackground = AlmostBlack,
            surface = AlmostWhite,
            onSurface = AlmostBlack,
            surfaceVariant = LightGray,
            onSurfaceVariant = DarkGray,
            error = Color(0xFFBA1A1A),
            onError = AlmostWhite
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(57f, TextUnitType.Sp)
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(45f, TextUnitType.Sp)
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(36f, TextUnitType.Sp)
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(32f, TextUnitType.Sp)
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(28f, TextUnitType.Sp)
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = TextUnit(24f, TextUnitType.Sp)
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(22f, TextUnitType.Sp)
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(16f, TextUnitType.Sp)
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(14f, TextUnitType.Sp)
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = TextUnit(16f, TextUnitType.Sp)
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = TextUnit(14f, TextUnitType.Sp)
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = TextUnit(12f, TextUnitType.Sp)
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(14f, TextUnitType.Sp)
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(12f, TextUnitType.Sp)
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = TextUnit(11f, TextUnitType.Sp)
    )
)
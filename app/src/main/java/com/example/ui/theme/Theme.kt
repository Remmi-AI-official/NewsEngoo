package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = EditorialNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F5),
    onPrimaryContainer = EditorialNavy,
    secondary = EditorialAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEEBC8),
    onSecondaryContainer = Color(0xFF7B341E),
    tertiary = EditorialBurgundy,
    onTertiary = Color.White,
    background = EditorialPaper,
    onBackground = EditorialInk,
    surface = EditorialSurface,
    onSurface = EditorialInk,
    surfaceVariant = EditorialPaperVariant,
    onSurfaceVariant = EditorialInkLight,
    outline = EditorialBorder,
    outlineVariant = Color(0xFFD5CEC2)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkNavy,
    onPrimary = Color(0xFF0D253E),
    primaryContainer = Color(0xFF1A365D),
    onPrimaryContainer = Color(0xFFDCE6F5),
    secondary = DarkAmber,
    onSecondary = Color(0xFF4C2108),
    secondaryContainer = Color(0xFF652B0F),
    onSecondaryContainer = Color(0xFFFEEBC8),
    tertiary = DarkBurgundy,
    onTertiary = Color(0xFF4A101D),
    background = DarkPaper,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkPaperVariant,
    onSurfaceVariant = DarkInkLight,
    outline = DarkBorder,
    outlineVariant = Color(0xFF3B4252)
)

@Composable
fun EditorialEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep editorial identity by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

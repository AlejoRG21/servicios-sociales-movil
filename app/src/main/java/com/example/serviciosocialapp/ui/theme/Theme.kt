package com.example.serviciosocialapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema de colores oscuros con la paleta de Figma
private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoDark,
    tertiary = IndigoLight,
    background = TextPrimary,
    surface = TextPrimary
)

// Esquema de colores claros con la paleta de Figma
private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoDark,
    tertiary = IndigoLight,
    background = AppBackground,
    surface = CardSurface
)

@Composable
fun ServicioSocialAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 'false' para que mantenga los colores de Figma y no los del fondo de pantalla
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
package com.sysadmindoc.guitartuner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkScheme = darkColorScheme(
    primary = Color(0xFF3DDC84),
    onPrimary = Color(0xFF001F10),
    secondary = Color(0xFF9BD4B3),
    onSecondary = Color(0xFF052414),
    tertiary = Color(0xFFFFC857),
    onTertiary = Color(0xFF2B1A00),
    background = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF101211),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFC8D0CA),
    outline = Color(0xFF65706A),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF006D3D),
    onPrimary = Color.White,
    secondary = Color(0xFF476653),
    onSecondary = Color.White,
    tertiary = Color(0xFF765A00),
    onTertiary = Color.White,
    background = Color(0xFFF8FBF7),
    onBackground = Color(0xFF151C18),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151C18),
    onSurfaceVariant = Color(0xFF3F4943),
    outline = Color(0xFF707971),
)

@Composable
fun GuitarTunerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) AmoledDarkScheme else LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}

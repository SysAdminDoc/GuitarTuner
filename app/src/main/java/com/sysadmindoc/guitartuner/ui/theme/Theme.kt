package com.sysadmindoc.guitartuner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sysadmindoc.guitartuner.settings.ThemeMode

private val AmoledDarkScheme = darkColorScheme(
    primary = Color(0xFF3DDC84),
    onPrimary = Color(0xFF001F10),
    primaryContainer = Color(0xFF0D2F1C),
    onPrimaryContainer = Color(0xFFD6F9E3),
    secondary = Color(0xFF8DCFA9),
    onSecondary = Color(0xFF052414),
    secondaryContainer = Color(0xFF102017),
    onSecondaryContainer = Color(0xFFD8EADB),
    tertiary = Color(0xFFFFC857),
    onTertiary = Color(0xFF2B1A00),
    tertiaryContainer = Color(0xFF3A2B08),
    onTertiaryContainer = Color(0xFFFFE6A6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF3B1111),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF050806),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFC4CEC6),
    surfaceVariant = Color(0xFF0D1711),
    inverseSurface = Color(0xFFE0E6DF),
    inverseOnSurface = Color(0xFF172019),
    outline = Color(0xFF516058),
    outlineVariant = Color(0xFF223026),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF006D3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F9E3),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF476653),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EADB),
    onSecondaryContainer = Color(0xFF052114),
    tertiary = Color(0xFF765A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE6A6),
    onTertiaryContainer = Color(0xFF251A00),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FBF7),
    onBackground = Color(0xFF151C18),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151C18),
    onSurfaceVariant = Color(0xFF3F4943),
    surfaceVariant = Color(0xFFE0E6DF),
    inverseSurface = Color(0xFF2A322D),
    inverseOnSurface = Color(0xFFEFF5EE),
    outline = Color(0xFF707971),
    outlineVariant = Color(0xFFC0C9C1),
)

private val GuitarTunerTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 92.sp,
        lineHeight = 96.sp,
        fontWeight = FontWeight.Bold,
    ),
    displaySmall = TextStyle(
        fontSize = 54.sp,
        lineHeight = 58.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)

@Composable
fun GuitarTunerTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }
    val colorScheme: ColorScheme = if (useDarkTheme) AmoledDarkScheme else LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GuitarTunerTypography,
        content = content,
    )
}

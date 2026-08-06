package com.aicoding.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aicoding.assistant.data.local.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0D9488),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF1E3A8A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF2DD4BF),
    background = Color(0xFF0B1220),
    surface = Color(0xFF111827),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    outline = Color(0xFF374151),
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF0F2E5C),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF2DD4BF),
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF10151F),
    outline = Color(0xFF1F2937),
)

@Composable
fun AiCodingTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= 31 -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == ThemeMode.AMOLED -> AmoledColors
        isDark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
package com.trendvibes.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

data class AppColors(
    val background: Color,
    val surface: Color,
    val border: Color,
    val borderWidth: Dp,
    val cardBackground: Color,
    val cardText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentRed: Color,
    val accentYellow: Color,
    val buttonBackground: Color,
    val buttonTextColor: Color,
    val isHighContrast: Boolean
)

val DefaultAppColors = AppColors(
    background = BrandBlack,
    surface = BrandGrayDark,
    border = BrandBorder,
    borderWidth = 2.dp,
    cardBackground = BrandWhite,
    cardText = BrandBlack,
    textPrimary = BrandWhite,
    textSecondary = BrandTextMuted,
    accentRed = BrandRed,
    accentYellow = BrandYellow,
    buttonBackground = BrandRed,
    buttonTextColor = BrandWhite,
    isHighContrast = false
)

val HighContrastAppColors = AppColors(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    border = Color(0xFFFACC15),
    borderWidth = 3.dp,
    cardBackground = Color(0xFF000000),
    cardText = Color(0xFFFFFFFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFFACC15),
    accentRed = Color(0xFFFF2222),
    accentYellow = Color(0xFFFACC15),
    buttonBackground = Color(0xFFFACC15),
    buttonTextColor = Color(0xFF000000),
    isHighContrast = true
)

val LocalAppColors = compositionLocalOf { DefaultAppColors }
val LocalHighContrast = compositionLocalOf { false }
val LocalDyslexiaFont = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = BrandRed,
    onPrimary = BrandWhite,
    secondary = BrandYellow,
    onSecondary = BrandBlack,
    background = BrandBlack,
    onBackground = BrandWhite,
    surface = BrandGrayDark,
    onSurface = BrandWhite
)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = BrandWhite,
    secondary = BrandYellow,
    onSecondary = BrandBlack,
    background = BrandWhite,
    onBackground = BrandBlack,
    surface = BrandGrayLight,
    onSurface = BrandBlack
)

@Composable
fun TrendVibesTheme(
    darkTheme: Boolean = true,
    highContrast: Boolean = false,
    dyslexiaFont: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val appColors = if (highContrast) HighContrastAppColors else DefaultAppColors

    val colorScheme = when {
        highContrast -> DarkColorScheme.copy(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFFFFF),
            primary = BrandYellow,
            onPrimary = BrandBlack
        )
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val baseDensity = LocalDensity.current
    val customDensity = remember(baseDensity.density, baseDensity.fontScale, fontScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * fontScale
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BrandBlack.toArgb()
                window.navigationBarColor = BrandBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalHighContrast provides highContrast,
        LocalDyslexiaFont provides dyslexiaFont,
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(dyslexiaFont),
            content = content
        )
    }
}

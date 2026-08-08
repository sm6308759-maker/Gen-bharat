package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureWhite,
    primaryContainer = SoftGrayBg,
    onPrimaryContainer = TextDark,
    secondary = SaffronPrimary,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFFFF7ED),
    onSecondaryContainer = SaffronAccent,
    tertiary = EmeraldGreen,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark,
    surfaceVariant = OffWhite,
    onSurfaceVariant = TextMuted,
    outline = CardBorder
)

@Composable
fun GenZBharatTheme(
    darkTheme: Boolean = false, // ALWAYS White / Light theme per instructions
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AINewsIndiaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = GenZBharatTheme(darkTheme, dynamicColor, content)



package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** نفس ألوان دفتر المعلم (لوحة Bento البنفسجية) حتى تكون الواجهات متطابقة. */
private val Light = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF21005D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF21005D),
    tertiary = Color(0xFF7D5260),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFDF8F6),
    onBackground = Color(0xFF1D1B20),
    surface = Color.White,
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),
    outlineVariant = Color(0xFFE7E0EC),
    error = Color(0xFFB3261E)
)

@Suppress("unused")
private val Dark = darkColorScheme(
    primary = Color(0xFF9DB9EA),
    onPrimary = Color(0xFF0E2447),
    primaryContainer = Color(0xFF243F6E),
    onPrimaryContainer = Color(0xFFDCE6F7),
    secondary = Color(0xFF7FD1A6),
    secondaryContainer = Color(0xFF14492F),
    onSecondaryContainer = Color(0xFFD5EFE1),
    tertiary = Color(0xFFE8C468),
    tertiaryContainer = Color(0xFF4A3A0E),
    background = Color(0xFF10141B),
    surface = Color(0xFF181D26),
    surfaceVariant = Color(0xFF222936),
    onSurfaceVariant = Color(0xFFB9C2D3),
    outline = Color(0xFF465166)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val base = Typography()
private val AppTypography = base.copy(
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
)

@Composable
fun DaftarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Light,  // مثل دفتر المعلم: ألوان فاتحة دائمًا
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}

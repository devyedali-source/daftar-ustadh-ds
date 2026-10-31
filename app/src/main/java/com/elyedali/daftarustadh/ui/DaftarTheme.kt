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

/** ألوان هادئة رسمية: أزرق داكن (لون الوثائق الرسمية) مع أخضر موريتاني للنجاح والإنجاز. */
private val Light = lightColorScheme(
    primary = Color(0xFF1B3A6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F7),
    onPrimaryContainer = Color(0xFF0E2447),
    secondary = Color(0xFF1E7A4F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5EFE1),
    onSecondaryContainer = Color(0xFF0B3D25),
    tertiary = Color(0xFFB8860B),
    tertiaryContainer = Color(0xFFFFF1CC),
    background = Color(0xFFF3F5F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8ECF3),
    onSurfaceVariant = Color(0xFF4A5468),
    outline = Color(0xFFB9C2D3),
    error = Color(0xFFB3261E)
)

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
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}

package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(primary = Color(0xFF1B4F8A), secondary = Color(0xFF2E7D5B))
private val Dark = darkColorScheme(primary = Color(0xFF8FB8E8), secondary = Color(0xFF7CC9A4))

@Composable
fun DaftarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}

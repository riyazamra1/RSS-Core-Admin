package com.riyaz.rsscoreadmin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFD4AF37)
private val Dark = Color(0xFF090909)

private val DarkScheme = darkColorScheme(primary = Gold, onPrimary = Color.Black, background = Dark, surface = Color(0xFF121212), surfaceVariant = Color(0xFF1B1B1B))
private val LightScheme = lightColorScheme(primary = Color(0xFF8A6A00), background = Color(0xFFF7F7F5), surface = Color.White)

@Composable fun RssCoreAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme, typography = Typography(), content = content)
}
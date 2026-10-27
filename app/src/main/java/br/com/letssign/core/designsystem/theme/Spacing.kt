package br.com.letssign.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de espaçamento oficial do LetsSign:
 * esp-4, esp-8, esp-12, esp-16, esp-24, esp-32 (dp)
 */
@Immutable
data class LetsSignSpacing(
    val esp4: Dp = 4.dp,
    val esp8: Dp = 8.dp,
    val esp12: Dp = 12.dp,
    val esp16: Dp = 16.dp,
    val esp24: Dp = 24.dp,
    val esp32: Dp = 32.dp,
    val toqueMinimo: Dp = 48.dp
)

val LocalLetsSignSpacing = staticCompositionLocalOf { LetsSignSpacing() }

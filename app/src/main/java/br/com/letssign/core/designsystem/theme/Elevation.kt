package br.com.letssign.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de elevação do LetsSign:
 * nivel-0 (0dp): telas
 * nivel-1 (2dp): cartões
 * nivel-2 (4dp): top app bar rolada, bottom nav
 * nivel-3 (8dp): FAB, dialogs, bottom sheets
 */
@Immutable
data class LetsSignElevation(
    val nivel0: Dp = 0.dp,
    val nivel1: Dp = 2.dp,
    val nivel2: Dp = 4.dp,
    val nivel3: Dp = 8.dp
)

val LocalLetsSignElevation = staticCompositionLocalOf { LetsSignElevation() }

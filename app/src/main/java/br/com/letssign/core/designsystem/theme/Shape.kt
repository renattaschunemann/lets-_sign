package br.com.letssign.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Tokens de formas do LetsSign:
 * - forma-pequena: 8 dp (campos, chips, selos)
 * - forma-media: 12 dp (cartões)
 * - forma-grande: 16 dp (FAB, cartões de destaque)
 * - forma-extra-grande: 28 dp (dialogs e cantos superiores de bottom sheets)
 * - forma-total: pílula / CircleShape (botões)
 */
val FormaPequena = RoundedCornerShape(8.dp)
val FormaMedia = RoundedCornerShape(12.dp)
val FormaGrande = RoundedCornerShape(16.dp)
val FormaExtraGrande = RoundedCornerShape(28.dp)
val FormaTotal = CircleShape

val LetsSignShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = FormaPequena,
    medium = FormaMedia,
    large = FormaGrande,
    extraLarge = FormaExtraGrande
)

package br.com.letssign.core.designsystem.components.base

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * Efeito shimmer skeleton para listas e cartões em carregamento.
 */
fun Modifier.skeletonShimmer(
    forma: Shape = FormaPequena
): Modifier = composed {
    val transicao = rememberInfiniteTransition(label = "shimmer")
    val progresso by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    val corBase = LetsSignTheme.colors.corSuperficieVariante
    val corBrilho = LetsSignTheme.colors.corFundo

    val brush = Brush.linearGradient(
        colors = listOf(corBase, corBrilho, corBase),
        start = Offset(progresso - 400f, progresso - 400f),
        end = Offset(progresso, progresso)
    )

    this
        .clip(forma)
        .background(brush)
}

@Composable
fun IndicadorCarregamentoCircular(
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        color = LetsSignTheme.colors.corPrimaria,
        strokeWidth = 3.dp
    )
}

@Composable
fun SkeletonItem(
    modifier: Modifier = Modifier,
    altura: Dp = 64.dp,
    forma: Shape = FormaPequena
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(altura)
            .skeletonShimmer(forma)
    )
}

package br.com.letssign.core.designsystem.components.logo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class LogoSize(val width: Dp, val height: Dp) {
    GRANDE(160.dp, 160.dp),   // COM-01 Splash
    MEDIO(120.dp, 48.dp),     // COM-02 Login, COM-03 Cadastro
    PEQUENO(32.dp, 32.dp)     // Top App Bar de ADM-01 e SIG-01
}

/**
 * Componente oficial de espaço reservado para a logo:
 * - Retângulo com borda tracejada (corBorda)
 * - Fundo corSuperficieVariante
 * - Texto central "LOGO" em corTextoSecundario
 * - contentDescription: "LOGO — [INSERIR POSTERIORMENTE]"
 */
@Composable
fun LogoPlaceholder(
    tamanho: LogoSize = LogoSize.MEDIO,
    modifier: Modifier = Modifier
) {
    val corBorda = LetsSignTheme.colors.corBorda
    val corFundo = LetsSignTheme.colors.corSuperficieVariante
    val corTexto = LetsSignTheme.colors.corTextoSecundario
    val descricao = stringResource(R.string.logo_content_description)

    Box(
        modifier = modifier
            .size(tamanho.width, tamanho.height)
            .background(color = corFundo, shape = FormaPequena)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawRoundRect(
                    color = corBorda,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = dashPathEffect
                    )
                )
            }
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.logo_placeholder_text),
            style = if (tamanho == LogoSize.PEQUENO) {
                LetsSignTheme.colors.let { LetsSignTheme.colors } // fallback
                androidx.compose.material3.MaterialTheme.typography.labelSmall
            } else {
                androidx.compose.material3.MaterialTheme.typography.labelLarge
            },
            color = corTexto
        )
    }
}

@Preview(name = "Logo Grande 160x160", showBackground = true)
@Composable
private fun LogoGrandePreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.GRANDE)
    }
}

@Preview(name = "Logo Médio 120x48", showBackground = true)
@Composable
private fun LogoMedioPreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.MEDIO)
    }
}

@Preview(name = "Logo Pequeno 32x32", showBackground = true)
@Composable
private fun LogoPequenoPreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.PEQUENO)
    }
}

@Preview(name = "Logo Médio FontScale 2.0", fontScale = 2.0f, showBackground = true)
@Composable
private fun LogoFontScalePreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.MEDIO)
    }
}

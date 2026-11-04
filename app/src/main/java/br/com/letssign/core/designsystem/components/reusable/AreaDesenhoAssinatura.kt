package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class EstiloFonteCursiva(val nomeExibicao: String) {
    ESTILO_1("Dancing Script"),
    ESTILO_2("Great Vibes"),
    ESTILO_3("Allura"),
    ESTILO_4("Sacramento")
}

/**
 * Área de desenho interativa em Canvas para assinatura manual:
 * - Traço corTintaAssinatura
 * - Suavização de traço vetorial via curvas de Bézier quadráticas com pontos médios
 * - Linha de base corDivisor
 * - Botão "Limpar"
 */
@Composable
fun AreaDesenhoAssinatura(
    aoMudarTemDesenho: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    aoAtualizarPontos: ((List<Offset>) -> Unit)? = null
) {
    val pontos = remember { mutableStateListOf<Offset>() }
    val corTinta = LetsSignTheme.colors.corTintaAssinatura
    val corDivisor = LetsSignTheme.colors.corDivisor

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(LetsSignTheme.colors.corSuperficie, shape = FormaMedia)
                .border(1.dp, LetsSignTheme.colors.corBorda, shape = FormaMedia)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            pontos.add(offset)
                            aoMudarTemDesenho(true)
                            aoAtualizarPontos?.invoke(pontos.toList())
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            pontos.add(change.position)
                            aoAtualizarPontos?.invoke(pontos.toList())
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Linha de base guia
                drawLine(
                    color = corDivisor,
                    start = Offset(16.dp.toPx(), size.height - 32.dp.toPx()),
                    end = Offset(size.width - 16.dp.toPx(), size.height - 32.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                if (pontos.isNotEmpty()) {
                    val path = Path().apply {
                        if (pontos.size == 1) {
                            val p = pontos.first()
                            moveTo(p.x, p.y)
                            lineTo(p.x + 1f, p.y + 1f)
                        } else if (pontos.size == 2) {
                            moveTo(pontos[0].x, pontos[0].y)
                            lineTo(pontos[1].x, pontos[1].y)
                        } else {
                            // Suavização vetorial com curvas de Bézier e pontos médios
                            moveTo(pontos[0].x, pontos[0].y)
                            for (i in 1 until pontos.size - 1) {
                                val pAtual = pontos[i]
                                val pProximo = pontos[i + 1]
                                val midX = (pAtual.x + pProximo.x) / 2f
                                val midY = (pAtual.y + pProximo.y) / 2f
                                quadraticTo(pAtual.x, pAtual.y, midX, midY)
                            }
                            // Conecta até o último ponto
                            val penultimo = pontos[pontos.size - 2]
                            val ultimo = pontos.last()
                            quadraticTo(penultimo.x, penultimo.y, ultimo.x, ultimo.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = corTinta,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BotaoTexto(
                texto = stringResource(R.string.cd_limpar),
                onClick = {
                    pontos.clear()
                    aoMudarTemDesenho(false)
                    aoAtualizarPontos?.invoke(emptyList())
                }
            )
        }
    }
}

/**
 * Seletor de 4 estilos de escrita cursiva para assinatura tipográfica
 */
@Composable
fun SeletorEstiloCursivo(
    nomeParaAssinar: String,
    estiloSelecionado: EstiloFonteCursiva,
    aoSelecionarEstilo: (EstiloFonteCursiva) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EstiloFonteCursiva.values().forEach { estilo ->
            val selecionado = estilo == estiloSelecionado
            val corBorda = if (selecionado) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda
            val corFundo = if (selecionado) LetsSignTheme.colors.corPrimariaContainer else LetsSignTheme.colors.corSuperficie

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = corFundo, shape = FormaPequena)
                    .border(width = if (selecionado) 2.dp else 1.dp, color = corBorda, shape = FormaPequena)
                    .clickable { aoSelecionarEstilo(estilo) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nomeParaAssinar,
                    fontStyle = FontStyle.Italic,
                    fontFamily = when (estilo) {
                        EstiloFonteCursiva.ESTILO_1 -> FontFamily.Cursive
                        EstiloFonteCursiva.ESTILO_2 -> FontFamily.Serif
                        EstiloFonteCursiva.ESTILO_3 -> FontFamily.SansSerif
                        EstiloFonteCursiva.ESTILO_4 -> FontFamily.Default
                    },
                    fontSize = 20.sp,
                    color = LetsSignTheme.colors.corTintaAssinatura
                )
                Text(
                    text = estilo.nomeExibicao,
                    style = MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }
    }
}

package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class EstadoCampoAssinatura {
    VAZIO,
    SELECIONADO,
    ASSINADO,
    OUTRO_PARTICIPANTE
}

/**
 * Campo de assinatura sobreposto na página PDF:
 * Retângulo com o nome ou as iniciais do signatário SEMPRE em texto.
 */
@Composable
fun CampoAssinaturaSobreposto(
    nomeSignatario: String,
    estado: EstadoCampoAssinatura,
    modifier: Modifier = Modifier,
    largura: Dp = 160.dp,
    altura: Dp = 64.dp
) {
    val corVazioBorda = LetsSignTheme.colors.corBorda
    val corVazioFundo = LetsSignTheme.colors.corCampoAssinaturaVazio
    val corSelecionadoFundo = LetsSignTheme.colors.corCampoAssinaturaSelecionado
    val corSelecionadoBorda = LetsSignTheme.colors.corPrimaria
    val corAssinadoFundo = LetsSignTheme.colors.corCampoAssinaturaAssinado
    val corOutroFundo = LetsSignTheme.colors.corCampoAssinaturaOutroParticipante
    val corTinta = LetsSignTheme.colors.corTintaAssinatura

    Box(
        modifier = modifier
            .size(largura, altura)
            .then(
                when (estado) {
                    EstadoCampoAssinatura.VAZIO -> Modifier
                        .background(color = corVazioFundo, shape = FormaPequena)
                        .drawBehind {
                            drawRoundRect(
                                color = corVazioBorda,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            )
                        }
                    EstadoCampoAssinatura.SELECIONADO -> Modifier
                        .background(color = corSelecionadoFundo, shape = FormaPequena)
                        .border(width = 2.dp, color = corSelecionadoBorda, shape = FormaPequena)
                    EstadoCampoAssinatura.ASSINADO -> Modifier
                        .background(color = corAssinadoFundo, shape = FormaPequena)
                        .border(width = 1.dp, color = LetsSignTheme.colors.corSucesso, shape = FormaPequena)
                    EstadoCampoAssinatura.OUTRO_PARTICIPANTE -> Modifier
                        .background(color = corOutroFundo, shape = FormaPequena)
                        .border(width = 1.dp, color = corVazioBorda, shape = FormaPequena)
                }
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (estado) {
                EstadoCampoAssinatura.VAZIO -> {
                    Icon(
                        imageVector = Icons.Filled.Draw,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corTextoSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
                EstadoCampoAssinatura.SELECIONADO -> {
                    Icon(
                        imageVector = Icons.Filled.Draw,
                        contentDescription = null,
                        tint = corSelecionadoBorda,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = corSelecionadoBorda
                    )
                }
                EstadoCampoAssinatura.ASSINADO -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corSucesso,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = corTinta
                    )
                }
                EstadoCampoAssinatura.OUTRO_PARTICIPANTE -> {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corTextoDesabilitado,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }
        }

        // 4 alças de redimensionar visíveis no estado SELECIONADO com 48dp de área de toque
        if (estado == EstadoCampoAssinatura.SELECIONADO) {
            AlcaRedimensionamento(modifier = Modifier.align(Alignment.TopStart))
            AlcaRedimensionamento(modifier = Modifier.align(Alignment.TopEnd))
            AlcaRedimensionamento(modifier = Modifier.align(Alignment.BottomStart))
            AlcaRedimensionamento(modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun AlcaRedimensionamento(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp) // Área de toque mínima acessível de 48dp
            .padding(18.dp) // Ponto visual de 12dp centralizado
            .background(color = LetsSignTheme.colors.corPrimaria, shape = FormaTotal)
    )
}

@Preview(name = "Campo Vazio", showBackground = true)
@Composable
private fun CampoVazioPreview() {
    LetsSignTheme {
        CampoAssinaturaSobreposto(
            nomeSignatario = "Maria Souza",
            estado = EstadoCampoAssinatura.VAZIO
        )
    }
}

@Preview(name = "Campo Selecionado com Alças", showBackground = true)
@Composable
private fun CampoSelecionadoPreview() {
    LetsSignTheme {
        CampoAssinaturaSobreposto(
            nomeSignatario = "Maria Souza",
            estado = EstadoCampoAssinatura.SELECIONADO
        )
    }
}

@Preview(name = "Campo Assinado", showBackground = true)
@Composable
private fun CampoAssinadoPreview() {
    LetsSignTheme {
        CampoAssinaturaSobreposto(
            nomeSignatario = "Maria Souza",
            estado = EstadoCampoAssinatura.ASSINADO
        )
    }
}

@Preview(name = "Campo Outro Participante", showBackground = true)
@Composable
private fun CampoOutroPreview() {
    LetsSignTheme {
        CampoAssinaturaSobreposto(
            nomeSignatario = "Ana Lima",
            estado = EstadoCampoAssinatura.OUTRO_PARTICIPANTE
        )
    }
}

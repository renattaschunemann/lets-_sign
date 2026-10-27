package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

val NOMES_ETAPAS = listOf(
    "Novo fluxo",
    "Título",
    "PDF",
    "Signatários",
    "Obrigatoriedade",
    "Ordem de assinatura",
    "Páginas",
    "Campos",
    "Revisão"
)

/**
 * Indicador de etapas do Wizard do Admin (1 a 9):
 * Exibe faixa horizontal com progresso e o texto "Etapa N de 9 — Nome".
 */
@Composable
fun IndicadorEtapasWizard(
    etapaAtual: Int,
    modifier: Modifier = Modifier,
    nomeAgrupado: String? = null
) {
    val totalEtapas = 9
    val nomeEtapa = nomeAgrupado ?: NOMES_ETAPAS.getOrElse(etapaAtual - 1) { "" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LetsSignTheme.colors.corSuperficie)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Texto descritivo
        Text(
            text = "Etapa $etapaAtual de $totalEtapas — $nomeEtapa",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LetsSignTheme.colors.corTextoPrincipal
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progresso segmentada em 9 passos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalEtapas) {
                val corSegmento = when {
                    i < etapaAtual -> LetsSignTheme.colors.corSucesso
                    i == etapaAtual -> LetsSignTheme.colors.corPrimaria
                    else -> LetsSignTheme.colors.corDesabilitado
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(color = corSegmento, shape = FormaTotal)
                )
            }
        }
    }
}

@Preview(name = "Etapa 1 de 9", showBackground = true)
@Composable
private fun Etapa1Preview() {
    LetsSignTheme {
        IndicadorEtapasWizard(etapaAtual = 1)
    }
}

@Preview(name = "Etapas 1-2 Agrupada", showBackground = true)
@Composable
private fun EtapasAgrupadasPreview() {
    LetsSignTheme {
        IndicadorEtapasWizard(etapaAtual = 2, nomeAgrupado = "Título")
    }
}

@Preview(name = "Etapa 6 de 9", showBackground = true)
@Composable
private fun Etapa6Preview() {
    LetsSignTheme {
        IndicadorEtapasWizard(etapaAtual = 6)
    }
}

package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class ModoVisualizadorPdf {
    EDICAO,
    ASSINATURA,
    LEITURA
}

/**
 * Visualizador de PDF com camada de campos:
 * - Página em corPaginaPdf com simulação de linhas de texto em corDivisor
 * - Controles de zoom (Ampliar / Reduzir)
 * - Controles de página (Anterior / Próxima, indicador "pág. x de y")
 * - Slot para camada sobreposta de campos
 */
@Composable
fun VisualizadorPdfCamadas(
    totalPaginas: Int,
    paginaAtual: Int,
    aoMudarPagina: (Int) -> Unit,
    modo: ModoVisualizadorPdf,
    modifier: Modifier = Modifier,
    camadaCampos: @Composable () -> Unit = {}
) {
    var escalaZoom by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LetsSignTheme.colors.corFundoExterno)
    ) {
        // Barra de ferramentas superior de navegação de páginas e zoom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LetsSignTheme.colors.corSuperficie)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (paginaAtual > 1) aoMudarPagina(paginaAtual - 1) },
                    enabled = paginaAtual > 1
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Página anterior",
                        tint = if (paginaAtual > 1) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }
                Text(
                    text = "pág. $paginaAtual de $totalPaginas",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
                IconButton(
                    onClick = { if (paginaAtual < totalPaginas) aoMudarPagina(paginaAtual + 1) },
                    enabled = paginaAtual < totalPaginas
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Próxima página",
                        tint = if (paginaAtual < totalPaginas) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }
            }

            // Controles de zoom
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (escalaZoom > 0.8f) escalaZoom -= 0.2f },
                    enabled = escalaZoom > 0.8f
                ) {
                    Icon(Icons.Filled.ZoomOut, contentDescription = "Reduzir zoom")
                }
                IconButton(
                    onClick = { if (escalaZoom < 2.0f) escalaZoom += 0.2f },
                    enabled = escalaZoom < 2.0f
                ) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = "Ampliar zoom")
                }
            }
        }

        // Área da página central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(escalaZoom.coerceAtMost(1.0f))
                    .height(440.dp),
                shape = FormaMedia,
                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corPaginaPdf),
                elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Texto simulado em barras corDivisor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(16.dp)
                                .background(LetsSignTheme.colors.corDivisor, FormaPequena)
                        )
                        repeat(8) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(LetsSignTheme.colors.corDivisor, FormaPequena)
                            )
                        }
                    }

                    // Camada de campos sobrepostos
                    camadaCampos()
                }
            }
        }
    }
}

@Preview(name = "Visualizador PDF com Campos", showBackground = true)
@Composable
private fun VisualizadorPreview() {
    LetsSignTheme {
        VisualizadorPdfCamadas(
            totalPaginas = 8,
            paginaAtual = 3,
            aoMudarPagina = {},
            modo = ModoVisualizadorPdf.EDICAO,
            camadaCampos = {
                Box(modifier = Modifier.padding(24.dp)) {
                    CampoAssinaturaSobreposto(
                        nomeSignatario = "Ana Lima",
                        estado = EstadoCampoAssinatura.SELECIONADO
                    )
                }
            }
        )
    }
}

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
                    .heightIn(min = 520.dp),
                shape = FormaMedia,
                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corPaginaPdf),
                elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 520.dp)
                        .padding(20.dp)
                ) {
                    // 1. Conteúdo de fundo da página do documento
                    if (paginaAtual == 1) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(LetsSignTheme.colors.corPrimaria, FormaPequena)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LETSSIGN TECNOLOGIA",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = LetsSignTheme.colors.corPrimaria
                                )
                                Text(
                                    text = "ID: DOC-2026-LETSSIGN-8842",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
                            Text(
                                text = "CONTRATO DE PRESTAÇÃO DE SERVIÇOS DE TECNOLOGIA",
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "Versão 2.0 • Status: Concluído e Registrado Digitalmente",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corSucesso,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "DAS PARTES CONTRATANTES:\nCONTRATANTE: TechSolutions Inovação S.A.\nCONTRATADO: João Lima Desenvolvimento ME",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "CLÁUSULA 1ª — DO OBJETO:\nPrestação de serviços de engenharia de software para a plataforma LetsSign Mobile, com padrões de arquitetura limpa e segurança de KeyStore.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "CLÁUSULA 2ª — DO VALOR E PAGAMENTO:\nValor total pactuado em R$ 48.000,00 distribuídos em 4 parcelas mensais de R$ 12.000,00 com homologação técnica.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "CLÁUSULA 3ª — DA CONFIDENCIALIDADE E LGPD:\nSigilo integral e estrito cumprimento da Lei Federal nº 13.709/2018 com criptografia em repouso e em trânsito.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Spacer(modifier = Modifier.weight(1f, fill = false))
                            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "LetsSign Mobile Platform",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = "Pág. 1 de $totalPaginas",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(LetsSignTheme.colors.corSucesso, FormaPequena)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FOLHA DE ROSTO • ASSINATURAS",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = LetsSignTheme.colors.corSucesso
                                )
                                Text(
                                    text = "Pág. $paginaAtual de $totalPaginas",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
                            Text(
                                text = "TERMO DE ASSINATURA ELETRÔNICA",
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "Emitido em conformidade com a MP nº 2.200-2/2001 e a Lei nº 14.063/2020. As partes abaixo assinam e chancelam este documento.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoSecundario
                            )
                            Spacer(modifier = Modifier.weight(1f, fill = false))
                            Card(
                                shape = FormaPequena,
                                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corFundo),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "EVIDÊNCIAS DE INTEGRIDADE ICP-BRASIL",
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = LetsSignTheme.colors.corTextoPrincipal
                                    )
                                    Text(
                                        text = "SHA-256 Final: e4b2c9d1852a37f09c64821a7df83c9b74314e1f7d5267e89ab012f45c9d6482",
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                    Text(
                                        text = "Carimbo do Tempo: 2026-10-26T16:06:00Z | Protocolo: BR-2026-LS-991823",
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                }
                            }
                        }
                    }

                    // 2. Camada sobreposta de campos em coordenadas normalizadas (ativa em todas as páginas)
                    Box(
                        modifier = Modifier.matchParentSize()
                    ) {
                        camadaCampos()
                    }
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

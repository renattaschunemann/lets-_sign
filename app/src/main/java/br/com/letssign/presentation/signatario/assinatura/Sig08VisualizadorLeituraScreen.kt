package br.com.letssign.presentation.signatario.assinatura

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.pdf.PdfDemoGenerator
import br.com.letssign.data.local.file.PdfStorageManager
import br.com.letssign.domain.model.SituacaoParticipante
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig08VisualizadorLeituraScreen(
    aoVoltar: () -> Unit,
    documentoId: String = "",
    modifier: Modifier = Modifier,
    viewModel: Sig08VisualizadorLeituraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.documento?.titulo ?: "Visualizar documento assinado",
                        style = MaterialTheme.typography.titleMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                actions = {
                    br.com.letssign.core.designsystem.components.logo.LogoPlaceholder(
                        tamanho = br.com.letssign.core.designsystem.components.logo.LogoSize.PEQUENO,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            try {
                                val pdfRelativo = uiState.versao?.pdfPathInterno
                                val docFile = if (!pdfRelativo.isNullOrBlank()) {
                                    val f = PdfStorageManager.obterArquivoPdf(context, pdfRelativo)
                                    if (f.exists() && f.length() > 0) f else PdfDemoGenerator.obterOuCriarContratoDemoPdf(context)
                                } else if (documentoId.isNotBlank()) {
                                    val f = PdfStorageManager.obterArquivoPdf(context, "documentos/$documentoId/v1.pdf")
                                    if (f.exists() && f.length() > 0) f else PdfDemoGenerator.obterOuCriarContratoDemoPdf(context)
                                } else {
                                    PdfDemoGenerator.obterOuCriarContratoDemoPdf(context)
                                }
                                PdfDemoGenerator.compartilharPdf(context, docFile)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao abrir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Baixar PDF assinado",
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        }
    ) { paddingValues ->
        if (uiState.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                IndicadorCarregamentoCircular()
            }
        } else {
            val camposNaPagina = uiState.campos.filter { it.numeroPagina == uiState.paginaAtual }
            val pdfPath = uiState.versao?.pdfPathInterno?.ifBlank { null }
                ?: if (documentoId.isNotBlank()) "documentos/$documentoId/v1.pdf" else null

            VisualizadorPdfCamadas(
                totalPaginas = uiState.totalPaginas.coerceAtLeast(1),
                paginaAtual = uiState.paginaAtual,
                aoMudarPagina = { viewModel.mudarPagina(it) },
                modo = ModoVisualizadorPdf.LEITURA,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                pdfPath = pdfPath,
                tituloDocumento = uiState.documento?.titulo ?: "Documento Concluído",
                camadaCampos = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val totalLarguraPx = constraints.maxWidth.toFloat()
                        val totalAlturaPx = constraints.maxHeight.toFloat()
                        val densidade = LocalDensity.current

                        camposNaPagina.forEach { campo ->
                            val part = uiState.participantes.find { it.id == campo.participanteId }
                            val assinatura = uiState.assinaturasPorCampo[campo.id]
                            val isAssinado = assinatura != null || part?.situacao == SituacaoParticipante.ASSINADO
                            val papelRotulo = campo.papelEspecifico?.rotulo ?: part?.papel?.rotulo ?: ""
                            val nomeExibicao = if (part != null) {
                                if (papelRotulo.isNotBlank()) "${part.nomeSnapshot} ($papelRotulo)" else part.nomeSnapshot
                            } else "Signatário"

                            val larguraDp = with(densidade) { (totalLarguraPx * campo.normLargura).toDp() }.coerceAtLeast(80.dp)
                            val alturaDp = with(densidade) { (totalAlturaPx * campo.normAltura).toDp() }.coerceAtLeast(36.dp)
                            val posX = campo.normX * totalLarguraPx
                            val posY = campo.normY * totalAlturaPx

                            Box(
                                modifier = Modifier.offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                            ) {
                                CampoAssinaturaSobreposto(
                                    nomeSignatario = nomeExibicao,
                                    estado = if (isAssinado) EstadoCampoAssinatura.ASSINADO else EstadoCampoAssinatura.VAZIO,
                                    largura = larguraDp,
                                    altura = alturaDp,
                                    caminhoAssinatura = assinatura?.caminhoPngAssinatura,
                                    detalhesCertificado = assinatura?.detalhesCertificado
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

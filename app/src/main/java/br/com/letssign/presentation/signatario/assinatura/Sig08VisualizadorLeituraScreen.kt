package br.com.letssign.presentation.signatario.assinatura

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.pdf.PdfDemoGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig08VisualizadorLeituraScreen(
    aoVoltar: () -> Unit,
    documentoId: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var paginaAtual by remember { mutableIntStateOf(1) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Visualizar documento",
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
                                val docFile = if (documentoId.isNotBlank()) {
                                    val f = br.com.letssign.data.local.file.PdfStorageManager.obterArquivoPdf(context, "documentos/$documentoId/v1.pdf")
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
        VisualizadorPdfCamadas(
            totalPaginas = 2,
            paginaAtual = paginaAtual,
            aoMudarPagina = { paginaAtual = it },
            modo = ModoVisualizadorPdf.LEITURA,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            pdfPath = if (documentoId.isNotBlank()) "documentos/$documentoId/v1.pdf" else null,
            tituloDocumento = "Documento Concluído",
            camadaCampos = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CampoAssinaturaSobreposto(
                        nomeSignatario = "Carla Mendes (Contratante)",
                        estado = EstadoCampoAssinatura.ASSINADO
                    )
                    CampoAssinaturaSobreposto(
                        nomeSignatario = "João Lima (Contratado)",
                        estado = EstadoCampoAssinatura.ASSINADO
                    )
                    CampoAssinaturaSobreposto(
                        nomeSignatario = "Maria Souza (Testemunha)",
                        estado = EstadoCampoAssinatura.ASSINADO
                    )
                }
            }
        )
    }
}


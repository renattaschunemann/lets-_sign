package br.com.letssign.core.designsystem.components.reusable

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
import androidx.compose.runtime.LaunchedEffect
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
    pdfPath: String? = null,
    tituloDocumento: String? = null,
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
                    PaginaDocumentoDinamicaOuPdf(
                        pdfPath = pdfPath,
                        paginaAtual = paginaAtual,
                        totalPaginas = totalPaginas,
                        tituloDocumento = tituloDocumento
                    )

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

@Composable
fun PaginaDocumentoDinamicaOuPdf(
    pdfPath: String?,
    paginaAtual: Int,
    totalPaginas: Int,
    tituloDocumento: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmapPagina by remember(pdfPath, paginaAtual) { androidx.compose.runtime.mutableStateOf<Bitmap?>(null) }
    var renderizouPdf by remember(pdfPath, paginaAtual) { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(pdfPath, paginaAtual) {
        if (!pdfPath.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val file = br.com.letssign.data.local.file.PdfStorageManager.obterArquivoPdf(context, pdfPath)
                    val targetFile = if (file.exists() && file.length() > 0) file else {
                        val fDirect = File(pdfPath)
                        if (fDirect.exists() && fDirect.length() > 0) fDirect else null
                    }
                    if (targetFile != null) {
                        ParcelFileDescriptor.open(targetFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                            PdfRenderer(pfd).use { renderer ->
                                if (paginaAtual in 1..renderer.pageCount) {
                                    renderer.openPage(paginaAtual - 1).use { page ->
                                        val bmp = Bitmap.createBitmap(
                                            (page.width * 1.5f).toInt(),
                                            (page.height * 1.5f).toInt(),
                                            Bitmap.Config.ARGB_8888
                                        )
                                        bmp.eraseColor(android.graphics.Color.WHITE)
                                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                        bitmapPagina = bmp
                                        renderizouPdf = true
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    renderizouPdf = false
                }
            }
        }
    }

    if (renderizouPdf && bitmapPagina != null) {
        Image(
            bitmap = bitmapPagina!!.asImageBitmap(),
            contentDescription = "Página $paginaAtual do documento",
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
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
                    text = "LETSSIGN DOCUMENTO",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = LetsSignTheme.colors.corPrimaria
                )
                Text(
                    text = "Pág. $paginaAtual de $totalPaginas",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
            Text(
                text = (tituloDocumento ?: "Documento Selecionado").uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            Text(
                text = "Documento oficial selecionado para assinatura na plataforma LetsSign.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = LetsSignTheme.colors.corSucesso,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Página $paginaAtual — Conteúdo oficial do documento selecionado para coleta de assinaturas e autenticação digital conforme MP 2.200-2/2001.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    text = "Status: Registrado",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }
    }
}

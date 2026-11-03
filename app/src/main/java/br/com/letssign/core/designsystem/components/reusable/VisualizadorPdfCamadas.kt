package br.com.letssign.core.designsystem.components.reusable

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class ModoVisualizadorPdf {
    EDICAO,
    ASSINATURA,
    LEITURA
}

/**
 * Visualizador de PDF com camada de campos e suporte completo a Zoom:
 * - Zoom por gesto de pinça (pinch-to-zoom) e arrasto (pan) quando ampliado;
 * - Zoom por duplo toque (double tap);
 * - Botões de zoom in (+), zoom out (-) e indicador com reset para 100%;
 * - Navegação de páginas (anterior / próxima);
 * - Renderização nativa com PdfRenderer ou fallback de documento oficial;
 * - Camada sobreposta de campos perfeitamente sincronizada em coordenadas normalizadas.
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
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Reseta o pan se o zoom voltar a 1.0f
    LaunchedEffect(escalaZoom) {
        if (escalaZoom <= 1.01f) {
            panX = 0f
            panY = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LetsSignTheme.colors.corFundoExterno)
    ) {
        // Barra de ferramentas superior: navegação e controles de zoom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LetsSignTheme.colors.corSuperficie)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paginação
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (paginaAtual > 1) aoMudarPagina(paginaAtual - 1) },
                    enabled = paginaAtual > 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Página anterior",
                        tint = if (paginaAtual > 1) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }
                Text(
                    text = "$paginaAtual / $totalPaginas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LetsSignTheme.colors.corTextoPrincipal,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = { if (paginaAtual < totalPaginas) aoMudarPagina(paginaAtual + 1) },
                    enabled = paginaAtual < totalPaginas,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Próxima página",
                        tint = if (paginaAtual < totalPaginas) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }
            }

            // Controles de Zoom (+, -, chip % com reset)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val novoZoom = (escalaZoom - 0.25f).coerceAtLeast(1.0f)
                        escalaZoom = novoZoom
                        if (novoZoom <= 1.0f) {
                            panX = 0f
                            panY = 0f
                        }
                    },
                    enabled = escalaZoom > 1.0f,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomOut,
                        contentDescription = "Reduzir zoom",
                        tint = if (escalaZoom > 1.0f) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }

                // Chip de porcentagem com reset ao tocar
                Surface(
                    shape = FormaTotal,
                    color = if (escalaZoom > 1.05f) LetsSignTheme.colors.corPrimariaContainer else LetsSignTheme.colors.corSuperficieVariante,
                    modifier = Modifier.clickable {
                        escalaZoom = 1.0f
                        panX = 0f
                        panY = 0f
                    }
                ) {
                    Text(
                        text = "${(escalaZoom * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (escalaZoom > 1.05f) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corTextoSecundario,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = {
                        escalaZoom = (escalaZoom + 0.25f).coerceAtMost(3.5f)
                    },
                    enabled = escalaZoom < 3.5f,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Ampliar zoom",
                        tint = if (escalaZoom < 3.5f) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corDesabilitado
                    )
                }
            }
        }

        // Área da página com pinch-to-zoom, pan e double tap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val novoZoom = (escalaZoom * zoom).coerceIn(1.0f, 3.5f)
                        escalaZoom = novoZoom
                        if (novoZoom > 1.05f) {
                            val maxPanX = (novoZoom - 1f) * 450f
                            val maxPanY = (novoZoom - 1f) * 700f
                            panX = (panX + pan.x).coerceIn(-maxPanX, maxPanX)
                            panY = (panY + pan.y).coerceIn(-maxPanY, maxPanY)
                        } else {
                            panX = 0f
                            panY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (escalaZoom > 1.1f) {
                                escalaZoom = 1.0f
                                panX = 0f
                                panY = 0f
                            } else {
                                escalaZoom = 2.0f
                            }
                        }
                    )
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 520.dp)
                    .graphicsLayer {
                        scaleX = escalaZoom
                        scaleY = escalaZoom
                        translationX = panX
                        translationY = panY
                    },
                shape = FormaMedia,
                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corPaginaPdf),
                elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 520.dp)
                        .padding(16.dp)
                ) {
                    // 1. Conteúdo de fundo da página do documento (PDF real ou dinâmico)
                    PaginaDocumentoDinamicaOuPdf(
                        pdfPath = pdfPath,
                        paginaAtual = paginaAtual,
                        totalPaginas = totalPaginas,
                        tituloDocumento = tituloDocumento
                    )

                    // 2. Camada sobreposta de campos
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

@Composable
fun PaginaDocumentoDinamicaOuPdf(
    pdfPath: String?,
    paginaAtual: Int,
    totalPaginas: Int,
    tituloDocumento: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmapPagina by remember(pdfPath, paginaAtual) { mutableStateOf<Bitmap?>(null) }
    var renderizouPdf by remember(pdfPath, paginaAtual) { mutableStateOf(false) }

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
                                        // Renderização em alta definição para suporte a zoom nítido
                                        val renderWidth = (page.width * 2.0f).toInt()
                                        val renderHeight = (page.height * 2.0f).toInt()
                                        val bmp = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
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
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corPrimaria
                )
                Text(
                    text = "Pág. $paginaAtual de $totalPaginas",
                    style = MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
            Text(
                text = (tituloDocumento ?: "Documento Selecionado").uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            Text(
                text = "Documento oficial selecionado para assinatura na plataforma LetsSign.",
                style = MaterialTheme.typography.bodySmall,
                color = LetsSignTheme.colors.corSucesso,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Página $paginaAtual — Conteúdo oficial do documento selecionado para coleta de assinaturas e autenticação digital conforme MP 2.200-2/2001.",
                style = MaterialTheme.typography.bodySmall,
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
                    style = MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
                Text(
                    text = "Status: Registrado",
                    style = MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }
    }
}

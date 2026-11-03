package br.com.letssign.core.designsystem.components.reusable

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.data.local.file.AssinaturaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class EstadoCampoAssinatura {
    VAZIO,
    SELECIONADO,
    ASSINADO,
    OUTRO_PARTICIPANTE
}

/**
 * Campo de assinatura sobreposto na página PDF:
 * - Em modo VAZIO: retângulo tracejado com ícone e nome do signatário.
 * - Em modo SELECIONADO: borda de destaque com alças de redimensionamento nos cantos.
 * - Em modo ASSINADO: renderiza a assinatura gráfica real (PNG desenhado/digitado/certificado)
 *   ou carimbo digital oficial com metadados.
 */
@Composable
fun CampoAssinaturaSobreposto(
    nomeSignatario: String,
    estado: EstadoCampoAssinatura,
    modifier: Modifier = Modifier,
    largura: Dp = 160.dp,
    altura: Dp = 64.dp,
    caminhoAssinatura: String? = null,
    detalhesCertificado: String? = null,
    onRedimensionar: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null
) {
    val context = LocalContext.current
    val corVazioBorda = LetsSignTheme.colors.corBorda
    val corVazioFundo = LetsSignTheme.colors.corCampoAssinaturaVazio
    val corSelecionadoFundo = LetsSignTheme.colors.corCampoAssinaturaSelecionado
    val corSelecionadoBorda = LetsSignTheme.colors.corPrimaria
    val corAssinadoFundo = LetsSignTheme.colors.corCampoAssinaturaAssinado
    val corOutroFundo = LetsSignTheme.colors.corCampoAssinaturaOutroParticipante
    val corTinta = LetsSignTheme.colors.corTintaAssinatura

    var bitmapAssinatura by remember(caminhoAssinatura) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(caminhoAssinatura) {
        if (!caminhoAssinatura.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val arquivo = AssinaturaStorageManager.obterArquivoAssinatura(context, caminhoAssinatura)
                    if (arquivo.exists() && arquivo.length() > 0) {
                        bitmapAssinatura = BitmapFactory.decodeFile(arquivo.absolutePath)
                    }
                } catch (_: Exception) {
                    bitmapAssinatura = null
                }
            }
        } else {
            bitmapAssinatura = null
        }
    }

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
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (estado) {
            EstadoCampoAssinatura.VAZIO -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Draw,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corTextoSecundario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corTextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            EstadoCampoAssinatura.SELECIONADO -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Draw,
                        contentDescription = null,
                        tint = corSelecionadoBorda,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = MaterialTheme.typography.bodySmall,
                        color = corSelecionadoBorda,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            EstadoCampoAssinatura.ASSINADO -> {
                if (bitmapAssinatura != null) {
                    // Renderiza a assinatura PNG real capturada
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = bitmapAssinatura!!.asImageBitmap(),
                            contentDescription = "Assinatura de $nomeSignatario",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Surface(
                            shape = FormaTotal,
                            color = LetsSignTheme.colors.corSucesso.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Assinado",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else if (!detalhesCertificado.isNullOrBlank()) {
                    // Renderiza carimbo visual de Certificado Digital
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = LetsSignTheme.colors.corPrimaria,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "ICP-BRASIL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = LetsSignTheme.colors.corPrimaria
                            )
                        }
                        Text(
                            text = nomeSignatario,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = corTinta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = detalhesCertificado.take(24),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            color = LetsSignTheme.colors.corTextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // Assinatura cursiva com selo verde de validação
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = nomeSignatario,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Cursive,
                            color = corTinta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = LetsSignTheme.colors.corSucesso,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Assinado digitalmente",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = LetsSignTheme.colors.corSucesso
                            )
                        }
                    }
                }
            }
            EstadoCampoAssinatura.OUTRO_PARTICIPANTE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corTextoDesabilitado,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = nomeSignatario,
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corTextoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Alça interativa de redimensionamento no canto inferior direito quando selecionado
        if (estado == EstadoCampoAssinatura.SELECIONADO) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .pointerInput(onRedimensionar) {
                        if (onRedimensionar != null) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onRedimensionar(dragAmount)
                            }
                        }
                    }
                    .padding(6.dp)
                    .background(color = LetsSignTheme.colors.corPrimaria, shape = FormaTotal)
            )
        }
    }
}

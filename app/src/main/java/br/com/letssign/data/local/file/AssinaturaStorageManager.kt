package br.com.letssign.data.local.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gerenciador de armazenamento e renderização de assinaturas (desenhada, digitada ou certificado digital).
 * Grava e carrega arquivos PNG com transparência no armazenamento interno do aplicativo.
 */
object AssinaturaStorageManager {

    fun obterDiretorioAssinaturas(context: Context): File {
        val dir = File(context.filesDir, "assinaturas")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun obterArquivoAssinatura(context: Context, caminhoRelativoOuAbsoluto: String): File {
        if (caminhoRelativoOuAbsoluto.startsWith("/") || (caminhoRelativoOuAbsoluto.length > 2 && caminhoRelativoOuAbsoluto[1] == ':')) {
            return File(caminhoRelativoOuAbsoluto)
        }
        val pathLimpo = caminhoRelativoOuAbsoluto.removePrefix("assinaturas/").removePrefix("/")
        return File(obterDiretorioAssinaturas(context), pathLimpo)
    }

    fun salvarBitmapComoPng(context: Context, nomeArquivoSemCaminho: String, bitmap: Bitmap): String {
        val dir = obterDiretorioAssinaturas(context)
        val file = File(dir, nomeArquivoSemCaminho)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return "assinaturas/${file.name}"
    }

    /**
     * Gera bitmap de assinatura desenhada a partir de traços contínuos.
     */
    fun gerarBitmapDesenho(
        pontos: List<Offset>,
        larguraPx: Int = 600,
        alturaPx: Int = 240,
        corArgb: Int = Color.rgb(15, 76, 129) // Navy ink
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(larguraPx, alturaPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)

        if (pontos.size < 2) {
            // Se houver apenas 1 ponto, desenha uma rubrica estilizada padrão
            val paintPonto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = corArgb
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(larguraPx / 2f, alturaPx / 2f, 20f, paintPonto)
            return bitmap
        }

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        for (p in pontos) {
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
        }

        val larguraOrig = (maxX - minX).coerceAtLeast(10f)
        val alturaOrig = (maxY - minY).coerceAtLeast(10f)

        val margem = 24f
        val escalaX = (larguraPx - 2 * margem) / larguraOrig
        val escalaY = (alturaPx - 2 * margem) / alturaOrig
        val escala = minOf(escalaX, escalaY, 2.8f)

        val offsetX = (larguraPx - (larguraOrig * escala)) / 2f - (minX * escala)
        val offsetY = (alturaPx - (alturaOrig * escala)) / 2f - (minY * escala)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            color = corArgb
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = Path()
        val primeiro = pontos.first()
        path.moveTo(primeiro.x * escala + offsetX, primeiro.y * escala + offsetY)
        for (i in 1 until pontos.size) {
            val p = pontos[i]
            path.lineTo(p.x * escala + offsetX, p.y * escala + offsetY)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }

    /**
     * Gera bitmap estilizado para assinatura digitada (caligráfica).
     */
    fun gerarBitmapDigitado(
        nome: String,
        estiloFonteNome: String,
        larguraPx: Int = 600,
        alturaPx: Int = 240,
        corArgb: Int = Color.rgb(15, 76, 129)
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(larguraPx, alturaPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)

        val texto = nome.ifBlank { "Assinatura Eletrônica" }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = corArgb
            textSize = 50f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.SERIF,
                android.graphics.Typeface.ITALIC
            )
        }

        val yPos = (alturaPx / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(texto, larguraPx / 2f, yPos, paint)

        // Traço sublinhado decorativo
        val paintLinha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = corArgb
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val textWidth = paint.measureText(texto)
        val startX = (larguraPx - textWidth) / 2f
        canvas.drawLine(startX, yPos + 12f, startX + textWidth, yPos + 12f, paintLinha)

        return bitmap
    }

    /**
     * Gera bitmap do carimbo visual de Certificado Digital ICP-Brasil.
     */
    fun gerarBitmapCertificado(
        titular: String,
        documento: String,
        tipoCertificado: String,
        autoridadeCertificadora: String,
        larguraPx: Int = 600,
        alturaPx: Int = 240
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(larguraPx, alturaPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)

        val rect = RectF(6f, 6f, larguraPx - 6f, alturaPx - 6f)

        // Fundo suave
        val paintFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(22, 15, 76, 129)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 14f, 14f, paintFundo)

        // Borda do carimbo
        val paintBorda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 76, 129)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(rect, 14f, 14f, paintBorda)

        val paintHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 76, 129)
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("ASSINADO DIGITALMENTE (ICP-BRASIL)", 20f, 40f, paintHeader)

        val paintDiv = Paint().apply {
            color = Color.rgb(180, 205, 230)
            strokeWidth = 1.5f
        }
        canvas.drawLine(20f, 52f, larguraPx - 20f, 52f, paintDiv)

        val paintRotulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 80, 95)
            textSize = 20f
        }
        val paintValor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 20f
            isFakeBoldText = true
        }

        canvas.drawText("Titular:", 20f, 84f, paintRotulo)
        canvas.drawText(titular.take(28), 95f, 84f, paintValor)

        canvas.drawText("Doc:", 20f, 118f, paintRotulo)
        canvas.drawText("$tipoCertificado • $documento", 95f, 118f, paintValor)

        canvas.drawText("Emissor:", 20f, 152f, paintRotulo)
        val emissorCurto = if (autoridadeCertificadora.length > 32) autoridadeCertificadora.take(30) + "..." else autoridadeCertificadora
        canvas.drawText(emissorCurto, 95f, 152f, paintValor)

        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm 'UTC'", Locale.getDefault()).format(Date())
        val paintRodape = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 76, 129)
            textSize = 17f
            isFakeBoldText = true
        }
        canvas.drawText("Data: $dataHora • MP 2.200-2/2001", 20f, 190f, paintRodape)

        return bitmap
    }
}


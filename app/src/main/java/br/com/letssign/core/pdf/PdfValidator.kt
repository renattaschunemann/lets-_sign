package br.com.letssign.core.pdf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

enum class TipoErroPdf {
    SENHA_PROTEGIDA,
    TAMANHO_EXCEDIDO,
    PAGINAS_EXCEDIDAS,
    ARQUIVO_CORROMPIDO
}

sealed class PdfValidationResult {
    data class Sucesso(
        val nomeArquivo: String,
        val tamanhoBytes: Long,
        val totalPaginas: Int,
        val pathInterno: String
    ) : PdfValidationResult()

    data class Erro(val tipoErro: TipoErroPdf) : PdfValidationResult()
}

object PdfValidator {

    private const val LIMITE_BYTES: Long = 15 * 1024 * 1024 // 15 MB
    private const val LIMITE_PAGINAS = 50

    suspend fun validarECopiarPdf(
        context: Context,
        uri: Uri
    ): PdfValidationResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        // Obter nome e tamanho
        var nomeArquivo = "documento.pdf"
        var tamanhoBytes: Long = 0

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        nomeArquivo = cursor.getString(nameIndex) ?: nomeArquivo
                    }
                    if (sizeIndex != -1) {
                        tamanhoBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // Segue com tentativa de cópia
        }

        // Validação E03: Tamanho máximo 15 MB
        if (tamanhoBytes > LIMITE_BYTES) {
            return@withContext PdfValidationResult.Erro(TipoErroPdf.TAMANHO_EXCEDIDO)
        }

        // Copiar para armazenamento interno seguro do app
        val destinoDir = File(context.filesDir, "pdfs_upload").apply { mkdirs() }
        val arquivoDestino = File(destinoDir, "temp_${System.currentTimeMillis()}.pdf")

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(arquivoDestino).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext PdfValidationResult.Erro(TipoErroPdf.ARQUIVO_CORROMPIDO)
        } catch (e: Exception) {
            return@withContext PdfValidationResult.Erro(TipoErroPdf.ARQUIVO_CORROMPIDO)
        }

        val tamanhoReal = arquivoDestino.length()
        if (tamanhoReal > LIMITE_BYTES) {
            arquivoDestino.delete()
            return@withContext PdfValidationResult.Erro(TipoErroPdf.TAMANHO_EXCEDIDO)
        }

        // Validar integridade, senha e páginas via PdfRenderer nativo do Android
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(arquivoDestino, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)

            val totalPaginas = renderer.pageCount
            if (totalPaginas <= 0) {
                arquivoDestino.delete()
                return@withContext PdfValidationResult.Erro(TipoErroPdf.ARQUIVO_CORROMPIDO)
            }

            // Validação E04: Até 50 páginas
            if (totalPaginas > LIMITE_PAGINAS) {
                arquivoDestino.delete()
                return@withContext PdfValidationResult.Erro(TipoErroPdf.PAGINAS_EXCEDIDAS)
            }

            return@withContext PdfValidationResult.Sucesso(
                nomeArquivo = nomeArquivo,
                tamanhoBytes = tamanhoReal,
                totalPaginas = totalPaginas,
                pathInterno = arquivoDestino.absolutePath
            )
        } catch (e: SecurityException) {
            // E02: PDF protegido por senha
            arquivoDestino.delete()
            return@withContext PdfValidationResult.Erro(TipoErroPdf.SENHA_PROTEGIDA)
        } catch (e: IOException) {
            // E05: Arquivo corrompido ou inválido
            arquivoDestino.delete()
            return@withContext PdfValidationResult.Erro(TipoErroPdf.ARQUIVO_CORROMPIDO)
        } catch (e: Exception) {
            arquivoDestino.delete()
            return@withContext PdfValidationResult.Erro(TipoErroPdf.ARQUIVO_CORROMPIDO)
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }
}

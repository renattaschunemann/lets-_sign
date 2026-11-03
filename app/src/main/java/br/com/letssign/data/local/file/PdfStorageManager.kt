package br.com.letssign.data.local.file

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Gerenciador de armazenamento de arquivos PDF conforme os requisitos da Sprint 1:
 *
 * 1. Armazenamento em sistema de arquivos do device:
 *    Salva o PDF no armazenamento interno do app (filesDir) e registra metadados no SQLite.
 *
 * 2. Armazenamento direto no banco de dados (BLOB):
 *    Fornece utilitários para conversão bidirecional entre ByteArray (BLOB) e arquivos locais,
 *    possibilitando tráfego via APIs REST (Multipart/Base64) e persistência direta.
 */
object PdfStorageManager {

    /**
     * Salva o conteúdo binário do PDF no sistema de arquivos do dispositivo (Modo 1).
     */
    fun salvarNoSistemaDeArquivos(
        context: Context,
        documentoId: String,
        versao: Int,
        conteudoPdf: ByteArray
    ): String {
        val pasta = File(context.filesDir, "documentos/$documentoId").apply { mkdirs() }
        val arquivo = File(pasta, "v$versao.pdf")
        FileOutputStream(arquivo).use { output ->
            output.write(conteudoPdf)
            output.flush()
        }
        return "documentos/$documentoId/v$versao.pdf"
    }

    /**
     * Lê o arquivo PDF do sistema de arquivos e o converte para ByteArray (BLOB) (Modo 2).
     */
    fun lerComoBlob(context: Context, caminhoOuRelativo: String): ByteArray? {
        val arquivo = obterArquivoPdf(context, caminhoOuRelativo)
        if (!arquivo.exists() || !arquivo.isFile) return null
        return try {
            FileInputStream(arquivo).use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Obtém a referência File de um PDF a partir do caminho relativo gravado no banco de dados.
     */
    fun obterArquivoPdf(context: Context, caminhoOuRelativo: String): File {
        val fileDirect = File(caminhoOuRelativo)
        if (fileDirect.isAbsolute && fileDirect.exists()) {
            return fileDirect
        }
        val fileInFilesDir = File(context.filesDir, caminhoOuRelativo)
        if (fileInFilesDir.exists()) {
            return fileInFilesDir
        }
        return fileDirect
    }

    /**
     * Remove o arquivo PDF do sistema de arquivos do dispositivo (ex.: descarte de rascunho ou logout).
     */
    fun removerDoSistemaDeArquivos(context: Context, caminhoRelativo: String): Boolean {
        val arquivo = File(context.filesDir, caminhoRelativo)
        return if (arquivo.exists()) arquivo.delete() else true
    }
}


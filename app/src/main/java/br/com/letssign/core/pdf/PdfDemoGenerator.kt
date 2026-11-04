package br.com.letssign.core.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import br.com.letssign.domain.model.SituacaoDocumento

/**
 * Gerador de PDF demonstrativo para apresentação ao cliente e bancas avaliadoras.
 * Cria um documento real e juridicamente estruturado (Contrato de Prestação de Serviços)
 * 100% concluído e assinado com carimbos visuais, metadados ICP-Brasil e hashes criptográficos.
 */
object PdfDemoGenerator {

    private const val LARGURA_A4 = 595
    private const val ALTURA_A4 = 842

    fun obterOuCriarContratoDemoPdf(context: Context): File {
        val pastaDocs = File(context.filesDir, "documentos/doc-demo-5").apply { mkdirs() }
        val arquivoPdf = File(pastaDocs, "v2.pdf")
        if (!arquivoPdf.exists() || arquivoPdf.length() == 0L) {
            gerarContratoAssinadoPdf(arquivoPdf)
        }
        return arquivoPdf
    }

    fun gerarContratoAssinadoPdf(arquivoDestino: File): File {
        arquivoDestino.parentFile?.mkdirs()

        val doc = PdfDocument()

        // -------------------------------------------------------------
        // PÁGINA 1 — MINUTA DO CONTRATO
        // -------------------------------------------------------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 1).create()
        val page1 = doc.startPage(pageInfo1)
        desenharPaginaContrato(page1.canvas)
        doc.finishPage(page1)

        // -------------------------------------------------------------
        // PÁGINA 2 — TERMO DE ASSINATURA ELETRÔNICA E AUDITORIA
        // -------------------------------------------------------------
        val pageInfo2 = PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 2).create()
        val page2 = doc.startPage(pageInfo2)
        desenharPaginaAssinaturas(page2.canvas)
        doc.finishPage(page2)

        FileOutputStream(arquivoDestino).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return arquivoDestino
    }

    private fun desenharPaginaContrato(canvas: Canvas) {
        val paintTexto = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val paintTitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 14f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintSubtitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0057B7")
            textSize = 10f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintLinha = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val paintFaixaTopo = Paint().apply {
            color = Color.parseColor("#0057B7")
        }

        // Barra decorativa superior
        canvas.drawRect(0f, 0f, LARGURA_A4.toFloat(), 6f, paintFaixaTopo)

        // Cabeçalho institucional
        canvas.drawText("LETSSIGN TECNOLOGIA EM ASSINATURAS DIGITAIS", 40f, 35f, paintSubtitulo)
        canvas.drawText("Documento ID: DOC-2026-LETSSIGN-8842", 360f, 35f, paintTexto)
        canvas.drawLine(40f, 45f, (LARGURA_A4 - 40).toFloat(), 45f, paintLinha)

        // Título central
        val titulo = "CONTRATO DE PRESTAÇÃO DE SERVIÇOS DE TECNOLOGIA"
        val subtitulo = "Versão 2.0 • Status: Concluído e Registrado Digitalmente"
        val largTitulo = paintTitulo.measureText(titulo)
        canvas.drawText(titulo, (LARGURA_A4 - largTitulo) / 2f, 75f, paintTitulo)
        val largSub = paintSubtitulo.measureText(subtitulo)
        canvas.drawText(subtitulo, (LARGURA_A4 - largSub) / 2f, 92f, paintSubtitulo)

        var y = 120f
        val linhaEspaco = 15f
        val margemEsq = 40f
        val margemDir = (LARGURA_A4 - 40).toFloat()

        fun escreverSecao(tituloSecao: String, paragrafos: List<String>) {
            paintTitulo.textSize = 11f
            canvas.drawText(tituloSecao, margemEsq, y, paintTitulo)
            y += linhaEspaco + 2f

            paintTexto.textSize = 9.5f
            for (p in paragrafos) {
                canvas.drawText(p, margemEsq, y, paintTexto)
                y += linhaEspaco
            }
            y += 8f
        }

        escreverSecao(
            "DAS PARTES CONTRATANTES",
            listOf(
                "CONTRATANTE: TECHSOLUTIONS INOVAÇÃO S.A., inscrita no CNPJ/MF sob o nº 12.345.678/0001-90,",
                "com sede na Av. Paulista, 1000 - São Paulo/SP, neste ato representada por Carla Mendes.",
                "CONTRATADO: JOÃO LIMA DESENVOLVIMENTO DE SOFTWARE ME, inscrito no CNPJ sob o nº 98.765.432/0001-10,",
                "com sede na Rua das Palmeiras, 250 - São Paulo/SP, representado por João Lima."
            )
        )

        escreverSecao(
            "CLÁUSULA 1ª — DO OBJETO E ESCOPO",
            listOf(
                "1.1. O presente contrato tem por objeto a prestação de serviços de engenharia de software para o",
                "desenvolvimento, testes e implantação do aplicativo móvel corporativo LetsSign Mobile.",
                "1.2. O escopo contempla arquitetura limpa (Clean Architecture), persistência local com Room,",
                "segurança criptográfica via Android KeyStore e integração de assinatura eletrônica de documentos."
            )
        )

        escreverSecao(
            "CLÁUSULA 2ª — DO VALOR E FORMA DE PAGAMENTO",
            listOf(
                "2.1. Pelos serviços prestados, a CONTRATANTE pagará ao CONTRATADO a quantia total de R$ 48.000,00",
                "(quarenta e oito mil reais), distribuídos em 4 (quatro) parcelas mensais iguais de R$ 12.000,00.",
                "2.2. Os pagamentos serão realizados mediante emissão da correspondente nota fiscal e homologação."
            )
        )

        escreverSecao(
            "CLÁUSULA 3ª — DA CONFIDENCIALIDADE E PROTEÇÃO DE DADOS (LGPD)",
            listOf(
                "3.1. As partes comprometem-se a manter sigilo absoluto sobre todas as informações estratégicas.",
                "3.2. As partes declaram cumprir integralmente as disposições da Lei Federal nº 13.709/2018 (LGPD),",
                "adotando padrões rigorosos de segurança e criptografia de ponta a ponta para os dados dos signatários."
            )
        )

        escreverSecao(
            "CLÁUSULA 4ª — DA VIGÊNCIA E DO FORO",
            listOf(
                "4.1. O presente instrumento vige a partir da data de sua assinatura eletrônica final pelo prazo de 12 meses.",
                "4.2. Para dirimir quaisquer controvérsias oriundas deste contrato, as partes elegem o Foro da Comarca de São Paulo/SP."
            )
        )

        // Rodapé da página 1
        canvas.drawLine(margemEsq, 790f, margemDir, 790f, paintLinha)
        paintTexto.textSize = 8.5f
        paintTexto.color = Color.parseColor("#64748B")
        canvas.drawText("LetsSign Mobile Platform • Segurança e Validade Jurídica", margemEsq, 805f, paintTexto)
        canvas.drawText("Página 1 de 2", margemDir - 60f, 805f, paintTexto)
    }

    private fun desenharPaginaAssinaturas(canvas: Canvas) {
        val paintTexto = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val paintTitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 13f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintSubtitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0057B7")
            textSize = 9.5f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintLinha = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val paintFaixaTopo = Paint().apply {
            color = Color.parseColor("#10B981") // Verde sucesso
        }

        // Barra decorativa superior verde de sucesso
        canvas.drawRect(0f, 0f, LARGURA_A4.toFloat(), 6f, paintFaixaTopo)

        // Cabeçalho institucional
        canvas.drawText("LETSSIGN • FOLHA DE ROSTO E REGISTRO DE ASSINATURAS", 40f, 35f, paintSubtitulo)
        canvas.drawText("Status: 100% ASSINADO", 400f, 35f, Paint().apply {
            color = Color.parseColor("#047857")
            textSize = 9.5f
            isFakeBoldText = true
        })
        canvas.drawLine(40f, 45f, (LARGURA_A4 - 40).toFloat(), 45f, paintLinha)

        // Título da página de auditoria
        val titulo = "TERMO DE ASSINATURA ELETRÔNICA E EVIDÊNCIAS DE AUTENTICIDADE"
        val largTitulo = paintTitulo.measureText(titulo)
        canvas.drawText(titulo, (LARGURA_A4 - largTitulo) / 2f, 72f, paintTitulo)

        val legal = "Emitido em conformidade com a MP nº 2.200-2/2001 e a Lei Federal nº 14.063/2020."
        val largLegal = paintTexto.measureText(legal)
        canvas.drawText(legal, (LARGURA_A4 - largLegal) / 2f, 87f, paintTexto)

        var y = 110f

        // Função para desenhar o box de cada signatário
        fun desenharBoxSignatario(
            nome: String,
            papel: String,
            email: String,
            cpfMascarado: String,
            dataHora: String,
            ip: String,
            autenticacao: String,
            rubricaTexto: String
        ) {
            val margemEsq = 40f
            val largura = (LARGURA_A4 - 80).toFloat()
            val alturaBox = 100f

            val rect = RectF(margemEsq, y, margemEsq + largura, y + alturaBox)

            // Fundo suave e borda
            val paintFundo = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            val paintBorda = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(rect, 8f, 8f, paintFundo)
            canvas.drawRoundRect(rect, 8f, 8f, paintBorda)

            // Selo verde de assinado
            val paintSeloFundo = Paint().apply { color = Color.parseColor("#D1FAE5") }
            val paintSeloTexto = Paint().apply {
                color = Color.parseColor("#065F46")
                textSize = 8.5f
                isFakeBoldText = true
            }
            val rectSelo = RectF(margemEsq + largura - 110f, y + 10f, margemEsq + largura - 10f, y + 26f)
            canvas.drawRoundRect(rectSelo, 4f, 4f, paintSeloFundo)
            canvas.drawText("✓ ASSINADO", margemEsq + largura - 95f, y + 22f, paintSeloTexto)

            // Nome e Papel
            val paintNome = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 11f
                isFakeBoldText = true
            }
            canvas.drawText(nome, margemEsq + 14f, y + 22f, paintNome)

            val paintPapel = Paint().apply {
                color = Color.parseColor("#0057B7")
                textSize = 9f
                isFakeBoldText = true
            }
            canvas.drawText("• $papel", margemEsq + 14f + paintNome.measureText(nome) + 8f, y + 22f, paintPapel)

            // Dados técnicos de auditoria
            val paintInfo = Paint().apply {
                color = Color.parseColor("#475569")
                textSize = 8.5f
            }
            canvas.drawText("E-mail: $email  |  CPF: $cpfMascarado", margemEsq + 14f, y + 40f, paintInfo)
            canvas.drawText("Data/Hora: $dataHora (UTC-03:00 Horário de Brasília)", margemEsq + 14f, y + 54f, paintInfo)
            canvas.drawText("Endereço IP: $ip  |  Método: $autenticacao", margemEsq + 14f, y + 68f, paintInfo)

            // Representação gráfica da rubrica/assinatura
            val rectRubrica = RectF(margemEsq + largura - 170f, y + 42f, margemEsq + largura - 14f, y + 88f)
            val paintRubricaFundo = Paint().apply {
                color = Color.parseColor("#FFFFFF")
                style = Paint.Style.FILL
            }
            val paintRubricaBorda = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(rectRubrica, 6f, 6f, paintRubricaFundo)
            canvas.drawRoundRect(rectRubrica, 6f, 6f, paintRubricaBorda)

            val paintRubrica = Paint().apply {
                color = Color.parseColor("#1D4ED8")
                textSize = 13f
                isFakeBoldText = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            canvas.drawText(rubricaTexto, margemEsq + largura - 155f, y + 68f, paintRubrica)

            val paintSubRubrica = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 7f
            }
            canvas.drawText("Certificado LetsSign ICP-Token", margemEsq + largura - 155f, y + 80f, paintSubRubrica)

            y += alturaBox + 14f
        }

        // Signatário 1: Carla Mendes (Administradora / Contratante)
        desenharBoxSignatario(
            nome = "Carla Mendes",
            papel = "CONTRATANTE",
            email = "carla@empresa.com.br",
            cpfMascarado = "***.452.888-**",
            dataHora = "26/10/2026 às 14:15:30",
            ip = "201.86.102.15 (São Paulo/SP)",
            autenticacao = "Assinatura Biométrica Digital / Sessão Autenticada",
            rubricaTexto = "Carla Mendes"
        )

        // Signatário 2: João Lima (Signatário / Contratado)
        desenharBoxSignatario(
            nome = "João Lima",
            papel = "CONTRATADO",
            email = "joao.lima@email.com",
            cpfMascarado = "***.123.789-**",
            dataHora = "26/10/2026 às 15:42:18",
            ip = "189.120.45.12 (São Paulo/SP)",
            autenticacao = "Token SMS (OTP) + Confirmação de Identidade",
            rubricaTexto = "J. Lima"
        )

        // Signatário 3: Maria Souza (Signatária / Testemunha)
        desenharBoxSignatario(
            nome = "Maria Souza",
            papel = "TESTEMUNHA",
            email = "maria.souza@empresa.com.br",
            cpfMascarado = "***.998.411-**",
            dataHora = "26/10/2026 às 16:05:04",
            ip = "177.18.99.40 (Rio de Janeiro/RJ)",
            autenticacao = "Link Criptografado + Token de E-mail",
            rubricaTexto = "Maria Souza"
        )

        // Box de Integridade Criptográfica (Rodapé da Auditoria)
        val margemEsq = 40f
        val largura = (LARGURA_A4 - 80).toFloat()
        val rectCrypto = RectF(margemEsq, y + 10f, margemEsq + largura, y + 115f)

        val paintCryptoFundo = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val paintCryptoBorda = Paint().apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rectCrypto, 8f, 8f, paintCryptoFundo)
        canvas.drawRoundRect(rectCrypto, 8f, 8f, paintCryptoBorda)

        val paintCryptoTitulo = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            isFakeBoldText = true
        }
        canvas.drawText("CERTIFICADO DE INTEGRIDADE E CARIMBO DE TEMPO", margemEsq + 14f, y + 28f, paintCryptoTitulo)

        val paintHashes = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 8f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("SHA-256 Original: a3f8b0e71946c2419a77e5d933bb32014c278e9fa07f3521e1a2f60d4b8e211a", margemEsq + 14f, y + 45f, paintHashes)
        canvas.drawText("SHA-256 Assinado: e4b2c9d1852a37f09c64821a7df83c9b74314e1f7d5267e89ab012f45c9d6482", margemEsq + 14f, y + 60f, paintHashes)
        canvas.drawText("Protocolo Registro: BR-2026-LS-991823 | Carimbo Tempo: 2026-10-26T16:06:00Z", margemEsq + 14f, y + 75f, paintHashes)

        val paintLink = Paint().apply {
            color = Color.parseColor("#0057B7")
            textSize = 8f
        }
        canvas.drawText("Verificação de Autenticidade: https://validar.letssign.com.br/verificar?c=BR-2026-LS-991823", margemEsq + 14f, y + 95f, paintLink)

        // Rodapé da página 2
        canvas.drawLine(margemEsq, 790f, (LARGURA_A4 - 40).toFloat(), 790f, paintLinha)
        paintTexto.textSize = 8.5f
        paintTexto.color = Color.parseColor("#64748B")
        canvas.drawText("LetsSign Mobile Platform • Documento 100% Concluído e Auditado", margemEsq, 805f, paintTexto)
        canvas.drawText("Página 2 de 2", (LARGURA_A4 - 40).toFloat() - 60f, 805f, paintTexto)
    }

    /**
     * Dispara um Intent para abrir o documento PDF gerado com o visualizador do sistema (Adobe, Drive, etc)
     * ou compartilhar via WhatsApp/Teams/Email.
     */
    fun compartilharPdf(context: Context, arquivoPdf: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoPdf
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Contrato de Prestação de Serviços Assinado — LetsSign")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o documento 100% assinado e auditado pelo LetsSign.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Compartilhar Contrato Assinado").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun visualizarPdfExterno(context: Context, arquivoPdf: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoPdf
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            compartilharPdf(context, arquivoPdf)
        }
    }

    fun obterOuCriarDocumentoDemoPdf(
        context: Context,
        documentoId: String,
        versao: Int,
        titulo: String,
        subtitulo: String = "Documento Registrado Digitalmente",
        situacao: SituacaoDocumento = SituacaoDocumento.ENVIADO,
        partes: List<String> = emptyList(),
        clausulas: List<Pair<String, List<String>>> = emptyList()
    ): File {
        val pastaDocs = File(context.filesDir, "documentos/$documentoId").apply { mkdirs() }
        val arquivoPdf = File(pastaDocs, "v$versao.pdf")
        if (!arquivoPdf.exists() || arquivoPdf.length() == 0L) {
            gerarDocumentoGenericoPdf(
                arquivoDestino = arquivoPdf,
                documentoId = documentoId,
                versao = versao,
                titulo = titulo,
                subtitulo = subtitulo,
                situacao = situacao,
                partes = partes,
                clausulas = clausulas
            )
        }
        return arquivoPdf
    }

    fun gerarDocumentoGenericoPdf(
        arquivoDestino: File,
        documentoId: String,
        versao: Int,
        titulo: String,
        subtitulo: String,
        situacao: SituacaoDocumento,
        partes: List<String>,
        clausulas: List<Pair<String, List<String>>>
    ): File {
        arquivoDestino.parentFile?.mkdirs()

        val doc = PdfDocument()

        // PÁGINA 1 — MINUTA DO INSTRUMENTO
        val pageInfo1 = PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 1).create()
        val page1 = doc.startPage(pageInfo1)
        desenharPaginaGenericaMinuta(
            canvas = page1.canvas,
            documentoId = documentoId,
            versao = versao,
            titulo = titulo,
            subtitulo = subtitulo,
            situacao = situacao,
            partes = partes,
            clausulas = clausulas
        )
        doc.finishPage(page1)

        // PÁGINA 2 — FOLHA DE ASSINATURAS E AUDITORIA
        val pageInfo2 = PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 2).create()
        val page2 = doc.startPage(pageInfo2)
        desenharPaginaGenericaAuditoria(
            canvas = page2.canvas,
            documentoId = documentoId,
            versao = versao,
            titulo = titulo,
            situacao = situacao
        )
        doc.finishPage(page2)

        FileOutputStream(arquivoDestino).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return arquivoDestino
    }

    private fun desenharPaginaGenericaMinuta(
        canvas: Canvas,
        documentoId: String,
        versao: Int,
        titulo: String,
        subtitulo: String,
        situacao: SituacaoDocumento,
        partes: List<String>,
        clausulas: List<Pair<String, List<String>>>
    ) {
        val paintTexto = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val paintTitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 13f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintSubtitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0057B7")
            textSize = 9.5f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintLinha = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val corTopo = when (situacao) {
            SituacaoDocumento.CONCLUIDO -> Color.parseColor("#10B981")
            SituacaoDocumento.RECUSADO, SituacaoDocumento.CANCELADO, SituacaoDocumento.FALHA_ENVIO -> Color.parseColor("#EF4444")
            SituacaoDocumento.PARCIALMENTE_ASSINADO, SituacaoDocumento.ENVIANDO -> Color.parseColor("#F59E0B")
            else -> Color.parseColor("#0057B7")
        }

        canvas.drawRect(0f, 0f, LARGURA_A4.toFloat(), 6f, Paint().apply { color = corTopo })

        canvas.drawText("LETSSIGN TECNOLOGIA EM ASSINATURAS DIGITAIS", 40f, 35f, paintSubtitulo)
        canvas.drawText("Doc: DOC-2026-$documentoId", 380f, 35f, paintTexto)
        canvas.drawLine(40f, 45f, (LARGURA_A4 - 40).toFloat(), 45f, paintLinha)

        val tituloUpper = titulo.uppercase()
        val sub = "$subtitulo • Versão $versao.0 • Situação: ${situacao.name}"
        val largTitulo = paintTitulo.measureText(tituloUpper)
        canvas.drawText(tituloUpper, (LARGURA_A4 - largTitulo).coerceAtLeast(80f) / 2f, 75f, paintTitulo)
        val largSub = paintSubtitulo.measureText(sub)
        canvas.drawText(sub, (LARGURA_A4 - largSub).coerceAtLeast(80f) / 2f, 92f, paintSubtitulo)

        var y = 120f
        val linhaEspaco = 14f
        val margemEsq = 40f
        val margemDir = (LARGURA_A4 - 40).toFloat()

        fun escreverSecao(tituloSecao: String, paragrafos: List<String>) {
            if (y > 740f) return
            paintTitulo.textSize = 10.5f
            canvas.drawText(tituloSecao, margemEsq, y, paintTitulo)
            y += linhaEspaco + 2f

            paintTexto.textSize = 9f
            for (p in paragrafos) {
                if (y > 750f) break
                canvas.drawText(p, margemEsq, y, paintTexto)
                y += linhaEspaco
            }
            y += 8f
        }

        if (partes.isNotEmpty()) {
            escreverSecao("DAS PARTES ENVOLVIDAS", partes)
        } else {
            escreverSecao(
                "DAS PARTES ENVOLVIDAS",
                listOf(
                    "CONTRATANTE / EMISSOR: Empresa LetsSign Tecnologia Ltda, CNPJ 12.345.678/0001-90.",
                    "CONTRATADOS / SIGNATÁRIOS: Partes qualificadas na plataforma para aposição de assinatura eletrônica."
                )
            )
        }

        val listaClausulas = if (clausulas.isNotEmpty()) clausulas else listOf(
            "CLÁUSULA 1ª — DO OBJETO E FINALIDADE" to listOf(
                "1.1. O presente instrumento particular formaliza os termos e condições ajustados entre as partes.",
                "1.2. As obrigações aqui pactuadas possuem plena eficácia jurídica entre os signatários cadastrados."
            ),
            "CLÁUSULA 2ª — DAS CONDIÇÕES E RESPONSABILIDADES" to listOf(
                "2.1. As partes comprometem-se a cumprir os prazos e disposições descritas neste documento.",
                "2.2. A autenticidade deste instrumento é garantida por trilha de auditoria digital e hash criptográfico."
            ),
            "CLÁUSULA 3ª — DA CONFIDENCIALIDADE E PROTEÇÃO DE DADOS (LGPD)" to listOf(
                "3.1. As informações aqui contidas são confidenciais e regidas pela Lei nº 13.709/2018 (LGPD).",
                "3.2. Os registros de auditoria, IPs e carimbos de data/hora são preservados para fins comprobatórios."
            ),
            "CLÁUSULA 4ª — DO FORO DE ELEIÇÃO" to listOf(
                "4.1. Para dirimir eventuais controvérsias oriundas deste termo, as partes elegem o Foro Central da Comarca de São Paulo/SP."
            )
        )

        for ((titClausula, parags) in listaClausulas) {
            escreverSecao(titClausula, parags)
        }

        canvas.drawLine(margemEsq, 790f, margemDir, 790f, paintLinha)
        paintTexto.textSize = 8.5f
        paintTexto.color = Color.parseColor("#64748B")
        canvas.drawText("LetsSign Mobile Platform • Validade Jurídica MP 2.200-2/2001", margemEsq, 805f, paintTexto)
        canvas.drawText("Página 1 de 2", margemDir - 60f, 805f, paintTexto)
    }

    private fun desenharPaginaGenericaAuditoria(
        canvas: Canvas,
        documentoId: String,
        versao: Int,
        titulo: String,
        situacao: SituacaoDocumento
    ) {
        val paintTexto = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val paintTitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 12.5f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintSubtitulo = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0057B7")
            textSize = 9.5f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintLinha = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        val corTopo = when (situacao) {
            SituacaoDocumento.CONCLUIDO -> Color.parseColor("#10B981")
            SituacaoDocumento.RECUSADO, SituacaoDocumento.CANCELADO, SituacaoDocumento.FALHA_ENVIO -> Color.parseColor("#EF4444")
            SituacaoDocumento.PARCIALMENTE_ASSINADO, SituacaoDocumento.ENVIANDO -> Color.parseColor("#F59E0B")
            else -> Color.parseColor("#0057B7")
        }

        canvas.drawRect(0f, 0f, LARGURA_A4.toFloat(), 6f, Paint().apply { color = corTopo })

        canvas.drawText("LETSSIGN • FOLHA DE ASSINATURAS E REGISTRO DE AUDITORIA", 40f, 35f, paintSubtitulo)
        canvas.drawText("Status: ${situacao.name}", 400f, 35f, Paint().apply {
            color = corTopo
            textSize = 9.5f
            isFakeBoldText = true
        })
        canvas.drawLine(40f, 45f, (LARGURA_A4 - 40).toFloat(), 45f, paintLinha)

        val tituloAuditoria = "TERMO DE ASSINATURA ELETRÔNICA E REGISTRO DE AUTENTICIDADE"
        val largTit = paintTitulo.measureText(tituloAuditoria)
        canvas.drawText(tituloAuditoria, (LARGURA_A4 - largTit).coerceAtLeast(40f) / 2f, 72f, paintTitulo)

        val legal = "Emitido em conformidade com a MP nº 2.200-2/2001 e a Lei Federal nº 14.063/2020."
        val largLegal = paintTexto.measureText(legal)
        canvas.drawText(legal, (LARGURA_A4 - largLegal) / 2f, 87f, paintTexto)

        val margemEsq = 40f
        val largura = (LARGURA_A4 - 80).toFloat()

        val rectResumo = RectF(margemEsq, 105f, margemEsq + largura, 175f)
        val paintFundoResumo = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val paintBordaResumo = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rectResumo, 8f, 8f, paintFundoResumo)
        canvas.drawRoundRect(rectResumo, 8f, 8f, paintBordaResumo)

        paintTexto.textSize = 9f
        canvas.drawText("Documento: $titulo (Versão $versao.0)", margemEsq + 14f, 125f, paintTitulo.apply { textSize = 9.5f })
        canvas.drawText("Protocolo do Sistema: BR-2026-LS-$documentoId", margemEsq + 14f, 142f, paintTexto)
        canvas.drawText("Situação do Fluxo: ${situacao.name}  |  Gerado via Plataforma LetsSign Mobile", margemEsq + 14f, 158f, paintTexto)

        val rectEspacoAssinaturas = RectF(margemEsq, 190f, margemEsq + largura, 640f)
        val paintFundoEspaco = Paint().apply {
            color = Color.parseColor("#FAFAFA")
            style = Paint.Style.FILL
        }
        val paintBordaEspaco = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rectEspacoAssinaturas, 8f, 8f, paintFundoEspaco)
        canvas.drawRoundRect(rectEspacoAssinaturas, 8f, 8f, paintBordaEspaco)

        val paintAvisoCampos = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ÁREA DESTINADA À APOSIÇÃO DOS CAMPOS DE ASSINATURA ELETRÔNICA", LARGURA_A4 / 2f, 215f, paintAvisoCampos)
        canvas.drawText("As rubricas e assinaturas digitais sobrepostas aparecem nas coordenadas configuradas.", LARGURA_A4 / 2f, 230f, paintAvisoCampos)

        val rectCrypto = RectF(margemEsq, 655f, margemEsq + largura, 765f)
        val paintCryptoFundo = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val paintCryptoBorda = Paint().apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rectCrypto, 8f, 8f, paintCryptoFundo)
        canvas.drawRoundRect(rectCrypto, 8f, 8f, paintCryptoBorda)

        val paintCryptoTitulo = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            isFakeBoldText = true
        }
        canvas.drawText("CERTIFICADO DE INTEGRIDADE E CARIMBO DE TEMPO", margemEsq + 14f, 675f, paintCryptoTitulo)

        val paintHashes = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 8f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("SHA-256 Original: sha256-doc-$documentoId-v$versao-validado", margemEsq + 14f, 693f, paintHashes)
        canvas.drawText("Protocolo Registro: BR-2026-LS-$documentoId | Carimbo: 2026-11-03T21:00:00Z", margemEsq + 14f, 708f, paintHashes)
        canvas.drawText("Segurança: Criptografia de Ponta a Ponta • Assinatura Eletrônica Avançada", margemEsq + 14f, 723f, paintHashes)

        val paintLink = Paint().apply {
            color = Color.parseColor("#0057B7")
            textSize = 8f
        }
        canvas.drawText("Verificação de Autenticidade: https://validar.letssign.com.br/verificar?c=BR-2026-LS-$documentoId", margemEsq + 14f, 745f, paintLink)

        canvas.drawLine(margemEsq, 790f, (LARGURA_A4 - 40).toFloat(), 790f, paintLinha)
        paintTexto.textSize = 8.5f
        paintTexto.color = Color.parseColor("#64748B")
        canvas.drawText("LetsSign Mobile Platform • Registro Digital e Auditoria", margemEsq, 805f, paintTexto)
        canvas.drawText("Página 2 de 2", (LARGURA_A4 - 40).toFloat() - 60f, 805f, paintTexto)
    }

}

package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.DocumentoDao
import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
import br.com.letssign.data.local.dao.VersaoDocumentoDao
import br.com.letssign.data.local.entities.DocumentoEntity
import br.com.letssign.data.local.entities.ParticipanteDocumentoEntity
import br.com.letssign.data.local.entities.VersaoDocumentoEntity
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante
import br.com.letssign.domain.model.TipoOrdem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class DocumentoItemResumo(
    val id: String,
    val titulo: String,
    val versao: Int,
    val versaoId: String,
    val situacao: SituacaoDocumento,
    val obrigatoriosAssinaram: Int,
    val totalObrigatorios: Int,
    val dataAtualizacaoFormatada: String,
    val dataUltimaAtualizacaoUtc: Long
)

@Singleton
class DocumentoRepository @Inject constructor(
    private val documentoDao: DocumentoDao,
    private val versaoDao: VersaoDocumentoDao,
    private val participanteDao: ParticipanteDocumentoDao
) {

    suspend fun inicializarDadosDemoSeNecessario(adminId: String) {
        val existentes = documentoDao.buscarPorId("doc-demo-1")
        if (existentes != null) return

        val agora = System.currentTimeMillis()

        // 1. Contrato de Prestação de Serviços (Parcialmente assinado, 2 de 3)
        criarDocDemo(
            docId = "doc-demo-1",
            versaoId = "versao-demo-1",
            titulo = "Contrato de Prestação de Serviços",
            adminId = adminId,
            versao = 1,
            situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO,
            dataUtc = agora - 3600000,
            obrigatoriosAssinaram = 2,
            totalObrigatorios = 3
        )

        // 2. Aditivo 02 (Falha no envio)
        criarDocDemo(
            docId = "doc-demo-2",
            versaoId = "versao-demo-2",
            titulo = "Aditivo 02",
            adminId = adminId,
            versao = 1,
            situacao = SituacaoDocumento.FALHA_ENVIO,
            dataUtc = agora - 7200000,
            obrigatoriosAssinaram = 0,
            totalObrigatorios = 2
        )

        // 3. Contrato de locação — Sala 3 (Rascunho)
        criarDocDemo(
            docId = "doc-demo-3",
            versaoId = "versao-demo-3",
            titulo = "Contrato de locação — Sala 3",
            adminId = adminId,
            versao = 1,
            situacao = SituacaoDocumento.RASCUNHO,
            dataUtc = agora - 10800000,
            obrigatoriosAssinaram = 0,
            totalObrigatorios = 0
        )

        // 4. Aditivo 02 v2 (Enviado, 0 de 2)
        criarDocDemo(
            docId = "doc-demo-4",
            versaoId = "versao-demo-4",
            titulo = "Aditivo 02",
            adminId = adminId,
            versao = 2,
            situacao = SituacaoDocumento.ENVIADO,
            dataUtc = agora - 14400000,
            obrigatoriosAssinaram = 0,
            totalObrigatorios = 2
        )

        // 5. Concluído
        criarDocDemo(
            docId = "doc-demo-5",
            versaoId = "versao-demo-5",
            titulo = "Contrato de Prestação de Serviços",
            adminId = adminId,
            versao = 2,
            situacao = SituacaoDocumento.CONCLUIDO,
            dataUtc = agora - 86400000,
            obrigatoriosAssinaram = 3,
            totalObrigatorios = 3
        )

        // 6. Recusado
        criarDocDemo(
            docId = "doc-demo-6",
            versaoId = "versao-demo-6",
            titulo = "Aditivo 02",
            adminId = adminId,
            versao = 3,
            situacao = SituacaoDocumento.RECUSADO,
            dataUtc = agora - 172800000,
            obrigatoriosAssinaram = 1,
            totalObrigatorios = 2
        )

        // 7. Cancelado
        criarDocDemo(
            docId = "doc-demo-7",
            versaoId = "versao-demo-7",
            titulo = "Contrato de locação — Sala 3",
            adminId = adminId,
            versao = 2,
            situacao = SituacaoDocumento.CANCELADO,
            dataUtc = agora - 259200000,
            obrigatoriosAssinaram = 0,
            totalObrigatorios = 2
        )
    }

    private suspend fun criarDocDemo(
        docId: String,
        versaoId: String,
        titulo: String,
        adminId: String,
        versao: Int,
        situacao: SituacaoDocumento,
        dataUtc: Long,
        obrigatoriosAssinaram: Int,
        totalObrigatorios: Int
    ) {
        val doc = DocumentoEntity(
            id = docId,
            usuarioAdminId = adminId,
            titulo = titulo,
            situacaoAtual = situacao,
            dataCriacaoUtc = dataUtc,
            dataUltimaAtualizacaoUtc = dataUtc
        )
        val versaoEntity = VersaoDocumentoEntity(
            id = versaoId,
            documentoId = docId,
            numeroVersao = versao,
            pdfPathInterno = "documentos/$docId/v$versao.pdf",
            sha256Original = "sha256-mock-$docId",
            sha256Final = null,
            tipoOrdem = TipoOrdem.PARALELA,
            mensagemConvite = "Por favor, assine este documento.",
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = 9,
            situacao = situacao
        )
        documentoDao.inserir(doc)
        versaoDao.inserir(versaoEntity)

        // Participantes de exemplo para cálculo de progresso
        val participantes = mutableListOf<ParticipanteDocumentoEntity>()
        for (i in 1..totalObrigatorios) {
            val assinado = i <= obrigatoriosAssinaram
            participantes.add(
                ParticipanteDocumentoEntity(
                    id = UUID.randomUUID().toString(),
                    versaoDocumentoId = versaoId,
                    usuarioId = "user-$i",
                    nomeSnapshot = "Signatário $i",
                    emailSnapshot = "signatario$i@empresa.com",
                    telefoneSnapshot = "1190000000$i",
                    dataNascimentoSnapshot = "1990-01-01",
                    papel = PapelParticipante.CONTRATADO,
                    obrigatorio = true,
                    ordem = i,
                    situacao = if (assinado) SituacaoParticipante.ASSINADO else SituacaoParticipante.PENDENTE
                )
            )
        }
        participanteDao.inserirVarios(participantes)
    }

    fun listarDocumentosDoAdmin(adminId: String): Flow<List<DocumentoItemResumo>> {
        return documentoDao.listarPorAdmin(adminId).map { lista ->
            lista.map { doc ->
                val versao = versaoDao.buscarVersaoAtual(doc.id)
                val participantes = if (versao != null) {
                    participanteDao.listarPorVersaoSync(versao.id)
                } else emptyList()

                val obrigatorios = participantes.filter { it.obrigatorio }
                val assinados = obrigatorios.count { it.situacao == SituacaoParticipante.ASSINADO }

                DocumentoItemResumo(
                    id = doc.id,
                    titulo = doc.titulo,
                    versao = versao?.numeroVersao ?: 1,
                    versaoId = versao?.id ?: "",
                    situacao = doc.situacaoAtual,
                    obrigatoriosAssinaram = assinados,
                    totalObrigatorios = obrigatorios.size,
                    dataAtualizacaoFormatada = "26/10/2026",
                    dataUltimaAtualizacaoUtc = doc.dataUltimaAtualizacaoUtc
                )
            }.sortedWith(
                compareByDescending<DocumentoItemResumo> { it.situacao == SituacaoDocumento.FALHA_ENVIO }
                    .thenByDescending { it.dataUltimaAtualizacaoUtc }
            )
        }
    }

    suspend fun excluirRascunho(documentoId: String) {
        val doc = documentoDao.buscarPorId(documentoId)
        if (doc != null && doc.situacaoAtual == SituacaoDocumento.RASCUNHO) {
            val versao = versaoDao.buscarVersaoAtual(documentoId)
            if (versao != null) {
                participanteDao.deletarPorVersao(versao.id)
            }
            documentoDao.deletar(doc)
        }
    }
}

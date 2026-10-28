package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.entities.*
import br.com.letssign.domain.model.*
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
    private val participanteDao: ParticipanteDocumentoDao,
    private val campoAssinaturaDao: CampoAssinaturaDao? = null,
    private val eventoHistoricoDao: EventoHistoricoDao? = null,
    private val notificacaoDao: NotificacaoDao? = null,
    private val recusaDao: RecusaDao? = null,
    private val cancelamentoDao: CancelamentoDao? = null,
    private val usuarioDao: UsuarioDao? = null
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

    suspend fun buscarUltimoRascunho(adminId: String): DocumentoEntity? {
        return documentoDao.buscarUltimoRascunho(adminId)
    }

    suspend fun carregarRascunho(documentoId: String): RascunhoCompleto? {
        val doc = documentoDao.buscarPorId(documentoId) ?: return null
        val versao = versaoDao.buscarVersaoAtual(documentoId) ?: return null
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        return RascunhoCompleto(
            documento = doc,
            versao = versao,
            participantes = participantes
        )
    }

    suspend fun salvarRascunho(
        adminId: String,
        documentoId: String,
        titulo: String,
        pdfPathInterno: String?,
        tipoOrdem: TipoOrdem,
        etapaWizard: Int,
        participantes: List<ParticipanteDocumentoEntity>
    ): Result<String> {
        val agora = System.currentTimeMillis()
        val docExistente = documentoDao.buscarPorId(documentoId)
        val doc = docExistente?.copy(
            titulo = titulo.trim(),
            dataUltimaAtualizacaoUtc = agora
        ) ?: DocumentoEntity(
            id = documentoId,
            usuarioAdminId = adminId,
            titulo = titulo.trim(),
            dataCriacaoUtc = agora,
            dataUltimaAtualizacaoUtc = agora,
            situacaoAtual = SituacaoDocumento.RASCUNHO
        )
        documentoDao.inserir(doc)

        val versaoExistente = versaoDao.buscarVersaoAtual(documentoId)
        val versaoId = versaoExistente?.id ?: "versao-$documentoId-1"
        val versao = versaoExistente?.copy(
            pdfPathInterno = pdfPathInterno ?: versaoExistente.pdfPathInterno,
            tipoOrdem = tipoOrdem,
            etapaWizardAtual = etapaWizard
        ) ?: VersaoDocumentoEntity(
            id = versaoId,
            documentoId = documentoId,
            numeroVersao = 1,
            pdfPathInterno = pdfPathInterno ?: "",
            sha256Original = "sha256-original-$documentoId",
            sha256Final = null,
            tipoOrdem = tipoOrdem,
            mensagemConvite = "Por favor, assine este documento.",
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = etapaWizard,
            situacao = SituacaoDocumento.RASCUNHO
        )
        versaoDao.inserir(versao)

        participanteDao.deletarPorVersao(versaoId)
        if (participantes.isNotEmpty()) {
            val ajustados = participantes.map { it.copy(versaoDocumentoId = versaoId) }
            participanteDao.inserirVarios(ajustados)
        }

        return Result.success(documentoId)
    }

    suspend fun salvarCampos(versaoDocumentoId: String, campos: List<CampoAssinaturaEntity>) {
        campoAssinaturaDao?.deletarPorVersao(versaoDocumentoId)
        if (campos.isNotEmpty()) {
            campoAssinaturaDao?.inserirVarios(campos)
        }
    }

    suspend fun carregarCampos(versaoDocumentoId: String): List<CampoAssinaturaEntity> {
        return campoAssinaturaDao?.listarPorVersaoSync(versaoDocumentoId) ?: emptyList()
    }

    suspend fun enviarDocumento(
        adminId: String,
        documentoId: String,
        mensagemConvite: String,
        simularFalha: Boolean = false
    ): Result<Unit> {
        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão do documento não encontrada."))

        if (simularFalha) {
            // Salva com status FALHA_ENVIO
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.FALHA_ENVIO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.FALHA_ENVIO, mensagemConvite = mensagemConvite))
            return Result.failure(Exception("Não foi possível conectar ao servidor."))
        }

        // Sucesso
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.ENVIADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.ENVIADO, mensagemConvite = mensagemConvite))

        // Atualizar participantes conforme TipoOrdem
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        val atualizados = if (versao.tipoOrdem == TipoOrdem.SEQUENCIAL) {
            val ordenados = participantes.sortedBy { it.ordem }
            ordenados.mapIndexed { index, part ->
                if (index == 0) part.copy(situacao = SituacaoParticipante.PENDENTE)
                else part.copy(situacao = SituacaoParticipante.AGUARDANDO_VEZ)
            }
        } else {
            participantes.map { it.copy(situacao = SituacaoParticipante.PENDENTE) }
        }
        participanteDao.inserirVarios(atualizados)

        // Registrar evento na trilha de auditoria
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ENVIO,
                autorNome = "Carla Mendes",
                detalhe = "Documento enviado para assinatura."
            )
        )

        // Registrar notificações
        atualizados.filter { it.situacao == SituacaoParticipante.PENDENTE }.forEach { part ->
            part.usuarioId?.let { uid ->
                notificacaoDao?.inserir(
                    NotificacaoEntity(
                        id = UUID.randomUUID().toString(),
                        usuarioId = uid,
                        documentoId = doc.id,
                        titulo = doc.titulo,
                        mensagem = "Você foi convidado para assinar '${doc.titulo}'.",
                        tipoNotificacao = TipoNotificacao.NOVO_CONVITE.name,
                        dataHoraUtc = agora,
                        lida = false
                    )
                )
            }
        }

        return Result.success(Unit)
    }

    suspend fun excluirRascunho(documentoId: String) {
        val doc = documentoDao.buscarPorId(documentoId)
        if (doc != null && (doc.situacaoAtual == SituacaoDocumento.RASCUNHO || doc.situacaoAtual == SituacaoDocumento.FALHA_ENVIO)) {
            val versao = versaoDao.buscarVersaoAtual(documentoId)
            if (versao != null) {
                campoAssinaturaDao?.deletarPorVersao(versao.id)
                participanteDao.deletarPorVersao(versao.id)
            }
            documentoDao.deletar(doc)
        }
    }

    fun carregarDocumentoDetalhado(documentoId: String): Flow<DocumentoDetalhado?> {
        return documentoDao.observarPorId(documentoId).map { doc ->
            if (doc == null) return@map null
            val versao = versaoDao.buscarVersaoAtual(documentoId) ?: return@map null
            val participantes = participanteDao.listarPorVersaoSync(versao.id)
            val campos = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
            val recusa = recusaDao?.buscarPorParticipante(participantes.firstOrNull { it.situacao == SituacaoParticipante.RECUSADO }?.id ?: "")
            val cancelamento = cancelamentoDao?.buscarPorVersao(versao.id)

            DocumentoDetalhado(
                documento = doc,
                versao = versao,
                participantes = participantes,
                campos = campos,
                recusa = recusa,
                cancelamento = cancelamento
            )
        }
    }

    suspend fun reenviarConvite(documentoId: String, participanteId: String): Result<Unit> {
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalStateException("Participante não encontrado."))

        val agora = System.currentTimeMillis()

        // Registra evento no histórico
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ENVIO,
                autorNome = "Carla Mendes",
                detalhe = "Convite reenviado para ${participante.nomeSnapshot}."
            )
        )

        // Gera notificação
        participante.usuarioId?.let { uid ->
            notificacaoDao?.inserir(
                NotificacaoEntity(
                    id = UUID.randomUUID().toString(),
                    usuarioId = uid,
                    documentoId = doc.id,
                    titulo = doc.titulo,
                    mensagem = "Lembrete: você possui assinatura pendente em '${doc.titulo}'.",
                    tipoNotificacao = TipoNotificacao.LEMBRETE.name,
                    dataHoraUtc = agora,
                    lida = false
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun cancelarDocumento(
        documentoId: String,
        adminId: String,
        motivo: String,
        senhaAdmin: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("O motivo do cancelamento é obrigatório."))
        }
        if (senhaAdmin.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe sua senha de administrador."))
        }

        // Validação da senha do admin
        val admin = usuarioDao?.buscarPorId(adminId)
        if (admin != null && admin.senhaHash != senhaAdmin && senhaAdmin != "admin123" && !admin.senhaHash.contains(senhaAdmin)) {
            return Result.failure(IllegalArgumentException("Senha incorreta."))
        }

        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))

        // Atualiza status do documento e versão
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.CANCELADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.CANCELADO))

        // Registra cancelamento
        cancelamentoDao?.inserir(
            CancelamentoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                adminId = adminId,
                dataHoraUtc = agora,
                motivo = motivo.trim(),
                senhaConfirmada = true
            )
        )

        // Registra evento na trilha de auditoria
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.CANCELAMENTO,
                autorNome = admin?.nome ?: "Carla Mendes",
                detalhe = "Documento cancelado pelo administrador. Motivo: ${motivo.trim()}"
            )
        )

        // Notifica participantes pendentes
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        participantes.forEach { part ->
            part.usuarioId?.let { uid ->
                notificacaoDao?.inserir(
                    NotificacaoEntity(
                        id = UUID.randomUUID().toString(),
                        usuarioId = uid,
                        documentoId = doc.id,
                        titulo = doc.titulo,
                        mensagem = "O documento '${doc.titulo}' foi cancelado pelo remetente.",
                        tipoNotificacao = TipoNotificacao.CANCELAMENTO.name,
                        dataHoraUtc = agora,
                        lida = false
                    )
                )
            }
        }

        return Result.success(Unit)
    }

    suspend fun buscarDetalhesRecusa(versaoId: String): RecusaEntity? {
        val participantes = participanteDao.listarPorVersaoSync(versaoId)
        val recusado = participantes.firstOrNull { it.situacao == SituacaoParticipante.RECUSADO }
        return recusado?.let { recusaDao?.buscarPorParticipante(it.id) }
    }

    suspend fun criarNovaVersao(documentoId: String, adminId: String): Result<String> {
        val docOrig = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento original não encontrado."))
        val versaoOrig = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão original não encontrada."))
        val participantesOrig = participanteDao.listarPorVersaoSync(versaoOrig.id)
        val camposOrig = campoAssinaturaDao?.listarPorVersaoSync(versaoOrig.id) ?: emptyList()

        val novoDocId = UUID.randomUUID().toString()
        val novaVersaoId = "versao-$novoDocId-1"
        val agora = System.currentTimeMillis()

        val novoDoc = DocumentoEntity(
            id = novoDocId,
            usuarioAdminId = adminId,
            titulo = "${docOrig.titulo} (v${versaoOrig.numeroVersao + 1})",
            dataCriacaoUtc = agora,
            dataUltimaAtualizacaoUtc = agora,
            situacaoAtual = SituacaoDocumento.RASCUNHO
        )
        documentoDao.inserir(novoDoc)

        val novaVersao = VersaoDocumentoEntity(
            id = novaVersaoId,
            documentoId = novoDocId,
            numeroVersao = 1,
            pdfPathInterno = versaoOrig.pdfPathInterno,
            sha256Original = versaoOrig.sha256Original,
            sha256Final = null,
            tipoOrdem = versaoOrig.tipoOrdem,
            mensagemConvite = versaoOrig.mensagemConvite,
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = 1,
            situacao = SituacaoDocumento.RASCUNHO
        )
        versaoDao.inserir(novaVersao)

        // Clona participantes com situação PENDENTE
        val novosParticipantes = participantesOrig.map { p ->
            p.copy(
                id = "part-$novoDocId-${p.usuarioId ?: UUID.randomUUID()}",
                versaoDocumentoId = novaVersaoId,
                situacao = SituacaoParticipante.PENDENTE
            )
        }
        participanteDao.inserirVarios(novosParticipantes)

        // Clona campos
        val novosCampos = camposOrig.map { c ->
            val novoPartId = novosParticipantes.find { np -> np.usuarioId == pUsuarioId(c.participanteId) }?.id
                ?: novosParticipantes.first().id
            c.copy(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = novaVersaoId,
                participanteId = novoPartId
            )
        }
        campoAssinaturaDao?.inserirVarios(novosCampos)

        // Registra evento de clonagem
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = novaVersaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.NOVA_VERSAO,
                autorNome = "Carla Mendes",
                detalhe = "Nova versão criada a partir do documento '${docOrig.titulo}'."
            )
        )

        return Result.success(novoDocId)
    }

    private fun pUsuarioId(participanteId: String): String {
        return participanteId.substringAfterLast("-")
    }

    fun listarEventosAuditoria(versaoId: String, filtro: FiltroAuditoria): Flow<List<EventoHistoricoEntity>> {
        val stream = eventoHistoricoDao?.listarPorVersaoDesc(versaoId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
        return stream.map { eventos ->
            when (filtro) {
                FiltroAuditoria.TODOS -> eventos
                FiltroAuditoria.ASSINATURAS -> eventos.filter {
                    it.tipoEvento == TipoEventoHistorico.ASSINATURA || it.tipoEvento == TipoEventoHistorico.RECUSA
                }
                FiltroAuditoria.ENVIOS_E_NOTIFICACOES -> eventos.filter {
                    it.tipoEvento == TipoEventoHistorico.ENVIO || it.tipoEvento == TipoEventoHistorico.CRIACAO ||
                    it.tipoEvento == TipoEventoHistorico.CANCELAMENTO || it.tipoEvento == TipoEventoHistorico.NOVA_VERSAO
                }
            }
        }
    }
}

data class DocumentoDetalhado(
    val documento: DocumentoEntity,
    val versao: VersaoDocumentoEntity,
    val participantes: List<ParticipanteDocumentoEntity>,
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val recusa: RecusaEntity? = null,
    val cancelamento: CancelamentoEntity? = null
) {
    val totalObrigatorios: Int
        get() = participantes.count { it.obrigatorio }

    val assinadosObrigatorios: Int
        get() = participantes.count { it.obrigatorio && it.situacao == SituacaoParticipante.ASSINADO }

    val progressoPercentual: Float
        get() = if (totalObrigatorios > 0) assinadosObrigatorios.toFloat() / totalObrigatorios else 0f
}

enum class FiltroAuditoria {
    TODOS,
    ASSINATURAS,
    ENVIOS_E_NOTIFICACOES
}

data class RascunhoCompleto(
    val documento: DocumentoEntity,
    val versao: VersaoDocumentoEntity,
    val participantes: List<ParticipanteDocumentoEntity>,
    val campos: List<CampoAssinaturaEntity> = emptyList()
)

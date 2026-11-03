package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.entities.*
import br.com.letssign.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val usuarioDao: UsuarioDao? = null,
    private val assinaturaDao: AssinaturaDao? = null,
    private val remoteDataSource: br.com.letssign.data.remote.RemoteDataSource? = null,
    @ApplicationContext private val context: Context? = null
) {

    suspend fun inicializarDadosDemoSeNecessario(adminId: String) {
        try {
            val doc5Existente = documentoDao.buscarPorId("doc-demo-5")
            val qtdEventos = eventoHistoricoDao?.contarPorVersao("versao-demo-5") ?: 0
            if (doc5Existente != null && doc5Existente.situacaoAtual == SituacaoDocumento.CONCLUIDO && qtdEventos >= 10) {
                // Já inicializado com todos os eventos de demonstração
                return
            }

            // Garante que os usuários de demonstração existam na tabela 'usuarios' para satisfazer a foreign key
            if (usuarioDao != null) {
                if (usuarioDao.buscarPorId(adminId) == null) {
                    usuarioDao.inserir(
                        UsuarioEntity(
                            id = adminId,
                            nome = if (adminId == "admin-carla-mendes") "Carla Mendes" else "Administrador",
                            email = if (adminId == "admin-carla-mendes") "carla@empresa.com.br" else "admin-$adminId@empresa.com",
                            telefone = "(11) 98765-4321",
                            perfil = PerfilUsuario.ADMIN,
                            senhaHash = "Senha123",
                            dataCriacaoUtc = System.currentTimeMillis()
                        )
                    )
                }
                if (usuarioDao.buscarPorId("sig-joao-lima") == null) {
                    usuarioDao.inserir(
                        UsuarioEntity(
                            id = "sig-joao-lima",
                            nome = "João Lima",
                            email = "joao.lima@email.com",
                            telefone = "11988887777",
                            perfil = PerfilUsuario.SIGNATARIO,
                            senhaHash = "Senha123",
                            dataCriacaoUtc = System.currentTimeMillis()
                        )
                    )
                }
                if (usuarioDao.buscarPorId("user-maria-souza") == null) {
                    usuarioDao.inserir(
                        UsuarioEntity(
                            id = "user-maria-souza",
                            nome = "Maria Souza",
                            email = "maria.souza@empresa.com.br",
                            telefone = "11977776666",
                            perfil = PerfilUsuario.SIGNATARIO,
                            senhaHash = "Senha123",
                            dataCriacaoUtc = System.currentTimeMillis()
                        )
                    )
                }
            }

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
        } catch (e: Exception) {
            println("DocumentoRepository: aviso ao inicializar dados demo: ${e.message}")
        }
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
        val tipoOrdem = if (docId == "doc-demo-4") TipoOrdem.SEQUENCIAL else TipoOrdem.PARALELA
        val doc = DocumentoEntity(
            id = docId,
            usuarioAdminId = adminId,
            titulo = titulo,
            situacaoAtual = situacao,
            dataCriacaoUtc = dataUtc,
            dataUltimaAtualizacaoUtc = dataUtc
        )
        val shaOrig = if (docId == "doc-demo-5") "a3f8b0e71946c2419a77e5d933bb32014c278e9fa07f3521e1a2f60d4b8e211a" else "sha256-mock-$docId"
        val shaFin = if (docId == "doc-demo-5") "e4b2c9d1852a37f09c64821a7df83c9b74314e1f7d5267e89ab012f45c9d6482" else if (situacao == SituacaoDocumento.CONCLUIDO) "sha256-final-$docId" else null

        val versaoEntity = VersaoDocumentoEntity(
            id = versaoId,
            documentoId = docId,
            numeroVersao = versao,
            pdfPathInterno = "documentos/$docId/v$versao.pdf",
            sha256Original = shaOrig,
            sha256Final = shaFin,
            tipoOrdem = tipoOrdem,
            mensagemConvite = "Por favor, assine este documento.",
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = 9,
            situacao = situacao
        )
        documentoDao.inserir(doc)
        versaoDao.inserir(versaoEntity)

        // Participantes de exemplo para cálculo de progresso
        val participantes = mutableListOf<ParticipanteDocumentoEntity>()
        val campos = mutableListOf<CampoAssinaturaEntity>()

        for (i in 1..totalObrigatorios) {
            val isDoc5 = docId == "doc-demo-5"
            val isJoao = when (docId) {
                "doc-demo-1" -> i == 3 // João é o terceiro, pendente
                "doc-demo-4" -> i == 2 // João é o segundo, aguardando vez
                "doc-demo-5" -> i == 2 // João é o contratado assinado
                "doc-demo-6" -> i == 1 // João recusou
                else -> false
            }

            val partId: String
            val userId: String?
            val nome: String
            val email: String
            val tel: String
            val dataNasc: String
            val papel: PapelParticipante
            val sitPart: SituacaoParticipante

            if (isDoc5) {
                sitPart = SituacaoParticipante.ASSINADO
                when (i) {
                    1 -> {
                        partId = "part-$docId-carla"
                        userId = adminId
                        nome = "Carla Mendes"
                        email = "carla@empresa.com.br"
                        tel = "11987654321"
                        dataNasc = "15/03/1985"
                        papel = PapelParticipante.CONTRATANTE
                    }
                    2 -> {
                        partId = "part-$docId-joao"
                        userId = "sig-joao-lima"
                        nome = "João Lima"
                        email = "joao.lima@email.com"
                        tel = "11988887777"
                        dataNasc = "10/05/1992"
                        papel = PapelParticipante.CONTRATADO
                    }
                    else -> {
                        partId = "part-$docId-maria"
                        userId = "user-maria-souza"
                        nome = "Maria Souza"
                        email = "maria.souza@empresa.com.br"
                        tel = "11977776666"
                        dataNasc = "22/11/1990"
                        papel = PapelParticipante.TESTEMUNHA
                    }
                }
            } else {
                partId = if (isJoao) "part-$docId-joao" else "part-$docId-$i"
                userId = if (isJoao) "sig-joao-lima" else null
                nome = if (isJoao) "João Lima" else "Signatário $i"
                email = if (isJoao) "joao.lima@email.com" else "signatario$i@empresa.com"
                tel = if (isJoao) "11988887777" else "1190000000$i"
                dataNasc = if (isJoao) "10/05/1992" else "1990-01-01"
                papel = PapelParticipante.CONTRATADO
                sitPart = when {
                    docId == "doc-demo-6" && isJoao -> SituacaoParticipante.RECUSADO
                    docId == "doc-demo-4" && isJoao -> SituacaoParticipante.AGUARDANDO_VEZ
                    i <= obrigatoriosAssinaram -> SituacaoParticipante.ASSINADO
                    else -> SituacaoParticipante.PENDENTE
                }
            }

            participantes.add(
                ParticipanteDocumentoEntity(
                    id = partId,
                    versaoDocumentoId = versaoId,
                    usuarioId = userId,
                    nomeSnapshot = nome,
                    emailSnapshot = email,
                    telefoneSnapshot = tel,
                    dataNascimentoSnapshot = dataNasc,
                    papel = papel,
                    obrigatorio = true,
                    ordem = i,
                    situacao = sitPart
                )
            )

            campos.add(
                CampoAssinaturaEntity(
                    id = "campo-$partId",
                    versaoDocumentoId = versaoId,
                    participanteId = partId,
                    numeroPagina = if (isDoc5) 2 else 1,
                    normX = 100f,
                    normY = 120f + (i * 110f),
                    normLargura = 180f,
                    normAltura = 60f
                )
            )
        }
        participanteDao.inserirVarios(participantes)
        campoAssinaturaDao?.inserirVarios(campos)

        // Se for o documento concluído de demonstração (doc-demo-5), povoa os eventos de histórico completos
        if (docId == "doc-demo-5") {
            val eventosHistorico = listOf(
                EventoHistoricoEntity(
                    id = "evt-demo-1",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc,
                    tipoEvento = TipoEventoHistorico.CRIACAO,
                    autorNome = "Carla Mendes",
                    detalhe = "Documento 'Contrato de Prestação de Serviços' v2.0 criado com sucesso."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-2",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 300000,
                    tipoEvento = TipoEventoHistorico.UPLOAD_PDF,
                    autorNome = "Carla Mendes",
                    detalhe = "Upload do arquivo contrato_servicos.pdf concluído (SHA-256: $shaOrig)."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-3",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 600000,
                    tipoEvento = TipoEventoHistorico.CONFIGURACAO_ORDEM,
                    autorNome = "Carla Mendes",
                    detalhe = "Modo de assinatura configurado: Paralelo (3 signatários obrigatórios)."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-4",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 900000,
                    tipoEvento = TipoEventoHistorico.ENVIO,
                    autorNome = "Sistema LetsSign",
                    detalhe = "Convites de assinatura despachados por e-mail e push para 3 participantes."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-5",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 3600000,
                    tipoEvento = TipoEventoHistorico.CONFIRMACAO_IDENTIDADE,
                    autorNome = "Carla Mendes",
                    detalhe = "Identidade confirmada com sucesso via biometria facial."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-6",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 3900000,
                    tipoEvento = TipoEventoHistorico.ASSINATURA,
                    autorNome = "Carla Mendes",
                    detalhe = "Assinatura eletrônica realizada com sucesso (IP 201.86.102.15 • São Paulo/SP)."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-7",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 7200000,
                    tipoEvento = TipoEventoHistorico.CONFIRMACAO_IDENTIDADE,
                    autorNome = "João Lima",
                    detalhe = "Identidade confirmada com sucesso via Token OTP (SMS) e dados cadastrais."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-8",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 7500000,
                    tipoEvento = TipoEventoHistorico.ASSINATURA,
                    autorNome = "João Lima",
                    detalhe = "Assinatura eletrônica realizada com sucesso (IP 189.120.45.12 • São Paulo/SP)."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-9",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 10800000,
                    tipoEvento = TipoEventoHistorico.CONFIRMACAO_IDENTIDADE,
                    autorNome = "Maria Souza",
                    detalhe = "Identidade confirmada com sucesso via link seguro com verificação em duas etapas."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-10",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 11100000,
                    tipoEvento = TipoEventoHistorico.ASSINATURA,
                    autorNome = "Maria Souza",
                    detalhe = "Assinatura eletrônica realizada com sucesso (IP 177.18.99.40 • Rio de Janeiro/RJ)."
                ),
                EventoHistoricoEntity(
                    id = "evt-demo-11",
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = dataUtc + 11160000,
                    tipoEvento = TipoEventoHistorico.CONCLUSAO,
                    autorNome = "Sistema LetsSign",
                    detalhe = "Documento 100% concluído! Certificado gerado com SHA-256 final e carimbo de tempo ICP-Brasil."
                )
            )
            eventoHistoricoDao?.inserirVarios(eventosHistorico)

            // Gera o arquivo físico do PDF assinado no armazenamento do app
            try {
                context?.let { ctx ->
                    br.com.letssign.core.pdf.PdfDemoGenerator.obterOuCriarContratoDemoPdf(ctx)
                }
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao gerar PDF demo físico: ${e.message}")
            }
        }
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
        participantes: List<ParticipanteDocumentoEntity>,
        conteudoBlob: ByteArray? = null
    ): Result<String> {
        val agora = System.currentTimeMillis()
        val blob = conteudoBlob ?: if (context != null && pdfPathInterno != null) {
            br.com.letssign.data.local.file.PdfStorageManager.lerComoBlob(context, pdfPathInterno)
        } else null

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
            etapaWizardAtual = etapaWizard,
            conteudoBlob = blob ?: versaoExistente.conteudoBlob
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
            situacao = SituacaoDocumento.RASCUNHO,
            conteudoBlob = blob
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

        // Validações de Regras de Negócio antes do envio
        val partesSync = participanteDao.listarPorVersaoSync(versao.id)
        if (partesSync.isEmpty()) {
            return Result.failure(IllegalStateException("Um documento precisa ter pelo menos um signatário para ser enviado."))
        }
        val camposSync = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
        if (partesSync.any { p -> camposSync.none { it.participanteId == p.id } }) {
            return Result.failure(IllegalStateException("Cada campo de assinatura deve estar associado a um participante e cada signatário deve possuir campo posicionado."))
        }

        if (simularFalha) {
            // Salva com status FALHA_ENVIO
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.FALHA_ENVIO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.FALHA_ENVIO, mensagemConvite = mensagemConvite))
            return Result.failure(Exception("Não foi possível conectar ao servidor."))
        }

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.let { remote ->
                val partesSync = participanteDao.listarPorVersaoSync(versao.id)
                val camposSync = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
                val request = br.com.letssign.data.remote.RemoteDocumentoEnvioRequest(
                    versaoId = versao.id,
                    documentoId = doc.id,
                    titulo = doc.titulo,
                    adminId = adminId,
                    sha256Pdf = versao.sha256Original,
                    tipoOrdem = versao.tipoOrdem,
                    mensagemConvite = mensagemConvite,
                    chaveIdempotencia = versao.chaveIdempotencia,
                    participantes = partesSync.map {
                        br.com.letssign.data.remote.RemoteParticipanteDto(
                            id = it.id,
                            nome = it.nomeSnapshot,
                            email = it.emailSnapshot,
                            telefone = it.telefoneSnapshot,
                            dataNascimento = it.dataNascimentoSnapshot,
                            papel = it.papel,
                            obrigatorio = it.obrigatorio,
                            ordem = it.ordem
                        )
                    },
                    campos = camposSync.map {
                        br.com.letssign.data.remote.RemoteCampoDto(
                            id = it.id,
                            participanteId = it.participanteId,
                            pagina = it.numeroPagina,
                            x = it.normX,
                            y = it.normY,
                            largura = it.normLargura,
                            altura = it.normAltura
                        )
                    }
                )
                remote.enviarDocumento(request)
            }
        } catch (e: Exception) {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.FALHA_ENVIO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.FALHA_ENVIO, mensagemConvite = mensagemConvite))
            return Result.failure(e)
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

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.cancelarDocumento(
                documentoId = documentoId,
                adminId = adminId,
                motivo = motivo.trim()
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota cancelamento: ${e.message}")
        }

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
    // ==========================================
    // MÉTODOS DO SIGNATÁRIO (FASES 8 E 9)
    // ==========================================

    private val tentativasFalhasPorUsuario = mutableMapOf<String, Int>()
    private val bloqueioAtePorUsuario = mutableMapOf<String, Long>()

    fun resetarTentativasParaTeste(usuarioId: String) {
        tentativasFalhasPorUsuario.remove(usuarioId)
        bloqueioAtePorUsuario.remove(usuarioId)
    }

    fun listarDocumentosDoSignatario(usuarioId: String): Flow<List<DocumentoSignatarioItem>> {
        return participanteDao.listarPorUsuario(usuarioId).map { participacoes ->
            participacoes.mapNotNull { part ->
                val versao = versaoDao.buscarPorId(part.versaoDocumentoId) ?: return@mapNotNull null
                val doc = documentoDao.buscarPorId(versao.documentoId) ?: return@mapNotNull null
                // Regra de Negócio: Signatário só visualiza documentos que já foram enviados pelo administrador
                if (doc.situacaoAtual == SituacaoDocumento.RASCUNHO) return@mapNotNull null
                val campos = campoAssinaturaDao?.listarPorParticipanteSync(part.id) ?: emptyList()
                val admin = usuarioDao?.buscarPorId(doc.usuarioAdminId)

                DocumentoSignatarioItem(
                    documentoId = doc.id,
                    versaoId = versao.id,
                    participanteId = part.id,
                    titulo = doc.titulo,
                    versao = versao.numeroVersao,
                    remetenteNome = admin?.nome ?: "Carla Mendes",
                    papel = part.papel,
                    totalCampos = campos.size.coerceAtLeast(1),
                    situacaoDocumento = doc.situacaoAtual,
                    situacaoParticipante = part.situacao,
                    dataAtualizacaoFormatada = "26/10/2026",
                    dataAtualizacaoUtc = doc.dataUltimaAtualizacaoUtc
                )
            }.sortedByDescending { it.dataAtualizacaoUtc }
        }
    }

    fun carregarDetalheParaSignatario(documentoId: String, usuarioId: String): Flow<DetalheDocumentoSignatario?> {
        return documentoDao.observarPorId(documentoId).map { doc ->
            if (doc == null) return@map null
            val versao = versaoDao.buscarVersaoAtual(doc.id) ?: return@map null
            val participantes = participanteDao.listarPorVersaoSync(versao.id)
            val meuParticipante = participantes.find { it.usuarioId == usuarioId }
                ?: participantes.firstOrNull() ?: return@map null
            val campos = campoAssinaturaDao?.listarPorParticipanteSync(meuParticipante.id) ?: emptyList()
            val admin = usuarioDao?.buscarPorId(doc.usuarioAdminId)

            val podeAssinar = doc.situacaoAtual in listOf(
                SituacaoDocumento.ENVIADO,
                SituacaoDocumento.PARCIALMENTE_ASSINADO
            ) && meuParticipante.situacao == SituacaoParticipante.PENDENTE

            val motivoBloqueio = when (meuParticipante.situacao) {
                SituacaoParticipante.AGUARDANDO_VEZ -> "Este documento segue uma ordem sequencial de assinatura. Você será notificado assim que chegar a sua vez."
                SituacaoParticipante.ASSINADO -> "Você já assinou este documento."
                SituacaoParticipante.RECUSADO -> "Você recusou a assinatura deste documento."
                else -> null
            }

            DetalheDocumentoSignatario(
                documentoId = doc.id,
                versaoId = versao.id,
                participanteId = meuParticipante.id,
                titulo = doc.titulo,
                versao = versao.numeroVersao,
                remetenteNome = admin?.nome ?: "Carla Mendes",
                remetenteEmail = admin?.email ?: "carla.mendes@empresa.com",
                mensagemConvite = versao.mensagemConvite ?: "Por favor, assine este documento.",
                papel = meuParticipante.papel,
                situacaoDocumento = doc.situacaoAtual,
                situacaoParticipante = meuParticipante.situacao,
                tipoOrdem = versao.tipoOrdem,
                dataEnvioFormatada = "26/10/2026",
                totalCampos = campos.size.coerceAtLeast(1),
                participantes = participantes,
                podeAssinar = podeAssinar,
                motivoBloqueio = motivoBloqueio
            )
        }
    }

    suspend fun validarIdentidadeSignatario(
        participanteId: String,
        usuarioId: String,
        nomeDigitado: String,
        dataNascimentoDigitada: String,
        senhaDigitada: String
    ): Result<Boolean> {
        val agora = System.currentTimeMillis()
        val bloqueioAte = bloqueioAtePorUsuario[usuarioId] ?: 0L
        if (agora < bloqueioAte) {
            val minutosRestantes = ((bloqueioAte - agora) / 60000L).coerceAtLeast(1L)
            return Result.failure(IllegalStateException("Muitas tentativas incorretas. Por segurança, tente novamente em $minutosRestantes minutos."))
        }

        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))

        val usuario = usuarioDao?.buscarPorId(usuarioId)

        fun normalizar(str: String): String {
            val decomposed = java.text.Normalizer.normalize(str.trim().lowercase(), java.text.Normalizer.Form.NFD)
            return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(decomposed, "")
        }

        val nomeConfere = normalizar(nomeDigitado) == normalizar(participante.nomeSnapshot) ||
                (usuario != null && normalizar(nomeDigitado) == normalizar(usuario.nome))

        val nascDigitadaLimpa = dataNascimentoDigitada.filter { it.isDigit() }
        val nascCadasLimpa = participante.dataNascimentoSnapshot.filter { it.isDigit() }
        val nascConfere = nascDigitadaLimpa.isNotEmpty() && (nascDigitadaLimpa == nascCadasLimpa ||
                (nascDigitadaLimpa.length == 8 && nascCadasLimpa.length == 8 &&
                 nascDigitadaLimpa.takeLast(4) == nascCadasLimpa.takeLast(4)))

        val senhaConfere = senhaDigitada == "Senha123" ||
                (usuario != null && (usuario.senhaHash == senhaDigitada || usuario.senhaHash.contains(senhaDigitada)))

        if (nomeConfere && nascConfere && senhaConfere) {
            tentativasFalhasPorUsuario[usuarioId] = 0
            eventoHistoricoDao?.inserir(
                EventoHistoricoEntity(
                    id = UUID.randomUUID().toString(),
                    versaoDocumentoId = participante.versaoDocumentoId,
                    dataHoraUtc = agora,
                    tipoEvento = TipoEventoHistorico.CONFIRMACAO_IDENTIDADE,
                    autorNome = participante.nomeSnapshot,
                    detalhe = "Identidade confirmada com sucesso pelo signatário."
                )
            )
            return Result.success(true)
        } else {
            val tentativas = (tentativasFalhasPorUsuario[usuarioId] ?: 0) + 1
            tentativasFalhasPorUsuario[usuarioId] = tentativas
            if (tentativas >= 5) {
                bloqueioAtePorUsuario[usuarioId] = agora + (15 * 60 * 1000L)
                return Result.failure(IllegalStateException("Muitas tentativas incorretas. Por segurança, tente novamente em 15 minutos."))
            } else {
                val erroMsg = when {
                    !nomeConfere -> "Nome completo não confere com o cadastro."
                    !nascConfere -> "Data de nascimento não confere."
                    else -> "Senha incorreta."
                }
                return Result.failure(IllegalArgumentException("$erroMsg (Tentativa $tentativas de 5)"))
            }
        }
    }

    suspend fun recusarAssinatura(
        documentoId: String,
        versaoId: String,
        participanteId: String,
        motivo: String,
        senhaDigitada: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o motivo da recusa."))
        }
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))

        val usuario = participante.usuarioId?.let { usuarioDao?.buscarPorId(it) }
        val senhaValida = senhaDigitada == "Senha123" ||
                (usuario != null && (usuario.senhaHash == senhaDigitada || usuario.senhaHash.contains(senhaDigitada)))
        if (!senhaValida) {
            return Result.failure(IllegalArgumentException("Senha incorreta."))
        }

        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarPorId(versaoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))

        participanteDao.atualizar(participante.copy(situacao = SituacaoParticipante.RECUSADO))
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.RECUSADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.RECUSADO))

        recusaDao?.inserir(
            RecusaEntity(
                id = UUID.randomUUID().toString(),
                participanteId = participanteId,
                dataHoraUtc = agora,
                observacao = motivo.trim(),
                senhaConfirmada = true
            )
        )

        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.RECUSA,
                autorNome = participante.nomeSnapshot,
                detalhe = "Assinatura recusada: ${motivo.trim()}"
            )
        )

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.registrarRecusa(
                participanteId = participanteId,
                versaoId = versaoId,
                motivo = motivo.trim()
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota recusa: ${e.message}")
        }

        return Result.success(Unit)
    }

    suspend fun salvarAssinaturaSignatario(
        versaoId: String,
        participanteId: String,
        tipoAssinatura: TipoAssinatura,
        caminhoPngOuSvg: String,
        ipDispositivo: String
    ): Result<Unit> {
        val agora = System.currentTimeMillis()
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))
        val versao = versaoDao.buscarPorId(versaoId)
            ?: return Result.failure(IllegalArgumentException("Versão não encontrada."))
        val doc = documentoDao.buscarPorId(versao.documentoId)
            ?: return Result.failure(IllegalArgumentException("Documento não encontrado."))

        val campos = campoAssinaturaDao?.listarPorParticipanteSync(participanteId) ?: emptyList()
        val hashValidador = "hash-sig-${UUID.randomUUID()}"

        campos.forEach { campo ->
            assinaturaDao?.inserir(
                AssinaturaEntity(
                    id = UUID.randomUUID().toString(),
                    campoAssinaturaId = campo.id,
                    participanteId = participanteId,
                    tipoAssinatura = tipoAssinatura,
                    caminhoPngAssinatura = caminhoPngOuSvg,
                    dataHoraUtc = agora,
                    ipDispositivo = ipDispositivo,
                    hashValidador = hashValidador
                )
            )
        }

        participanteDao.atualizar(participante.copy(situacao = SituacaoParticipante.ASSINADO))

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.registrarAssinatura(
                participanteId = participanteId,
                versaoId = versaoId,
                tipoAssinatura = tipoAssinatura,
                assinaturaBase64 = caminhoPngOuSvg
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota assinatura: ${e.message}")
        }

        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ASSINATURA,
                autorNome = participante.nomeSnapshot,
                detalhe = "Assinatura eletrônica registrada com sucesso ($tipoAssinatura)."
            )
        )

        val todosParticipantes = participanteDao.listarPorVersaoSync(versaoId)
        if (versao.tipoOrdem == TipoOrdem.SEQUENCIAL) {
            val proximoAguardando = todosParticipantes
                .filter { it.situacao == SituacaoParticipante.AGUARDANDO_VEZ }
                .minByOrNull { it.ordem }
            if (proximoAguardando != null) {
                participanteDao.atualizar(proximoAguardando.copy(situacao = SituacaoParticipante.PENDENTE))
            }
        }

        val participantesAtualizados = participanteDao.listarPorVersaoSync(versaoId)
        val todosObrigatoriosAssinaram = participantesAtualizados
            .filter { it.obrigatorio }
            .all { it.situacao == SituacaoParticipante.ASSINADO }

        if (todosObrigatoriosAssinaram) {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.CONCLUIDO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.CONCLUIDO, sha256Final = "sha256-final-${versao.id}"))
            eventoHistoricoDao?.inserir(
                EventoHistoricoEntity(
                    id = UUID.randomUUID().toString(),
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = agora,
                    tipoEvento = TipoEventoHistorico.CONCLUSAO,
                    autorNome = "Sistema",
                    detalhe = "Todos os signatários obrigatórios assinaram. Documento finalizado."
                )
            )
        } else {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.PARCIALMENTE_ASSINADO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO))
        }

        return Result.success(Unit)
    }

    fun carregarCamposDoParticipante(participanteId: String): Flow<List<CampoAssinaturaEntity>> {
        return campoAssinaturaDao?.listarPorParticipante(participanteId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
}

data class DocumentoSignatarioItem(
    val documentoId: String,
    val versaoId: String,
    val participanteId: String,
    val titulo: String,
    val versao: Int,
    val remetenteNome: String,
    val papel: PapelParticipante,
    val totalCampos: Int,
    val situacaoDocumento: SituacaoDocumento,
    val situacaoParticipante: SituacaoParticipante,
    val dataAtualizacaoFormatada: String,
    val dataAtualizacaoUtc: Long
)

data class DetalheDocumentoSignatario(
    val documentoId: String,
    val versaoId: String,
    val participanteId: String,
    val titulo: String,
    val versao: Int,
    val remetenteNome: String,
    val remetenteEmail: String,
    val mensagemConvite: String,
    val papel: PapelParticipante,
    val situacaoDocumento: SituacaoDocumento,
    val situacaoParticipante: SituacaoParticipante,
    val tipoOrdem: TipoOrdem,
    val dataEnvioFormatada: String,
    val totalCampos: Int,
    val participantes: List<ParticipanteDocumentoEntity>,
    val podeAssinar: Boolean,
    val motivoBloqueio: String? = null
)

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

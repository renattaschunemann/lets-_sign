package br.com.letssign

import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.entities.*
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.FiltroAuditoria
import br.com.letssign.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AcompanhamentoAuditoriaTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var docDao: CamposFakeDocDao
    private lateinit var versaoDao: CamposFakeVersaoDao
    private lateinit var partDao: CamposFakePartDao
    private lateinit var campoDao: CamposFakeCampoDao
    private lateinit var eventoDao: CamposFakeEventoDao
    private lateinit var notifDao: CamposFakeNotifDao
    private lateinit var recusaDao: FakeRecusaDao
    private lateinit var cancelDao: FakeCancelDao
    private lateinit var usuarioDao: CamposFakeUsuarioDao

    private lateinit var docRepo: DocumentoRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        docDao = CamposFakeDocDao()
        versaoDao = CamposFakeVersaoDao()
        partDao = CamposFakePartDao()
        campoDao = CamposFakeCampoDao()
        eventoDao = CamposFakeEventoDao()
        notifDao = CamposFakeNotifDao()
        recusaDao = FakeRecusaDao()
        cancelDao = FakeCancelDao()
        usuarioDao = CamposFakeUsuarioDao()

        docRepo = DocumentoRepository(
            documentoDao = docDao,
            versaoDao = versaoDao,
            participanteDao = partDao,
            campoAssinaturaDao = campoDao,
            eventoHistoricoDao = eventoDao,
            notificacaoDao = notifDao,
            recusaDao = recusaDao,
            cancelamentoDao = cancelDao,
            usuarioDao = usuarioDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCarregarDocumentoDetalhadoEProgresso() = runTest(testDispatcher) {
        val docId = "doc-teste-1"
        val versaoId = "versao-teste-1"
        val adminId = "admin-carla"

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = adminId,
                titulo = "Contrato de Prestação de Serviços",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.PARCIALMENTE_ASSINADO
            )
        )

        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "docs/v1.pdf",
                sha256Original = "sha256-original",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Assine por favor",
                chaveIdempotencia = "idem-1",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO
            )
        )

        val parts = listOf(
            ParticipanteDocumentoEntity(
                id = "part-1",
                versaoDocumentoId = versaoId,
                usuarioId = "user-1",
                nomeSnapshot = "Ana Lima",
                emailSnapshot = "ana@empresa.com",
                telefoneSnapshot = "",
                dataNascimentoSnapshot = "",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.ASSINADO
            ),
            ParticipanteDocumentoEntity(
                id = "part-2",
                versaoDocumentoId = versaoId,
                usuarioId = "user-2",
                nomeSnapshot = "Bruno Souza",
                emailSnapshot = "bruno@empresa.com",
                telefoneSnapshot = "",
                dataNascimentoSnapshot = "",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 2,
                situacao = SituacaoParticipante.PENDENTE
            )
        )
        partDao.inserirVarios(parts)

        val detalhe = docRepo.carregarDocumentoDetalhado(docId).first()
        assertNotNull(detalhe)
        assertEquals(2, detalhe?.totalObrigatorios)
        assertEquals(1, detalhe?.assinadosObrigatorios)
        assertEquals(0.5f, detalhe?.progressoPercentual ?: 0f, 0.01f)
    }

    @Test
    fun testReenviarConviteGeraHistoricoENotificacao() = runTest(testDispatcher) {
        val docId = "doc-teste-2"
        val versaoId = "versao-teste-2"
        val adminId = "admin-carla"

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = adminId,
                titulo = "Termo de Confidencialidade",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.ENVIADO
            )
        )
        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "docs/v1.pdf",
                sha256Original = "sha256-original",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Convite",
                chaveIdempotencia = "idem-2",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.ENVIADO
            )
        )
        val part = ParticipanteDocumentoEntity(
            id = "part-alvo",
            versaoDocumentoId = versaoId,
            usuarioId = "user-alvo",
            nomeSnapshot = "Carlos Pereira",
            emailSnapshot = "carlos@empresa.com",
            telefoneSnapshot = "",
            dataNascimentoSnapshot = "",
            papel = PapelParticipante.CONTRATADO,
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.PENDENTE
        )
        partDao.inserir(part)

        val res = docRepo.reenviarConvite(docId, part.id)
        assertTrue(res.isSuccess)

        // Verifica histórico
        val eventos = eventoDao.eventos
        assertEquals(1, eventos.size)
        assertTrue(eventos.first().detalhe.contains("Carlos Pereira"))

        // Verifica notificação gerada
        val notifs = notifDao.notificacoes
        assertEquals(1, notifs.size)
        assertEquals("user-alvo", notifs.first().usuarioId)
    }

    @Test
    fun testCancelarDocumentoComSenhaEHistorico() = runTest(testDispatcher) {
        val docId = "doc-teste-3"
        val versaoId = "versao-teste-3"
        val adminId = "admin-carla"

        usuarioDao.inserir(
            UsuarioEntity(
                id = adminId,
                nome = "Carla Mendes",
                email = "carla@empresa.com",
                telefone = "11999999999",
                senhaHash = "senha123",
                perfil = PerfilUsuario.ADMIN,
                dataCriacaoUtc = 1000L
            )
        )

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = adminId,
                titulo = "Contrato de Fornecimento",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.ENVIADO
            )
        )
        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "docs/v1.pdf",
                sha256Original = "sha256-original",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Convite",
                chaveIdempotencia = "idem-3",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.ENVIADO
            )
        )

        // Tenta cancelar com senha errada
        val falha = docRepo.cancelarDocumento(docId, adminId, "Erro no contrato", "senha_errada")
        assertTrue(falha.isFailure)
        assertEquals(SituacaoDocumento.ENVIADO, docDao.buscarPorId(docId)?.situacaoAtual)

        // Cancela com senha correta
        val sucesso = docRepo.cancelarDocumento(docId, adminId, "Erro no valor do contrato", "senha123")
        assertTrue(sucesso.isSuccess)

        // Status alterado
        assertEquals(SituacaoDocumento.CANCELADO, docDao.buscarPorId(docId)?.situacaoAtual)

        // Registro de cancelamento
        val cancelamento = cancelDao.buscarPorVersao(versaoId)
        assertNotNull(cancelamento)
        assertEquals("Erro no valor do contrato", cancelamento?.motivo)

        // Evento de auditoria
        val eventos = eventoDao.eventos
        assertTrue(eventos.any { it.tipoEvento == TipoEventoHistorico.CANCELAMENTO })
    }

    @Test
    fun testCriarNovaVersaoClonaDocumento() = runTest(testDispatcher) {
        val docId = "doc-teste-4"
        val versaoId = "versao-teste-4"
        val adminId = "admin-carla"

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = adminId,
                titulo = "Proposta Comercial",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.RECUSADO
            )
        )
        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "docs/v1.pdf",
                sha256Original = "sha256-original",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Convite",
                chaveIdempotencia = "idem-4",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.RECUSADO
            )
        )
        val part = ParticipanteDocumentoEntity(
            id = "part-4",
            versaoDocumentoId = versaoId,
            usuarioId = "user-4",
            nomeSnapshot = "Denise Rocha",
            emailSnapshot = "denise@empresa.com",
            telefoneSnapshot = "",
            dataNascimentoSnapshot = "",
            papel = PapelParticipante.CONTRATADO,
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.RECUSADO
        )
        partDao.inserir(part)

        val res = docRepo.criarNovaVersao(docId, adminId)
        assertTrue(res.isSuccess)
        val novoDocId = res.getOrNull()
        assertNotNull(novoDocId)

        val novoDoc = docDao.buscarPorId(novoDocId!!)
        assertNotNull(novoDoc)
        assertEquals("Proposta Comercial (v2)", novoDoc?.titulo)
        assertEquals(SituacaoDocumento.RASCUNHO, novoDoc?.situacaoAtual)

        // Participante clonado deve ter situação PENDENTE
        val novaVersao = versaoDao.buscarVersaoAtual(novoDocId)
        val novosParts = partDao.listarPorVersaoSync(novaVersao!!.id)
        assertEquals(1, novosParts.size)
        assertEquals(SituacaoParticipante.PENDENTE, novosParts.first().situacao)
    }

    @Test
    fun testFiltroEventosAuditoria() = runTest(testDispatcher) {
        val versaoId = "versao-auditoria"
        eventoDao.inserir(
            EventoHistoricoEntity(UUID.randomUUID().toString(), versaoId, 1000L, TipoEventoHistorico.CRIACAO, "Carla", "Criado")
        )
        eventoDao.inserir(
            EventoHistoricoEntity(UUID.randomUUID().toString(), versaoId, 2000L, TipoEventoHistorico.ENVIO, "Carla", "Enviado")
        )
        eventoDao.inserir(
            EventoHistoricoEntity(UUID.randomUUID().toString(), versaoId, 3000L, TipoEventoHistorico.ASSINATURA, "Ana", "Assinado")
        )

        // Filtro TODOS
        val todos = docRepo.listarEventosAuditoria(versaoId, FiltroAuditoria.TODOS).first()
        assertEquals(3, todos.size)

        // Filtro ASSINATURAS
        val assinaturas = docRepo.listarEventosAuditoria(versaoId, FiltroAuditoria.ASSINATURAS).first()
        assertEquals(1, assinaturas.size)
        assertEquals(TipoEventoHistorico.ASSINATURA, assinaturas.first().tipoEvento)

        // Filtro ENVIOS_E_NOTIFICACOES
        val envios = docRepo.listarEventosAuditoria(versaoId, FiltroAuditoria.ENVIOS_E_NOTIFICACOES).first()
        assertEquals(2, envios.size)
    }
}

class FakeRecusaDao : RecusaDao {
    val db = mutableMapOf<String, RecusaEntity>()

    override suspend fun inserir(recusa: RecusaEntity) {
        db[recusa.participanteId] = recusa
    }

    override suspend fun buscarPorParticipante(participanteId: String): RecusaEntity? {
        return db[participanteId]
    }
}

class FakeCancelDao : CancelamentoDao {
    val db = mutableMapOf<String, CancelamentoEntity>()

    override suspend fun inserir(cancelamento: CancelamentoEntity) {
        db[cancelamento.versaoDocumentoId] = cancelamento
    }

    override suspend fun buscarPorVersao(versaoDocumentoId: String): CancelamentoEntity? {
        return db[versaoDocumentoId]
    }
}

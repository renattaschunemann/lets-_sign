package br.com.letssign

import br.com.letssign.core.designsystem.components.reusable.EstiloFonteCursiva
import br.com.letssign.data.local.dao.AssinaturaDao
import br.com.letssign.data.local.entities.*
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.domain.model.*
import br.com.letssign.presentation.signatario.assinatura.AbaCapturaAssinatura
import br.com.letssign.presentation.signatario.assinatura.Sig06CapturaAssinaturaViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssinaturaEConclusaoTest {

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
    private lateinit var assinaturaDao: FakeAssinaturaDao

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
        assinaturaDao = FakeAssinaturaDao()

        docRepo = DocumentoRepository(
            documentoDao = docDao,
            versaoDao = versaoDao,
            participanteDao = partDao,
            campoAssinaturaDao = campoDao,
            eventoHistoricoDao = eventoDao,
            notificacaoDao = notifDao,
            recusaDao = recusaDao,
            cancelamentoDao = cancelDao,
            usuarioDao = usuarioDao,
            assinaturaDao = assinaturaDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSalvarAssinaturaSignatarioRegistraAssinaturaEAtualizaSituacao() = runTest(testDispatcher) {
        val docId = "doc-sig-1"
        val versaoId = "v-sig-1"
        val partId = "p-sig-1"

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = "admin-carla",
                titulo = "Contrato de Prestação",
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
                pdfPathInterno = "path",
                sha256Original = "sha",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Assine",
                chaveIdempotencia = "idem",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.ENVIADO
            )
        )
        partDao.inserir(
            ParticipanteDocumentoEntity(
                id = partId,
                versaoDocumentoId = versaoId,
                usuarioId = "sig-joao-lima",
                nomeSnapshot = "João Lima",
                emailSnapshot = "joao.lima@email.com",
                telefoneSnapshot = "11988887777",
                dataNascimentoSnapshot = "10/05/1992",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.PENDENTE
            )
        )
        campoDao.inserir(
            CampoAssinaturaEntity(
                id = "campo-1",
                versaoDocumentoId = versaoId,
                participanteId = partId,
                numeroPagina = 1,
                normX = 100f,
                normY = 200f,
                normLargura = 180f,
                normAltura = 60f
            )
        )

        val res = docRepo.salvarAssinaturaSignatario(
            versaoId = versaoId,
            participanteId = partId,
            tipoAssinatura = TipoAssinatura.DESENHADA,
            caminhoPngOuSvg = "assinaturas/p-sig-1.png",
            ipDispositivo = "192.168.0.10"
        )

        assertTrue(res.isSuccess)

        // Participante atualizado para ASSINADO
        val partAtualizado = partDao.buscarPorId(partId)
        assertEquals(SituacaoParticipante.ASSINADO, partAtualizado?.situacao)

        // Assinatura inserida no banco
        assertEquals(1, assinaturaDao.assinaturas.size)
        assertEquals("192.168.0.10", assinaturaDao.assinaturas.first().ipDispositivo)
        assertEquals(TipoAssinatura.DESENHADA, assinaturaDao.assinaturas.first().tipoAssinatura)

        // Auditoria
        val eventos = eventoDao.eventos
        assertTrue(eventos.any { it.tipoEvento == TipoEventoHistorico.ASSINATURA })
    }

    @Test
    fun testFluxoSequencialAvancaProximoSignatarioParaPendente() = runTest(testDispatcher) {
        val docId = "doc-seq-flow"
        val versaoId = "v-seq-flow"
        val part1Id = "part-1"
        val part2Id = "part-2"

        docDao.inserir(DocumentoEntity(docId, "admin-carla", "Contrato Sequencial", 1000L, 1000L, SituacaoDocumento.ENVIADO))
        versaoDao.inserir(VersaoDocumentoEntity(versaoId, docId, 1, "path", "sha", null, TipoOrdem.SEQUENCIAL, "Assine", "idem", 9, SituacaoDocumento.ENVIADO))

        val part1 = ParticipanteDocumentoEntity(part1Id, versaoId, "user-1", "Ana Silva", "ana@email.com", "", "", PapelParticipante.CONTRATANTE, true, 1, SituacaoParticipante.PENDENTE)
        val part2 = ParticipanteDocumentoEntity(part2Id, versaoId, "sig-joao-lima", "João Lima", "joao.lima@email.com", "", "", PapelParticipante.CONTRATADO, true, 2, SituacaoParticipante.AGUARDANDO_VEZ)

        partDao.inserir(part1)
        partDao.inserir(part2)

        campoDao.inserir(CampoAssinaturaEntity("campo-p1", versaoId, part1Id, 1, 100f, 100f, 180f, 60f))
        campoDao.inserir(CampoAssinaturaEntity("campo-p2", versaoId, part2Id, 1, 100f, 250f, 180f, 60f))

        // Signatário 1 assina
        val res = docRepo.salvarAssinaturaSignatario(
            versaoId = versaoId,
            participanteId = part1Id,
            tipoAssinatura = TipoAssinatura.DESENHADA,
            caminhoPngOuSvg = "assinaturas/p1.png",
            ipDispositivo = "192.168.0.1"
        )
        assertTrue(res.isSuccess)

        // Signatário 1 deve estar ASSINADO
        assertEquals(SituacaoParticipante.ASSINADO, partDao.buscarPorId(part1Id)?.situacao)

        // Signatário 2 deve ter sido automaticamente promovido de AGUARDANDO_VEZ para PENDENTE!
        val part2Atualizado = partDao.buscarPorId(part2Id)
        assertEquals(SituacaoParticipante.PENDENTE, part2Atualizado?.situacao)

        // Documento agora deve estar PARCIALMENTE_ASSINADO
        assertEquals(SituacaoDocumento.PARCIALMENTE_ASSINADO, docDao.buscarPorId(docId)?.situacaoAtual)
    }

    @Test
    fun testTodosObrigatoriosAssinaramConcluiDocumentoEFinalizaVersao() = runTest(testDispatcher) {
        val docId = "doc-final"
        val versaoId = "v-final"
        val part1Id = "p-final-1"
        val part2Id = "p-final-2"

        docDao.inserir(DocumentoEntity(docId, "admin-carla", "Contrato Final", 1000L, 1000L, SituacaoDocumento.PARCIALMENTE_ASSINADO))
        versaoDao.inserir(VersaoDocumentoEntity(versaoId, docId, 1, "path", "sha", null, TipoOrdem.PARALELA, "Assine", "idem", 9, SituacaoDocumento.PARCIALMENTE_ASSINADO))

        // Part 1 já assinou
        val part1 = ParticipanteDocumentoEntity(part1Id, versaoId, "user-1", "Ana Silva", "ana@email.com", "", "", PapelParticipante.CONTRATANTE, true, 1, SituacaoParticipante.ASSINADO)
        // Part 2 pendente
        val part2 = ParticipanteDocumentoEntity(part2Id, versaoId, "sig-joao-lima", "João Lima", "joao.lima@email.com", "", "", PapelParticipante.CONTRATADO, true, 2, SituacaoParticipante.PENDENTE)

        partDao.inserir(part1)
        partDao.inserir(part2)

        campoDao.inserir(CampoAssinaturaEntity("c-final-2", versaoId, part2Id, 1, 100f, 200f, 180f, 60f))

        // Part 2 assina
        val res = docRepo.salvarAssinaturaSignatario(
            versaoId = versaoId,
            participanteId = part2Id,
            tipoAssinatura = TipoAssinatura.DIGITADA,
            caminhoPngOuSvg = "assinaturas/p2.png",
            ipDispositivo = "192.168.0.2"
        )
        assertTrue(res.isSuccess)

        // Documento e versão devem estar CONCLUIDO com sha256Final
        val docAtualizado = docDao.buscarPorId(docId)
        val versaoAtualizada = versaoDao.buscarPorId(versaoId)

        assertEquals(SituacaoDocumento.CONCLUIDO, docAtualizado?.situacaoAtual)
        assertEquals(SituacaoDocumento.CONCLUIDO, versaoAtualizada?.situacao)
        assertNotNull(versaoAtualizada?.sha256Final)

        // Evento de conclusão registrado
        val eventos = eventoDao.eventos
        assertTrue(eventos.any { it.tipoEvento == TipoEventoHistorico.CONCLUSAO })
    }

    @Test
    fun testCapturaAssinaturaDesenhoEDigitacao() {
        val vm = Sig06CapturaAssinaturaViewModel(SavedStateHandle(mapOf("documentoId" to "doc-1")))

        // Inicial: Desenhar, sem desenho -> não pode confirmar
        assertEquals(AbaCapturaAssinatura.DESENHAR, vm.uiState.value.abaSelecionada)
        assertFalse(vm.uiState.value.podeConfirmar)

        // Adiciona desenho
        vm.atualizarTemDesenho(true)
        assertTrue(vm.uiState.value.podeConfirmar)

        // Limpa
        vm.limpar()
        assertFalse(vm.uiState.value.podeConfirmar)

        // Muda para aba Digitar
        vm.selecionarAba(AbaCapturaAssinatura.DIGITAR)
        vm.atualizarTextoDigitado("João Lima")
        assertTrue(vm.uiState.value.podeConfirmar)

        // Alterna fontes cursivas
        vm.selecionarEstiloFonte(EstiloFonteCursiva.ESTILO_3)
        assertEquals(EstiloFonteCursiva.ESTILO_3, vm.uiState.value.estiloFonteSelecionado)

        // Confirma
        vm.confirmarAssinatura()
        assertTrue(vm.uiState.value.assinaturaPronta)
    }
}

class FakeAssinaturaDao : AssinaturaDao {
    val assinaturas = mutableListOf<AssinaturaEntity>()
    private val flow = MutableStateFlow<List<AssinaturaEntity>>(emptyList())

    override suspend fun inserir(assinatura: AssinaturaEntity) {
        assinaturas.add(assinatura)
        flow.value = assinaturas.toList()
    }

    override suspend fun buscarPorCampo(campoAssinaturaId: String): AssinaturaEntity? =
        assinaturas.find { it.campoAssinaturaId == campoAssinaturaId }

    override fun listarPorParticipante(participanteId: String): Flow<List<AssinaturaEntity>> =
        flow.map { it.filter { a -> a.participanteId == participanteId } }
}

package br.com.letssign

import androidx.lifecycle.SavedStateHandle
import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.*
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.SignatarioRepository
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.domain.model.PerfilUsuario
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.TipoOrdem
import br.com.letssign.presentation.admin.wizard.WizardNovoDocViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.crypto.spec.SecretKeySpec

@OptIn(ExperimentalCoroutinesApi::class)
class WizardEtapas1a6Test {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var docDao: FakeDocDao
    private lateinit var versaoDao: FakeVersaoDao
    private lateinit var partDao: FakePartDao
    private lateinit var adminSigDao: FakeAdminSignatarioDao
    private lateinit var usuarioDao: FakeUsuarioDao
    private lateinit var sessionManager: SessionManager

    private lateinit var docRepo: DocumentoRepository
    private lateinit var sigRepo: SignatarioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        docDao = FakeDocDao()
        versaoDao = FakeVersaoDao()
        partDao = FakePartDao()
        adminSigDao = FakeAdminSignatarioDao()
        usuarioDao = FakeUsuarioDao()

        val dummyCipher = CryptoManager(customKey = SecretKeySpec(ByteArray(32), "AES"))
        val fakeSessionFlow = MutableStateFlow<UsuarioSessao?>(
            UsuarioSessao("admin-carla-mendes", "Carla Mendes", "carla@empresa.com.br", PerfilUsuario.ADMIN, "token-teste")
        )
        sessionManager = object : SessionManager(DummyDataStore(), dummyCipher) {
            override val sessaoFlow: Flow<UsuarioSessao?> = fakeSessionFlow
            override suspend fun obterUsuarioId(): String? = fakeSessionFlow.value?.usuarioId
            override suspend fun obterPerfil(): PerfilUsuario? = fakeSessionFlow.value?.perfil
        }

        docRepo = DocumentoRepository(docDao, versaoDao, partDao)
        sigRepo = SignatarioRepository(adminSigDao, usuarioDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testWizardStepValidations() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        // Etapa 1: Título
        assertEquals(1, vm.uiState.value.etapaAtual)
        assertFalse(vm.uiState.value.podeAvancarEtapa1)
        assertFalse(vm.avancarEtapa()) // Não avança sem título

        vm.atualizarTitulo("Contrato de Prestação de Serviços")
        assertTrue(vm.uiState.value.podeAvancarEtapa1)
        assertTrue(vm.avancarEtapa())
        testScheduler.advanceUntilIdle()

        // Etapa 2: PDF
        assertEquals(2, vm.uiState.value.etapaAtual)
        assertFalse(vm.uiState.value.podeAvancarEtapa2)
        assertFalse(vm.avancarEtapa()) // Não avança sem PDF
    }

    @Test
    fun testSignersSelectionAndMandatoryRule() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        // Etapa 3: Seleção de signatários
        val signatarios = vm.uiState.value.signatariosCadastrados
        assertTrue(signatarios.isNotEmpty())
        assertFalse(vm.uiState.value.podeAvancarEtapa3)

        // Seleciona 2 signatários
        val sig1 = signatarios[0]
        val sig2 = signatarios[1]
        vm.alternarSelecaoSignatario(sig1.signatarioId)
        vm.alternarSelecaoSignatario(sig2.signatarioId)
        assertTrue(vm.uiState.value.podeAvancarEtapa3)

        // Etapa 4: Obrigatoriedade
        assertTrue(vm.uiState.value.podeAvancarEtapa4) // Por padrão ambos são obrigatórios

        // Se desmarcar a obrigatoriedade de ambos, bloqueia avanço
        vm.alternarObrigatoriedade(sig1.signatarioId, false)
        vm.alternarObrigatoriedade(sig2.signatarioId, false)
        assertFalse(vm.uiState.value.podeAvancarEtapa4)

        // Torna sig1 obrigatório novamente
        vm.alternarObrigatoriedade(sig1.signatarioId, true)
        assertTrue(vm.uiState.value.podeAvancarEtapa4)

        // Altera papel
        vm.definirPapel(sig1.signatarioId, PapelParticipante.CONTRATANTE)
        assertEquals(PapelParticipante.CONTRATANTE, vm.uiState.value.papelPorSignatario[sig1.signatarioId])
    }

    @Test
    fun testSigningOrderSequentialAndReordering() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        val signatarios = vm.uiState.value.signatariosCadastrados
        val sig1 = signatarios[0]
        val sig2 = signatarios[1]
        vm.alternarSelecaoSignatario(sig1.signatarioId)
        vm.alternarSelecaoSignatario(sig2.signatarioId)

        // Ordem sequencial
        vm.definirTipoOrdem(TipoOrdem.SEQUENCIAL)
        assertEquals(TipoOrdem.SEQUENCIAL, vm.uiState.value.tipoOrdem)

        // Reordenação
        assertEquals(sig1.signatarioId, vm.uiState.value.ordemSignatarios[0])
        assertEquals(sig2.signatarioId, vm.uiState.value.ordemSignatarios[1])

        vm.moverSignatarioParaBaixo(0)
        assertEquals(sig2.signatarioId, vm.uiState.value.ordemSignatarios[0])
        assertEquals(sig1.signatarioId, vm.uiState.value.ordemSignatarios[1])

        vm.moverSignatarioParaCima(1)
        assertEquals(sig1.signatarioId, vm.uiState.value.ordemSignatarios[0])
        assertEquals(sig2.signatarioId, vm.uiState.value.ordemSignatarios[1])
    }

    @Test
    fun testQuickSignerRegistrationInModal02() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        // Cadastro rápido
        vm.salvarNovoSignatarioRapido(
            nome = "Lucas Pereira",
            email = "lucas.pereira@empresa.com",
            telefone = "(11) 98888-7777",
            dataNascimento = "15/04/1991"
        )
        testScheduler.advanceUntilIdle()

        val cadastrados = vm.uiState.value.signatariosCadastrados
        val recemCriado = cadastrados.find { it.email == "lucas.pereira@empresa.com" }
        assertNotNull(recemCriado)
        assertTrue(vm.uiState.value.signatariosSelecionadosIds.contains(recemCriado!!.signatarioId))
    }

    @Test
    fun testDraftAutosaveAndDiscardModal04() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        vm.atualizarTitulo("Novo Contrato Rascunho")
        var callbackExecutado = false
        vm.salvarRascunhoManual { callbackExecutado = true }
        testScheduler.advanceUntilIdle()

        assertTrue(callbackExecutado)
        val salvo = docRepo.buscarUltimoRascunho(adminId)
        assertNotNull(salvo)
        assertEquals("Novo Contrato Rascunho", salvo!!.titulo)

        // Descartar rascunho
        var descarteExecutado = false
        vm.descartarRascunhoManual { descarteExecutado = true }
        testScheduler.advanceUntilIdle()

        assertTrue(descarteExecutado)
        val aposDescarte = docRepo.carregarRascunho(salvo.id)
        assertNull(aposDescarte)
    }

    // ==========================================
    // FAKE DAOS
    // ==========================================
    private class FakeDocDao : DocumentoDao {
        val db = mutableMapOf<String, DocumentoEntity>()
        private val flow = MutableStateFlow<List<DocumentoEntity>>(emptyList())

        override suspend fun inserir(documento: DocumentoEntity) {
            db[documento.id] = documento
            flow.value = db.values.toList()
        }
        override suspend fun atualizar(documento: DocumentoEntity) {
            db[documento.id] = documento
            flow.value = db.values.toList()
        }
        override suspend fun buscarPorId(id: String): DocumentoEntity? = db[id]
        override fun observarPorId(id: String): Flow<DocumentoEntity?> = flow.map { it.find { d -> d.id == id } }
        override fun listarPorAdmin(adminId: String): Flow<List<DocumentoEntity>> = flow.map { it.filter { d -> d.usuarioAdminId == adminId } }
        override suspend fun buscarUltimoRascunho(adminId: String): DocumentoEntity? =
            db.values.filter { it.usuarioAdminId == adminId && it.situacaoAtual == SituacaoDocumento.RASCUNHO }
                .maxByOrNull { it.dataUltimaAtualizacaoUtc }
        override suspend fun deletar(documento: DocumentoEntity) {
            db.remove(documento.id)
            flow.value = db.values.toList()
        }
    }

    private class FakeVersaoDao : VersaoDocumentoDao {
        val db = mutableMapOf<String, VersaoDocumentoEntity>()
        private val flow = MutableStateFlow<List<VersaoDocumentoEntity>>(emptyList())

        override suspend fun inserir(versao: VersaoDocumentoEntity) {
            db[versao.id] = versao
            flow.value = db.values.toList()
        }
        override suspend fun atualizar(versao: VersaoDocumentoEntity) {
            db[versao.id] = versao
            flow.value = db.values.toList()
        }
        override suspend fun buscarPorId(id: String): VersaoDocumentoEntity? = db[id]
        override fun observarPorId(id: String): Flow<VersaoDocumentoEntity?> = flow.map { it.find { v -> v.id == id } }
        override suspend fun buscarVersaoAtual(documentoId: String): VersaoDocumentoEntity? =
            db.values.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao }
        override fun observarVersaoAtual(documentoId: String): Flow<VersaoDocumentoEntity?> =
            flow.map { list -> list.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao } }
        override fun listarVersoesDoDocumento(documentoId: String): Flow<List<VersaoDocumentoEntity>> =
            flow.map { list -> list.filter { it.documentoId == documentoId }.sortedBy { it.numeroVersao } }
    }

    private class FakePartDao : ParticipanteDocumentoDao {
        val db = mutableMapOf<String, ParticipanteDocumentoEntity>()
        private val flow = MutableStateFlow<List<ParticipanteDocumentoEntity>>(emptyList())

        override suspend fun inserir(participante: ParticipanteDocumentoEntity) {
            db[participante.id] = participante
            flow.value = db.values.toList()
        }
        override suspend fun inserirVarios(participantes: List<ParticipanteDocumentoEntity>) {
            participantes.forEach { db[it.id] = it }
            flow.value = db.values.toList()
        }
        override suspend fun atualizar(participante: ParticipanteDocumentoEntity) {
            db[participante.id] = participante
            flow.value = db.values.toList()
        }
        override suspend fun buscarPorId(id: String): ParticipanteDocumentoEntity? = db[id]
        override fun listarPorVersao(versaoDocumentoId: String): Flow<List<ParticipanteDocumentoEntity>> =
            flow.map { it.filter { p -> p.versaoDocumentoId == versaoDocumentoId } }
        override suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<ParticipanteDocumentoEntity> =
            db.values.filter { it.versaoDocumentoId == versaoDocumentoId }
        override fun listarPorUsuario(usuarioId: String): Flow<List<ParticipanteDocumentoEntity>> =
            flow.map { it.filter { p -> p.usuarioId == usuarioId } }
        override suspend fun deletarPorId(id: String) {
            db.remove(id)
            flow.value = db.values.toList()
        }
        override suspend fun deletarPorVersao(versaoDocumentoId: String) {
            db.entries.removeIf { it.value.versaoDocumentoId == versaoDocumentoId }
            flow.value = db.values.toList()
        }
    }

    private class FakeAdminSignatarioDao : AdminSignatarioDao {
        val db = mutableMapOf<String, AdminSignatarioEntity>()
        private val flow = MutableStateFlow<List<AdminSignatarioEntity>>(emptyList())

        override suspend fun inserir(relacao: AdminSignatarioEntity) {
            db["${relacao.adminId}_${relacao.signatarioId}"] = relacao
            flow.value = db.values.toList()
        }
        override suspend fun inserirVarios(relacoes: List<AdminSignatarioEntity>) {
            relacoes.forEach { db["${it.adminId}_${it.signatarioId}"] = it }
            flow.value = db.values.toList()
        }
        override suspend fun atualizar(relacao: AdminSignatarioEntity) {
            db["${relacao.adminId}_${relacao.signatarioId}"] = relacao
            flow.value = db.values.toList()
        }
        override fun listarSignatariosDoAdmin(adminId: String): Flow<List<AdminSignatarioEntity>> =
            flow.map { it.filter { s -> s.adminId == adminId } }
        override suspend fun buscarSignatarioPorEmail(adminId: String, email: String): AdminSignatarioEntity? =
            db.values.find { it.adminId == adminId && it.email == email }
        override suspend fun buscarSignatarioPorId(adminId: String, signatarioId: String): AdminSignatarioEntity? =
            db.values.find { it.adminId == adminId && it.signatarioId == signatarioId }
        override suspend fun deletarSignatario(adminId: String, signatarioId: String) {
            db.entries.removeIf { it.value.adminId == adminId && it.value.signatarioId == signatarioId }
            flow.value = db.values.toList()
        }
    }

    private class FakeUsuarioDao : UsuarioDao {
        val db = mutableMapOf<String, UsuarioEntity>()
        override suspend fun inserir(usuario: UsuarioEntity) { db[usuario.id] = usuario }
        override suspend fun atualizar(usuario: UsuarioEntity) { db[usuario.id] = usuario }
        override suspend fun buscarPorId(id: String): UsuarioEntity? = db[id]
        override suspend fun buscarPorEmail(email: String): UsuarioEntity? = db.values.find { it.email == email }
        override fun buscarPorEmailFlow(email: String): Flow<UsuarioEntity?> = MutableStateFlow(db.values.find { it.email == email })
    }

    private class DummyDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        override val data: Flow<androidx.datastore.preferences.core.Preferences> =
            MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
        override suspend fun updateData(transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
            return androidx.datastore.preferences.core.emptyPreferences()
        }
    }
}

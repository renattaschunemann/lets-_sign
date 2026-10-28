package br.com.letssign

import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.*
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.NotificacaoRepository
import br.com.letssign.data.repository.SignatarioRepository
import br.com.letssign.domain.model.*
import br.com.letssign.presentation.conta.Com06MinhaContaViewModel
import androidx.lifecycle.SavedStateHandle
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
class AdminBaseTest {

    private val testDispatcher = StandardTestDispatcher()

    // Fakes
    private lateinit var docDao: FakeDocDao
    private lateinit var versaoDao: FakeVersaoDao
    private lateinit var partDao: FakePartDao
    private lateinit var adminSigDao: FakeAdminSignatarioDao
    private lateinit var usuarioDao: FakeUsuarioDao
    private lateinit var notifDao: FakeNotificacaoDao
    private lateinit var sessionManager: SessionManager

    private lateinit var docRepo: DocumentoRepository
    private lateinit var sigRepo: SignatarioRepository
    private lateinit var notifRepo: NotificacaoRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        docDao = FakeDocDao()
        versaoDao = FakeVersaoDao()
        partDao = FakePartDao()
        adminSigDao = FakeAdminSignatarioDao()
        usuarioDao = FakeUsuarioDao()
        notifDao = FakeNotificacaoDao()

        val dummyCipher = CryptoManager(customKey = SecretKeySpec(ByteArray(32), "AES"))
        val fakeSessionFlow = MutableStateFlow<UsuarioSessao?>(
            UsuarioSessao("admin-carla-mendes", "Carla Mendes", "carla@empresa.com.br", PerfilUsuario.ADMIN, "token-teste")
        )
        sessionManager = object : SessionManager(DummyDataStore(), dummyCipher) {
            override val sessaoFlow: Flow<UsuarioSessao?> = fakeSessionFlow
            override suspend fun obterUsuarioId(): String? = fakeSessionFlow.value?.usuarioId
            override suspend fun obterPerfil(): PerfilUsuario? = fakeSessionFlow.value?.perfil
            override suspend fun salvarSessao(sessao: UsuarioSessao) {
                fakeSessionFlow.value = sessao
            }
            override suspend fun limparSessao() {
                fakeSessionFlow.value = null
            }
        }

        docRepo = DocumentoRepository(docDao, versaoDao, partDao)
        sigRepo = SignatarioRepository(adminSigDao, usuarioDao)
        notifRepo = NotificacaoRepository(notifDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDocumentoRepositoryPriorityAndFilters() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        docRepo.inicializarDadosDemoSeNecessario(adminId)

        val docs = docRepo.listarDocumentosDoAdmin(adminId).first()
        assertEquals(7, docs.size)

        // Regra do Figma: itens com Falha no envio ficam SEMPRE no topo
        assertEquals(SituacaoDocumento.FALHA_ENVIO, docs.first().situacao)

        // Excluir rascunho
        val rascunho = docs.first { it.situacao == SituacaoDocumento.RASCUNHO }
        docRepo.excluirRascunho(rascunho.id)

        val docsAposExclusao = docRepo.listarDocumentosDoAdmin(adminId).first()
        assertEquals(6, docsAposExclusao.size)
        assertFalse(docsAposExclusao.any { it.id == rascunho.id })
    }

    @Test
    fun testSignatarioRepositoryValidationAndOperations() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val signatarios = sigRepo.listarSignatarios(adminId).first()
        assertEquals(5, signatarios.size)

        // Validação de nome incompleto
        val resNomeInvalido = sigRepo.salvarSignatario(
            adminId = adminId,
            signatarioIdExistente = null,
            nome = "Maria",
            email = "maria@teste.com",
            telefone = "11999999999",
            dataNascimento = "10/10/1990",
            senha = null
        )
        assertTrue(resNomeInvalido.isFailure)

        // Validação P03: menor de 18 anos
        val resMenor = sigRepo.salvarSignatario(
            adminId = adminId,
            signatarioIdExistente = null,
            nome = "Menor de Idade",
            email = "menor@teste.com",
            telefone = "11999999999",
            dataNascimento = "10/10/2015",
            senha = null
        )
        assertTrue(resMenor.isFailure)

        // Validação P03: data futura
        val resFutura = sigRepo.salvarSignatario(
            adminId = adminId,
            signatarioIdExistente = null,
            nome = "Futuro Pessoa",
            email = "futuro@teste.com",
            telefone = "11999999999",
            dataNascimento = "10/10/2030",
            senha = null
        )
        assertTrue(resFutura.isFailure)

        // Cadastro válido
        val resValido = sigRepo.salvarSignatario(
            adminId = adminId,
            signatarioIdExistente = null,
            nome = "Novo Signatário Teste",
            email = "novo.teste@empresa.com",
            telefone = "11988887777",
            dataNascimento = "15/05/1995",
            senha = "SenhaValida123"
        )
        assertTrue(resValido.isSuccess)

        val signatariosAtualizados = sigRepo.listarSignatarios(adminId).first()
        assertEquals(6, signatariosAtualizados.size)

        // Excluir
        val cadastrado = signatariosAtualizados.first { it.email == "novo.teste@empresa.com" }
        sigRepo.excluirSignatario(adminId, cadastrado.signatarioId)
        val signatariosFinais = sigRepo.listarSignatarios(adminId).first()
        assertEquals(5, signatariosFinais.size)
    }

    @Test
    fun testNotificacaoRepositoryCountersAndReadStatus() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        notifRepo.inicializarDadosDemoSeNecessario(adminId, "ADMIN")

        val lista = notifRepo.listarNotificacoes(adminId).first()
        assertEquals(4, lista.size)

        val naoLidasInicial = notifRepo.contarNaoLidas(adminId).first()
        assertEquals(2, naoLidasInicial)

        // Marcar uma como lida
        val primeiraNaoLida = lista.first { !it.lida }
        notifRepo.marcarComoLida(primeiraNaoLida.id)

        val naoLidasDepois = notifRepo.contarNaoLidas(adminId).first()
        assertEquals(1, naoLidasDepois)

        // Marcar todas como lidas
        notifRepo.marcarTodasComoLidas(adminId)
        val naoLidasFim = notifRepo.contarNaoLidas(adminId).first()
        assertEquals(0, naoLidasFim)
    }

    @Test
    fun testCom06MinhaContaLogout() = runTest(testDispatcher) {
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = usuarioDao,
            savedStateHandle = SavedStateHandle(mapOf("perfil" to "ADMIN"))
        )

        testScheduler.advanceUntilIdle()
        assertEquals("ADMIN", viewModel.uiState.value.perfil)

        viewModel.aoConfirmarSair()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.logoutConcluido)
        assertNull(sessionManager.obterUsuarioId())
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
        override fun listarPorAdminESituacao(adminId: String, situacao: SituacaoDocumento): Flow<List<DocumentoEntity>> = flow.map { it.filter { d -> d.usuarioAdminId == adminId && d.situacaoAtual == situacao } }
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
        override suspend fun buscarVersaoAtual(documentoId: String): VersaoDocumentoEntity? = db.values.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao }
        override fun observarVersaoAtual(documentoId: String): Flow<VersaoDocumentoEntity?> = flow.map { list -> list.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao } }
        override fun listarVersoesDoDocumento(documentoId: String): Flow<List<VersaoDocumentoEntity>> = flow.map { list -> list.filter { it.documentoId == documentoId } }
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
        override fun listarPorVersao(versaoDocumentoId: String): Flow<List<ParticipanteDocumentoEntity>> = flow.map { it.filter { p -> p.versaoDocumentoId == versaoDocumentoId } }
        override suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<ParticipanteDocumentoEntity> = db.values.filter { it.versaoDocumentoId == versaoDocumentoId }
        override fun listarPorUsuario(usuarioId: String): Flow<List<ParticipanteDocumentoEntity>> = flow.map { it.filter { p -> p.usuarioId == usuarioId } }
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
            val key = "${relacao.adminId}_${relacao.signatarioId}"
            db[key] = relacao
            flow.value = db.values.toList()
        }
        override suspend fun inserirVarios(relacoes: List<AdminSignatarioEntity>) {
            relacoes.forEach {
                val key = "${it.adminId}_${it.signatarioId}"
                db[key] = it
            }
            flow.value = db.values.toList()
        }
        override suspend fun atualizar(relacao: AdminSignatarioEntity) {
            val key = "${relacao.adminId}_${relacao.signatarioId}"
            db[key] = relacao
            flow.value = db.values.toList()
        }
        override fun listarSignatariosDoAdmin(adminId: String): Flow<List<AdminSignatarioEntity>> = flow.map { it.filter { s -> s.adminId == adminId } }
        override suspend fun buscarSignatarioPorEmail(adminId: String, email: String): AdminSignatarioEntity? = db.values.find { it.adminId == adminId && it.email == email }
        override suspend fun buscarSignatarioPorId(adminId: String, signatarioId: String): AdminSignatarioEntity? = db.values.find { it.adminId == adminId && it.signatarioId == signatarioId }
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

    private class FakeNotificacaoDao : NotificacaoDao {
        val db = mutableMapOf<String, NotificacaoEntity>()
        private val flow = MutableStateFlow<List<NotificacaoEntity>>(emptyList())

        override suspend fun inserir(notificacao: NotificacaoEntity) {
            db[notificacao.id] = notificacao
            flow.value = db.values.toList()
        }
        override fun listarPorUsuario(usuarioId: String): Flow<List<NotificacaoEntity>> = flow.map { it.filter { n -> n.usuarioId == usuarioId } }
        override suspend fun buscarPorUsuarioSync(usuarioId: String): List<NotificacaoEntity> = db.values.filter { it.usuarioId == usuarioId }
        override suspend fun marcarComoLida(id: String) {
            db[id]?.let {
                db[id] = it.copy(lida = true)
                flow.value = db.values.toList()
            }
        }
        override suspend fun marcarTodasComoLidas(usuarioId: String) {
            db.values.filter { it.usuarioId == usuarioId }.forEach {
                db[it.id] = it.copy(lida = true)
            }
            flow.value = db.values.toList()
        }
        override fun contarNaoLidas(usuarioId: String): Flow<Int> = flow.map { it.count { n -> n.usuarioId == usuarioId && !n.lida } }
    }

    private class DummyDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        override val data: Flow<androidx.datastore.preferences.core.Preferences> = MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
        override suspend fun updateData(transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
            return androidx.datastore.preferences.core.emptyPreferences()
        }
    }
}

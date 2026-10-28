package br.com.letssign

import androidx.lifecycle.SavedStateHandle
import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.*
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.SignatarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.presentation.admin.wizard.EstadoEnvio
import br.com.letssign.presentation.admin.wizard.WizardNovoDocViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class CamposEnvioTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var docDao: CamposFakeDocDao
    private lateinit var versaoDao: CamposFakeVersaoDao
    private lateinit var partDao: CamposFakePartDao
    private lateinit var campoDao: CamposFakeCampoDao
    private lateinit var eventoDao: CamposFakeEventoDao
    private lateinit var notifDao: CamposFakeNotifDao
    private lateinit var adminSigDao: CamposFakeAdminSigDao
    private lateinit var usuarioDao: CamposFakeUsuarioDao
    private lateinit var sessionManager: SessionManager

    private lateinit var docRepo: DocumentoRepository
    private lateinit var sigRepo: SignatarioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        docDao = CamposFakeDocDao()
        versaoDao = CamposFakeVersaoDao()
        partDao = CamposFakePartDao()
        campoDao = CamposFakeCampoDao()
        eventoDao = CamposFakeEventoDao()
        notifDao = CamposFakeNotifDao()
        adminSigDao = CamposFakeAdminSigDao()
        usuarioDao = CamposFakeUsuarioDao()

        val dummyCipher = CryptoManager(customKey = SecretKeySpec(ByteArray(32), "AES"))
        val fakeSessionFlow = MutableStateFlow<UsuarioSessao?>(
            UsuarioSessao("admin-carla-mendes", "Carla Mendes", "carla@empresa.com.br", PerfilUsuario.ADMIN, "token-teste")
        )
        sessionManager = object : SessionManager(CamposFakeDataStore(), dummyCipher) {
            override val sessaoFlow: Flow<UsuarioSessao?> = fakeSessionFlow
            override suspend fun obterUsuarioId(): String? = fakeSessionFlow.value?.usuarioId
            override suspend fun obterPerfil(): PerfilUsuario? = fakeSessionFlow.value?.perfil
        }

        docRepo = DocumentoRepository(
            documentoDao = docDao,
            versaoDao = versaoDao,
            participanteDao = partDao,
            campoAssinaturaDao = campoDao,
            eventoHistoricoDao = eventoDao,
            notificacaoDao = notifDao
        )
        sigRepo = SignatarioRepository(adminSigDao, usuarioDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPosicionamentoCamposEValidacaoAvanco() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        val sigs = vm.uiState.value.signatariosCadastrados
        assertTrue("Deve haver ao menos 2 signatários cadastrados", sigs.size >= 2)
        val sig1 = sigs[0].signatarioId
        val sig2 = sigs[1].signatarioId

        vm.alternarSelecaoSignatario(sig1)
        vm.alternarSelecaoSignatario(sig2)

        // Inicialmente nenhum campo foi posicionado
        assertEquals(0, vm.uiState.value.campos.size)
        assertFalse(vm.uiState.value.todosParticipantesTemCampo)
        assertFalse(vm.uiState.value.podeAvancarEtapa7)
        assertEquals(2, vm.uiState.value.participantesSemCampo.size)

        // Adiciona o primeiro campo -> atribuído automaticamente a um dos pendentes
        vm.adicionarCampo()
        assertEquals(1, vm.uiState.value.campos.size)
        assertNotNull(vm.uiState.value.campoSelecionadoId)

        // Ainda falta campo para o outro signatário
        assertFalse(vm.uiState.value.todosParticipantesTemCampo)
        assertEquals(1, vm.uiState.value.participantesSemCampo.size)

        // Adiciona o segundo campo -> atribuído ao que faltava
        vm.adicionarCampo()
        assertEquals(2, vm.uiState.value.campos.size)
        assertTrue(vm.uiState.value.todosParticipantesTemCampo)
        assertTrue(vm.uiState.value.podeAvancarEtapa7)
        assertEquals(0, vm.uiState.value.participantesSemCampo.size)

        // Exclui um campo -> volta a ficar inválido
        val campoIdParaExcluir = vm.uiState.value.campos.first().id
        vm.excluirCampo(campoIdParaExcluir)
        assertEquals(1, vm.uiState.value.campos.size)
        assertFalse(vm.uiState.value.todosParticipantesTemCampo)
    }

    @Test
    fun testEnvioDocumentoSucessoECriacaoDeNotificacoes() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        vm.atualizarTitulo("Contrato de Prestação de Serviços")
        val sig1 = vm.uiState.value.signatariosCadastrados[0].signatarioId
        vm.alternarSelecaoSignatario(sig1)
        vm.adicionarCampo()
        testScheduler.advanceUntilIdle()

        // Envia com sucesso
        vm.atualizarMensagemConvite("Favor assinar com brevidade.")
        vm.enviarDocumento(simularFalha = false)
        testScheduler.advanceUntilIdle()

        // Verifica UiState do envio
        assertEquals(EstadoEnvio.SUCESSO, vm.uiState.value.estadoModalResultado)

        // Verifica no banco simulado
        val docSalvo = docDao.buscarPorId(vm.uiState.value.documentoId)
        assertNotNull(docSalvo)
        assertEquals(SituacaoDocumento.ENVIADO, docSalvo?.situacaoAtual)

        // Verifica histórico
        assertEquals(1, eventoDao.eventos.size)
        assertEquals("Documento enviado para assinatura.", eventoDao.eventos.first().detalhe)

        // Verifica notificação gerada para o participante
        assertTrue(notifDao.notificacoes.isNotEmpty())
        assertEquals(sig1, notifDao.notificacoes.first().usuarioId)
    }

    @Test
    fun testEnvioDocumentoSimulandoFalhaDeConexao() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        sigRepo.inicializarDadosDemoSeNecessario(adminId)

        val vm = WizardNovoDocViewModel(
            documentoRepository = docRepo,
            signatarioRepository = sigRepo,
            sessionManager = sessionManager,
            savedStateHandle = SavedStateHandle()
        )
        testScheduler.advanceUntilIdle()

        vm.atualizarTitulo("Acordo de Confidencialidade")
        val sig1 = vm.uiState.value.signatariosCadastrados[0].signatarioId
        vm.alternarSelecaoSignatario(sig1)
        vm.adicionarCampo()
        testScheduler.advanceUntilIdle()

        // Envia com falha simulada
        vm.enviarDocumento(simularFalha = true)
        testScheduler.advanceUntilIdle()

        // Modal deve exibir erro
        assertEquals(EstadoEnvio.ERRO, vm.uiState.value.estadoModalResultado)

        // Documento deve ter sido salvo com status FALHA_ENVIO
        val docSalvo = docDao.buscarPorId(vm.uiState.value.documentoId)
        assertNotNull(docSalvo)
        assertEquals(SituacaoDocumento.FALHA_ENVIO, docSalvo?.situacaoAtual)
    }
}

class CamposFakeDocDao : DocumentoDao {
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
    override suspend fun deletar(documento: DocumentoEntity) {
        db.remove(documento.id)
        flow.value = db.values.toList()
    }
    override suspend fun buscarPorId(id: String): DocumentoEntity? = db[id]
    override fun listarPorAdmin(usuarioAdminId: String): Flow<List<DocumentoEntity>> =
        flow.map { it.filter { d -> d.usuarioAdminId == usuarioAdminId } }
    override suspend fun buscarUltimoRascunho(adminId: String): DocumentoEntity? =
        db.values.filter { it.usuarioAdminId == adminId && it.situacaoAtual == SituacaoDocumento.RASCUNHO }
            .maxByOrNull { it.dataUltimaAtualizacaoUtc }
    override fun observarPorId(id: String): Flow<DocumentoEntity?> =
        flow.map { it.find { d -> d.id == id } }
}

class CamposFakeVersaoDao : VersaoDocumentoDao {
    val db = mutableMapOf<String, VersaoDocumentoEntity>()
    override suspend fun inserir(versao: VersaoDocumentoEntity) { db[versao.id] = versao }
    override suspend fun atualizar(versao: VersaoDocumentoEntity) { db[versao.id] = versao }
    override suspend fun buscarPorId(id: String): VersaoDocumentoEntity? = db[id]
    override suspend fun buscarVersaoAtual(documentoId: String): VersaoDocumentoEntity? =
        db.values.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao }
    override fun observarPorId(id: String): Flow<VersaoDocumentoEntity?> =
        flowOf(db[id])
    override fun observarVersaoAtual(documentoId: String): Flow<VersaoDocumentoEntity?> =
        flowOf(db.values.filter { it.documentoId == documentoId }.maxByOrNull { it.numeroVersao })
    override fun listarVersoesDoDocumento(documentoId: String): Flow<List<VersaoDocumentoEntity>> =
        flowOf(db.values.filter { it.documentoId == documentoId })
}

class CamposFakePartDao : ParticipanteDocumentoDao {
    val db = mutableMapOf<String, ParticipanteDocumentoEntity>()
    override suspend fun inserir(participante: ParticipanteDocumentoEntity) { db[participante.id] = participante }
    override suspend fun inserirVarios(participantes: List<ParticipanteDocumentoEntity>) {
        participantes.forEach { db[it.id] = it }
    }
    override suspend fun atualizar(participante: ParticipanteDocumentoEntity) { db[participante.id] = participante }
    override suspend fun buscarPorId(id: String): ParticipanteDocumentoEntity? = db[id]
    override fun listarPorVersao(versaoDocumentoId: String): Flow<List<ParticipanteDocumentoEntity>> =
        flowOf(db.values.filter { it.versaoDocumentoId == versaoDocumentoId })
    override suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<ParticipanteDocumentoEntity> =
        db.values.filter { it.versaoDocumentoId == versaoDocumentoId }
    override fun listarPorUsuario(usuarioId: String): Flow<List<ParticipanteDocumentoEntity>> =
        flowOf(db.values.filter { it.usuarioId == usuarioId })
    override suspend fun deletarPorId(id: String) { db.remove(id) }
    override suspend fun deletarPorVersao(versaoDocumentoId: String) {
        db.entries.removeIf { it.value.versaoDocumentoId == versaoDocumentoId }
    }
}

class CamposFakeCampoDao : CampoAssinaturaDao {
    val campos = mutableListOf<CampoAssinaturaEntity>()

    override suspend fun inserir(campo: CampoAssinaturaEntity) {
        campos.removeAll { it.id == campo.id }
        campos.add(campo)
    }
    override suspend fun inserirVarios(novos: List<CampoAssinaturaEntity>) {
        val ids = novos.map { it.id }.toSet()
        campos.removeAll { ids.contains(it.id) }
        campos.addAll(novos)
    }
    override suspend fun atualizar(campo: CampoAssinaturaEntity) {
        campos.removeAll { it.id == campo.id }
        campos.add(campo)
    }
    override suspend fun deletar(campo: CampoAssinaturaEntity) {
        campos.removeAll { it.id == campo.id }
    }
    override suspend fun deletarPorId(id: String) {
        campos.removeAll { it.id == id }
    }
    override fun listarPorVersao(versaoDocumentoId: String): Flow<List<CampoAssinaturaEntity>> {
        return flowOf(campos.filter { it.versaoDocumentoId == versaoDocumentoId })
    }
    override suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<CampoAssinaturaEntity> {
        return campos.filter { it.versaoDocumentoId == versaoDocumentoId }
    }
    override suspend fun deletarPorVersao(versaoDocumentoId: String) {
        campos.removeAll { it.versaoDocumentoId == versaoDocumentoId }
    }
    override fun listarPorVersaoEPagina(versaoDocumentoId: String, numeroPagina: Int): Flow<List<CampoAssinaturaEntity>> {
        return flowOf(campos.filter { it.versaoDocumentoId == versaoDocumentoId && it.numeroPagina == numeroPagina })
    }
    override fun listarPorParticipante(participanteId: String): Flow<List<CampoAssinaturaEntity>> {
        return flowOf(campos.filter { it.participanteId == participanteId })
    }
    override suspend fun listarPorParticipanteSync(participanteId: String): List<CampoAssinaturaEntity> {
        return campos.filter { it.participanteId == participanteId }
    }
}

class CamposFakeEventoDao : EventoHistoricoDao {
    val eventos = mutableListOf<EventoHistoricoEntity>()

    override suspend fun inserir(evento: EventoHistoricoEntity) {
        eventos.add(evento)
    }
    override fun listarPorVersaoAsc(versaoDocumentoId: String): Flow<List<EventoHistoricoEntity>> {
        return flowOf(eventos.filter { it.versaoDocumentoId == versaoDocumentoId }.sortedBy { it.dataHoraUtc })
    }
    override fun listarPorVersaoDesc(versaoDocumentoId: String): Flow<List<EventoHistoricoEntity>> {
        return flowOf(eventos.filter { it.versaoDocumentoId == versaoDocumentoId }.sortedByDescending { it.dataHoraUtc })
    }
}

class CamposFakeNotifDao : NotificacaoDao {
    val notificacoes = mutableListOf<NotificacaoEntity>()

    override suspend fun inserir(notificacao: NotificacaoEntity) {
        notificacoes.add(notificacao)
    }
    override fun listarPorUsuario(usuarioId: String): Flow<List<NotificacaoEntity>> {
        return flowOf(notificacoes.filter { it.usuarioId == usuarioId })
    }
    override suspend fun buscarPorUsuarioSync(usuarioId: String): List<NotificacaoEntity> {
        return notificacoes.filter { it.usuarioId == usuarioId }
    }
    override suspend fun marcarComoLida(id: String) {
        val index = notificacoes.indexOfFirst { it.id == id }
        if (index != -1) {
            notificacoes[index] = notificacoes[index].copy(lida = true)
        }
    }
    override suspend fun marcarTodasComoLidas(usuarioId: String) {
        for (i in notificacoes.indices) {
            if (notificacoes[i].usuarioId == usuarioId) {
                notificacoes[i] = notificacoes[i].copy(lida = true)
            }
        }
    }
    override fun contarNaoLidas(usuarioId: String): Flow<Int> {
        return flowOf(notificacoes.count { it.usuarioId == usuarioId && !it.lida })
    }
}

class CamposFakeAdminSigDao : AdminSignatarioDao {
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

class CamposFakeUsuarioDao : UsuarioDao {
    val db = mutableMapOf<String, UsuarioEntity>()
    override suspend fun inserir(usuario: UsuarioEntity) { db[usuario.id] = usuario }
    override suspend fun atualizar(usuario: UsuarioEntity) { db[usuario.id] = usuario }
    override suspend fun buscarPorId(id: String): UsuarioEntity? = db[id]
    override suspend fun buscarPorEmail(email: String): UsuarioEntity? = db.values.find { it.email == email }
    override fun buscarPorEmailFlow(email: String): Flow<UsuarioEntity?> = MutableStateFlow(db.values.find { it.email == email })
}

class CamposFakeDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
    override val data: Flow<androidx.datastore.preferences.core.Preferences> =
        MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
    override suspend fun updateData(transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
        return androidx.datastore.preferences.core.emptyPreferences()
    }
}

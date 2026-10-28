package br.com.letssign

import br.com.letssign.data.local.dao.DocumentoDao
import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.DocumentoEntity
import br.com.letssign.data.local.entities.ParticipanteDocumentoEntity
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.remote.FakeRemoteDataSource
import br.com.letssign.data.remote.RemoteCampoDto
import br.com.letssign.data.remote.RemoteDocumentoEnvioRequest
import br.com.letssign.data.remote.RemoteParticipanteDto
import br.com.letssign.data.repository.LinkResolutionResult
import br.com.letssign.data.repository.LinkResolverRepository
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.*
import br.com.letssign.presentation.cadastro.CadastroAdminViewModel
import br.com.letssign.presentation.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationAndLinkTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRemoteDataSource: FakeRemoteDataSource
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var fakeDocDao: FakeDocDao
    private lateinit var fakePartDao: FakePartDao
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var linkResolverRepository: LinkResolverRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRemoteDataSource = FakeRemoteDataSource(latenciaSimuladaMs = 0L)
        fakeUsuarioDao = FakeUsuarioDao()
        fakeDocDao = FakeDocDao()
        fakePartDao = FakePartDao()

        val dummyCipher = CryptoManager(customKey = SecretKeySpec(ByteArray(32), "AES"))
        val fakeSessionFlow = MutableStateFlow<UsuarioSessao?>(null)

        usuarioRepository = UsuarioRepository(
            usuarioDao = fakeUsuarioDao,
            remoteDataSource = fakeRemoteDataSource,
            sessionManager = SessionManager(
                dataStore = DummyDataStore(),
                cipher = dummyCipher
            )
        )

        linkResolverRepository = LinkResolverRepository(
            remoteDataSource = fakeRemoteDataSource,
            documentoDao = fakeDocDao,
            participanteDocumentoDao = fakePartDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `testar validacao de formato de email no LoginViewModel`() = runTest {
        val viewModel = LoginViewModel(usuarioRepository)
        viewModel.onEmailChanged("email-invalido-sem-arroba")
        viewModel.onSenhaChanged("Senha123")
        viewModel.entrar()

        val state = viewModel.uiState.value
        assertNotNull(state.erroFormatoEmail)
        assertTrue(state.erroFormatoEmail!!.contains("Informe um e-mail válido"))
    }

    @Test
    fun `testar login com credenciais incorretas e bloqueio apos 5 tentativas`() = runTest {
        val viewModel = LoginViewModel(usuarioRepository)
        viewModel.onEmailChanged("carla@empresa.com.br")
        viewModel.onSenhaChanged("SenhaErrada")

        // 4 tentativas falhas
        for (i in 1..4) {
            viewModel.entrar()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = viewModel.uiState.value
            assertEquals(i, state.tentativasFalhas)
            assertFalse(state.estaBloqueado)
            assertTrue(state.erroCredenciais?.contains("E-mail ou senha incorretos") == true)
        }

        // 5ª tentativa falha -> Bloqueado
        viewModel.entrar()
        testDispatcher.scheduler.advanceUntilIdle()
        val stateFinal = viewModel.uiState.value
        assertTrue(stateFinal.estaBloqueado)
        assertTrue(stateFinal.erroCredenciais?.contains("Muitas tentativas") == true)
    }

    @Test
    fun `testar cadastro do Admin com validacao de senha forte P02 e termos de uso`() = runTest {
        val viewModel = CadastroAdminViewModel(usuarioRepository)
        viewModel.onNomeChanged("Carla Admin")
        viewModel.onEmailChanged("nova.carla@empresa.com")
        viewModel.onTelefoneChanged("11988887777")

        // Senha com menos de 8 caracteres
        viewModel.onSenhaChanged("Abc1")
        viewModel.onConfirmacaoSenhaChanged("Abc1")
        viewModel.onTermosChanged(true)
        viewModel.criarConta()
        assertTrue(viewModel.uiState.value.erroSenhaRequisitos)

        // Senhas diferentes
        viewModel.onSenhaChanged("SenhaForte123")
        viewModel.onConfirmacaoSenhaChanged("OutraSenha123")
        viewModel.criarConta()
        assertTrue(viewModel.uiState.value.erroSenhasDiferentes)

        // Cadastro com sucesso
        viewModel.onSenhaChanged("SenhaForte123")
        viewModel.onConfirmacaoSenhaChanged("SenhaForte123")
        viewModel.criarConta()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.cadastroSucesso)
    }

    @Test
    fun `testar resolucao de links COM-04 para todos os estados de negocio`() = runBlocking {
        // Enviar documento no servidor simulado para gerar token válido
        val chave = UUID.randomUUID().toString()
        val request = RemoteDocumentoEnvioRequest(
            versaoId = "v-link-test",
            documentoId = "doc-link-test",
            titulo = "Contrato Convite",
            adminId = "admin-1",
            sha256Pdf = "hash",
            tipoOrdem = TipoOrdem.PARALELA,
            mensagemConvite = "Convite",
            chaveIdempotencia = chave,
            participantes = listOf(
                RemoteParticipanteDto(
                    id = "part-convidado",
                    nome = "Pedro Alves",
                    email = "pedro@empresa.com",
                    telefone = "11900000000",
                    dataNascimento = "1992-03-10",
                    papel = PapelParticipante.CONTRATADO,
                    obrigatorio = true,
                    ordem = 1
                )
            ),
            campos = listOf(RemoteCampoDto("c1", "part-convidado", 1, 0.1f, 0.1f, 0.3f, 0.1f))
        )
        val envio = fakeRemoteDataSource.enviarDocumento(request)
        val linkUrl = envio.linksConvite["part-convidado"]!!
        val token = linkUrl.substringAfterLast("/")

        // 1. Link válido sem usuário logado
        val resValido = linkResolverRepository.resolverLink(token, null)
        assertTrue(resValido is LinkResolutionResult.ValidoParaAssinatura)

        // 2. Link acessado por outra conta logada (Bloqueio)
        val resBloqueio = linkResolverRepository.resolverLink(token, "outro.usuario@email.com")
        // O participante precisa estar registrado localmente no banco para checar o email local
        fakePartDao.salvar(
            ParticipanteDocumentoEntity(
                id = "part-convidado",
                versaoDocumentoId = "v-link-test",
                nomeSnapshot = "Pedro Alves",
                emailSnapshot = "pedro@empresa.com",
                telefoneSnapshot = "11900000000",
                dataNascimentoSnapshot = "1992-03-10",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.PENDENTE
            )
        )
        val resBloqueioComPartLocal = linkResolverRepository.resolverLink(token, "outro.usuario@email.com")
        assertTrue(resBloqueioComPartLocal is LinkResolutionResult.BloqueioOutraConta)

        // 3. Documento cancelado
        fakeDocDao.salvar(
            DocumentoEntity(
                id = "doc-link-test",
                usuarioAdminId = "admin-1",
                titulo = "Contrato",
                dataCriacaoUtc = 0L,
                dataUltimaAtualizacaoUtc = 0L,
                situacaoAtual = SituacaoDocumento.CANCELADO
            )
        )
        val resCancelado = linkResolverRepository.resolverLink(token, "pedro@empresa.com")
        assertTrue(resCancelado is LinkResolutionResult.DocumentoCancelado)

        // 4. Token inexistente ou expirado
        val resInvalido = linkResolverRepository.resolverLink("token-falso-inexistente", null)
        assertTrue(resInvalido is LinkResolutionResult.LinkInvalidoOuExpirado)
    }
}

// Fakes para suporte a testes unitários rápidos
private class FakeUsuarioDao : UsuarioDao {
    private val map = mutableMapOf<String, UsuarioEntity>()
    override suspend fun inserir(usuario: UsuarioEntity) { map[usuario.id] = usuario }
    override suspend fun atualizar(usuario: UsuarioEntity) { map[usuario.id] = usuario }
    override suspend fun buscarPorId(id: String): UsuarioEntity? = map[id]
    override suspend fun buscarPorEmail(email: String): UsuarioEntity? = map.values.find { it.email == email }
    override fun buscarPorEmailFlow(email: String): Flow<UsuarioEntity?> = MutableStateFlow(map.values.find { it.email == email })
}

private class FakeDocDao : DocumentoDao {
    private val map = mutableMapOf<String, DocumentoEntity>()
    fun salvar(doc: DocumentoEntity) { map[doc.id] = doc }
    override suspend fun inserir(documento: DocumentoEntity) { map[documento.id] = documento }
    override suspend fun atualizar(documento: DocumentoEntity) { map[documento.id] = documento }
    override suspend fun buscarPorId(id: String): DocumentoEntity? = map[id]
    override fun observarPorId(id: String): Flow<DocumentoEntity?> = MutableStateFlow(map[id])
    override fun listarPorAdmin(adminId: String): Flow<List<DocumentoEntity>> = MutableStateFlow(map.values.toList())
    override fun listarPorAdminESituacao(adminId: String, situacao: SituacaoDocumento): Flow<List<DocumentoEntity>> =
        MutableStateFlow(map.values.filter { it.situacaoAtual == situacao })
    override suspend fun deletar(documento: DocumentoEntity) { map.remove(documento.id) }
}

private class FakePartDao : ParticipanteDocumentoDao {
    private val map = mutableMapOf<String, ParticipanteDocumentoEntity>()
    fun salvar(part: ParticipanteDocumentoEntity) { map[part.id] = part }
    override suspend fun inserir(participante: ParticipanteDocumentoEntity) { map[participante.id] = participante }
    override suspend fun inserirVarios(participantes: List<ParticipanteDocumentoEntity>) { participantes.forEach { map[it.id] = it } }
    override suspend fun atualizar(participante: ParticipanteDocumentoEntity) { map[participante.id] = participante }
    override suspend fun buscarPorId(id: String): ParticipanteDocumentoEntity? = map[id]
    override fun listarPorVersao(versaoDocumentoId: String): Flow<List<ParticipanteDocumentoEntity>> = MutableStateFlow(map.values.toList())
    override suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<ParticipanteDocumentoEntity> = map.values.toList()
    override fun listarPorUsuario(usuarioId: String): Flow<List<ParticipanteDocumentoEntity>> = MutableStateFlow(map.values.toList())
    override suspend fun deletarPorId(id: String) { map.remove(id) }
    override suspend fun deletarPorVersao(versaoDocumentoId: String) { map.clear() }
}

private class DummyDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
    override val data: Flow<androidx.datastore.preferences.core.Preferences> = MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
    override suspend fun updateData(transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
        return androidx.datastore.preferences.core.emptyPreferences()
    }
}

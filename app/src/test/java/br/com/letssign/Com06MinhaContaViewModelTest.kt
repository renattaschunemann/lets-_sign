package br.com.letssign

import androidx.lifecycle.SavedStateHandle
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.remote.FakeRemoteDataSource
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import br.com.letssign.presentation.conta.Com06MinhaContaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class Com06MinhaContaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var fakeSessionFlow: MutableStateFlow<UsuarioSessao?>
    private lateinit var sessionManager: SessionManager
    private lateinit var usuarioRepository: UsuarioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUsuarioDao = FakeUsuarioDao()
        fakeSessionFlow = MutableStateFlow(
            UsuarioSessao(
                usuarioId = "admin-123",
                nome = "Carla Mendes",
                email = "carla@empresa.com.br",
                perfil = PerfilUsuario.ADMIN,
                tokenAcesso = "token-admin"
            )
        )
        val dummyCipher = CryptoManager(customKey = SecretKeySpec(ByteArray(32), "AES"))
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
        usuarioRepository = UsuarioRepository(
            usuarioDao = fakeUsuarioDao,
            remoteDataSource = FakeRemoteDataSource(latenciaSimuladaMs = 0L),
            sessionManager = sessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ao abrir edicao, deve preencher todos os campos editaveis com dados atuais`() = runTest {
        val admin = UsuarioEntity(
            id = "admin-123",
            nome = "Carla Mendes",
            email = "carla@empresa.com.br",
            telefone = "(11) 98765-4321",
            perfil = PerfilUsuario.ADMIN,
            senhaHash = "hash123",
            dataCriacaoUtc = System.currentTimeMillis(),
            cpf = "12345678901",
            dataNascimento = "15051985",
            endereco = "Av. Paulista, 1000",
            profissao = "Administradora"
        )
        fakeUsuarioDao.inserir(admin)

        val handle = SavedStateHandle(mapOf("perfil" to "ADMIN"))
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = fakeUsuarioDao,
            usuarioRepository = usuarioRepository,
            savedStateHandle = handle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.aoAbrirEdicaoConta()
        val state = viewModel.uiState.value

        assertTrue(state.mostrandoModalEditar)
        assertEquals("Carla Mendes", state.editandoNome)
        assertEquals("carla@empresa.com.br", state.editandoEmail)
        assertEquals("11987654321", state.editandoTelefone)
        assertEquals("12345678901", state.editandoCpf)
        assertEquals("15051985", state.editandoDataNascimento)
        assertEquals("Av. Paulista, 1000", state.editandoEndereco)
        assertEquals("Administradora", state.editandoProfissao)
    }

    @Test
    fun `campos numericos nao devem permitir caracteres alfabeticos ou especiais`() = runTest {
        val handle = SavedStateHandle(mapOf("perfil" to "ADMIN"))
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = fakeUsuarioDao,
            usuarioRepository = usuarioRepository,
            savedStateHandle = handle
        )

        viewModel.aoAbrirEdicaoConta()

        // Testar CPF com letras e símbolos
        viewModel.aoAlterarCpfEdicao("123.abc.456-xy78")
        assertEquals("12345678", viewModel.uiState.value.editandoCpf)

        // Testar Data de Nascimento com letras
        viewModel.aoAlterarDataNascimentoEdicao("15/05/aaaa1985")
        assertEquals("15051985", viewModel.uiState.value.editandoDataNascimento)

        // Testar Telefone com letras
        viewModel.aoAlterarTelefoneEdicao("(11) 98765-abcd4321")
        assertEquals("11987654321", viewModel.uiState.value.editandoTelefone)
    }

    @Test
    fun `ao submeter edicao com cpf e data de nascimento invalidos, deve exibir erros correspondentes`() = runTest {
        val handle = SavedStateHandle(mapOf("perfil" to "ADMIN"))
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = fakeUsuarioDao,
            usuarioRepository = usuarioRepository,
            savedStateHandle = handle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.aoAbrirEdicaoConta()
        viewModel.aoAlterarNomeEdicao("Carla Mendes")
        viewModel.aoAlterarEmailEdicao("carla@empresa.com.br")
        viewModel.aoAlterarTelefoneEdicao("11987654321")
        viewModel.aoAlterarCpfEdicao("11111111111") // CPF com dígitos todos iguais (inválido)
        viewModel.aoAlterarDataNascimentoEdicao("32132025") // Dia 32 e mês 13 (inválido)
        viewModel.aoAlterarEnderecoEdicao("Av") // Endereço muito curto
        viewModel.aoAlterarProfissaoEdicao("") // Profissão em branco

        viewModel.aoSalvarEdicaoConta()

        val state = viewModel.uiState.value
        assertNotNull(state.erroCpf)
        assertNotNull(state.erroDataNascimento)
        assertNotNull(state.erroEndereco)
        assertNotNull(state.erroProfissao)
        assertFalse(state.salvandoEdicao)
    }

    @Test
    fun `ao salvar edicao valida do signatario, deve atualizar todos os dados da conta com sucesso`() = runTest {
        fakeSessionFlow.value = UsuarioSessao(
            usuarioId = "sig-456",
            nome = "João Lima",
            email = "joao.lima@email.com",
            perfil = PerfilUsuario.SIGNATARIO,
            tokenAcesso = "token-sig"
        )
        val signatario = UsuarioEntity(
            id = "sig-456",
            nome = "João Lima",
            email = "joao.lima@email.com",
            telefone = "(11) 91234-5678",
            perfil = PerfilUsuario.SIGNATARIO,
            senhaHash = "hash456",
            dataCriacaoUtc = System.currentTimeMillis(),
            cpf = "12345678901",
            dataNascimento = "20101990",
            endereco = "Rua Antiga, 10",
            profissao = "Analista"
        )
        fakeUsuarioDao.inserir(signatario)

        val handle = SavedStateHandle(mapOf("perfil" to "SIGNATARIO"))
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = fakeUsuarioDao,
            usuarioRepository = usuarioRepository,
            savedStateHandle = handle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.aoAbrirEdicaoConta()
        viewModel.aoAlterarNomeEdicao("João Lima Santos")
        viewModel.aoAlterarEmailEdicao("joao.santos@email.com")
        viewModel.aoAlterarTelefoneEdicao("11988887777")
        viewModel.aoAlterarCpfEdicao("12345678901")
        viewModel.aoAlterarDataNascimentoEdicao("20101990")
        viewModel.aoAlterarEnderecoEdicao("Rua Nova dos Signatários, 456")
        viewModel.aoAlterarProfissaoEdicao("Engenheiro Civil")

        viewModel.aoSalvarEdicaoConta()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.mostrandoModalEditar)
        assertFalse(state.salvandoEdicao)
        assertEquals("João Lima Santos", state.nome)
        assertEquals("joao.santos@email.com", state.email)
        assertEquals("(11) 98888-7777", state.telefone)
        assertEquals("12345678901", state.cpf)
        assertEquals("20101990", state.dataNascimento)
        assertEquals("Rua Nova dos Signatários, 456", state.endereco)
        assertEquals("Engenheiro Civil", state.profissao)
        assertNotNull(state.mensagemSucesso)

        // Verificar persistência no DAO
        val usuarioAtualizado = fakeUsuarioDao.buscarPorId("sig-456")
        assertNotNull(usuarioAtualizado)
        assertEquals("João Lima Santos", usuarioAtualizado!!.nome)
        assertEquals("joao.santos@email.com", usuarioAtualizado.email)
        assertEquals("Engenheiro Civil", usuarioAtualizado.profissao)
        assertEquals("Rua Nova dos Signatários, 456", usuarioAtualizado.endereco)
    }

    @Test
    fun `quando logado como Carlos Silva, deve carregar perfil fidedigno de Carlos`() = runTest {
        val carlos = UsuarioEntity(
            id = "sig-carlos-silva",
            nome = "Carlos Silva",
            email = "carlos.silva@empresa.com",
            telefone = "(11) 95678-9012",
            perfil = PerfilUsuario.SIGNATARIO,
            senhaHash = "hashCarlos",
            dataCriacaoUtc = System.currentTimeMillis(),
            cpf = "56789012345",
            dataNascimento = "18091995",
            endereco = "Av. Presidente Juscelino Kubitschek, 1800, Vila Nova Conceição, São Paulo - SP",
            profissao = "Arquiteto de Soluções Cloud"
        )
        fakeUsuarioDao.inserir(carlos)

        fakeSessionFlow.value = UsuarioSessao(
            usuarioId = "sig-carlos-silva",
            nome = "Carlos Silva",
            email = "carlos.silva@empresa.com",
            perfil = PerfilUsuario.SIGNATARIO,
            tokenAcesso = "token-carlos"
        )

        val handle = SavedStateHandle(mapOf("perfil" to "SIGNATARIO"))
        val viewModel = Com06MinhaContaViewModel(
            sessionManager = sessionManager,
            usuarioDao = fakeUsuarioDao,
            usuarioRepository = usuarioRepository,
            savedStateHandle = handle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("sig-carlos-silva", state.usuarioId)
        assertEquals("Carlos Silva", state.nome)
        assertEquals("carlos.silva@empresa.com", state.email)
        assertEquals("(11) 95678-9012", state.telefone)
        assertEquals("56789012345", state.cpf)
        assertEquals("18091995", state.dataNascimento)
        assertEquals("Av. Presidente Juscelino Kubitschek, 1800, Vila Nova Conceição, São Paulo - SP", state.endereco)
        assertEquals("Arquiteto de Soluções Cloud", state.profissao)
        assertEquals("SIGNATARIO", state.perfil)
    }

    // Fakes para suporte ao teste
    private class FakeUsuarioDao : UsuarioDao {
        private val map = mutableMapOf<String, UsuarioEntity>()
        override suspend fun inserir(usuario: UsuarioEntity) { map[usuario.id] = usuario }
        override suspend fun atualizar(usuario: UsuarioEntity) { map[usuario.id] = usuario }
        override suspend fun buscarPorId(id: String): UsuarioEntity? = map[id]
        override suspend fun buscarPorEmail(email: String): UsuarioEntity? = map.values.find { it.email == email }
        override fun buscarPorEmailFlow(email: String): Flow<UsuarioEntity?> = MutableStateFlow(map.values.find { it.email == email })
    }

    private class DummyDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        override val data: Flow<androidx.datastore.preferences.core.Preferences> = MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences())
        override suspend fun updateData(transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
            return androidx.datastore.preferences.core.emptyPreferences()
        }
    }
}

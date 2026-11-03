package br.com.letssign.presentation.conta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class Com06UiState(
    val carregando: Boolean = true,
    val usuarioId: String = "",
    val perfil: String = "ADMIN",
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val cpf: String = "",
    val dataNascimento: String = "",
    val endereco: String = "",
    val profissao: String = "",
    val dataCriacao: String = "",
    val statusConta: String = "Ativo",
    val versaoApp: String = "1.0.0",
    val mostrandoDialogSair: Boolean = false,
    val saindo: Boolean = false,
    val logoutConcluido: Boolean = false,

    // Edição de perfil do usuário (Admin ou Signatário)
    val mostrandoModalEditar: Boolean = false,
    val editandoNome: String = "",
    val editandoEmail: String = "",
    val editandoTelefone: String = "",
    val editandoCpf: String = "",
    val editandoDataNascimento: String = "",
    val editandoEndereco: String = "",
    val editandoProfissao: String = "",
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroTelefone: String? = null,
    val erroCpf: String? = null,
    val erroDataNascimento: String? = null,
    val erroEndereco: String? = null,
    val erroProfissao: String? = null,
    val erroGeralEdicao: String? = null,
    val salvandoEdicao: Boolean = false,
    val mensagemSucesso: String? = null
)

@HiltViewModel
class Com06MinhaContaViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val usuarioDao: UsuarioDao,
    private val usuarioRepository: UsuarioRepository? = null,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Construtor de conveniência para testes que passam apenas 3 parâmetros
    constructor(
        sessionManager: SessionManager,
        usuarioDao: UsuarioDao,
        savedStateHandle: SavedStateHandle
    ) : this(
        sessionManager = sessionManager,
        usuarioDao = usuarioDao,
        usuarioRepository = null,
        savedStateHandle = savedStateHandle
    )

    val perfilArg: String = savedStateHandle["perfil"] ?: "ADMIN"

    private val _uiState = MutableStateFlow(Com06UiState(perfil = perfilArg))
    val uiState: StateFlow<Com06UiState> = _uiState.asStateFlow()

    init {
        carregarDadosUsuario()
    }

    private fun formatarDataHora(millis: Long): String {
        return try {
            if (millis <= 0) return "Não disponível"
            val instant = Instant.ofEpochMilli(millis)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (_: Exception) {
            "Não disponível"
        }
    }

    fun carregarDadosUsuario() {
        viewModelScope.launch {
            val usuarioId = sessionManager.obterUsuarioId()
            val usuario = if (usuarioId != null) usuarioDao.buscarPorId(usuarioId) else null

            if (usuario != null) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        usuarioId = usuario.id,
                        perfil = if (usuario.perfil == PerfilUsuario.ADMIN) "ADMIN" else "SIGNATARIO",
                        nome = usuario.nome,
                        email = usuario.email,
                        telefone = usuario.telefone,
                        cpf = usuario.cpf,
                        dataNascimento = usuario.dataNascimento,
                        endereco = usuario.endereco,
                        profissao = usuario.profissao,
                        dataCriacao = formatarDataHora(usuario.dataCriacaoUtc),
                        statusConta = "Ativo"
                    )
                }
            } else {
                // Fallback para Carla Mendes ou João Lima dependendo do perfil informado
                val ehAdmin = perfilArg.equals("ADMIN", ignoreCase = true)
                _uiState.update {
                    it.copy(
                        carregando = false,
                        usuarioId = if (ehAdmin) "admin-carla-mendes" else "sig-joao-lima",
                        perfil = if (ehAdmin) "ADMIN" else "SIGNATARIO",
                        nome = if (ehAdmin) "Carla Mendes" else "João Lima",
                        email = if (ehAdmin) "carla@empresa.com.br" else "joao.lima@email.com",
                        telefone = if (ehAdmin) "(11) 98765-4321" else "(11) 91234-5678",
                        cpf = if (ehAdmin) "12345678900" else "98765432199",
                        dataNascimento = if (ehAdmin) "15041988" else "20081992",
                        endereco = if (ehAdmin) "Av. Paulista, 1000, Bela Vista, São Paulo - SP" else "Rua Augusta, 500, Consolação, São Paulo - SP",
                        profissao = if (ehAdmin) "Administradora de Contratos" else "Signatário / Analista",
                        dataCriacao = "Recente",
                        statusConta = "Ativo"
                    )
                }
            }
        }
    }

    fun aoSolicitarSair() {
        _uiState.update { it.copy(mostrandoDialogSair = true) }
    }

    fun aoCancelarSair() {
        _uiState.update { it.copy(mostrandoDialogSair = false) }
    }

    fun aoConfirmarSair() {
        _uiState.update { it.copy(mostrandoDialogSair = false, saindo = true) }
        viewModelScope.launch {
            sessionManager.limparSessao()
            _uiState.update { it.copy(saindo = false, logoutConcluido = true) }
        }
    }

    // Ações de edição de conta do usuário (Admin e Signatário)
    fun aoAbrirEdicaoConta() {
        val estadoAtual = _uiState.value
        _uiState.update {
            it.copy(
                mostrandoModalEditar = true,
                editandoNome = estadoAtual.nome,
                editandoEmail = estadoAtual.email,
                editandoTelefone = CadastroValidator.apenasNumeros(estadoAtual.telefone).take(11),
                editandoCpf = CadastroValidator.apenasNumeros(estadoAtual.cpf).take(11),
                editandoDataNascimento = CadastroValidator.apenasNumeros(estadoAtual.dataNascimento).take(8),
                editandoEndereco = estadoAtual.endereco,
                editandoProfissao = estadoAtual.profissao,
                erroNome = null,
                erroEmail = null,
                erroTelefone = null,
                erroCpf = null,
                erroDataNascimento = null,
                erroEndereco = null,
                erroProfissao = null,
                erroGeralEdicao = null
            )
        }
    }

    fun aoFecharEdicaoConta() {
        _uiState.update {
            it.copy(
                mostrandoModalEditar = false,
                erroNome = null,
                erroEmail = null,
                erroTelefone = null,
                erroCpf = null,
                erroDataNascimento = null,
                erroEndereco = null,
                erroProfissao = null,
                erroGeralEdicao = null
            )
        }
    }

    fun aoAlterarNomeEdicao(novoNome: String) {
        _uiState.update { it.copy(editandoNome = novoNome, erroNome = null, erroGeralEdicao = null) }
    }

    fun aoAlterarEmailEdicao(novoEmail: String) {
        _uiState.update { it.copy(editandoEmail = novoEmail.trim(), erroEmail = null, erroGeralEdicao = null) }
    }

    // Onde é número NÃO permite caracteres alfabéticos/especiais
    fun aoAlterarTelefoneEdicao(novoTelefone: String) {
        val apenasDigitos = CadastroValidator.apenasNumeros(novoTelefone).take(11)
        _uiState.update { it.copy(editandoTelefone = apenasDigitos, erroTelefone = null, erroGeralEdicao = null) }
    }

    fun aoAlterarCpfEdicao(novoCpf: String) {
        val apenasDigitos = CadastroValidator.apenasNumeros(novoCpf).take(11)
        _uiState.update { it.copy(editandoCpf = apenasDigitos, erroCpf = null, erroGeralEdicao = null) }
    }

    fun aoAlterarDataNascimentoEdicao(novaData: String) {
        val apenasDigitos = CadastroValidator.apenasNumeros(novaData).take(8)
        _uiState.update { it.copy(editandoDataNascimento = apenasDigitos, erroDataNascimento = null, erroGeralEdicao = null) }
    }

    fun aoAlterarEnderecoEdicao(novoEndereco: String) {
        _uiState.update { it.copy(editandoEndereco = novoEndereco, erroEndereco = null, erroGeralEdicao = null) }
    }

    fun aoAlterarProfissaoEdicao(novaProfissao: String) {
        _uiState.update { it.copy(editandoProfissao = novaProfissao, erroProfissao = null, erroGeralEdicao = null) }
    }

    fun aoSalvarEdicaoConta() {
        val estado = _uiState.value
        val nomeValido = CadastroValidator.validarNome(estado.editandoNome)
        val emailValido = CadastroValidator.validarEmail(estado.editandoEmail)
        val telefoneValido = CadastroValidator.validarTelefoneComDdd(estado.editandoTelefone)
        val cpfValido = CadastroValidator.validarCpf(estado.editandoCpf)
        val dataNascimentoValida = CadastroValidator.validarDataNascimento(estado.editandoDataNascimento)
        val enderecoValido = CadastroValidator.validarEndereco(estado.editandoEndereco)
        val profissaoValida = estado.editandoProfissao.trim().isNotBlank()

        var temErro = false
        var erroNomeMsg: String? = null
        var erroEmailMsg: String? = null
        var erroTelefoneMsg: String? = null
        var erroCpfMsg: String? = null
        var erroDataNascimentoMsg: String? = null
        var erroEnderecoMsg: String? = null
        var erroProfissaoMsg: String? = null

        if (!nomeValido) {
            erroNomeMsg = "Informe seu nome completo (ao menos 3 caracteres)"
            temErro = true
        }
        if (!emailValido) {
            erroEmailMsg = "Informe um e-mail válido"
            temErro = true
        }
        if (!telefoneValido) {
            erroTelefoneMsg = "Informe um telefone válido com DDD (10 ou 11 dígitos)"
            temErro = true
        }
        if (!cpfValido) {
            erroCpfMsg = "Informe um CPF válido (11 dígitos)"
            temErro = true
        }
        if (!dataNascimentoValida) {
            erroDataNascimentoMsg = "Informe uma data de nascimento válida (DD/MM/AAAA)"
            temErro = true
        }
        if (!enderecoValido) {
            erroEnderecoMsg = "Informe um endereço válido (ao menos 5 caracteres)"
            temErro = true
        }
        if (!profissaoValida) {
            erroProfissaoMsg = "Informe sua profissão"
            temErro = true
        }

        if (temErro) {
            _uiState.update {
                it.copy(
                    erroNome = erroNomeMsg,
                    erroEmail = erroEmailMsg,
                    erroTelefone = erroTelefoneMsg,
                    erroCpf = erroCpfMsg,
                    erroDataNascimento = erroDataNascimentoMsg,
                    erroEndereco = erroEnderecoMsg,
                    erroProfissao = erroProfissaoMsg
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(salvandoEdicao = true, erroGeralEdicao = null) }

            val telefoneFormatado = CadastroValidator.formatarTelefone(estado.editandoTelefone)
            val idUsuario = estado.usuarioId.ifBlank {
                if (estado.perfil.equals("ADMIN", ignoreCase = true)) "admin-carla-mendes" else "sig-joao-lima"
            }

            val resultado: Result<UsuarioEntity> = if (usuarioRepository != null) {
                usuarioRepository.atualizarPerfil(
                    usuarioId = idUsuario,
                    novoNome = estado.editandoNome.trim(),
                    novoEmail = estado.editandoEmail.trim(),
                    novoTelefone = telefoneFormatado,
                    novoCpf = estado.editandoCpf,
                    novaDataNascimento = estado.editandoDataNascimento,
                    novoEndereco = estado.editandoEndereco.trim(),
                    novaProfissao = estado.editandoProfissao.trim()
                )
            } else {
                try {
                    val usuarioPorEmail = usuarioDao.buscarPorEmail(estado.editandoEmail.trim())
                    if (usuarioPorEmail != null && usuarioPorEmail.id != idUsuario) {
                        Result.failure(IllegalArgumentException("Este e-mail já está em uso por outro usuário."))
                    } else {
                        val usuarioAtual = usuarioDao.buscarPorId(idUsuario) ?: usuarioPorEmail
                        val perfilAtual = usuarioAtual?.perfil ?: if (estado.perfil.equals("ADMIN", ignoreCase = true)) PerfilUsuario.ADMIN else PerfilUsuario.SIGNATARIO
                        val usuarioSalvar = if (usuarioAtual != null) {
                            usuarioAtual.copy(
                                nome = estado.editandoNome.trim(),
                                email = estado.editandoEmail.trim(),
                                telefone = telefoneFormatado,
                                cpf = estado.editandoCpf,
                                dataNascimento = estado.editandoDataNascimento,
                                endereco = estado.editandoEndereco.trim(),
                                profissao = estado.editandoProfissao.trim()
                            )
                        } else {
                            UsuarioEntity(
                                id = idUsuario,
                                nome = estado.editandoNome.trim(),
                                email = estado.editandoEmail.trim(),
                                telefone = telefoneFormatado,
                                perfil = perfilAtual,
                                senhaHash = "123456",
                                dataCriacaoUtc = System.currentTimeMillis(),
                                cpf = estado.editandoCpf,
                                dataNascimento = estado.editandoDataNascimento,
                                endereco = estado.editandoEndereco.trim(),
                                profissao = estado.editandoProfissao.trim()
                            )
                        }

                        if (usuarioAtual != null) {
                            usuarioDao.atualizar(usuarioSalvar)
                        } else {
                            usuarioDao.inserir(usuarioSalvar)
                        }

                        sessionManager.salvarSessao(
                            UsuarioSessao(
                                usuarioId = usuarioSalvar.id,
                                nome = usuarioSalvar.nome,
                                email = usuarioSalvar.email,
                                perfil = usuarioSalvar.perfil,
                                tokenAcesso = "token-${usuarioSalvar.id}"
                            )
                        )

                        Result.success(usuarioSalvar)
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            resultado.onSuccess { usuarioSalvo ->
                _uiState.update {
                    it.copy(
                        salvandoEdicao = false,
                        mostrandoModalEditar = false,
                        nome = usuarioSalvo.nome,
                        email = usuarioSalvo.email,
                        telefone = usuarioSalvo.telefone,
                        cpf = usuarioSalvo.cpf,
                        dataNascimento = usuarioSalvo.dataNascimento,
                        endereco = usuarioSalvo.endereco,
                        profissao = usuarioSalvo.profissao,
                        dataCriacao = formatarDataHora(usuarioSalvo.dataCriacaoUtc),
                        mensagemSucesso = "Dados da conta atualizados com sucesso!"
                    )
                }
            }.onFailure { excecao ->
                val mensagem = excecao.message ?: "Erro ao atualizar dados da conta"
                _uiState.update {
                    it.copy(
                        salvandoEdicao = false,
                        erroGeralEdicao = mensagem,
                        erroEmail = if (mensagem.contains("e-mail", ignoreCase = true)) mensagem else null
                    )
                }
            }
        }
    }

    fun aoLimparMensagemSucesso() {
        _uiState.update { it.copy(mensagemSucesso = null) }
    }
}

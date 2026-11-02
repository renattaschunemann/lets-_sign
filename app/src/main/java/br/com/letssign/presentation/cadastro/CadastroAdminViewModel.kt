package br.com.letssign.presentation.cadastro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class CadastroAdminUiState(
    val nome: String = "",
    val cpf: String = "",
    val dataNascimento: String = "",
    val telefone: String = "",
    val endereco: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmacaoSenha: String = "",
    val termosAceitos: Boolean = false,
    val perfilSelecionado: PerfilUsuario = PerfilUsuario.SIGNATARIO,
    val estaCarregando: Boolean = false,
    val erroNome: String? = null,
    val erroCpf: String? = null,
    val erroDataNascimento: String? = null,
    val erroTelefone: String? = null,
    val erroEndereco: String? = null,
    val erroEmail: String? = null,
    val erroEmailEmUso: Boolean = false,
    val erroSenhasDiferentes: Boolean = false,
    val erroSenhaRequisitos: Boolean = false,
    val erroConexao: String? = null,
    val cadastroSucesso: Boolean = false,
    val cadastroSucessoPerfil: PerfilUsuario? = null
) {
    val temPeloMenos8Caracteres: Boolean get() = senha.length >= 8
    val temLetras: Boolean get() = senha.any { it.isLetter() }
    val temNumeros: Boolean get() = senha.any { it.isDigit() }
    val senhaValida: Boolean get() = temPeloMenos8Caracteres && temLetras && temNumeros

    val nomeValido: Boolean get() = CadastroValidator.validarNome(nome)
    val cpfValido: Boolean get() = CadastroValidator.validarCpf(cpf)
    val dataNascimentoValida: Boolean get() = CadastroValidator.validarDataNascimento(dataNascimento)
    val telefoneValido: Boolean get() = CadastroValidator.validarTelefoneComDdd(telefone)
    val enderecoValido: Boolean get() = CadastroValidator.validarEndereco(endereco)
    val emailValido: Boolean get() = CadastroValidator.validarEmail(email)

    val podeSubmeter: Boolean
        get() = termosAceitos && senhaValida && senha == confirmacaoSenha &&
                nome.isNotBlank() && cpf.isNotBlank() && dataNascimento.isNotBlank() &&
                telefone.isNotBlank() && endereco.isNotBlank() && email.isNotBlank()
}

@HiltViewModel
class CadastroAdminViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroAdminUiState())
    val uiState: StateFlow<CadastroAdminUiState> = _uiState.asStateFlow()

    fun onPerfilChanged(perfil: PerfilUsuario) {
        _uiState.value = _uiState.value.copy(perfilSelecionado = perfil)
    }

    fun onNomeChanged(nome: String) {
        _uiState.value = _uiState.value.copy(
            nome = nome,
            erroNome = null
        )
    }

    fun onCpfChanged(cpfInput: String) {
        val digitos = CadastroValidator.apenasNumeros(cpfInput).take(11)
        _uiState.value = _uiState.value.copy(
            cpf = digitos,
            erroCpf = null
        )
    }

    fun onDataNascimentoChanged(dataInput: String) {
        val digitos = CadastroValidator.apenasNumeros(dataInput).take(8)
        _uiState.value = _uiState.value.copy(
            dataNascimento = digitos,
            erroDataNascimento = null
        )
    }

    fun onTelefoneChanged(telefoneInput: String) {
        val digitos = CadastroValidator.apenasNumeros(telefoneInput).take(11)
        _uiState.value = _uiState.value.copy(
            telefone = digitos,
            erroTelefone = null
        )
    }

    fun onEnderecoChanged(endereco: String) {
        _uiState.value = _uiState.value.copy(
            endereco = endereco,
            erroEndereco = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            erroEmail = null,
            erroEmailEmUso = false,
            erroConexao = null
        )
    }

    fun onSenhaChanged(senha: String) {
        _uiState.value = _uiState.value.copy(
            senha = senha,
            erroSenhaRequisitos = false,
            erroSenhasDiferentes = false
        )
    }

    fun onConfirmacaoSenhaChanged(confirmacao: String) {
        _uiState.value = _uiState.value.copy(
            confirmacaoSenha = confirmacao,
            erroSenhasDiferentes = false
        )
    }

    fun onTermosChanged(aceito: Boolean) {
        _uiState.value = _uiState.value.copy(termosAceitos = aceito)
    }

    fun criarConta() {
        val state = _uiState.value
        var temErro = false
        var novoState = state

        if (!CadastroValidator.validarNome(state.nome)) {
            novoState = novoState.copy(erroNome = "Informe o nome completo.")
            temErro = true
        }

        if (!CadastroValidator.validarCpf(state.cpf)) {
            novoState = novoState.copy(erroCpf = "CPF deve conter exatamente 11 dígitos numéricos.")
            temErro = true
        }

        if (!CadastroValidator.validarDataNascimento(state.dataNascimento)) {
            novoState = novoState.copy(erroDataNascimento = "Data de nascimento inválida (somente números, DD/MM/AAAA).")
            temErro = true
        }

        if (!CadastroValidator.validarTelefoneComDdd(state.telefone)) {
            novoState = novoState.copy(erroTelefone = "Telefone com DDD obrigatório (mínimo 10 dígitos, ex: 11 98765-4321).")
            temErro = true
        }

        if (!CadastroValidator.validarEndereco(state.endereco)) {
            novoState = novoState.copy(erroEndereco = "Informe o endereço completo (rua, número, bairro e cidade).")
            temErro = true
        }

        if (!CadastroValidator.validarEmail(state.email)) {
            novoState = novoState.copy(erroEmail = "E-mail inválido. Deve conter '@' e um domínio válido.")
            temErro = true
        }

        if (!state.senhaValida) {
            novoState = novoState.copy(erroSenhaRequisitos = true)
            temErro = true
        }

        if (state.senha != state.confirmacaoSenha) {
            novoState = novoState.copy(erroSenhasDiferentes = true)
            temErro = true
        }

        if (!state.termosAceitos) {
            novoState = novoState.copy(erroConexao = "É necessário aceitar os termos de uso e privacidade para criar a conta.")
            temErro = true
        }

        if (temErro) {
            _uiState.value = novoState
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                estaCarregando = true,
                erroConexao = null,
                erroEmailEmUso = false
            )
            val resultado = usuarioRepository.cadastrarUsuario(
                nome = state.nome.trim(),
                email = state.email.trim(),
                telefone = CadastroValidator.formatarTelefone(state.telefone),
                senha = state.senha,
                perfil = state.perfilSelecionado,
                cpf = CadastroValidator.formatarCpf(state.cpf),
                dataNascimento = CadastroValidator.formatarDataNascimento(state.dataNascimento),
                endereco = state.endereco.trim()
            )
            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.value = _uiState.value.copy(
                        estaCarregando = false,
                        cadastroSucesso = true,
                        cadastroSucessoPerfil = usuario.perfil
                    )
                },
                onFailure = { error ->
                    if (error is IOException) {
                        _uiState.value = _uiState.value.copy(
                            estaCarregando = false,
                            erroConexao = "Sem conexão. Verifique a internet e tente novamente."
                        )
                    } else if (error.message?.contains("P01") == true || error.message?.contains("E06") == true) {
                        _uiState.value = _uiState.value.copy(
                            estaCarregando = false,
                            erroEmailEmUso = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            estaCarregando = false,
                            erroConexao = error.message ?: "Erro ao criar conta."
                        )
                    }
                }
            )
        }
    }
}

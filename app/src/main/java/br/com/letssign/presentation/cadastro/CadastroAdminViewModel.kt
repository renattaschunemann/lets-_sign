package br.com.letssign.presentation.cadastro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class CadastroAdminUiState(
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val senha: String = "",
    val confirmacaoSenha: String = "",
    val termosAceitos: Boolean = false,
    val estaCarregando: Boolean = false,
    val erroEmailEmUso: Boolean = false,
    val erroSenhasDiferentes: Boolean = false,
    val erroSenhaRequisitos: Boolean = false,
    val erroConexao: String? = null,
    val cadastroSucesso: Boolean = false
) {
    val temPeloMenos8Caracteres: Boolean get() = senha.length >= 8
    val temLetras: Boolean get() = senha.any { it.isLetter() }
    val temNumeros: Boolean get() = senha.any { it.isDigit() }
    val senhaValida: Boolean get() = temPeloMenos8Caracteres && temLetras && temNumeros
}

@HiltViewModel
class CadastroAdminViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroAdminUiState())
    val uiState: StateFlow<CadastroAdminUiState> = _uiState.asStateFlow()

    fun onNomeChanged(nome: String) {
        _uiState.value = _uiState.value.copy(nome = nome)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            erroEmailEmUso = false,
            erroConexao = null
        )
    }

    fun onTelefoneChanged(telefone: String) {
        // Formatar telefone se necessário
        _uiState.value = _uiState.value.copy(telefone = telefone)
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

        // Validação P02: Senha de pelo menos 8 caracteres com letras e números
        if (!state.senhaValida) {
            _uiState.value = state.copy(erroSenhaRequisitos = true)
            return
        }

        // Validação E05: Senhas conferem
        if (state.senha != state.confirmacaoSenha) {
            _uiState.value = state.copy(erroSenhasDiferentes = true)
            return
        }

        if (!state.termosAceitos) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCarregando = true, erroConexao = null, erroEmailEmUso = false)
            val resultado = usuarioRepository.cadastrarAdmin(
                nome = state.nome,
                email = state.email,
                telefone = state.telefone,
                senha = state.senha
            )
            resultado.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        estaCarregando = false,
                        cadastroSucesso = true
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

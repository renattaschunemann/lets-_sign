package br.com.letssign.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val estaCarregando: Boolean = false,
    val erroFormatoEmail: String? = null,
    val erroCredenciais: String? = null,
    val erroConexao: String? = null,
    val estaBloqueado: Boolean = false,
    val tentativasFalhas: Int = 0,
    val loginSucessoPerfil: PerfilUsuario? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            erroFormatoEmail = null,
            erroCredenciais = null,
            erroConexao = null
        )
    }

    fun onSenhaChanged(senha: String) {
        _uiState.value = _uiState.value.copy(
            senha = senha,
            erroCredenciais = null,
            erroConexao = null
        )
    }

    fun entrar() {
        val state = _uiState.value
        val email = state.email.trim()
        val senha = state.senha

        // Validação de formato de e-mail
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.value = state.copy(
                erroFormatoEmail = "Informe um e-mail válido, como nome@empresa.com."
            )
            return
        }

        if (state.tentativasFalhas >= 5) {
            _uiState.value = state.copy(
                estaBloqueado = true,
                erroCredenciais = "Muitas tentativas. Aguarde alguns minutos para tentar de novo."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCarregando = true, erroCredenciais = null, erroConexao = null)
            val resultado = usuarioRepository.autenticar(email, senha)
            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.value = _uiState.value.copy(
                        estaCarregando = false,
                        loginSucessoPerfil = usuario.perfil,
                        tentativasFalhas = 0
                    )
                },
                onFailure = { error ->
                    val novasFalhas = _uiState.value.tentativasFalhas + 1
                    val bloqueado = novasFalhas >= 5
                    if (error is IOException) {
                        _uiState.value = _uiState.value.copy(
                            estaCarregando = false,
                            erroConexao = "Sem conexão. Verifique a internet e tente novamente."
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            estaCarregando = false,
                            tentativasFalhas = novasFalhas,
                            estaBloqueado = bloqueado,
                            erroCredenciais = if (bloqueado) {
                                "Muitas tentativas. Aguarde alguns minutos para tentar de novo."
                            } else {
                                "E-mail ou senha incorretos. Confira e tente novamente."
                            }
                        )
                    }
                }
            )
        }
    }
}

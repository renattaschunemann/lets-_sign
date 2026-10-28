package br.com.letssign.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.repository.UsuarioRepository
import br.com.letssign.domain.model.PerfilUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Carregando : SplashUiState
    data object Erro : SplashUiState
    data class RedirecionarAdmin(val usuarioId: String) : SplashUiState
    data class RedirecionarSignatario(val usuarioId: String) : SplashUiState
    data object RedirecionarLogin : SplashUiState
    data class RedirecionarLink(val token: String) : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Carregando)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun verificarSessao(tokenLinkPendente: String? = null) {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Carregando
            try {
                // Aguarda 1.5s conforme UX do Figma Make sem animação longa
                delay(1500)

                if (!tokenLinkPendente.isNullOrBlank()) {
                    _uiState.value = SplashUiState.RedirecionarLink(tokenLinkPendente)
                    return@launch
                }

                val sessao = usuarioRepository.sessaoAtiva.first()
                if (sessao != null && sessao.usuarioId.isNotBlank()) {
                    if (sessao.perfil == PerfilUsuario.ADMIN) {
                        _uiState.value = SplashUiState.RedirecionarAdmin(sessao.usuarioId)
                    } else {
                        _uiState.value = SplashUiState.RedirecionarSignatario(sessao.usuarioId)
                    }
                } else {
                    _uiState.value = SplashUiState.RedirecionarLogin
                }
            } catch (_: Exception) {
                _uiState.value = SplashUiState.Erro
            }
        }
    }
}

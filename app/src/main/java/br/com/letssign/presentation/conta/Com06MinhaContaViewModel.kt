package br.com.letssign.presentation.conta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.domain.model.PerfilUsuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Com06UiState(
    val carregando: Boolean = true,
    val perfil: String = "ADMIN",
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val versaoApp: String = "1.0.0",
    val mostrandoDialogSair: Boolean = false,
    val saindo: Boolean = false,
    val logoutConcluido: Boolean = false
)

@HiltViewModel
class Com06MinhaContaViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val usuarioDao: UsuarioDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val perfilArg: String = savedStateHandle["perfil"] ?: "ADMIN"

    private val _uiState = MutableStateFlow(Com06UiState(perfil = perfilArg))
    val uiState: StateFlow<Com06UiState> = _uiState.asStateFlow()

    init {
        carregarDadosUsuario()
    }

    fun carregarDadosUsuario() {
        viewModelScope.launch {
            val usuarioId = sessionManager.obterUsuarioId()
            val usuario = if (usuarioId != null) usuarioDao.buscarPorId(usuarioId) else null

            if (usuario != null) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        perfil = if (usuario.perfil == PerfilUsuario.ADMIN) "ADMIN" else "SIGNATARIO",
                        nome = usuario.nome,
                        email = usuario.email,
                        telefone = usuario.telefone
                    )
                }
            } else {
                // Fallback para Carla Mendes ou João Lima dependendo do perfil informado
                val ehAdmin = perfilArg.equals("ADMIN", ignoreCase = true)
                _uiState.update {
                    it.copy(
                        carregando = false,
                        perfil = if (ehAdmin) "ADMIN" else "SIGNATARIO",
                        nome = if (ehAdmin) "Carla Mendes" else "João Lima",
                        email = if (ehAdmin) "carla@empresa.com.br" else "joao.lima@email.com",
                        telefone = if (ehAdmin) "(11) 98765-4321" else "(11) 91234-5678"
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
}

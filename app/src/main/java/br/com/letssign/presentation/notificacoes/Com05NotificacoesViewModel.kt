package br.com.letssign.presentation.notificacoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.entities.NotificacaoEntity
import br.com.letssign.data.repository.NotificacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Com05UiState(
    val carregando: Boolean = true,
    val perfil: String = "ADMIN",
    val notificacoes: List<NotificacaoEntity> = emptyList(),
    val quantidadeNaoLidas: Int = 0,
    val filtroApenasNaoLidas: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class Com05NotificacoesViewModel @Inject constructor(
    private val notificacaoRepository: NotificacaoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val perfilArg: String = savedStateHandle["perfil"] ?: "ADMIN"

    private val _filtroApenasNaoLidas = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(Com05UiState(perfil = perfilArg))
    val uiState: StateFlow<Com05UiState> = _uiState.asStateFlow()

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes() {
        viewModelScope.launch {
            val usuarioId = sessionManager.obterUsuarioId()
                ?: if (perfilArg.equals("SIGNATARIO", ignoreCase = true)) "sig-joao-lima" else "admin-carla-mendes"

            notificacaoRepository.inicializarDadosDemoSeNecessario(usuarioId, perfilArg)

            combine(
                notificacaoRepository.listarNotificacoes(usuarioId),
                notificacaoRepository.contarNaoLidas(usuarioId),
                _filtroApenasNaoLidas
            ) { lista, naoLidas, apenasNaoLidas ->
                val filtradas = if (apenasNaoLidas) lista.filter { !it.lida } else lista
                Com05UiState(
                    carregando = false,
                    perfil = perfilArg,
                    notificacoes = filtradas,
                    quantidadeNaoLidas = naoLidas,
                    filtroApenasNaoLidas = apenasNaoLidas,
                    erro = null
                )
            }.catch { e ->
                _uiState.update { it.copy(carregando = false, erro = e.message) }
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun aoAlternarFiltroNaoLidas(apenasNaoLidas: Boolean) {
        _filtroApenasNaoLidas.value = apenasNaoLidas
    }

    fun aoMarcarComoLida(id: String) {
        viewModelScope.launch {
            notificacaoRepository.marcarComoLida(id)
        }
    }

    fun aoMarcarTodasComoLidas() {
        viewModelScope.launch {
            val usuarioId = sessionManager.obterUsuarioId()
                ?: if (perfilArg.equals("SIGNATARIO", ignoreCase = true)) "sig-joao-lima" else "admin-carla-mendes"
            notificacaoRepository.marcarTodasComoLidas(usuarioId)
        }
    }
}

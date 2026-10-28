package br.com.letssign.presentation.admin.signatarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.data.repository.NotificacaoRepository
import br.com.letssign.data.repository.SignatarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Adm02UiState(
    val carregando: Boolean = true,
    val termoBusca: String = "",
    val signatarios: List<AdminSignatarioEntity> = emptyList(),
    val quantidadeNaoLidas: Int = 0,
    val erro: String? = null
)

@HiltViewModel
class Adm02SignatariosViewModel @Inject constructor(
    private val signatarioRepository: SignatarioRepository,
    private val notificacaoRepository: NotificacaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _termoBusca = MutableStateFlow("")
    private val _uiState = MutableStateFlow(Adm02UiState())
    val uiState: StateFlow<Adm02UiState> = _uiState.asStateFlow()

    init {
        carregarSignatarios()
    }

    fun carregarSignatarios() {
        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            signatarioRepository.inicializarDadosDemoSeNecessario(adminId)

            combine(
                signatarioRepository.listarSignatarios(adminId),
                notificacaoRepository.contarNaoLidas(adminId),
                _termoBusca
            ) { lista, naoLidas, busca ->
                val filtrados = if (busca.isBlank()) {
                    lista
                } else {
                    val termo = busca.trim().lowercase()
                    lista.filter {
                        it.nome.lowercase().contains(termo) || it.email.lowercase().contains(termo)
                    }
                }
                Adm02UiState(
                    carregando = false,
                    termoBusca = busca,
                    signatarios = filtrados,
                    quantidadeNaoLidas = naoLidas,
                    erro = null
                )
            }.catch { e ->
                _uiState.update { it.copy(carregando = false, erro = e.message) }
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun aoAlterarBusca(novaBusca: String) {
        _termoBusca.value = novaBusca
    }

    fun aoExcluirSignatario(signatarioId: String) {
        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            signatarioRepository.excluirSignatario(adminId, signatarioId)
        }
    }
}

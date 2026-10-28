package br.com.letssign.presentation.admin.documentos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.dao.VersaoDocumentoDao
import br.com.letssign.data.local.entities.EventoHistoricoEntity
import br.com.letssign.data.local.entities.VersaoDocumentoEntity
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.FiltroAuditoria
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Adm12AuditoriaUiState(
    val versao: VersaoDocumentoEntity? = null,
    val filtroSelecionado: FiltroAuditoria = FiltroAuditoria.TODOS,
    val eventos: List<EventoHistoricoEntity> = emptyList(),
    val carregando: Boolean = true,
    val mensagemFeedback: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class Adm12AuditoriaViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val versaoDao: VersaoDocumentoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val paramDocId: String = savedStateHandle.get<String>("documentoId") ?: ""
    private val paramVersaoId: String = savedStateHandle.get<String>("versaoId") ?: ""

    private val _filtro = MutableStateFlow(FiltroAuditoria.TODOS)
    private val _uiState = MutableStateFlow(Adm12AuditoriaUiState())
    val uiState: StateFlow<Adm12AuditoriaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val versao = if (paramVersaoId.isNotBlank()) {
                versaoDao.buscarPorId(paramVersaoId)
            } else if (paramDocId.isNotBlank()) {
                versaoDao.buscarVersaoAtual(paramDocId)
            } else null

            _uiState.update { it.copy(versao = versao) }

            val resolvedVersaoId = versao?.id ?: paramVersaoId

            _filtro.flatMapLatest { filtro ->
                documentoRepository.listarEventosAuditoria(resolvedVersaoId, filtro)
            }.collect { lista ->
                _uiState.update {
                    it.copy(
                        eventos = lista,
                        filtroSelecionado = _filtro.value,
                        carregando = false
                    )
                }
            }
        }
    }

    fun mudarFiltro(novoFiltro: FiltroAuditoria) {
        _filtro.value = novoFiltro
    }

    fun definirFeedback(msg: String) {
        _uiState.update { it.copy(mensagemFeedback = msg) }
    }

    fun limparFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }
}

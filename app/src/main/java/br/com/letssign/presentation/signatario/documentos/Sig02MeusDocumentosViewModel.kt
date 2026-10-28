package br.com.letssign.presentation.signatario.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.DocumentoSignatarioItem
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FiltroDocumentoSignatario {
    TODOS,
    ASSINADOS,
    RECUSADOS,
    CANCELADOS
}

data class Sig02UiState(
    val carregando: Boolean = true,
    val busca: String = "",
    val filtroSelecionado: FiltroDocumentoSignatario = FiltroDocumentoSignatario.TODOS,
    val todosDocumentos: List<DocumentoSignatarioItem> = emptyList(),
    val erro: String? = null
) {
    val documentosFiltrados: List<DocumentoSignatarioItem>
        get() = todosDocumentos.filter { doc ->
            val matchBusca = busca.isBlank() || doc.titulo.contains(busca.trim(), ignoreCase = true)
            val matchFiltro = when (filtroSelecionado) {
                FiltroDocumentoSignatario.TODOS -> true
                FiltroDocumentoSignatario.ASSINADOS -> doc.situacaoParticipante == SituacaoParticipante.ASSINADO
                FiltroDocumentoSignatario.RECUSADOS -> doc.situacaoParticipante == SituacaoParticipante.RECUSADO
                FiltroDocumentoSignatario.CANCELADOS -> doc.situacaoDocumento == SituacaoDocumento.CANCELADO
            }
            matchBusca && matchFiltro
        }
}

@HiltViewModel
class Sig02MeusDocumentosViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(Sig02UiState())
    val uiState: StateFlow<Sig02UiState> = _uiState.asStateFlow()

    init {
        carregarDocumentos()
    }

    fun atualizarBusca(query: String) {
        _uiState.update { it.copy(busca = query) }
    }

    fun selecionarFiltro(filtro: FiltroDocumentoSignatario) {
        _uiState.update { it.copy(filtroSelecionado = filtro) }
    }

    fun carregarDocumentos() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            val sessao = sessionManager.sessaoFlow.firstOrNull()
            val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

            documentoRepository.listarDocumentosDoSignatario(usuarioId)
                .catch { ex ->
                    _uiState.update { it.copy(carregando = false, erro = ex.message) }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            todosDocumentos = lista
                        )
                    }
                }
        }
    }
}

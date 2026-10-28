package br.com.letssign.presentation.admin.documentos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DocumentoDetalhado
import br.com.letssign.data.repository.DocumentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Adm11AcompanhamentoUiState(
    val documento: DocumentoDetalhado? = null,
    val carregando: Boolean = true,
    val mostrarModalCancelar: Boolean = false,
    val mostrarModalRecusa: Boolean = false,
    val mostrarModalNovaVersao: Boolean = false,
    val erroCancelamento: String? = null,
    val mensagemFeedback: String? = null
)

@HiltViewModel
class Adm11AcompanhamentoViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = checkNotNull(savedStateHandle.get<String>("documentoId"))

    private val _uiState = MutableStateFlow(Adm11AcompanhamentoUiState())
    val uiState: StateFlow<Adm11AcompanhamentoUiState> = _uiState.asStateFlow()

    private var adminIdAtual: String = ""

    init {
        viewModelScope.launch {
            adminIdAtual = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            documentoRepository.carregarDocumentoDetalhado(documentoId).collect { detalhe ->
                _uiState.update { it.copy(documento = detalhe, carregando = false) }
            }
        }
    }

    fun reenviarConvite(participanteId: String) {
        viewModelScope.launch {
            val res = documentoRepository.reenviarConvite(documentoId, participanteId)
            if (res.isSuccess) {
                _uiState.update { it.copy(mensagemFeedback = "Convite reenviado com sucesso.") }
            }
        }
    }

    fun abrirModalCancelar() {
        _uiState.update { it.copy(mostrarModalCancelar = true, erroCancelamento = null) }
    }

    fun fecharModalCancelar() {
        _uiState.update { it.copy(mostrarModalCancelar = false, erroCancelamento = null) }
    }

    fun cancelarDocumento(motivo: String, senhaAdmin: String, aoSucesso: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erroCancelamento = null) }
            val res = documentoRepository.cancelarDocumento(
                documentoId = documentoId,
                adminId = adminIdAtual,
                motivo = motivo,
                senhaAdmin = senhaAdmin
            )
            if (res.isSuccess) {
                _uiState.update { it.copy(carregando = false, mostrarModalCancelar = false) }
                aoSucesso()
            } else {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        erroCancelamento = res.exceptionOrNull()?.message ?: "Erro ao cancelar documento."
                    )
                }
            }
        }
    }

    fun abrirModalRecusa() {
        _uiState.update { it.copy(mostrarModalRecusa = true) }
    }

    fun fecharModalRecusa() {
        _uiState.update { it.copy(mostrarModalRecusa = false) }
    }

    fun abrirModalNovaVersao() {
        _uiState.update { it.copy(mostrarModalNovaVersao = true) }
    }

    fun fecharModalNovaVersao() {
        _uiState.update { it.copy(mostrarModalNovaVersao = false) }
    }

    fun criarNovaVersao(aoSucesso: (novoDocumentoId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val res = documentoRepository.criarNovaVersao(documentoId, adminIdAtual)
            _uiState.update { it.copy(carregando = false, mostrarModalNovaVersao = false) }
            res.onSuccess { novoId ->
                aoSucesso(novoId)
            }
        }
    }

    fun definirFeedback(mensagem: String) {
        _uiState.update { it.copy(mensagemFeedback = mensagem) }
    }

    fun limparFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }
}

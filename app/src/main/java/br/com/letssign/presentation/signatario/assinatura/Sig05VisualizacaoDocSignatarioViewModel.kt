package br.com.letssign.presentation.signatario.assinatura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.entities.CampoAssinaturaEntity
import br.com.letssign.data.repository.DetalheDocumentoSignatario
import br.com.letssign.data.repository.DocumentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Sig05UiState(
    val carregando: Boolean = true,
    val detalhe: DetalheDocumentoSignatario? = null,
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val campoFocadoIndex: Int = 0,
    val campoAssinado: Boolean = false,
    val erro: String? = null
) {
    val totalCampos: Int get() = campos.size
    val camposRestantes: Int get() = if (campoAssinado) 0 else campos.size
    val todosCamposAssinados: Boolean get() = campoAssinado || campos.isEmpty()
}

@HiltViewModel
class Sig05VisualizacaoDocSignatarioViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var documentoId: String = savedStateHandle.get<String>("documentoId")
        ?.takeIf { it.isNotBlank() }
        ?: "doc-demo-4"
        private set

    private val _uiState = MutableStateFlow(Sig05UiState())
    val uiState: StateFlow<Sig05UiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null

    init {
        carregarDocumentoECampos(documentoId)
    }

    fun carregarDocumentoECampos(idDoc: String = documentoId) {
        val targetId = if (idDoc.isNotBlank()) idDoc else "doc-demo-4"
        documentoId = targetId

        carregarJob?.cancel()
        carregarJob = viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            try {
                val sessao = sessionManager.sessaoFlow.firstOrNull()
                val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

                documentoRepository.carregarDetalheParaSignatario(targetId, usuarioId)
                    .collect { detalhe ->
                        if (detalhe != null) {
                            documentoRepository.carregarCamposDoParticipante(detalhe.participanteId)
                                .collect { campos ->
                                    _uiState.update {
                                        it.copy(
                                            carregando = false,
                                            detalhe = detalhe,
                                            campos = campos
                                        )
                                    }
                                }
                        } else {
                            _uiState.update { it.copy(carregando = false) }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregando = false,
                        erro = e.localizedMessage ?: "Erro ao carregar documento para assinatura."
                    )
                }
            }
        }
    }

    fun focarProximoCampo() {
        val total = _uiState.value.campos.size
        if (total > 0) {
            _uiState.update {
                it.copy(campoFocadoIndex = (it.campoFocadoIndex + 1) % total)
            }
        }
    }

    fun marcarCampoComoAssinado() {
        _uiState.update { it.copy(campoAssinado = true) }
    }
}

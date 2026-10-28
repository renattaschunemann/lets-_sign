package br.com.letssign.presentation.signatario.detalhes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DetalheDocumentoSignatario
import br.com.letssign.data.repository.DocumentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Sig03UiState(
    val carregando: Boolean = true,
    val detalhe: DetalheDocumentoSignatario? = null,
    val erro: String? = null,
    val exibindoDialogRecusa: Boolean = false,
    val processandoRecusa: Boolean = false,
    val recusaSucesso: Boolean = false,
    val erroRecusa: String? = null
)

@HiltViewModel
class Sig03DetalheDocumentoViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""

    private val _uiState = MutableStateFlow(Sig03UiState())
    val uiState: StateFlow<Sig03UiState> = _uiState.asStateFlow()

    init {
        carregarDetalhes()
    }

    fun carregarDetalhes() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            val sessao = sessionManager.sessaoFlow.firstOrNull()
            val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

            documentoRepository.carregarDetalheParaSignatario(documentoId, usuarioId)
                .catch { ex ->
                    _uiState.update { it.copy(carregando = false, erro = ex.message) }
                }
                .collect { detalhe ->
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            detalhe = detalhe
                        )
                    }
                }
        }
    }

    fun exibirDialogRecusa(exibir: Boolean) {
        _uiState.update { it.copy(exibindoDialogRecusa = exibir, erroRecusa = null) }
    }

    fun confirmarRecusa(motivo: String, senha: String) {
        val detalhe = _uiState.value.detalhe ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(processandoRecusa = true, erroRecusa = null) }
            val resultado = documentoRepository.recusarAssinatura(
                documentoId = detalhe.documentoId,
                versaoId = detalhe.versaoId,
                participanteId = detalhe.participanteId,
                motivo = motivo,
                senhaDigitada = senha
            )
            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            processandoRecusa = false,
                            exibindoDialogRecusa = false,
                            recusaSucesso = true
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            processandoRecusa = false,
                            erroRecusa = ex.message ?: "Erro ao recusar documento."
                        )
                    }
                }
            )
        }
    }
}

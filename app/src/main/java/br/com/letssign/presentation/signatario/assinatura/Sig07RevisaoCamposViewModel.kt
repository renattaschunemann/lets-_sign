package br.com.letssign.presentation.signatario.assinatura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DetalheDocumentoSignatario
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.domain.model.TipoAssinatura
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Sig07UiState(
    val carregando: Boolean = true,
    val detalhe: DetalheDocumentoSignatario? = null,
    val exibindoTermosAceite: Boolean = false,
    val emitindoAssinatura: Boolean = false,
    val conclusaoComSucesso: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class Sig07RevisaoCamposViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""

    private val _uiState = MutableStateFlow(Sig07UiState())
    val uiState: StateFlow<Sig07UiState> = _uiState.asStateFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            val sessao = sessionManager.sessaoFlow.firstOrNull()
            val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

            documentoRepository.carregarDetalheParaSignatario(documentoId, usuarioId)
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

    fun exibirTermosAceite(exibir: Boolean) {
        _uiState.update { it.copy(exibindoTermosAceite = exibir) }
    }

    fun emitirAssinatura() {
        val detalhe = _uiState.value.detalhe ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(emitindoAssinatura = true, erro = null) }
            val resultado = documentoRepository.salvarAssinaturaSignatario(
                versaoId = detalhe.versaoId,
                participanteId = detalhe.participanteId,
                tipoAssinatura = TipoAssinatura.DESENHADA,
                caminhoPngOuSvg = "assinaturas/${detalhe.participanteId}.png",
                ipDispositivo = "192.168.1.100"
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            emitindoAssinatura = false,
                            exibindoTermosAceite = false,
                            conclusaoComSucesso = true
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            emitindoAssinatura = false,
                            erro = ex.message ?: "Falha ao emitir assinatura."
                        )
                    }
                }
            )
        }
    }
}

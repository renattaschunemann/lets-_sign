package br.com.letssign.presentation.signatario.inicio

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

enum class AbaInicioSignatario {
    PENDENTES,
    AGUARDANDO_VEZ
}

data class Sig01UiState(
    val carregando: Boolean = true,
    val abaSelecionada: AbaInicioSignatario = AbaInicioSignatario.PENDENTES,
    val documentosPendentes: List<DocumentoSignatarioItem> = emptyList(),
    val documentosAguardandoVez: List<DocumentoSignatarioItem> = emptyList(),
    val erro: String? = null
) {
    val totalPendentes: Int get() = documentosPendentes.size
    val totalAguardandoVez: Int get() = documentosAguardandoVez.size
}

@HiltViewModel
class Sig01InicioPendentesViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(Sig01UiState())
    val uiState: StateFlow<Sig01UiState> = _uiState.asStateFlow()

    init {
        carregarDocumentos()
    }

    fun selecionarAba(aba: AbaInicioSignatario) {
        _uiState.update { it.copy(abaSelecionada = aba) }
    }

    fun carregarDocumentos() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            val sessao = sessionManager.sessaoFlow.firstOrNull()
            val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

            // Inicializa dados de demonstração caso necessário
            documentoRepository.inicializarDadosDemoSeNecessario("admin-carla-mendes")

            documentoRepository.listarDocumentosDoSignatario(usuarioId)
                .catch { ex ->
                    _uiState.update { it.copy(carregando = false, erro = ex.message) }
                }
                .collect { lista ->
                    val pendentes = lista.filter {
                        it.situacaoParticipante == SituacaoParticipante.PENDENTE &&
                                it.situacaoDocumento in listOf(
                            SituacaoDocumento.ENVIADO,
                            SituacaoDocumento.PARCIALMENTE_ASSINADO
                        )
                    }
                    val aguardando = lista.filter {
                        it.situacaoParticipante == SituacaoParticipante.AGUARDANDO_VEZ &&
                                it.situacaoDocumento in listOf(
                            SituacaoDocumento.ENVIADO,
                            SituacaoDocumento.PARCIALMENTE_ASSINADO
                        )
                    }
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            documentosPendentes = pendentes,
                            documentosAguardandoVez = aguardando
                        )
                    }
                }
        }
    }
}

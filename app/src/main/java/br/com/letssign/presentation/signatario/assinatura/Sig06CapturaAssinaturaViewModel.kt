package br.com.letssign.presentation.signatario.assinatura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import br.com.letssign.core.designsystem.components.reusable.EstiloFonteCursiva
import br.com.letssign.domain.model.TipoAssinatura
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class AbaCapturaAssinatura {
    DESENHAR,
    DIGITAR
}

data class Sig06UiState(
    val abaSelecionada: AbaCapturaAssinatura = AbaCapturaAssinatura.DESENHAR,
    val temDesenho: Boolean = false,
    val textoDigitado: String = "João Lima",
    val estiloFonteSelecionado: EstiloFonteCursiva = EstiloFonteCursiva.ESTILO_1,
    val assinaturaPronta: Boolean = false
) {
    val podeConfirmar: Boolean
        get() = if (abaSelecionada == AbaCapturaAssinatura.DESENHAR) {
            temDesenho
        } else {
            textoDigitado.isNotBlank()
        }

    val tipoAssinatura: TipoAssinatura
        get() = if (abaSelecionada == AbaCapturaAssinatura.DESENHAR) TipoAssinatura.DESENHADA else TipoAssinatura.DIGITADA
}

@HiltViewModel
class Sig06CapturaAssinaturaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""

    private val _uiState = MutableStateFlow(Sig06UiState())
    val uiState: StateFlow<Sig06UiState> = _uiState.asStateFlow()

    fun selecionarAba(aba: AbaCapturaAssinatura) {
        _uiState.update { it.copy(abaSelecionada = aba) }
    }

    fun atualizarTemDesenho(tem: Boolean) {
        _uiState.update { it.copy(temDesenho = tem) }
    }

    fun atualizarTextoDigitado(texto: String) {
        _uiState.update { it.copy(textoDigitado = texto) }
    }

    fun selecionarEstiloFonte(estilo: EstiloFonteCursiva) {
        _uiState.update { it.copy(estiloFonteSelecionado = estilo) }
    }

    fun limpar() {
        _uiState.update {
            it.copy(
                temDesenho = false,
                textoDigitado = ""
            )
        }
    }

    fun confirmarAssinatura() {
        if (_uiState.value.podeConfirmar) {
            _uiState.update { it.copy(assinaturaPronta = true) }
        }
    }
}

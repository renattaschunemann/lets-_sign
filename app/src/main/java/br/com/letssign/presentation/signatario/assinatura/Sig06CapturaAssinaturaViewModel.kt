package br.com.letssign.presentation.signatario.assinatura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.core.designsystem.components.reusable.EstiloFonteCursiva
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.domain.model.TipoAssinatura
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AbaCapturaAssinatura {
    DESENHAR,
    DIGITAR,
    UPLOAD_FOTO,
    CERTIFICADO_DIGITAL
}

data class Sig06UiState(
    val abaSelecionada: AbaCapturaAssinatura = AbaCapturaAssinatura.DESENHAR,
    val temDesenho: Boolean = false,
    val textoDigitado: String = "João Lima",
    val estiloFonteSelecionado: EstiloFonteCursiva = EstiloFonteCursiva.ESTILO_1,
    // Certificado Digital (ICP-Brasil)
    val tipoCertificado: String = "e-CPF", // "e-CPF" ou "e-CNPJ"
    val subtipoCertificado: String = "A1 (Nuvem / Arquivo)",
    val titularCertificado: String = "JOAO CARLOS LIMA",
    val documentoCertificado: String = "123.456.789-00",
    val autoridadeCertificadora: String = "AC SERPRO v5 (ICP-Brasil)",
    val validadeCertificado: String = "18/12/2027",
    val pinCertificado: String = "",
    val certificadoInstalado: Boolean = true,
    val uriImagemUpload: android.net.Uri? = null,
    val assinaturaPronta: Boolean = false
) {
    val podeConfirmar: Boolean
        get() = when (abaSelecionada) {
            AbaCapturaAssinatura.DESENHAR -> temDesenho
            AbaCapturaAssinatura.DIGITAR -> textoDigitado.isNotBlank()
            AbaCapturaAssinatura.UPLOAD_FOTO -> uriImagemUpload != null
            AbaCapturaAssinatura.CERTIFICADO_DIGITAL -> pinCertificado.length >= 4 && titularCertificado.isNotBlank()
        }

    val tipoAssinatura: TipoAssinatura
        get() = when (abaSelecionada) {
            AbaCapturaAssinatura.DESENHAR -> TipoAssinatura.DESENHADA
            AbaCapturaAssinatura.DIGITAR -> TipoAssinatura.DIGITADA
            AbaCapturaAssinatura.UPLOAD_FOTO -> TipoAssinatura.DESENHADA
            AbaCapturaAssinatura.CERTIFICADO_DIGITAL -> {
                if (tipoCertificado == "e-CNPJ") TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ
                else TipoAssinatura.CERTIFICADO_DIGITAL_ECPF
            }
        }

    val detalhesCertificado: String
        get() = if (abaSelecionada == AbaCapturaAssinatura.CERTIFICADO_DIGITAL) {
            "Certificado Digital $tipoCertificado $subtipoCertificado | Titular: $titularCertificado | Doc: $documentoCertificado | Emissor: $autoridadeCertificadora | Validade: $validadeCertificado | Padrão: ICP-Brasil (RSA 2048-bit / SHA-256)"
        } else ""
}

@HiltViewModel
class Sig06CapturaAssinaturaViewModel @Inject constructor(
    private val sessionManager: SessionManager? = null,
    private val usuarioDao: UsuarioDao? = null,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Construtor de conveniência para instâncias ou testes que passam apenas savedStateHandle
    constructor(savedStateHandle: SavedStateHandle) : this(null, null, savedStateHandle)

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""
    private val abaInicial: String = savedStateHandle.get<String>("abaInicial") ?: "DESENHAR"

    private val _uiState = MutableStateFlow(
        Sig06UiState(
            abaSelecionada = if (abaInicial.equals("CERTIFICADO", ignoreCase = true)) {
                AbaCapturaAssinatura.CERTIFICADO_DIGITAL
            } else {
                AbaCapturaAssinatura.DESENHAR
            }
        )
    )
    val uiState: StateFlow<Sig06UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val sessao = sessionManager?.sessaoFlow?.firstOrNull()
                val usuarioId = sessao?.usuarioId
                val usuario = if (!usuarioId.isNullOrBlank()) usuarioDao?.buscarPorId(usuarioId) else null
                val usuarioPorEmail = if (usuario == null && !sessao?.email.isNullOrBlank()) usuarioDao?.buscarPorEmail(sessao!!.email) else null
                val userFinal = usuario ?: usuarioPorEmail

                val nomeTitular = userFinal?.nome ?: sessao?.nome ?: "João Lima"
                val rawDoc = userFinal?.cpf?.ifBlank { userFinal.cpfCnpj } ?: ""
                val docFormatado = if (rawDoc.isNotBlank()) {
                    val dig = CadastroValidator.apenasNumeros(rawDoc)
                    if (dig.length > 11) CadastroValidator.formatarCnpj(dig) else CadastroValidator.formatarCpf(dig)
                } else "123.456.789-00"

                _uiState.update {
                    it.copy(
                        textoDigitado = nomeTitular,
                        titularCertificado = nomeTitular.uppercase(),
                        documentoCertificado = docFormatado
                    )
                }
            } catch (_: Exception) {}
        }
    }

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

    fun atualizarTipoCertificado(tipo: String) {
        val nomeAtual = _uiState.value.textoDigitado.ifBlank { "João Lima" }
        val cpfAtual = _uiState.value.documentoCertificado.ifBlank { "123.456.789-00" }
        _uiState.update {
            if (tipo == "e-CNPJ") {
                it.copy(
                    tipoCertificado = "e-CNPJ",
                    titularCertificado = "LETS SIGN SOLUCOES DIGITAIS LTDA",
                    documentoCertificado = "12.345.678/0001-90",
                    autoridadeCertificadora = "AC Certisign Multipla v5 (ICP-Brasil)"
                )
            } else {
                it.copy(
                    tipoCertificado = "e-CPF",
                    titularCertificado = nomeAtual.uppercase(),
                    documentoCertificado = cpfAtual,
                    autoridadeCertificadora = "AC SERPRO v5 (ICP-Brasil)"
                )
            }
        }
    }

    fun atualizarSubtipoCertificado(subtipo: String) {
        _uiState.update { it.copy(subtipoCertificado = subtipo) }
    }

    fun atualizarPinCertificado(pin: String) {
        _uiState.update { it.copy(pinCertificado = pin.filter { c -> c.isDigit() }.take(8)) }
    }

        fun definirImagemUpload(uri: android.net.Uri?) {
        _uiState.update { it.copy(uriImagemUpload = uri) }
    }

    fun removerImagemUpload() {
        _uiState.update { it.copy(uriImagemUpload = null) }
    }

    fun limpar() {
        _uiState.update {
            it.copy(
                temDesenho = false,
                textoDigitado = "",
                pinCertificado = "",
                uriImagemUpload = null
            )
        }
    }

    fun confirmarAssinatura() {
        if (_uiState.value.podeConfirmar) {
            _uiState.update { it.copy(assinaturaPronta = true) }
        }
    }
}

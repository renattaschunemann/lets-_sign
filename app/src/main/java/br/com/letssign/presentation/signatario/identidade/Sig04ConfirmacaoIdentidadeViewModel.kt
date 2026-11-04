package br.com.letssign.presentation.signatario.identidade

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DocumentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Sig04UiState(
    val carregando: Boolean = true,
    val participanteId: String = "",
    val usuarioId: String = "",
    val nome: String = "",
    val dataNascimento: String = "",
    val senha: String = "",
    val validando: Boolean = false,
    val sucesso: Boolean = false,
    val erro: String? = null,
    val bloqueado: Boolean = false
)

@HiltViewModel
class Sig04ConfirmacaoIdentidadeViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""

    private val _uiState = MutableStateFlow(Sig04UiState())
    val uiState: StateFlow<Sig04UiState> = _uiState.asStateFlow()

    init {
        carregarDadosIniciais()
    }

    private fun carregarDadosIniciais() {
        viewModelScope.launch {
            val sessao = sessionManager.sessaoFlow.firstOrNull()
            val usuarioId = sessao?.usuarioId ?: "sig-joao-lima"

            documentoRepository.carregarDetalheParaSignatario(documentoId, usuarioId)
                .collect { detalhe ->
                    if (detalhe != null) {
                        _uiState.update {
                            it.copy(
                                carregando = false,
                                participanteId = detalhe.participanteId,
                                usuarioId = usuarioId
                            )
                        }
                    } else {
                        _uiState.update { it.copy(carregando = false) }
                    }
                }
        }
    }

    fun atualizarNome(novoNome: String) {
        _uiState.update { it.copy(nome = novoNome, erro = null) }
    }

    fun atualizarDataNascimento(novaData: String) {
        val digitos = CadastroValidator.apenasNumeros(novaData).take(8)
        _uiState.update { it.copy(dataNascimento = digitos, erro = null) }
    }

    fun atualizarSenha(novaSenha: String) {
        _uiState.update { it.copy(senha = novaSenha, erro = null) }
    }

    fun confirmarIdentidade() {
        val state = _uiState.value
        if (state.nome.isBlank()) {
            _uiState.update { it.copy(erro = "Informe seu nome completo.") }
            return
        }
        val digitosNascimento = CadastroValidator.apenasNumeros(state.dataNascimento)
        if (digitosNascimento.isBlank()) {
            _uiState.update { it.copy(erro = "Informe sua data de nascimento.") }
            return
        }
        if (digitosNascimento.length < 8) {
            _uiState.update { it.copy(erro = "Informe a data de nascimento completa no formato 00/00/0000 com ano de 4 dígitos.") }
            return
        }
        if (!CadastroValidator.validarDataNascimento(digitosNascimento)) {
            _uiState.update { it.copy(erro = "Data de nascimento inválida. Digite no formato 00/00/0000.") }
            return
        }
        if (state.senha.isBlank()) {
            _uiState.update { it.copy(erro = "Informe sua senha de acesso.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(validando = true, erro = null) }
            val resultado = documentoRepository.validarIdentidadeSignatario(
                participanteId = state.participanteId,
                usuarioId = state.usuarioId,
                nomeDigitado = state.nome,
                dataNascimentoDigitada = digitosNascimento,
                senhaDigitada = state.senha
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(validando = false, sucesso = true) }
                },
                onFailure = { ex ->
                    val msg = ex.message ?: "Erro ao confirmar identidade."
                    val isBloqueio = msg.contains("15 minutos") || msg.contains("segurança")
                    _uiState.update {
                        it.copy(
                            validando = false,
                            erro = msg,
                            bloqueado = isBloqueio
                        )
                    }
                }
            )
        }
    }
}

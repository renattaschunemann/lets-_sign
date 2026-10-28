package br.com.letssign.presentation.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.repository.LinkResolutionResult
import br.com.letssign.data.repository.LinkResolverRepository
import br.com.letssign.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EntradaLinkUiState {
    data object Carregando : EntradaLinkUiState
    data class ValidoRedirecionando(val documentoId: String, val participanteId: String) : EntradaLinkUiState
    data object BloqueioOutraConta : EntradaLinkUiState
    data object DocumentoCancelado : EntradaLinkUiState
    data class JaAssinado(val documentoId: String) : EntradaLinkUiState
    data object LinkInvalido : EntradaLinkUiState
    data class ErroConexao(val mensagem: String) : EntradaLinkUiState
}

@HiltViewModel
class EntradaLinkViewModel @Inject constructor(
    private val linkResolverRepository: LinkResolverRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EntradaLinkUiState>(EntradaLinkUiState.Carregando)
    val uiState: StateFlow<EntradaLinkUiState> = _uiState.asStateFlow()

    fun processarToken(token: String) {
        viewModelScope.launch {
            _uiState.value = EntradaLinkUiState.Carregando
            val sessao = usuarioRepository.sessaoAtiva.first()
            val emailLogado = sessao?.email

            val resultado = linkResolverRepository.resolverLink(token, emailLogado)
            _uiState.value = when (resultado) {
                is LinkResolutionResult.ValidoParaAssinatura ->
                    EntradaLinkUiState.ValidoRedirecionando(resultado.documentoId, resultado.participanteId)
                is LinkResolutionResult.BloqueioOutraConta ->
                    EntradaLinkUiState.BloqueioOutraConta
                is LinkResolutionResult.DocumentoCancelado ->
                    EntradaLinkUiState.DocumentoCancelado
                is LinkResolutionResult.JaAssinado ->
                    EntradaLinkUiState.JaAssinado(resultado.documentoId)
                is LinkResolutionResult.LinkInvalidoOuExpirado ->
                    EntradaLinkUiState.LinkInvalido
                is LinkResolutionResult.ErroConexao ->
                    EntradaLinkUiState.ErroConexao(resultado.mensagem)
            }
        }
    }

    fun deslogarESair(onConcluido: () -> Unit) {
        viewModelScope.launch {
            usuarioRepository.encerrarSessao()
            onConcluido()
        }
    }
}

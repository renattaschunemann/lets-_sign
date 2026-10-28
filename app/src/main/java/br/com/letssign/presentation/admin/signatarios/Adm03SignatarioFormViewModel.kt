package br.com.letssign.presentation.admin.signatarios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.SignatarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Adm03UiState(
    val ehEdicao: Boolean = false,
    val signatarioId: String? = null,
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val dataNascimento: String = "",
    val senha: String = "",
    val emailJaExiste: Boolean = false,
    val salvando: Boolean = false,
    val salvoComSucesso: Boolean = false,
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroTelefone: String? = null,
    val erroDataNascimento: String? = null,
    val erroGeral: String? = null
)

@HiltViewModel
class Adm03SignatarioFormViewModel @Inject constructor(
    private val signatarioRepository: SignatarioRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val signatarioIdArg: String? = savedStateHandle["signatarioId"]

    private val _uiState = MutableStateFlow(Adm03UiState(ehEdicao = signatarioIdArg != null, signatarioId = signatarioIdArg))
    val uiState: StateFlow<Adm03UiState> = _uiState.asStateFlow()

    init {
        if (signatarioIdArg != null) {
            carregarSignatario(signatarioIdArg)
        }
    }

    private fun carregarSignatario(id: String) {
        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            val signatario = signatarioRepository.buscarSignatario(adminId, id)
            if (signatario != null) {
                _uiState.update {
                    it.copy(
                        nome = signatario.nome,
                        email = signatario.email,
                        telefone = signatario.telefone,
                        dataNascimento = signatario.dataNascimento
                    )
                }
            }
        }
    }

    fun aoAlterarNome(novoNome: String) {
        _uiState.update { it.copy(nome = novoNome, erroNome = null) }
    }

    fun aoAlterarEmail(novoEmail: String) {
        _uiState.update { it.copy(email = novoEmail, erroEmail = null) }
        verificarEmailExistente(novoEmail)
    }

    private fun verificarEmailExistente(email: String) {
        viewModelScope.launch {
            val existe = signatarioRepository.verificarEmailPossuiConta(email)
            _uiState.update { it.copy(emailJaExiste = existe) }
        }
    }

    fun aoAlterarTelefone(novoTelefone: String) {
        _uiState.update { it.copy(telefone = novoTelefone, erroTelefone = null) }
    }

    fun aoAlterarDataNascimento(novaData: String) {
        _uiState.update { it.copy(dataNascimento = novaData, erroDataNascimento = null) }
    }

    fun aoAlterarSenha(novaSenha: String) {
        _uiState.update { it.copy(senha = novaSenha) }
    }

    fun aoSalvar() {
        val estado = _uiState.value

        // Validações locais antes de submeter
        var temErro = false
        if (estado.nome.trim().isBlank() || !estado.nome.trim().contains(" ")) {
            _uiState.update { it.copy(erroNome = "Informe o nome completo.") }
            temErro = true
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(estado.email.trim())) {
            _uiState.update { it.copy(erroEmail = "Informe um e-mail válido.") }
            temErro = true
        }

        if (estado.dataNascimento.trim().length != 10) {
            _uiState.update { it.copy(erroDataNascimento = "Data de nascimento inválida.") }
            temErro = true
        }

        if (temErro) return

        _uiState.update { it.copy(salvando = true, erroGeral = null) }

        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            val resultado = signatarioRepository.salvarSignatario(
                adminId = adminId,
                signatarioIdExistente = estado.signatarioId,
                nome = estado.nome,
                email = estado.email,
                telefone = estado.telefone,
                dataNascimento = estado.dataNascimento,
                senha = estado.senha.ifBlank { null }
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(salvando = false, salvoComSucesso = true) }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            salvando = false,
                            erroGeral = erro.message ?: "Erro ao salvar signatário."
                        )
                    }
                }
            )
        }
    }
}

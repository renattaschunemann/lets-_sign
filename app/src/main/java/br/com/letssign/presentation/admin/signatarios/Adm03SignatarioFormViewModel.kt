package br.com.letssign.presentation.admin.signatarios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.common.cep.CepService
import br.com.letssign.core.common.validation.CadastroValidator
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
    val tipoPessoa: String = "PF", // "PF" ou "PJ"
    val cpfCnpj: String = "",
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val dataNascimento: String = "",
    val cep: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val buscandoCep: Boolean = false,
    val senha: String = "",
    val emailJaExiste: Boolean = false,
    val salvando: Boolean = false,
    val salvoComSucesso: Boolean = false,
    val erroCpfCnpj: String? = null,
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroTelefone: String? = null,
    val erroDataNascimento: String? = null,
    val erroCep: String? = null,
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
                        tipoPessoa = signatario.tipoPessoa,
                        cpfCnpj = signatario.cpfCnpj,
                        nome = signatario.nome,
                        email = signatario.email,
                        telefone = signatario.telefone,
                        dataNascimento = signatario.dataNascimento,
                        cep = signatario.cep,
                        logradouro = signatario.logradouro,
                        numero = signatario.numero,
                        complemento = signatario.complemento,
                        bairro = signatario.bairro,
                        cidade = signatario.cidade,
                        estado = signatario.estado
                    )
                }
            }
        }
    }

    fun aoAlterarTipoPessoa(novoTipo: String) {
        _uiState.update { it.copy(tipoPessoa = novoTipo, erroCpfCnpj = null) }
    }

    fun aoAlterarCpfCnpj(novoValor: String) {
        val maxLen = if (_uiState.value.tipoPessoa == "PJ") 14 else 11
        val digitos = novoValor.filter { it.isDigit() }.take(maxLen)
        _uiState.update { it.copy(cpfCnpj = digitos, erroCpfCnpj = null) }
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

    fun preencherDadosImportados(nome: String, email: String, telefone: String) {
        if (nome.isNotBlank()) aoAlterarNome(nome)
        if (email.isNotBlank()) aoAlterarEmail(email)
        if (telefone.isNotBlank()) aoAlterarTelefone(telefone)
    }

    fun aoAlterarTelefone(novoTelefone: String) {
        val digitos = novoTelefone.filter { it.isDigit() }.take(11)
        _uiState.update { it.copy(telefone = digitos, erroTelefone = null) }
    }

    fun aoAlterarDataNascimento(novaData: String) {
        val digitos = novaData.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(dataNascimento = digitos, erroDataNascimento = null) }
    }

    fun aoAlterarCep(novoCep: String) {
        val digitos = novoCep.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(cep = digitos, erroCep = null) }
        if (digitos.length == 8) {
            buscarCep()
        }
    }

    fun buscarCep() {
        val cepAtual = _uiState.value.cep.filter { it.isDigit() }
        if (cepAtual.length != 8) {
            _uiState.update { it.copy(erroCep = "CEP deve conter 8 dígitos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(buscandoCep = true, erroCep = null) }
            val resultado = CepService.buscarCep(cepAtual)
            resultado.fold(
                onSuccess = { end ->
                    _uiState.update {
                        it.copy(
                            buscandoCep = false,
                            logradouro = end.logradouro.ifBlank { it.logradouro },
                            complemento = end.complemento.ifBlank { it.complemento },
                            bairro = end.bairro.ifBlank { it.bairro },
                            cidade = end.cidade.ifBlank { it.cidade },
                            estado = end.estado.ifBlank { it.estado }
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            buscandoCep = false,
                            erroCep = err.message ?: "Erro ao buscar CEP."
                        )
                    }
                }
            )
        }
    }

    fun aoAlterarLogradouro(novo: String) {
        _uiState.update { it.copy(logradouro = novo) }
    }

    fun aoAlterarNumero(novo: String) {
        _uiState.update { it.copy(numero = novo) }
    }

    fun aoAlterarComplemento(novo: String) {
        _uiState.update { it.copy(complemento = novo) }
    }

    fun aoAlterarBairro(novo: String) {
        _uiState.update { it.copy(bairro = novo) }
    }

    fun aoAlterarCidade(novo: String) {
        _uiState.update { it.copy(cidade = novo) }
    }

    fun aoAlterarEstado(novo: String) {
        _uiState.update { it.copy(estado = novo.take(2).uppercase()) }
    }

    fun aoAlterarSenha(novaSenha: String) {
        _uiState.update { it.copy(senha = novaSenha) }
    }

    fun aoSalvar() {
        val estado = _uiState.value

        var temErro = false
        if (estado.tipoPessoa == "PF") {
            if (estado.cpfCnpj.length != 11) {
                _uiState.update { it.copy(erroCpfCnpj = "CPF deve conter 11 dígitos.") }
                temErro = true
            }
        } else {
            if (estado.cpfCnpj.length != 14) {
                _uiState.update { it.copy(erroCpfCnpj = "CNPJ deve conter 14 dígitos.") }
                temErro = true
            }
        }

        if (estado.nome.trim().isBlank() || !estado.nome.trim().contains(" ")) {
            _uiState.update { it.copy(erroNome = "Informe o nome completo ou razão social.") }
            temErro = true
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(estado.email.trim())) {
            _uiState.update { it.copy(erroEmail = "Informe um e-mail válido.") }
            temErro = true
        }

        val dataFormatada = if (estado.dataNascimento.length == 8) {
            "${estado.dataNascimento.substring(0, 2)}/${estado.dataNascimento.substring(2, 4)}/${estado.dataNascimento.substring(4)}"
        } else {
            estado.dataNascimento
        }

        if (dataFormatada.length != 10) {
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
                dataNascimento = dataFormatada,
                senha = estado.senha.ifBlank { null },
                tipoPessoa = estado.tipoPessoa,
                cpfCnpj = estado.cpfCnpj,
                cep = estado.cep,
                logradouro = estado.logradouro,
                numero = estado.numero,
                complemento = estado.complemento,
                bairro = estado.bairro,
                cidade = estado.cidade,
                estado = estado.estado
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

package br.com.letssign.presentation.admin.wizard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.core.pdf.PdfValidationResult
import br.com.letssign.core.pdf.PdfValidator
import br.com.letssign.core.pdf.TipoErroPdf
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.data.local.entities.ParticipanteDocumentoEntity
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.SignatarioRepository
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.domain.model.SituacaoParticipante
import br.com.letssign.domain.model.TipoOrdem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WizardNovoDocUiState(
    val documentoId: String = UUID.randomUUID().toString(),
    val titulo: String = "",
    val pdfPath: String? = null,
    val pdfNome: String? = null,
    val pdfTamanhoBytes: Long = 0,
    val pdfTotalPaginas: Int = 0,
    val signatariosCadastrados: List<AdminSignatarioEntity> = emptyList(),
    val signatariosSelecionadosIds: Set<String> = emptySet(),
    val buscaSignatarios: String = "",
    val papelPorSignatario: Map<String, PapelParticipante> = emptyMap(),
    val obrigatorioPorSignatario: Map<String, Boolean> = emptyMap(),
    val ordemSignatarios: List<String> = emptyList(),
    val tipoOrdem: TipoOrdem = TipoOrdem.PARALELA,
    val etapaAtual: Int = 1,
    val mostrarModalSair: Boolean = false,
    val erroPdf: TipoErroPdf? = null,
    val mostrarModalNovoSignatario: Boolean = false,
    val mostrarModalRetomarRascunho: Boolean = false,
    val rascunhoPendenteId: String? = null,
    val carregando: Boolean = false,
    val rascunhoSalvo: Boolean = false,
    val rascunhoDescartado: Boolean = false
) {
    val signatariosFiltrados: List<AdminSignatarioEntity>
        get() = if (buscaSignatarios.isBlank()) {
            signatariosCadastrados
        } else {
            val termo = buscaSignatarios.trim().lowercase()
            signatariosCadastrados.filter {
                it.nome.lowercase().contains(termo) || it.email.lowercase().contains(termo)
            }
        }

    val participantesOrdenados: List<AdminSignatarioEntity>
        get() = ordemSignatarios.mapNotNull { id ->
            signatariosCadastrados.find { it.signatarioId == id }
        }

    val podeAvancarEtapa1: Boolean
        get() = titulo.isNotBlank()

    val podeAvancarEtapa2: Boolean
        get() = pdfPath != null

    val podeAvancarEtapa3: Boolean
        get() = signatariosSelecionadosIds.isNotEmpty()

    val podeAvancarEtapa4: Boolean
        get() = signatariosSelecionadosIds.any { id -> obrigatorioPorSignatario[id] != false }

    val podeAvancarEtapa5: Boolean
        get() = true
}

@HiltViewModel
class WizardNovoDocViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val signatarioRepository: SignatarioRepository,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardNovoDocUiState())
    val uiState: StateFlow<WizardNovoDocUiState> = _uiState.asStateFlow()

    private var adminIdAtual: String = ""

    init {
        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"
            adminIdAtual = adminId
            carregarSignatariosCadastrados()

            val rascunhoIdParam = savedStateHandle.get<String>("rascunhoId")
            if (!rascunhoIdParam.isNullOrBlank()) {
                carregarRascunhoExistente(rascunhoIdParam)
            } else {
                verificarRascunhoPendente(adminId)
            }
        }
    }

    private suspend fun carregarSignatariosCadastrados() {
        val signatarios = signatarioRepository.listarSignatarios(adminIdAtual).first()
        _uiState.update { it.copy(signatariosCadastrados = signatarios) }
    }

    private suspend fun verificarRascunhoPendente(adminId: String) {
        val rascunho = documentoRepository.buscarUltimoRascunho(adminId)
        if (rascunho != null) {
            _uiState.update {
                it.copy(
                    mostrarModalRetomarRascunho = true,
                    rascunhoPendenteId = rascunho.id
                )
            }
        }
    }

    fun retomarRascunhoPendente() {
        val rascunhoId = _uiState.value.rascunhoPendenteId ?: return
        _uiState.update { it.copy(mostrarModalRetomarRascunho = false) }
        viewModelScope.launch {
            carregarRascunhoExistente(rascunhoId)
        }
    }

    fun descartarRascunhoPendenteEIniciarNovo() {
        val rascunhoId = _uiState.value.rascunhoPendenteId
        _uiState.update {
            it.copy(
                mostrarModalRetomarRascunho = false,
                rascunhoPendenteId = null,
                documentoId = UUID.randomUUID().toString(),
                titulo = "",
                pdfPath = null,
                etapaAtual = 1
            )
        }
        if (rascunhoId != null) {
            viewModelScope.launch {
                documentoRepository.excluirRascunho(rascunhoId)
            }
        }
    }

    private suspend fun carregarRascunhoExistente(docId: String) {
        val rascunho = documentoRepository.carregarRascunho(docId) ?: return
        val selecionadosIds = rascunho.participantes.mapNotNull { it.usuarioId }.toSet()
        val papeis = rascunho.participantes.associate { (it.usuarioId ?: it.id) to it.papel }
        val obrigatorios = rascunho.participantes.associate { (it.usuarioId ?: it.id) to it.obrigatorio }
        val ordem = rascunho.participantes.sortedBy { it.ordem }.mapNotNull { it.usuarioId }

        _uiState.update {
            it.copy(
                documentoId = docId,
                titulo = rascunho.documento.titulo,
                pdfPath = rascunho.versao.pdfPathInterno.ifBlank { null },
                pdfNome = if (rascunho.versao.pdfPathInterno.isNotBlank()) "documento.pdf" else null,
                signatariosSelecionadosIds = selecionadosIds,
                papelPorSignatario = papeis,
                obrigatorioPorSignatario = obrigatorios,
                ordemSignatarios = ordem.ifEmpty { selecionadosIds.toList() },
                tipoOrdem = rascunho.versao.tipoOrdem,
                etapaAtual = rascunho.versao.etapaWizardAtual.coerceIn(1, 6)
            )
        }
    }

    fun atualizarTitulo(novoTitulo: String) {
        _uiState.update { it.copy(titulo = novoTitulo) }
    }

    fun validarECarregarPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            when (val resultado = PdfValidator.validarECopiarPdf(context, uri)) {
                is PdfValidationResult.Sucesso -> {
                    _uiState.update {
                        it.copy(
                            pdfPath = resultado.pathInterno,
                            pdfNome = resultado.nomeArquivo,
                            pdfTamanhoBytes = resultado.tamanhoBytes,
                            pdfTotalPaginas = resultado.totalPaginas,
                            carregando = false,
                            erroPdf = null
                        )
                    }
                    salvarRascunhoAutomatico()
                }
                is PdfValidationResult.Erro -> {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            erroPdf = resultado.tipoErro
                        )
                    }
                }
            }
        }
    }

    fun fecharModalErroPdf() {
        _uiState.update { it.copy(erroPdf = null) }
    }

    fun atualizarBuscaSignatarios(termo: String) {
        _uiState.update { it.copy(buscaSignatarios = termo) }
    }

    fun alternarSelecaoSignatario(signatarioId: String) {
        _uiState.update { estado ->
            val novosSelecionados = estado.signatariosSelecionadosIds.toMutableSet()
            val novaOrdem = estado.ordemSignatarios.toMutableList()
            val novosPapeis = estado.papelPorSignatario.toMutableMap()
            val novasObrigatoriedades = estado.obrigatorioPorSignatario.toMutableMap()

            if (novosSelecionados.contains(signatarioId)) {
                novosSelecionados.remove(signatarioId)
                novaOrdem.remove(signatarioId)
                novosPapeis.remove(signatarioId)
                novasObrigatoriedades.remove(signatarioId)
            } else {
                novosSelecionados.add(signatarioId)
                novaOrdem.add(signatarioId)
                novosPapeis[signatarioId] = PapelParticipante.CONTRATADO
                novasObrigatoriedades[signatarioId] = true
            }

            estado.copy(
                signatariosSelecionadosIds = novosSelecionados,
                ordemSignatarios = novaOrdem,
                papelPorSignatario = novosPapeis,
                obrigatorioPorSignatario = novasObrigatoriedades
            )
        }
    }

    fun abrirModalNovoSignatario() {
        _uiState.update { it.copy(mostrarModalNovoSignatario = true) }
    }

    fun fecharModalNovoSignatario() {
        _uiState.update { it.copy(mostrarModalNovoSignatario = false) }
    }

    fun salvarNovoSignatarioRapido(nome: String, email: String, telefone: String, dataNascimento: String) {
        viewModelScope.launch {
            val res = signatarioRepository.salvarSignatario(
                adminId = adminIdAtual,
                signatarioIdExistente = null,
                nome = nome,
                email = email,
                telefone = telefone,
                dataNascimento = dataNascimento,
                senha = null
            )
            if (res.isSuccess) {
                carregarSignatariosCadastrados()
                // Acha o signatario cadastrado pelo email para selecionar
                val recemCriado = _uiState.value.signatariosCadastrados.find { it.email.equals(email, ignoreCase = true) }
                if (recemCriado != null) {
                    alternarSelecaoSignatario(recemCriado.signatarioId)
                }
                _uiState.update { it.copy(mostrarModalNovoSignatario = false) }
            }
        }
    }

    fun definirPapel(signatarioId: String, papel: PapelParticipante) {
        _uiState.update { estado ->
            val novosPapeis = estado.papelPorSignatario.toMutableMap()
            novosPapeis[signatarioId] = papel
            estado.copy(papelPorSignatario = novosPapeis)
        }
    }

    fun alternarObrigatoriedade(signatarioId: String, obrigatorio: Boolean) {
        _uiState.update { estado ->
            val novasObrigatoriedades = estado.obrigatorioPorSignatario.toMutableMap()
            novasObrigatoriedades[signatarioId] = obrigatorio
            estado.copy(obrigatorioPorSignatario = novasObrigatoriedades)
        }
    }

    fun definirTipoOrdem(tipo: TipoOrdem) {
        _uiState.update { it.copy(tipoOrdem = tipo) }
    }

    fun moverSignatarioParaCima(posicao: Int) {
        if (posicao <= 0) return
        _uiState.update { estado ->
            val lista = estado.ordemSignatarios.toMutableList()
            val item = lista.removeAt(posicao)
            lista.add(posicao - 1, item)
            estado.copy(ordemSignatarios = lista)
        }
    }

    fun moverSignatarioParaBaixo(posicao: Int) {
        val lista = _uiState.value.ordemSignatarios
        if (posicao >= lista.size - 1) return
        _uiState.update { estado ->
            val mutavel = estado.ordemSignatarios.toMutableList()
            val item = mutavel.removeAt(posicao)
            mutavel.add(posicao + 1, item)
            estado.copy(ordemSignatarios = mutavel)
        }
    }

    fun avancarEtapa(): Boolean {
        val estado = _uiState.value
        val podeAvancar = when (estado.etapaAtual) {
            1 -> estado.podeAvancarEtapa1
            2 -> estado.podeAvancarEtapa2
            3 -> estado.podeAvancarEtapa3
            4 -> estado.podeAvancarEtapa4
            5 -> estado.podeAvancarEtapa5
            else -> true
        }

        if (podeAvancar) {
            val proxima = (estado.etapaAtual + 1).coerceAtMost(6)
            _uiState.update { it.copy(etapaAtual = proxima) }
            salvarRascunhoAutomatico()
            return true
        }
        return false
    }

    fun voltarEtapa(aoSair: () -> Unit) {
        val etapa = _uiState.value.etapaAtual
        if (etapa > 1) {
            _uiState.update { it.copy(etapaAtual = etapa - 1) }
        } else {
            _uiState.update { it.copy(mostrarModalSair = true) }
        }
    }

    fun abrirModalSair() {
        _uiState.update { it.copy(mostrarModalSair = true) }
    }

    fun fecharModalSair() {
        _uiState.update { it.copy(mostrarModalSair = false) }
    }

    fun salvarRascunhoManual(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            salvarRascunhoNoBanco()
            _uiState.update { it.copy(mostrarModalSair = false, rascunhoSalvo = true) }
            aoConcluir()
        }
    }

    fun descartarRascunhoManual(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            documentoRepository.excluirRascunho(_uiState.value.documentoId)
            _uiState.update { it.copy(mostrarModalSair = false, rascunhoDescartado = true) }
            aoConcluir()
        }
    }

    private fun salvarRascunhoAutomatico() {
        viewModelScope.launch {
            salvarRascunhoNoBanco()
        }
    }

    private suspend fun salvarRascunhoNoBanco() {
        val estado = _uiState.value
        if (estado.titulo.isBlank()) return

        val participantes = estado.ordemSignatarios.mapIndexed { index, sigId ->
            val sig = estado.signatariosCadastrados.find { it.signatarioId == sigId }
            ParticipanteDocumentoEntity(
                id = "part-${estado.documentoId}-$sigId",
                versaoDocumentoId = "",
                usuarioId = sigId,
                nomeSnapshot = sig?.nome ?: "Signatário",
                emailSnapshot = sig?.email ?: "email@teste.com",
                telefoneSnapshot = sig?.telefone ?: "",
                dataNascimentoSnapshot = sig?.dataNascimento ?: "",
                papel = estado.papelPorSignatario[sigId] ?: PapelParticipante.CONTRATADO,
                obrigatorio = estado.obrigatorioPorSignatario[sigId] ?: true,
                ordem = index + 1,
                situacao = SituacaoParticipante.PENDENTE
            )
        }

        documentoRepository.salvarRascunho(
            adminId = adminIdAtual,
            documentoId = estado.documentoId,
            titulo = estado.titulo,
            pdfPathInterno = estado.pdfPath,
            tipoOrdem = estado.tipoOrdem,
            etapaWizard = estado.etapaAtual,
            participantes = participantes
        )
    }
}

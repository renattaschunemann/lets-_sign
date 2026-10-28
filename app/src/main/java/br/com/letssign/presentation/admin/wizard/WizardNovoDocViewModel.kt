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
import br.com.letssign.data.local.entities.CampoAssinaturaEntity
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

data class CampoVisual(
    val id: String = UUID.randomUUID().toString(),
    val signatarioId: String,
    val numeroPagina: Int = 1,
    val normX: Float = 0.25f,
    val normY: Float = 0.5f,
    val normLargura: Float = 0.5f,
    val normAltura: Float = 0.08f
)

enum class EstadoEnvio {
    ENVIANDO,
    SUCESSO,
    ERRO
}

data class WizardNovoDocUiState(
    val documentoId: String = UUID.randomUUID().toString(),
    val titulo: String = "",
    val pdfPath: String? = null,
    val pdfNome: String? = null,
    val pdfTamanhoBytes: Long = 0,
    val pdfTotalPaginas: Int = 1,
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
    val rascunhoDescartado: Boolean = false,
    val campos: List<CampoVisual> = emptyList(),
    val campoSelecionadoId: String? = null,
    val paginaAtualPdf: Int = 1,
    val mensagemConvite: String = "",
    val mostrarModalAtribuirCampo: Boolean = false,
    val estadoModalResultado: EstadoEnvio? = null
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

    val camposNaPaginaAtual: List<CampoVisual>
        get() = campos.filter { it.numeroPagina == paginaAtualPdf }

    val participantesSemCampo: List<String>
        get() = signatariosSelecionadosIds.filter { sigId -> campos.none { it.signatarioId == sigId } }

    val todosParticipantesTemCampo: Boolean
        get() = signatariosSelecionadosIds.isNotEmpty() && participantesSemCampo.isEmpty()

    val podeAvancarEtapa7: Boolean
        get() = todosParticipantesTemCampo
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

        val camposSalvos = documentoRepository.carregarCampos(rascunho.versao.id)
        val camposVisuais = camposSalvos.map { ce ->
            val sigId = ce.participanteId.removePrefix("part-$docId-")
            CampoVisual(
                id = ce.id,
                signatarioId = sigId,
                numeroPagina = ce.numeroPagina,
                normX = ce.normX,
                normY = ce.normY,
                normLargura = ce.normLargura,
                normAltura = ce.normAltura
            )
        }

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
                etapaAtual = rascunho.versao.etapaWizardAtual.coerceIn(1, 9),
                campos = camposVisuais
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

    fun adicionarCampo() {
        val estado = _uiState.value
        // Atribui preferencialmente a quem ainda não tem campo, ou ao primeiro signatário
        val participanteAlvo = estado.participantesSemCampo.firstOrNull()
            ?: estado.signatariosSelecionadosIds.firstOrNull()
            ?: return

        val offsetContador = estado.camposNaPaginaAtual.size
        val novoY = (0.25f + (offsetContador * 0.12f)).coerceAtMost(0.75f)

        val novoCampo = CampoVisual(
            id = UUID.randomUUID().toString(),
            signatarioId = participanteAlvo,
            numeroPagina = estado.paginaAtualPdf,
            normX = 0.25f,
            normY = novoY,
            normLargura = 0.5f,
            normAltura = 0.08f
        )

        _uiState.update {
            it.copy(
                campos = it.campos + novoCampo,
                campoSelecionadoId = novoCampo.id
            )
        }
        salvarRascunhoAutomatico()
    }

    fun selecionarCampo(campoId: String?) {
        _uiState.update { it.copy(campoSelecionadoId = campoId) }
    }

    fun abrirModalAtribuirCampo() {
        _uiState.update { it.copy(mostrarModalAtribuirCampo = true) }
    }

    fun fecharModalAtribuirCampo() {
        _uiState.update { it.copy(mostrarModalAtribuirCampo = false) }
    }

    fun atribuirCampo(campoId: String, novoSignatarioId: String) {
        _uiState.update { estado ->
            val atualizados = estado.campos.map {
                if (it.id == campoId) it.copy(signatarioId = novoSignatarioId) else it
            }
            estado.copy(campos = atualizados, mostrarModalAtribuirCampo = false)
        }
        salvarRascunhoAutomatico()
    }

    fun excluirCampo(campoId: String) {
        _uiState.update { estado ->
            val atualizados = estado.campos.filterNot { it.id == campoId }
            val novoSel = if (estado.campoSelecionadoId == campoId) null else estado.campoSelecionadoId
            estado.copy(campos = atualizados, campoSelecionadoId = novoSel)
        }
        salvarRascunhoAutomatico()
    }

    fun mudarPaginaPdf(pagina: Int) {
        val total = _uiState.value.pdfTotalPaginas.coerceAtLeast(1)
        val novaPagina = pagina.coerceIn(1, total)
        _uiState.update { it.copy(paginaAtualPdf = novaPagina, campoSelecionadoId = null) }
    }

    fun atualizarMensagemConvite(mensagem: String) {
        _uiState.update { it.copy(mensagemConvite = mensagem) }
    }

    fun fecharModalResultado() {
        _uiState.update { it.copy(estadoModalResultado = null) }
    }

    fun enviarDocumento(simularFalha: Boolean = false, aoSucesso: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(estadoModalResultado = EstadoEnvio.ENVIANDO) }
            val estado = _uiState.value

            // Salva os campos antes do envio
            val versaoId = "versao-${estado.documentoId}-1"
            val camposEntity = estado.campos.map { cv ->
                CampoAssinaturaEntity(
                    id = cv.id,
                    versaoDocumentoId = versaoId,
                    participanteId = "part-${estado.documentoId}-${cv.signatarioId}",
                    numeroPagina = cv.numeroPagina,
                    normX = cv.normX,
                    normY = cv.normY,
                    normLargura = cv.normLargura,
                    normAltura = cv.normAltura
                )
            }
            documentoRepository.salvarCampos(versaoId, camposEntity)

            val resultado = documentoRepository.enviarDocumento(
                adminId = adminIdAtual,
                documentoId = estado.documentoId,
                mensagemConvite = estado.mensagemConvite,
                simularFalha = simularFalha
            )

            if (resultado.isSuccess) {
                _uiState.update { it.copy(estadoModalResultado = EstadoEnvio.SUCESSO) }
                aoSucesso?.invoke()
            } else {
                _uiState.update { it.copy(estadoModalResultado = EstadoEnvio.ERRO) }
            }
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
            7 -> estado.podeAvancarEtapa7
            else -> true
        }

        if (podeAvancar) {
            val proxima = when (estado.etapaAtual) {
                5 -> 7 // Da etapa 5 (Ordem) vai para 7 (Campos)
                7 -> 9 // Da etapa 7 (Campos) vai para 9 (Revisão)
                else -> (estado.etapaAtual + 1).coerceAtMost(9)
            }
            _uiState.update { it.copy(etapaAtual = proxima) }
            salvarRascunhoAutomatico()
            return true
        }
        return false
    }

    fun voltarEtapa(aoSair: () -> Unit) {
        val etapa = _uiState.value.etapaAtual
        if (etapa > 1) {
            val anterior = when (etapa) {
                9 -> 7
                7 -> 5
                else -> etapa - 1
            }
            _uiState.update { it.copy(etapaAtual = anterior) }
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

        val versaoId = "versao-${estado.documentoId}-1"
        val camposEntity = estado.campos.map { cv ->
            CampoAssinaturaEntity(
                id = cv.id,
                versaoDocumentoId = versaoId,
                participanteId = "part-${estado.documentoId}-${cv.signatarioId}",
                numeroPagina = cv.numeroPagina,
                normX = cv.normX,
                normY = cv.normY,
                normLargura = cv.normLargura,
                normAltura = cv.normAltura
            )
        }
        documentoRepository.salvarCampos(versaoId, camposEntity)
    }
}

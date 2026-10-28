package br.com.letssign.presentation.admin.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.repository.DocumentoItemResumo
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.NotificacaoRepository
import br.com.letssign.data.repository.SignatarioRepository
import br.com.letssign.domain.model.SituacaoDocumento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FiltroDocumento {
    TODOS,
    RASCUNHOS,
    ENVIADOS,
    PARCIALMENTE_ASSINADOS,
    CONCLUIDOS,
    RECUSADOS,
    CANCELADOS,
    FALHA_ENVIO
}

data class Adm01UiState(
    val carregando: Boolean = true,
    val termoBusca: String = "",
    val filtroSelecionado: FiltroDocumento = FiltroDocumento.TODOS,
    val documentos: List<DocumentoItemResumo> = emptyList(),
    val contadores: Map<SituacaoDocumento, Int> = emptyMap(),
    val temSignatariosCadastrados: Boolean = true,
    val quantidadeNaoLidas: Int = 0,
    val rascunhoParaExcluir: DocumentoItemResumo? = null,
    val erro: String? = null
)

@HiltViewModel
class Adm01DocumentosViewModel @Inject constructor(
    private val documentoRepository: DocumentoRepository,
    private val signatarioRepository: SignatarioRepository,
    private val notificacaoRepository: NotificacaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _termoBusca = MutableStateFlow("")
    private val _filtroSelecionado = MutableStateFlow(FiltroDocumento.TODOS)
    private val _rascunhoParaExcluir = MutableStateFlow<DocumentoItemResumo?>(null)
    private val _erro = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(Adm01UiState())
    val uiState: StateFlow<Adm01UiState> = _uiState.asStateFlow()

    init {
        carregarDados()
    }

    fun carregarDados() {
        viewModelScope.launch {
            val adminId = sessionManager.obterUsuarioId() ?: "admin-carla-mendes"

            // Inicializar seeds se necessário
            documentoRepository.inicializarDadosDemoSeNecessario(adminId)
            signatarioRepository.inicializarDadosDemoSeNecessario(adminId)
            notificacaoRepository.inicializarDadosDemoSeNecessario(adminId, "ADMIN")

            combine(
                documentoRepository.listarDocumentosDoAdmin(adminId),
                signatarioRepository.listarSignatarios(adminId),
                notificacaoRepository.contarNaoLidas(adminId),
                _termoBusca,
                _filtroSelecionado,
                _rascunhoParaExcluir,
                _erro
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val docs = args[0] as List<DocumentoItemResumo>
                val signatarios = args[1] as List<*>
                val naoLidas = args[2] as Int
                val busca = args[3] as String
                val filtro = args[4] as FiltroDocumento
                val rascunho = args[5] as DocumentoItemResumo?
                val err = args[6] as String?

                // Calcular contadores por situação
                val contadores = docs.groupingBy { it.situacao }.eachCount()

                // Filtrar por busca e chip
                val filtrados = docs.filter { doc ->
                    val atendeBusca = busca.isBlank() || doc.titulo.contains(busca.trim(), ignoreCase = true)
                    val atendeFiltro = when (filtro) {
                        FiltroDocumento.TODOS -> true
                        FiltroDocumento.RASCUNHOS -> doc.situacao == SituacaoDocumento.RASCUNHO
                        FiltroDocumento.ENVIADOS -> doc.situacao == SituacaoDocumento.ENVIADO
                        FiltroDocumento.PARCIALMENTE_ASSINADOS -> doc.situacao == SituacaoDocumento.PARCIALMENTE_ASSINADO
                        FiltroDocumento.CONCLUIDOS -> doc.situacao == SituacaoDocumento.CONCLUIDO
                        FiltroDocumento.RECUSADOS -> doc.situacao == SituacaoDocumento.RECUSADO
                        FiltroDocumento.CANCELADOS -> doc.situacao == SituacaoDocumento.CANCELADO
                        FiltroDocumento.FALHA_ENVIO -> doc.situacao == SituacaoDocumento.FALHA_ENVIO
                    }
                    atendeBusca && atendeFiltro
                }

                Adm01UiState(
                    carregando = false,
                    termoBusca = busca,
                    filtroSelecionado = filtro,
                    documentos = filtrados,
                    contadores = contadores,
                    temSignatariosCadastrados = signatarios.isNotEmpty(),
                    quantidadeNaoLidas = naoLidas,
                    rascunhoParaExcluir = rascunho,
                    erro = err
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun aoAlterarBusca(novaBusca: String) {
        _termoBusca.value = novaBusca
    }

    fun aoSelecionarFiltro(filtro: FiltroDocumento) {
        _filtroSelecionado.value = filtro
    }

    fun aoLimparFiltro() {
        _termoBusca.value = ""
        _filtroSelecionado.value = FiltroDocumento.TODOS
    }

    fun aoSolicitarExclusaoRascunho(doc: DocumentoItemResumo) {
        _rascunhoParaExcluir.value = doc
    }

    fun aoCancelarExclusaoRascunho() {
        _rascunhoParaExcluir.value = null
    }

    fun aoConfirmarExclusaoRascunho() {
        val rascunho = _rascunhoParaExcluir.value ?: return
        viewModelScope.launch {
            documentoRepository.excluirRascunho(rascunho.id)
            _rascunhoParaExcluir.value = null
        }
    }
}

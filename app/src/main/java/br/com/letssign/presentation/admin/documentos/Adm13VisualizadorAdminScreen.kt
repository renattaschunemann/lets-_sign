package br.com.letssign.presentation.admin.documentos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.data.local.dao.CampoAssinaturaDao
import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
import br.com.letssign.data.local.dao.VersaoDocumentoDao
import br.com.letssign.data.local.entities.CampoAssinaturaEntity
import br.com.letssign.data.local.entities.ParticipanteDocumentoEntity
import br.com.letssign.data.local.entities.VersaoDocumentoEntity
import br.com.letssign.domain.model.SituacaoParticipante
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Adm13VisualizadorUiState(
    val versao: VersaoDocumentoEntity? = null,
    val participantes: List<ParticipanteDocumentoEntity> = emptyList(),
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val paginaAtual: Int = 1,
    val totalPaginas: Int = 3,
    val carregando: Boolean = true,
    val mensagemFeedback: String? = null
)

@HiltViewModel
class Adm13VisualizadorViewModel @Inject constructor(
    private val versaoDao: VersaoDocumentoDao,
    private val participanteDao: ParticipanteDocumentoDao,
    private val campoDao: CampoAssinaturaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val paramDocId: String = savedStateHandle.get<String>("documentoId") ?: ""
    private val paramVersaoId: String = savedStateHandle.get<String>("versaoId") ?: ""

    private val _uiState = MutableStateFlow(Adm13VisualizadorUiState())
    val uiState: StateFlow<Adm13VisualizadorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val versao = if (paramVersaoId.isNotBlank()) {
                versaoDao.buscarPorId(paramVersaoId)
            } else if (paramDocId.isNotBlank()) {
                versaoDao.buscarVersaoAtual(paramDocId)
            } else null

            val resolvedVersaoId = versao?.id ?: paramVersaoId
            val parts = if (resolvedVersaoId.isNotBlank()) participanteDao.listarPorVersaoSync(resolvedVersaoId) else emptyList()
            val campos = if (resolvedVersaoId.isNotBlank()) campoDao.listarPorVersaoSync(resolvedVersaoId) else emptyList()
            _uiState.update {
                it.copy(
                    versao = versao,
                    participantes = parts,
                    campos = campos,
                    carregando = false
                )
            }
        }
    }

    fun mudarPagina(novaPagina: Int) {
        _uiState.update { it.copy(paginaAtual = novaPagina) }
    }

    fun baixarPdf() {
        _uiState.update { it.copy(mensagemFeedback = "Download do PDF iniciado...") }
    }

    fun limparFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }
}

/**
 * ADM-13 — Visualizador de Documento para o Administrador.
 * Leitura completa com paginação, zoom e camadas sobrepostas de assinaturas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm13VisualizadorAdminScreen(
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm13VisualizadorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagemFeedback) {
        uiState.mensagemFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.adm13_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.wizard_btn_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.baixarPdf() }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = stringResource(R.string.adm13_btn_baixar),
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LetsSignTheme.colors.corFundo,
        modifier = modifier
    ) { paddingValues ->
        if (uiState.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                IndicadorCarregamentoCircular()
            }
        } else {
            val camposNaPagina = uiState.campos.filter { it.numeroPagina == uiState.paginaAtual }

            VisualizadorPdfCamadas(
                totalPaginas = uiState.totalPaginas,
                paginaAtual = uiState.paginaAtual,
                aoMudarPagina = { viewModel.mudarPagina(it) },
                modo = ModoVisualizadorPdf.LEITURA,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                camadaCampos = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        camposNaPagina.forEach { campo ->
                            val part = uiState.participantes.find { it.id == campo.participanteId }
                            val isAssinado = part?.situacao == SituacaoParticipante.ASSINADO

                            CampoAssinaturaSobreposto(
                                nomeSignatario = part?.nomeSnapshot ?: "Signatário",
                                estado = if (isAssinado) EstadoCampoAssinatura.ASSINADO else EstadoCampoAssinatura.VAZIO
                            )
                        }
                    }
                }
            )
        }
    }
}

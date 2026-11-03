package br.com.letssign.presentation.admin.documentos

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
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
import br.com.letssign.core.pdf.PdfDemoGenerator
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
import kotlin.math.roundToInt

data class Adm13VisualizadorUiState(
    val versao: VersaoDocumentoEntity? = null,
    val participantes: List<ParticipanteDocumentoEntity> = emptyList(),
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val paginaAtual: Int = 1,
    val totalPaginas: Int = 2,
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
            val maxPagina = maxOf(2, campos.maxOfOrNull { it.numeroPagina } ?: 2)

            _uiState.update {
                it.copy(
                    versao = versao,
                    participantes = parts,
                    campos = campos,
                    totalPaginas = maxPagina,
                    carregando = false
                )
            }
        }
    }

    fun mudarPagina(novaPagina: Int) {
        val total = _uiState.value.totalPaginas.coerceAtLeast(1)
        val clamped = novaPagina.coerceIn(1, total)
        _uiState.update { it.copy(paginaAtual = clamped) }
    }

    fun baixarPdf(context: Context) {
        try {
            val arquivo = PdfDemoGenerator.obterOuCriarContratoDemoPdf(context)
            PdfDemoGenerator.compartilharPdf(context, arquivo)
            _uiState.update { it.copy(mensagemFeedback = "Contrato assinado aberto para download e compartilhamento!") }
        } catch (e: Exception) {
            _uiState.update { it.copy(mensagemFeedback = "Erro ao exportar PDF: ${e.message}") }
        }
    }

    fun limparFeedback() {
        _uiState.update { it.copy(mensagemFeedback = null) }
    }
}

/**
 * ADM-13 — Visualizador de Documento para o Administrador.
 * Leitura completa com paginação, zoom e camadas sobrepostas de assinaturas com coordenadas normalizadas.
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
    val context = LocalContext.current

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
                    br.com.letssign.core.designsystem.components.logo.LogoPlaceholder(
                        tamanho = br.com.letssign.core.designsystem.components.logo.LogoSize.PEQUENO,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { viewModel.baixarPdf(context) }) {
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
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val totalLarguraPx = constraints.maxWidth.toFloat()
                        val totalAlturaPx = constraints.maxHeight.toFloat()
                        val densidade = LocalDensity.current

                        camposNaPagina.forEach { campo ->
                            val part = uiState.participantes.find { it.id == campo.participanteId }
                            val isAssinado = part?.situacao == SituacaoParticipante.ASSINADO
                            val papelRotulo = campo.papelEspecifico?.rotulo ?: part?.papel?.rotulo ?: ""
                            val nomeExibicao = if (part != null) {
                                if (papelRotulo.isNotBlank()) "${part.nomeSnapshot} ($papelRotulo)" else part.nomeSnapshot
                            } else "Signatário"

                            val larguraDp = with(densidade) { (totalLarguraPx * campo.normLargura).toDp() }.coerceIn(130.dp, 280.dp)
                            val alturaDp = with(densidade) { (totalAlturaPx * campo.normAltura).toDp() }.coerceIn(50.dp, 84.dp)
                            val posX = campo.normX * totalLarguraPx
                            val posY = campo.normY * totalAlturaPx

                            Box(
                                modifier = Modifier.offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                            ) {
                                CampoAssinaturaSobreposto(
                                    nomeSignatario = nomeExibicao,
                                    estado = if (isAssinado) EstadoCampoAssinatura.ASSINADO else EstadoCampoAssinatura.VAZIO,
                                    largura = larguraDp,
                                    altura = alturaDp
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

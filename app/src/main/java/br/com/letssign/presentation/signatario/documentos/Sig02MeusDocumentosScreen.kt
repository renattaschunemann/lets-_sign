package br.com.letssign.presentation.signatario.documentos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.CartaoDocumentoSignatario
import br.com.letssign.core.designsystem.components.reusable.EstadoVazio
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar
import br.com.letssign.domain.model.SituacaoParticipante

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig02MeusDocumentosScreen(
    aoNavegarParaDetalhes: (String) -> Unit,
    aoNavegarParaAssinar: (String) -> Unit,
    aoNavegarBottomNav: (DestinoSignatario) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig02MeusDocumentosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig02_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            SignatarioBottomBar(
                destinoAtual = DestinoSignatario.DOCUMENTOS,
                aoNavegarPara = aoNavegarBottomNav
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
        ) {
            // Campo de busca
            Box(modifier = Modifier.padding(16.dp)) {
                CampoTextoContornado(
                    valor = uiState.busca,
                    aoMudarValor = { viewModel.atualizarBusca(it) },
                    rotulo = stringResource(R.string.sig02_busca_placeholder),
                    iconeInicio = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                )
            }

            // Chips de filtro
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filtroSelecionado == FiltroDocumentoSignatario.TODOS,
                        onClick = { viewModel.selecionarFiltro(FiltroDocumentoSignatario.TODOS) },
                        label = { Text(stringResource(R.string.sig02_filtro_todos)) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filtroSelecionado == FiltroDocumentoSignatario.ASSINADOS,
                        onClick = { viewModel.selecionarFiltro(FiltroDocumentoSignatario.ASSINADOS) },
                        label = { Text(stringResource(R.string.sig02_filtro_assinados)) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filtroSelecionado == FiltroDocumentoSignatario.RECUSADOS,
                        onClick = { viewModel.selecionarFiltro(FiltroDocumentoSignatario.RECUSADOS) },
                        label = { Text(stringResource(R.string.sig02_filtro_recusados)) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filtroSelecionado == FiltroDocumentoSignatario.CANCELADOS,
                        onClick = { viewModel.selecionarFiltro(FiltroDocumentoSignatario.CANCELADOS) },
                        label = { Text(stringResource(R.string.sig02_filtro_cancelados)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.carregando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IndicadorCarregamentoCircular()
                }
            } else if (uiState.documentosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EstadoVazio(
                        mensagem = stringResource(R.string.sig02_vazio)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.documentosFiltrados, key = { it.documentoId }) { doc ->
                        CartaoDocumentoSignatario(
                            titulo = "${doc.titulo} — v${doc.versao}",
                            remetente = doc.remetenteNome,
                            papel = doc.papel.name,
                            dataConvite = doc.dataAtualizacaoFormatada,
                            numeroCampos = doc.totalCampos,
                            onClick = { aoNavegarParaDetalhes(doc.documentoId) },
                            aoClicarAssinar = if (doc.situacaoParticipante == SituacaoParticipante.PENDENTE) {
                                { aoNavegarParaAssinar(doc.documentoId) }
                            } else null,
                            situacaoGeral = doc.situacaoDocumento,
                            situacaoParticipante = doc.situacaoParticipante
                        )
                    }
                }
            }
        }
    }
}

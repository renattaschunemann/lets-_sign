package br.com.letssign.presentation.signatario.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.CartaoDocumentoSignatario
import br.com.letssign.core.designsystem.components.reusable.EstadoVazio
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig01InicioPendentesScreen(
    aoNavegarParaDetalhes: (String) -> Unit,
    aoNavegarParaAssinar: (String) -> Unit,
    aoNavegarBottomNav: (DestinoSignatario) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig01InicioPendentesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig01_titulo),
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
                destinoAtual = DestinoSignatario.INICIO,
                aoNavegarPara = aoNavegarBottomNav,
                quantidadePendentes = uiState.totalPendentes
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
        ) {
            // Abas Pendentes / Aguardando vez
            TabRow(
                selectedTabIndex = if (uiState.abaSelecionada == AbaInicioSignatario.PENDENTES) 0 else 1,
                containerColor = LetsSignTheme.colors.corSuperficie,
                contentColor = LetsSignTheme.colors.corPrimaria
            ) {
                Tab(
                    selected = uiState.abaSelecionada == AbaInicioSignatario.PENDENTES,
                    onClick = { viewModel.selecionarAba(AbaInicioSignatario.PENDENTES) },
                    text = {
                        Text(
                            text = "${stringResource(R.string.sig01_aba_pendentes)} (${uiState.totalPendentes})"
                        )
                    }
                )
                Tab(
                    selected = uiState.abaSelecionada == AbaInicioSignatario.AGUARDANDO_VEZ,
                    onClick = { viewModel.selecionarAba(AbaInicioSignatario.AGUARDANDO_VEZ) },
                    text = {
                        Text(
                            text = "${stringResource(R.string.sig01_aba_aguardando)} (${uiState.totalAguardandoVez})"
                        )
                    }
                )
            }

            // Anúncio de acessibilidade para leitores de tela (TalkBack)
            val textoAcessibilidade = stringResource(
                R.string.sig01_contador_anunciado,
                uiState.totalPendentes
            )
            Box(
                modifier = Modifier
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = textoAcessibilidade
                    }
            )

            if (uiState.carregando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IndicadorCarregamentoCircular()
                }
            } else {
                val listaAtual = if (uiState.abaSelecionada == AbaInicioSignatario.PENDENTES) {
                    uiState.documentosPendentes
                } else {
                    uiState.documentosAguardandoVez
                }

                if (listaAtual.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EstadoVazio(
                            mensagem = if (uiState.abaSelecionada == AbaInicioSignatario.PENDENTES) {
                                stringResource(R.string.sig01_vazio_pendentes)
                            } else {
                                stringResource(R.string.sig01_vazio_aguardando)
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listaAtual, key = { it.documentoId }) { doc ->
                            CartaoDocumentoSignatario(
                                titulo = "${doc.titulo} — v${doc.versao}",
                                remetente = doc.remetenteNome,
                                papel = doc.papel.name,
                                dataConvite = doc.dataAtualizacaoFormatada,
                                numeroCampos = doc.totalCampos,
                                onClick = { aoNavegarParaDetalhes(doc.documentoId) },
                                aoClicarAssinar = if (uiState.abaSelecionada == AbaInicioSignatario.PENDENTES) {
                                    { aoNavegarParaAssinar(doc.documentoId) }
                                } else null,
                                situacaoParticipante = doc.situacaoParticipante
                            )
                        }
                    }
                }
            }
        }
    }
}

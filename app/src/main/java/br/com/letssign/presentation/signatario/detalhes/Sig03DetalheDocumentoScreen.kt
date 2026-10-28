package br.com.letssign.presentation.signatario.detalhes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.BadgePapel
import br.com.letssign.core.designsystem.components.reusable.SeloSituacaoDocumento
import br.com.letssign.core.designsystem.components.reusable.SeloSituacaoParticipante
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.TipoOrdem
import br.com.letssign.presentation.signatario.modals.Mod08RecusarAssinaturaDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig03DetalheDocumentoScreen(
    aoVoltar: () -> Unit,
    aoNavegarParaAssinar: (String) -> Unit,
    aoNavegarParaVisualizar: (String) -> Unit,
    aoRecusaConcluida: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig03DetalheDocumentoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.recusaSucesso) {
        if (uiState.recusaSucesso) {
            aoRecusaConcluida()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig03_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        }
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
            val detalhe = uiState.detalhe
            if (detalhe == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.erro ?: "Documento não encontrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(LetsSignTheme.colors.corFundo)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card Principal de Metadados
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = FormaMedia,
                            colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                            elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "${detalhe.titulo} — v${detalhe.versao}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SeloSituacaoDocumento(situacao = detalhe.situacaoDocumento)
                                    SeloSituacaoParticipante(situacao = detalhe.situacaoParticipante)
                                }

                                HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                                Text(
                                    text = "${stringResource(R.string.sig03_remetente)} ${detalhe.remetenteNome} (${detalhe.remetenteEmail})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )

                                Text(
                                    text = "${stringResource(R.string.sig03_data_envio)} ${detalhe.dataEnvioFormatada}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )

                                Text(
                                    text = "${stringResource(R.string.sig03_mensagem_convite)}\n${detalhe.mensagemConvite}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                            }
                        }
                    }

                    // Seção Ordem de Assinatura
                    item {
                        Text(
                            text = "${stringResource(R.string.sig03_secao_ordem)} (${if (detalhe.tipoOrdem == TipoOrdem.SEQUENCIAL) "Sequencial" else "Paralela"})",
                            style = MaterialTheme.typography.titleMedium,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    items(detalhe.participantes, key = { it.id }) { part ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = FormaMedia,
                            colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${part.ordem}. ${part.nomeSnapshot}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = LetsSignTheme.colors.corTextoPrincipal
                                    )
                                    BadgePapel(papel = part.papel)
                                }
                                SeloSituacaoParticipante(situacao = part.situacao)
                            }
                        }
                    }

                    // Aviso de bloqueio (se aguardando vez ou já assinado)
                    if (detalhe.motivoBloqueio != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = FormaMedia,
                                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficieVariante)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = LetsSignTheme.colors.corPrimaria
                                    )
                                    Text(
                                        text = detalhe.motivoBloqueio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LetsSignTheme.colors.corTextoPrincipal
                                    )
                                }
                            }
                        }
                    }

                    // Ações principais
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (detalhe.podeAssinar) {
                                BotaoPrincipal(
                                    texto = stringResource(R.string.sig03_btn_assinar),
                                    onClick = { aoNavegarParaAssinar(detalhe.documentoId) }
                                )
                                BotaoDestrutivo(
                                    texto = stringResource(R.string.sig03_btn_recusar),
                                    onClick = { viewModel.exibirDialogRecusa(true) }
                                )
                            }

                            BotaoSecundario(
                                texto = stringResource(R.string.sig03_btn_visualizar),
                                onClick = { aoNavegarParaVisualizar(detalhe.documentoId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.exibindoDialogRecusa) {
        Mod08RecusarAssinaturaDialog(
            aoConfirmarRecusa = { motivo, senha ->
                viewModel.confirmarRecusa(motivo, senha)
            },
            aoFechar = { viewModel.exibirDialogRecusa(false) },
            erro = uiState.erroRecusa
        )
    }
}

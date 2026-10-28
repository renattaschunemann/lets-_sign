package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.BadgePapel
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.presentation.signatario.modals.Mod10TermosAceiteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig07RevisaoCamposScreen(
    aoVoltar: () -> Unit,
    aoConclusao: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig07RevisaoCamposViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.conclusaoComSucesso) {
        if (uiState.conclusaoComSucesso) {
            aoConclusao(viewModel.documentoId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig07_titulo),
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LetsSignTheme.colors.corSuperficie,
                tonalElevation = LetsSignTheme.elevation.nivel2
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    BotaoPrincipal(
                        texto = stringResource(R.string.sig07_btn_finalizar),
                        onClick = { viewModel.exibirTermosAceite(true) },
                        icone = Icons.Filled.Lock,
                        carregando = uiState.emitindoAssinatura
                    )
                }
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LetsSignTheme.colors.corFundo)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner Sucesso de preenchimento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormaMedia,
                    colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSucesso.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corSucesso
                        )
                        Text(
                            text = stringResource(R.string.sig07_campos_assinados_resumo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                }

                // Resumo do Documento e Signatário
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
                            text = "${detalhe?.titulo ?: ""} — v${detalhe?.versao ?: 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                        HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                        if (detalhe != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Signatário: João Lima",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                BadgePapel(papel = detalhe.papel)
                            }
                        }

                        Text(
                            text = "Campos preenchidos: ${detalhe?.totalCampos ?: 1} de ${detalhe?.totalCampos ?: 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario
                        )

                        Text(
                            text = "Validação: Autenticação em 2 fatores e registro de trilha criptográfica.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }
            }
        }
    }

    if (uiState.exibindoTermosAceite) {
        Mod10TermosAceiteDialog(
            aoConfirmarEmissao = { viewModel.emitirAssinatura() },
            aoFechar = { viewModel.exibirTermosAceite(false) },
            processando = uiState.emitindoAssinatura
        )
    }
}

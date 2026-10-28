package br.com.letssign.presentation.signatario.identidade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig04ConfirmacaoIdentidadeScreen(
    aoVoltar: () -> Unit,
    aoIdentidadeConfirmada: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig04ConfirmacaoIdentidadeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso) {
            aoIdentidadeConfirmada(viewModel.documentoId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig04_titulo),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LetsSignTheme.colors.corFundo)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.sig04_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )

                // Mensagem de erro / tentativas / bloqueio
                if (uiState.erro != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Assertive },
                        shape = FormaMedia,
                        colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficieVariante)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.bloqueado) Icons.Filled.Lock else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = LetsSignTheme.colors.corErro
                            )
                            Text(
                                text = uiState.erro ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LetsSignTheme.colors.corErro
                            )
                        }
                    }
                }

                CampoTextoContornado(
                    valor = uiState.nome,
                    aoMudarValor = { viewModel.atualizarNome(it) },
                    rotulo = stringResource(R.string.sig04_label_nome),
                    habilitado = !uiState.bloqueado && !uiState.validando
                )

                CampoTextoContornado(
                    valor = uiState.dataNascimento,
                    aoMudarValor = { viewModel.atualizarDataNascimento(it) },
                    rotulo = stringResource(R.string.sig04_label_nascimento),
                    dicaAjuda = "Ex: 10/05/1992",
                    habilitado = !uiState.bloqueado && !uiState.validando
                )

                CampoTextoContornado(
                    valor = uiState.senha,
                    aoMudarValor = { viewModel.atualizarSenha(it) },
                    rotulo = stringResource(R.string.sig04_label_senha),
                    ehSenha = true,
                    habilitado = !uiState.bloqueado && !uiState.validando
                )

                Spacer(modifier = Modifier.height(16.dp))

                BotaoPrincipal(
                    texto = stringResource(R.string.sig04_btn_confirmar),
                    onClick = { viewModel.confirmarIdentidade() },
                    habilitado = !uiState.bloqueado && !uiState.validando,
                    carregando = uiState.validando
                )
            }
        }
    }
}

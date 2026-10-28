package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.reusable.AreaDesenhoAssinatura
import br.com.letssign.core.designsystem.components.reusable.EstiloFonteCursiva
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig06CapturaAssinaturaScreen(
    aoVoltar: () -> Unit,
    aoAssinaturaConfirmada: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig06CapturaAssinaturaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.assinaturaPronta) {
        if (uiState.assinaturaPronta) {
            aoAssinaturaConfirmada(viewModel.documentoId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig06_titulo),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotaoSecundario(
                        texto = stringResource(R.string.sig06_btn_limpar),
                        onClick = { viewModel.limpar() },
                        icone = Icons.Filled.Delete,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoPrincipal(
                        texto = stringResource(R.string.sig06_btn_confirmar),
                        onClick = { viewModel.confirmarAssinatura() },
                        habilitado = uiState.podeConfirmar,
                        icone = Icons.Filled.Done,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
        ) {
            // Abas Desenhar / Digitar
            TabRow(
                selectedTabIndex = if (uiState.abaSelecionada == AbaCapturaAssinatura.DESENHAR) 0 else 1,
                containerColor = LetsSignTheme.colors.corSuperficie,
                contentColor = LetsSignTheme.colors.corPrimaria
            ) {
                Tab(
                    selected = uiState.abaSelecionada == AbaCapturaAssinatura.DESENHAR,
                    onClick = { viewModel.selecionarAba(AbaCapturaAssinatura.DESENHAR) },
                    text = { Text(stringResource(R.string.sig06_aba_desenhar)) }
                )
                Tab(
                    selected = uiState.abaSelecionada == AbaCapturaAssinatura.DIGITAR,
                    onClick = { viewModel.selecionarAba(AbaCapturaAssinatura.DIGITAR) },
                    text = { Text(stringResource(R.string.sig06_aba_digitar)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.abaSelecionada == AbaCapturaAssinatura.DESENHAR) {
                    Text(
                        text = stringResource(R.string.sig06_dica_desenho),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )

                    AreaDesenhoAssinatura(
                        aoMudarTemDesenho = { tem -> viewModel.atualizarTemDesenho(tem) }
                    )
                } else {
                    CampoTextoContornado(
                        valor = uiState.textoDigitado,
                        aoMudarValor = { viewModel.atualizarTextoDigitado(it) },
                        rotulo = stringResource(R.string.sig06_placeholder_digitada)
                    )

                    Text(
                        text = "Selecione o estilo da assinatura:",
                        style = MaterialTheme.typography.titleSmall,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )

                    EstiloFonteCursiva.entries.forEach { estilo ->
                        val selecionado = uiState.estiloFonteSelecionado == estilo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selecionarEstiloFonte(estilo) }
                                .border(
                                    width = if (selecionado) 2.dp else 1.dp,
                                    color = if (selecionado) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda,
                                    shape = FormaMedia
                                ),
                            shape = FormaMedia,
                            colors = CardDefaults.cardColors(
                                containerColor = if (selecionado) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.3f)
                                else LetsSignTheme.colors.corSuperficie
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = estilo.nomeExibicao,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = uiState.textoDigitado.ifBlank { "Sua Assinatura" },
                                    fontSize = 24.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = LetsSignTheme.colors.corTintaAssinatura
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

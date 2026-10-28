package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.TipoOrdem
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog

/**
 * ADM-08 — Definir ordem de assinatura (Etapa 6).
 */
@Composable
fun Adm08OrdemScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaCampos: () -> Unit,
    aoVoltarParaPapeis: () -> Unit,
    aoSairParaInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.mostrarModalSair) {
        Mod04SairAssistenteDialog(
            aoSalvarRascunho = { viewModel.salvarRascunhoManual(aoSairParaInicio) },
            aoDescartar = { viewModel.descartarRascunhoManual(aoSairParaInicio) },
            aoContinuarEditando = { viewModel.fecharModalSair() }
        )
    }

    WizardScaffold(
        etapaAtual = 5,
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaPapeis()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            viewModel.avancarEtapa()
            aoAvancarParaCampos()
        },
        podeAvancar = uiState.podeAvancarEtapa5,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Opções Paralela vs Sequencial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opção Paralela
                Surface(
                    shape = FormaMedia,
                    color = if (uiState.tipoOrdem == TipoOrdem.PARALELA) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.25f) else LetsSignTheme.colors.corSuperficie,
                    border = BorderStroke(
                        if (uiState.tipoOrdem == TipoOrdem.PARALELA) 2.dp else 1.dp,
                        if (uiState.tipoOrdem == TipoOrdem.PARALELA) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.definirTipoOrdem(TipoOrdem.PARALELA) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = uiState.tipoOrdem == TipoOrdem.PARALELA,
                                onClick = { viewModel.definirTipoOrdem(TipoOrdem.PARALELA) },
                                colors = RadioButtonDefaults.colors(selectedColor = LetsSignTheme.colors.corPrimaria)
                            )
                            Text(
                                text = stringResource(R.string.adm08_opcao_paralela),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                        }
                        Text(
                            text = stringResource(R.string.adm08_opcao_paralela_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }

                // Opção Sequencial
                Surface(
                    shape = FormaMedia,
                    color = if (uiState.tipoOrdem == TipoOrdem.SEQUENCIAL) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.25f) else LetsSignTheme.colors.corSuperficie,
                    border = BorderStroke(
                        if (uiState.tipoOrdem == TipoOrdem.SEQUENCIAL) 2.dp else 1.dp,
                        if (uiState.tipoOrdem == TipoOrdem.SEQUENCIAL) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.definirTipoOrdem(TipoOrdem.SEQUENCIAL) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = uiState.tipoOrdem == TipoOrdem.SEQUENCIAL,
                                onClick = { viewModel.definirTipoOrdem(TipoOrdem.SEQUENCIAL) },
                                colors = RadioButtonDefaults.colors(selectedColor = LetsSignTheme.colors.corPrimaria)
                            )
                            Text(
                                text = stringResource(R.string.adm08_opcao_sequencial),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                        }
                        Text(
                            text = stringResource(R.string.adm08_opcao_sequencial_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }
            }

            if (uiState.tipoOrdem == TipoOrdem.SEQUENCIAL) {
                Text(
                    text = stringResource(R.string.adm08_subtitulo_sequencial),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.participantesOrdenados, key = { _, s -> s.signatarioId }) { index, signatario ->
                        Surface(
                            shape = FormaMedia,
                            color = LetsSignTheme.colors.corSuperficie,
                            shadowElevation = LetsSignTheme.elevation.nivel1,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Badge numérico da ordem
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(LetsSignTheme.colors.corPrimaria, shape = FormaTotal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}º",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LetsSignTheme.colors.corSuperficie
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = signatario.nome,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LetsSignTheme.colors.corTextoPrincipal
                                    )
                                    Text(
                                        text = signatario.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                }

                                // Botões para mover para cima / baixo
                                Row {
                                    IconButton(
                                        onClick = { viewModel.moverSignatarioParaCima(index) },
                                        enabled = index > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = stringResource(R.string.adm08_btn_mover_cima),
                                            tint = if (index > 0) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corDesabilitado
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.moverSignatarioParaBaixo(index) },
                                        enabled = index < uiState.participantesOrdenados.size - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = stringResource(R.string.adm08_btn_mover_baixo),
                                            tint = if (index < uiState.participantesOrdenados.size - 1) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corDesabilitado
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

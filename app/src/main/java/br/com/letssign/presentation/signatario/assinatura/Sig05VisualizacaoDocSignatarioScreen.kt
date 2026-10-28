package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig05VisualizacaoDocSignatarioScreen(
    aoVoltar: () -> Unit,
    aoNavegarParaCaptura: (String) -> Unit,
    aoNavegarParaRevisao: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig05VisualizacaoDocSignatarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig05_titulo),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!uiState.todosCamposAssinados) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.totalCampos > 1) {
                                BotaoSecundario(
                                    texto = stringResource(R.string.sig05_btn_proximo_campo, uiState.camposRestantes),
                                    onClick = { viewModel.focarProximoCampo() },
                                    icone = Icons.AutoMirrored.Filled.ArrowForward,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            BotaoPrincipal(
                                texto = stringResource(R.string.sig05_btn_assinar_campo),
                                onClick = { aoNavegarParaCaptura(viewModel.documentoId) },
                                icone = Icons.Filled.Draw,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        BotaoPrincipal(
                            texto = stringResource(R.string.sig05_btn_revisar),
                            onClick = { aoNavegarParaRevisao(viewModel.documentoId) },
                            icone = Icons.Filled.CheckCircle
                        )
                    }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Simulação da página do documento PDF
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = FormaMedia,
                    colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                    elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel2)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = detalhe?.titulo ?: "Documento",
                                style = MaterialTheme.typography.titleMedium,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "Versão ${detalhe?.versao ?: 1} · Página 1 de 1",
                                style = MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoSecundario
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
                            Text(
                                text = "Por meio deste instrumento particular, as partes confirmam e aceitam todas as cláusulas e condições estabelecidas...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                        }

                        // Caixa de campo de assinatura guiado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    if (uiState.campoAssinado) LetsSignTheme.colors.corSucesso.copy(alpha = 0.1f)
                                    else LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (uiState.campoAssinado) LetsSignTheme.colors.corSucesso
                                    else LetsSignTheme.colors.corPrimaria,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    aoNavegarParaCaptura(viewModel.documentoId)
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.campoAssinado) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = LetsSignTheme.colors.corSucesso
                                    )
                                    Text(
                                        text = "Assinatura vinculada com sucesso",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = LetsSignTheme.colors.corSucesso,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Draw,
                                        contentDescription = null,
                                        tint = LetsSignTheme.colors.corPrimaria
                                    )
                                    Text(
                                        text = stringResource(R.string.sig05_btn_assinar_campo),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = LetsSignTheme.colors.corPrimaria,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Toque para criar ou aplicar sua assinatura",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
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

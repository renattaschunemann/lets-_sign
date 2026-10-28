package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.reusable.IndicadorEtapasWizard
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * Padrão do Wizard do Admin:
 * TopAppBar com voltar/fechar + Indicador de progresso segmentado + Barra inferior com Voltar e Avançar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScaffold(
    etapaAtual: Int,
    tituloTopo: String = stringResource(R.string.adm04_titulo),
    aoVoltar: () -> Unit,
    aoFechar: () -> Unit,
    aoAvancar: () -> Unit,
    podeAvancar: Boolean,
    textoAvancar: String = stringResource(R.string.wizard_btn_avancar),
    modifier: Modifier = Modifier,
    conteudo: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = tituloTopo,
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
                        IconButton(onClick = aoFechar) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.wizard_btn_fechar),
                                tint = LetsSignTheme.colors.corTextoPrincipal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LetsSignTheme.colors.corSuperficie
                    )
                )
                IndicadorEtapasWizard(etapaAtual = etapaAtual)
            }
        },
        bottomBar = {
            Surface(
                color = LetsSignTheme.colors.corSuperficie,
                shadowElevation = LetsSignTheme.elevation.nivel2
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotaoSecundario(
                        texto = stringResource(R.string.wizard_btn_voltar),
                        aoClicar = aoVoltar,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoPrincipal(
                        texto = textoAvancar,
                        aoClicar = aoAvancar,
                        habilitado = podeAvancar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        containerColor = LetsSignTheme.colors.corFundo,
        modifier = modifier,
        content = conteudo
    )
}

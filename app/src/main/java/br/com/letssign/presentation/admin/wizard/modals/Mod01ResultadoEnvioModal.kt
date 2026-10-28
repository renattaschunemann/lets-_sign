package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.theme.FormaGrande
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.presentation.admin.wizard.EstadoEnvio

/**
 * MOD-01 — Modal de status do envio do documento (Enviando, Sucesso ou Erro).
 */
@Composable
fun Mod01ResultadoEnvioModal(
    estado: EstadoEnvio,
    aoConcluirSucesso: () -> Unit,
    aoTentarNovamente: () -> Unit,
    aoVoltarInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            if (estado == EstadoEnvio.SUCESSO) aoConcluirSucesso()
            else if (estado == EstadoEnvio.ERRO) aoVoltarInicio()
        },
        properties = DialogProperties(
            dismissOnBackPress = estado != EstadoEnvio.ENVIANDO,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = FormaGrande,
            color = LetsSignTheme.colors.corSuperficie,
            shadowElevation = LetsSignTheme.elevation.nivel3,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (estado) {
                    EstadoEnvio.ENVIANDO -> {
                        IndicadorCarregamentoCircular()
                        Text(
                            text = stringResource(R.string.mod01_titulo_enviando),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.mod01_subtitulo_enviando),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }

                    EstadoEnvio.SUCESSO -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corSucesso,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = stringResource(R.string.mod01_titulo_sucesso),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.mod01_subtitulo_sucesso),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BotaoPrincipal(
                            texto = stringResource(R.string.mod01_btn_concluir),
                            aoClicar = aoConcluirSucesso,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EstadoEnvio.ERRO -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corErro,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = stringResource(R.string.mod01_titulo_erro),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.mod01_subtitulo_erro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BotaoPrincipal(
                            texto = stringResource(R.string.mod01_btn_tentar_novamente),
                            aoClicar = aoTentarNovamente,
                            modifier = Modifier.fillMaxWidth()
                        )
                        BotaoSecundario(
                            texto = stringResource(R.string.mod01_btn_voltar_inicio),
                            aoClicar = aoVoltarInicio,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

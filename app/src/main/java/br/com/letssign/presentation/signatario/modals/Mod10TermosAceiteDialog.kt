package br.com.letssign.presentation.signatario.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun Mod10TermosAceiteDialog(
    aoConfirmarEmissao: () -> Unit,
    aoFechar: () -> Unit,
    modifier: Modifier = Modifier,
    processando: Boolean = false
) {
    var concordo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!processando) aoFechar() },
        shape = FormaMedia,
        containerColor = LetsSignTheme.colors.corSuperficie,
        title = {
            Text(
                text = stringResource(R.string.mod10_titulo),
                style = MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.mod10_texto_termos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { concordo = !concordo }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = concordo,
                        onCheckedChange = { concordo = it }
                    )
                    Text(
                        text = stringResource(R.string.mod10_checkbox_concordo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }
            }
        },
        confirmButton = {
            BotaoPrincipal(
                texto = stringResource(R.string.mod10_btn_concluir_assinatura),
                onClick = aoConfirmarEmissao,
                habilitado = concordo && !processando,
                carregando = processando
            )
        },
        dismissButton = {
            BotaoTexto(
                texto = stringResource(R.string.btn_cancelar),
                onClick = aoFechar,
                habilitado = !processando
            )
        },
        modifier = modifier
    )
}

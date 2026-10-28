package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * MOD-05 — Dialog para retomar rascunho anterior ou iniciar novo.
 */
@Composable
fun Mod05RetomarRascunhoDialog(
    aoRetomar: () -> Unit,
    aoIniciarNovo: () -> Unit,
    aoDispensar: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = aoDispensar,
        title = {
            Text(
                text = stringResource(R.string.mod05_titulo),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
        },
        text = {
            Text(
                text = stringResource(R.string.mod05_mensagem),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corTextoSecundario
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotaoPrincipal(
                    texto = stringResource(R.string.mod05_btn_retomar),
                    aoClicar = aoRetomar,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoSecundario(
                    texto = stringResource(R.string.mod05_btn_novo),
                    aoClicar = aoIniciarNovo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {},
        shape = FormaMedia,
        containerColor = LetsSignTheme.colors.corSuperficie,
        modifier = modifier
    )
}

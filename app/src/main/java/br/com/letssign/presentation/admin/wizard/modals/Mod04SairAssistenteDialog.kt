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
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * MOD-04 — Dialog de confirmação ao sair do assistente.
 * Oferece as opções de salvar rascunho, descartar ou continuar editando.
 */
@Composable
fun Mod04SairAssistenteDialog(
    aoSalvarRascunho: () -> Unit,
    aoDescartar: () -> Unit,
    aoContinuarEditando: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = aoContinuarEditando,
        title = {
            Text(
                text = stringResource(R.string.mod04_titulo),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
        },
        text = {
            Text(
                text = stringResource(R.string.mod04_mensagem),
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
                    texto = stringResource(R.string.mod04_btn_salvar_rascunho),
                    aoClicar = aoSalvarRascunho,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoSecundario(
                    texto = stringResource(R.string.mod04_btn_descartar),
                    aoClicar = aoDescartar,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoTexto(
                    texto = stringResource(R.string.mod04_btn_continuar),
                    aoClicar = aoContinuarEditando,
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

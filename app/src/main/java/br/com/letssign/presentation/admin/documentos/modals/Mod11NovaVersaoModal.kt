package br.com.letssign.presentation.admin.documentos.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.theme.FormaGrande
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * MOD-11 — Modal de confirmação para criar nova versão / clonagem de rascunho.
 */
@Composable
fun Mod11NovaVersaoModal(
    aoConfirmar: () -> Unit,
    aoDispensar: () -> Unit,
    carregando: Boolean = false,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = aoDispensar) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.mod11_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )

                Text(
                    text = stringResource(R.string.mod11_descricao),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotaoSecundario(
                        texto = stringResource(R.string.wizard_btn_voltar),
                        aoClicar = aoDispensar,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoPrincipal(
                        texto = stringResource(R.string.mod11_btn_criar_versao),
                        aoClicar = aoConfirmar,
                        habilitado = !carregando,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

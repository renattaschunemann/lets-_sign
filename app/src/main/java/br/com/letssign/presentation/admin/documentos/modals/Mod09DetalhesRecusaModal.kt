package br.com.letssign.presentation.admin.documentos.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * MOD-09 — Modal / Bottom Sheet com os detalhes da recusa da assinatura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mod09DetalhesRecusaModal(
    nomeSignatario: String,
    dataHoraFormatada: String,
    justificativa: String,
    aoFechar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = aoFechar,
        containerColor = LetsSignTheme.colors.corSuperficie,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = LetsSignTheme.colors.corErro,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.mod09_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
            }

            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.mod09_rotulo_recusado_por),
                    style = MaterialTheme.typography.labelMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
                Text(
                    text = nomeSignatario,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.mod09_rotulo_data_hora),
                    style = MaterialTheme.typography.labelMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
                Text(
                    text = dataHoraFormatada,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.mod09_rotulo_justificativa),
                    style = MaterialTheme.typography.labelMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
                Surface(
                    shape = FormaMedia,
                    color = LetsSignTheme.colors.corErroContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corErro.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = justificativa.ifBlank { "Nenhuma justificativa detalhada foi fornecida." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BotaoPrincipal(
                texto = stringResource(R.string.mod09_btn_fechar),
                aoClicar = aoFechar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

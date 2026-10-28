package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.reusable.BadgePapel
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.presentation.admin.wizard.CampoVisual

/**
 * MOD-03 — Modal / Bottom Sheet para atribuir o campo selecionado a um signatário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mod03AtribuirCampoModal(
    participantes: List<AdminSignatarioEntity>,
    papeis: Map<String, PapelParticipante>,
    campos: List<CampoVisual>,
    signatarioAtualId: String?,
    aoSelecionarSignatario: (String) -> Unit,
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
            Text(
                text = stringResource(R.string.mod03_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participantes, key = { it.signatarioId }) { part ->
                    val isSelecionado = part.signatarioId == signatarioAtualId
                    val qtdCampos = campos.count { it.signatarioId == part.signatarioId }
                    val papel = papeis[part.signatarioId] ?: PapelParticipante.CONTRATADO

                    Surface(
                        shape = FormaMedia,
                        color = if (isSelecionado) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.25f) else LetsSignTheme.colors.corSuperficie,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelecionado) 2.dp else 1.dp,
                            color = if (isSelecionado) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoSelecionarSignatario(part.signatarioId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar inicial
                            val inicial = part.nome.trim().take(1).uppercase()
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(LetsSignTheme.colors.corPrimaria, shape = FormaTotal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = inicial,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corSuperficie
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.nome,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BadgePapel(papel = papel)
                                    Text(
                                        text = if (qtdCampos > 0) {
                                            stringResource(R.string.mod03_qtd_campos_formato, qtdCampos)
                                        } else {
                                            stringResource(R.string.mod03_nenhum_campo)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (qtdCampos > 0) LetsSignTheme.colors.corTextoSecundario else LetsSignTheme.colors.corAviso
                                    )
                                }
                            }

                            if (isSelecionado) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LetsSignTheme.colors.corPrimaria,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

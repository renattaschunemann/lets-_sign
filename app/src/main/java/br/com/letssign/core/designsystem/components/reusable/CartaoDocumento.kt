package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante

/**
 * Cartão de Documento para o Painel do Admin:
 * Título, versão, SeloSituacao, progresso "x de y obrigatórios assinaram", data da última atualização.
 */
@Composable
fun CartaoDocumentoAdmin(
    titulo: String,
    versao: Int,
    situacao: SituacaoDocumento,
    obrigatoriosAssinaram: Int,
    totalObrigatorios: Int,
    dataAtualizacao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FormaMedia,
        colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
        elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$titulo — v$versao",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal,
                    modifier = Modifier.weight(1f)
                )
                if (onMenuClick != null) {
                    IconButton(onClick = onMenuClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.cd_mais_opcoes),
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }
            }

            SeloSituacaoDocumento(situacao = situacao)

            if (situacao == SituacaoDocumento.FALHA_ENVIO) {
                Text(
                    text = "Envio não concluído — toque para tentar novamente.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corErro
                )
            } else if (totalObrigatorios > 0) {
                val progresso = obrigatoriosAssinaram.toFloat() / totalObrigatorios.toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "$obrigatoriosAssinaram de $totalObrigatorios obrigatórios assinaram",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                    LinearProgressIndicator(
                        progress = { progresso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = LetsSignTheme.colors.corPrimaria,
                        trackColor = LetsSignTheme.colors.corDesabilitado,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            Text(
                text = "Atualizado em $dataAtualizacao",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = LetsSignTheme.colors.corTextoSecundario
            )
        }
    }
}

/**
 * Cartão de Documento para o Signatário:
 * Título, remetente, papel, data do convite, nº de campos e botão "Assinar".
 */
@Composable
fun CartaoDocumentoSignatario(
    titulo: String,
    remetente: String,
    papel: String,
    dataConvite: String,
    numeroCampos: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aoClicarAssinar: (() -> Unit)? = null,
    situacaoGeral: SituacaoDocumento? = null,
    situacaoParticipante: SituacaoParticipante? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FormaMedia,
        colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
        elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corTextoPrincipal
            )

            Text(
                text = "Enviado por $remetente · $papel",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corTextoSecundario
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$numeroCampos campo(s) para assinar",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
                Text(
                    text = dataConvite,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }

            if (situacaoParticipante != null) {
                SeloSituacaoParticipante(situacao = situacaoParticipante)
            } else if (situacaoGeral != null) {
                SeloSituacaoDocumento(situacao = situacaoGeral)
            }

            if (aoClicarAssinar != null) {
                Spacer(modifier = Modifier.height(4.dp))
                BotaoPrincipal(
                    texto = stringResource(R.string.btn_assinar),
                    onClick = aoClicarAssinar
                )
            }
        }
    }
}

@Preview(name = "Cartão Admin Parcialmente Assinado", showBackground = true)
@Composable
private fun CartaoAdminPreview() {
    LetsSignTheme {
        CartaoDocumentoAdmin(
            titulo = "Contrato de Prestação de Serviços",
            versao = 1,
            situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO,
            obrigatoriosAssinaram = 2,
            totalObrigatorios = 3,
            dataAtualizacao = "26/10/2026",
            onClick = {}
        )
    }
}

@Preview(name = "Cartão Signatário Pendente", showBackground = true)
@Composable
private fun CartaoSignatarioPreview() {
    LetsSignTheme {
        CartaoDocumentoSignatario(
            titulo = "Aditivo 02",
            remetente = "Carla Mendes",
            papel = "Contratado",
            dataConvite = "26/10/2026",
            numeroCampos = 2,
            onClick = {},
            aoClicarAssinar = {}
        )
    }
}

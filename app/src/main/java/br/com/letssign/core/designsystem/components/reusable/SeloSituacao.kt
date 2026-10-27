package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class SituacaoDocumento {
    RASCUNHO,
    FALHA_ENVIO,
    ENVIADO,
    PARCIALMENTE_ASSINADO,
    CONCLUIDO,
    RECUSADO,
    CANCELADO,
    SUBSTITUIDO
}

enum class SituacaoParticipante {
    AGUARDANDO_VEZ,
    PENDENTE,
    ASSINADO,
    RECUSADO,
    ENCERRADO
}

/**
 * Selo de situação acessível (WCAG 2.1 AA):
 * NUNCA informação só por cor — sempre texto + ícone.
 * Fundo: cor-<grupo>-container; Texto e ícone: cor-<grupo>.
 */
@Composable
fun SeloSituacaoDocumento(
    situacao: SituacaoDocumento,
    modifier: Modifier = Modifier
) {
    val corContainer: Color
    val corConteudo: Color
    val icone: ImageVector
    val texto: String

    when (situacao) {
        SituacaoDocumento.RASCUNHO -> {
            corContainer = LetsSignTheme.colors.corNeutroContainer
            corConteudo = LetsSignTheme.colors.corNeutro
            icone = Icons.Filled.EditNote
            texto = stringResource(R.string.sit_doc_rascunho)
        }
        SituacaoDocumento.FALHA_ENVIO -> {
            corContainer = LetsSignTheme.colors.corErroContainer
            corConteudo = LetsSignTheme.colors.corErro
            icone = Icons.Filled.Error
            texto = stringResource(R.string.sit_doc_falha_envio)
        }
        SituacaoDocumento.ENVIADO -> {
            corContainer = LetsSignTheme.colors.corInfoContainer
            corConteudo = LetsSignTheme.colors.corInfo
            icone = Icons.AutoMirrored.Filled.Send
            texto = stringResource(R.string.sit_doc_enviado)
        }
        SituacaoDocumento.PARCIALMENTE_ASSINADO -> {
            corContainer = LetsSignTheme.colors.corAvisoContainer
            corConteudo = LetsSignTheme.colors.corAviso
            icone = Icons.Filled.HourglassTop
            texto = stringResource(R.string.sit_doc_parcialmente_assinado)
        }
        SituacaoDocumento.CONCLUIDO -> {
            corContainer = LetsSignTheme.colors.corSucessoContainer
            corConteudo = LetsSignTheme.colors.corSucesso
            icone = Icons.Filled.CheckCircle
            texto = stringResource(R.string.sit_doc_concluido)
        }
        SituacaoDocumento.RECUSADO -> {
            corContainer = LetsSignTheme.colors.corErroContainer
            corConteudo = LetsSignTheme.colors.corErro
            icone = Icons.Filled.Cancel
            texto = stringResource(R.string.sit_doc_recusado)
        }
        SituacaoDocumento.CANCELADO -> {
            corContainer = LetsSignTheme.colors.corNeutroContainer
            corConteudo = LetsSignTheme.colors.corNeutro
            icone = Icons.Filled.Block
            texto = stringResource(R.string.sit_doc_cancelado)
        }
        SituacaoDocumento.SUBSTITUIDO -> {
            corContainer = LetsSignTheme.colors.corNeutroContainer
            corConteudo = LetsSignTheme.colors.corNeutro
            icone = Icons.Filled.History
            texto = stringResource(R.string.sit_doc_substituido)
        }
    }

    Row(
        modifier = modifier
            .background(color = corContainer, shape = FormaPequena)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corConteudo,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = corConteudo
        )
    }
}

@Composable
fun SeloSituacaoParticipante(
    situacao: SituacaoParticipante,
    modifier: Modifier = Modifier
) {
    val corContainer: Color
    val corConteudo: Color
    val icone: ImageVector
    val texto: String

    when (situacao) {
        SituacaoParticipante.AGUARDANDO_VEZ -> {
            corContainer = LetsSignTheme.colors.corInfoContainer
            corConteudo = LetsSignTheme.colors.corInfo
            icone = Icons.Filled.Schedule
            texto = stringResource(R.string.sit_part_aguardando_vez)
        }
        SituacaoParticipante.PENDENTE -> {
            corContainer = LetsSignTheme.colors.corAvisoContainer
            corConteudo = LetsSignTheme.colors.corAviso
            icone = Icons.Filled.Pending
            texto = stringResource(R.string.sit_part_pendente)
        }
        SituacaoParticipante.ASSINADO -> {
            corContainer = LetsSignTheme.colors.corSucessoContainer
            corConteudo = LetsSignTheme.colors.corSucesso
            icone = Icons.Filled.TaskAlt
            texto = stringResource(R.string.sit_part_assinado)
        }
        SituacaoParticipante.RECUSADO -> {
            corContainer = LetsSignTheme.colors.corErroContainer
            corConteudo = LetsSignTheme.colors.corErro
            icone = Icons.Filled.Cancel
            texto = stringResource(R.string.sit_part_recusado)
        }
        SituacaoParticipante.ENCERRADO -> {
            corContainer = LetsSignTheme.colors.corNeutroContainer
            corConteudo = LetsSignTheme.colors.corNeutro
            icone = Icons.Filled.DoNotDisturbOn
            texto = stringResource(R.string.sit_part_encerrado)
        }
    }

    Row(
        modifier = modifier
            .background(color = corContainer, shape = FormaPequena)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corConteudo,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = corConteudo
        )
    }
}

@Preview(name = "Selos de Documento", showBackground = true)
@Composable
private fun SelosDocumentoPreview() {
    LetsSignTheme {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SituacaoDocumento.values().forEach { sit ->
                SeloSituacaoDocumento(situacao = sit)
            }
        }
    }
}

@Preview(name = "Selos de Participante", showBackground = true)
@Composable
private fun SelosParticipantePreview() {
    LetsSignTheme {
        androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SituacaoParticipante.values().forEach { sit ->
                SeloSituacaoParticipante(situacao = sit)
            }
        }
    }
}

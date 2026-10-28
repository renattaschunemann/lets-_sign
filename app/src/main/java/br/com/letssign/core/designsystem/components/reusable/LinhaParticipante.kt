package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.SituacaoParticipante

enum class ModoLinhaParticipante {
    SOMENTE_LEITURA,
    SELECAO_PAPEL,
    ALTERNANCIA_OBRIGATORIEDADE,
    REORDENAVEL,
    ACOMPANHAMENTO
}

/**
 * Linha de Participante com suporte a todas as variantes:
 * Avatar com iniciais, nome, papel, obrigatoriedade, ordem, selo de situação e controles acessíveis.
 */
@Composable
fun LinhaParticipante(
    nome: String,
    papel: String,
    obrigatorio: Boolean,
    ordem: Int,
    modifier: Modifier = Modifier,
    email: String? = null,
    situacao: SituacaoParticipante? = null,
    dataHoraEvento: String? = null,
    modo: ModoLinhaParticipante = ModoLinhaParticipante.SOMENTE_LEITURA,
    aoMoverCima: (() -> Unit)? = null,
    aoMoverBaixo: (() -> Unit)? = null,
    slotAcao: (@Composable () -> Unit)? = null
) {
    val iniciais = nome
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Alça de arrastar para modo reordenável
        if (modo == ModoLinhaParticipante.REORDENAVEL) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Alça de arrastar para reordenar",
                tint = LetsSignTheme.colors.corTextoSecundario,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Avatar com iniciais
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(LetsSignTheme.colors.corPrimariaContainer, shape = FormaTotal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corSobrePrimariaContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Dados do participante
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nome,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            if (email != null) {
                Text(
                    text = email,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
            val detalheObrigatoriedade = if (obrigatorio) "Obrigatório" else "Opcional"
            Text(
                text = "$papel · $detalheObrigatoriedade · ${ordem}º",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = LetsSignTheme.colors.corTextoSecundario
            )
            if (dataHoraEvento != null) {
                Text(
                    text = dataHoraEvento,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }

        // Situação ou controles de reordenação
        if (situacao != null) {
            SeloSituacaoParticipante(situacao = situacao)
        }

        if (modo == ModoLinhaParticipante.REORDENAVEL) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (aoMoverCima != null) {
                    IconButton(onClick = aoMoverCima, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Mover para cima",
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }
                if (aoMoverBaixo != null) {
                    IconButton(onClick = aoMoverBaixo, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Mover para baixo",
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                }
            }
        }

        if (slotAcao != null) {
            slotAcao()
        }
    }
}

@Preview(name = "Linha Acompanhamento Assinado", showBackground = true)
@Composable
private fun LinhaAcompanhamentoPreview() {
    LetsSignTheme {
        LinhaParticipante(
            nome = "Ana Lima",
            papel = "Contratante",
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.ASSINADO,
            dataHoraEvento = "26/10/2026 14:20",
            modo = ModoLinhaParticipante.ACOMPANHAMENTO
        )
    }
}

@Preview(name = "Linha Reordenável com Botões", showBackground = true)
@Composable
private fun LinhaReordenavelPreview() {
    LetsSignTheme {
        LinhaParticipante(
            nome = "Maria Souza",
            papel = "Contratado",
            obrigatorio = true,
            ordem = 2,
            email = "maria.souza@empresa.com",
            modo = ModoLinhaParticipante.REORDENAVEL,
            aoMoverCima = {},
            aoMoverBaixo = {}
        )
    }
}

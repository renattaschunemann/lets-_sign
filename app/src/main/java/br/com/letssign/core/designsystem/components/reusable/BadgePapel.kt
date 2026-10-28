package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.PapelParticipante

@Composable
fun BadgePapel(
    papel: PapelParticipante,
    modifier: Modifier = Modifier
) {
    val rotulo = when (papel) {
        PapelParticipante.CONTRATANTE -> "Contratante"
        PapelParticipante.CONTRATADO -> "Contratado"
        PapelParticipante.TESTEMUNHA -> "Testemunha"
        PapelParticipante.INTERVENIENTE -> "Interveniente"
        PapelParticipante.OUTRO -> "Outro"
    }

    Box(
        modifier = modifier
            .background(color = LetsSignTheme.colors.corNeutroContainer, shape = FormaPequena)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = LetsSignTheme.colors.corNeutro
        )
    }
}

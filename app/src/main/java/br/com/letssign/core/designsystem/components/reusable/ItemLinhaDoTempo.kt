package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun ItemLinhaDoTempo(
    dataHora: String,
    evento: String,
    modifier: Modifier = Modifier,
    autor: String? = null,
    detalhe: String? = null,
    ehUltimo: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Coluna da linha e marcador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color = LetsSignTheme.colors.corPrimaria, shape = FormaTotal)
            )
            if (!ehUltimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(color = LetsSignTheme.colors.corDivisor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Conteúdo textual
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (ehUltimo) 0.dp else 16.dp)
        ) {
            Text(
                text = "$dataHora — $evento",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            if (autor != null) {
                Text(
                    text = "Autor: $autor",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
            if (detalhe != null) {
                Text(
                    text = detalhe,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }
    }
}

@Preview(name = "Item Linha do Tempo", showBackground = true)
@Composable
private fun LinhaTempoPreview() {
    LetsSignTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ItemLinhaDoTempo(
                dataHora = "26/10/2026 14:03",
                evento = "Convite enviado por e-mail para Ana Lima",
                autor = "Carla Mendes"
            )
            ItemLinhaDoTempo(
                dataHora = "26/10/2026 14:20",
                evento = "Assinado com sucesso",
                autor = "Ana Lima",
                ehUltimo = true
            )
        }
    }
}

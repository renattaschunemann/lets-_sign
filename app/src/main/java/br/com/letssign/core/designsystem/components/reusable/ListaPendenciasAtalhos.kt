package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

data class ItemPendencia(
    val texto: String,
    val aoClicarAtalho: () -> Unit
)

/**
 * Lista de pendências com atalhos:
 * Cartão corAvisoContainer com ícone warning, uma linha por pendência e um atalho por linha.
 */
@Composable
fun ListaPendenciasAtalhos(
    pendencias: List<ItemPendencia>,
    modifier: Modifier = Modifier
) {
    if (pendencias.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = FormaPequena,
        colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corAvisoContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pendencias.forEach { pendencia ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corAviso,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pendencia.texto,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = pendencia.aoClicarAtalho) {
                        Text(
                            text = "Resolver",
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = LetsSignTheme.colors.corAviso
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corAviso,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Lista de Pendências", showBackground = true)
@Composable
private fun PendenciasPreview() {
    LetsSignTheme {
        ListaPendenciasAtalhos(
            pendencias = listOf(
                ItemPendencia("Falta posicionar o campo de assinatura de Maria Souza.", {}),
                ItemPendencia("Defina pelo menos uma pessoa como obrigatória.", {})
            )
        )
    }
}

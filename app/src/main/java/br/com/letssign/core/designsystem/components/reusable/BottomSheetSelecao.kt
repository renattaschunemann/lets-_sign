package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaExtraGrande
import br.com.letssign.core.designsystem.theme.LetsSignTheme

data class ItemSelecaoSheet<T>(
    val id: T,
    val titulo: String,
    val subtitulo: String? = null,
    val tagTopo: String? = null
)

/**
 * Bottom sheet de seleção com alça, título e itens tocáveis com área >= 48dp.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BottomSheetSelecao(
    titulo: String,
    itens: List<ItemSelecaoSheet<T>>,
    aoSelecionarItem: (T) -> Unit,
    aoDispensar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = aoDispensar,
        sheetState = sheetState,
        shape = FormaExtraGrande,
        containerColor = LetsSignTheme.colors.corSuperficie,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = titulo,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(itens) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoSelecionarItem(item.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.titulo,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            if (item.subtitulo != null) {
                                Text(
                                    text = item.subtitulo,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }

                        if (item.tagTopo != null) {
                            Text(
                                text = item.tagTopo,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = LetsSignTheme.colors.corAviso
                            )
                        }
                    }
                }
            }
        }
    }
}

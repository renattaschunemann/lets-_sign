package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun BadgePendencias(
    quantidade: Int,
    modifier: Modifier = Modifier
) {
    if (quantidade <= 0) return

    val descricao = stringResource(R.string.badge_pendentes, quantidade)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(color = LetsSignTheme.colors.corBadge, shape = FormaTotal)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = quantidade.toString(),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = LetsSignTheme.colors.corSobreBadge
        )
    }
}

@Preview(name = "Badge Pendências", showBackground = true)
@Composable
private fun BadgePreview() {
    LetsSignTheme {
        BadgePendencias(quantidade = 3)
    }
}

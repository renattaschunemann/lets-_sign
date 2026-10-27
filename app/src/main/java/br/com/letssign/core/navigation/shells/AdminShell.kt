package br.com.letssign.core.navigation.shells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.reusable.BadgePendencias
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class DestinoAdmin {
    DOCUMENTOS,
    SIGNATARIOS,
    NOTIFICACOES,
    CONTA
}

@Composable
fun AdminBottomBar(
    destinoAtual: DestinoAdmin,
    aoNavegarPara: (DestinoAdmin) -> Unit,
    quantidadeNaoLidas: Int = 2,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = LetsSignTheme.colors.corSuperficie,
        tonalElevation = LetsSignTheme.elevation.nivel2
    ) {
        ItemNavegacao(
            titulo = stringResource(R.string.nav_documentos),
            icone = Icons.Filled.Description,
            selecionado = destinoAtual == DestinoAdmin.DOCUMENTOS,
            onClick = { aoNavegarPara(DestinoAdmin.DOCUMENTOS) }
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_signatarios),
            icone = Icons.Filled.Group,
            selecionado = destinoAtual == DestinoAdmin.SIGNATARIOS,
            onClick = { aoNavegarPara(DestinoAdmin.SIGNATARIOS) }
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_notificacoes),
            icone = Icons.Filled.Notifications,
            selecionado = destinoAtual == DestinoAdmin.NOTIFICACOES,
            onClick = { aoNavegarPara(DestinoAdmin.NOTIFICACOES) },
            badgeCount = quantidadeNaoLidas
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_conta),
            icone = Icons.Filled.AccountCircle,
            selecionado = destinoAtual == DestinoAdmin.CONTA,
            onClick = { aoNavegarPara(DestinoAdmin.CONTA) }
        )
    }
}

@Composable
private fun RowScope.ItemNavegacao(
    titulo: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    NavigationBarItem(
        selected = selecionado,
        onClick = onClick,
        icon = {
            if (badgeCount > 0) {
                BadgedBox(badge = { BadgePendencias(quantidade = badgeCount) }) {
                    Icon(icone, contentDescription = null)
                }
            } else {
                Icon(icone, contentDescription = null)
            }
        },
        label = {
            Text(
                text = titulo,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = LetsSignTheme.colors.corPrimariaContainer,
            selectedIconColor = LetsSignTheme.colors.corSobrePrimariaContainer,
            selectedTextColor = LetsSignTheme.colors.corTextoPrincipal,
            unselectedIconColor = LetsSignTheme.colors.corTextoSecundario,
            unselectedTextColor = LetsSignTheme.colors.corTextoSecundario
        )
    )
}

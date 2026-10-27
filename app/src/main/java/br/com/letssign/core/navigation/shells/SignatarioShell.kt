package br.com.letssign.core.navigation.shells

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
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
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.reusable.BadgePendencias
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class DestinoSignatario {
    INICIO,
    DOCUMENTOS,
    NOTIFICACOES,
    CONTA
}

@Composable
fun SignatarioBottomBar(
    destinoAtual: DestinoSignatario,
    aoNavegarPara: (DestinoSignatario) -> Unit,
    quantidadePendentes: Int = 2,
    quantidadeNotificacoes: Int = 1,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = LetsSignTheme.colors.corSuperficie,
        tonalElevation = LetsSignTheme.elevation.nivel2
    ) {
        ItemNavegacao(
            titulo = stringResource(R.string.nav_inicio),
            icone = Icons.Filled.Home,
            selecionado = destinoAtual == DestinoSignatario.INICIO,
            onClick = { aoNavegarPara(DestinoSignatario.INICIO) },
            badgeCount = quantidadePendentes
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_documentos),
            icone = Icons.Filled.Folder,
            selecionado = destinoAtual == DestinoSignatario.DOCUMENTOS,
            onClick = { aoNavegarPara(DestinoSignatario.DOCUMENTOS) }
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_notificacoes),
            icone = Icons.Filled.Notifications,
            selecionado = destinoAtual == DestinoSignatario.NOTIFICACOES,
            onClick = { aoNavegarPara(DestinoSignatario.NOTIFICACOES) },
            badgeCount = quantidadeNotificacoes
        )
        ItemNavegacao(
            titulo = stringResource(R.string.nav_conta),
            icone = Icons.Filled.AccountCircle,
            selecionado = destinoAtual == DestinoSignatario.CONTA,
            onClick = { aoNavegarPara(DestinoSignatario.CONTA) }
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

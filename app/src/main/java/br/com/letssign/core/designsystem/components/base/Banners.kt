package br.com.letssign.core.designsystem.components.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

enum class TipoBanner {
    INFORMATIVO,
    ALERTA,
    ERRO
}

@Composable
fun BannerInformativo(
    texto: String,
    modifier: Modifier = Modifier,
    tipo: TipoBanner = TipoBanner.INFORMATIVO,
    icone: ImageVector? = null,
    textoAcao: String? = null,
    aoClicarAcao: (() -> Unit)? = null
) {
    val corContainer: Color
    val corConteudo: Color
    val iconePadrao: ImageVector

    when (tipo) {
        TipoBanner.INFORMATIVO -> {
            corContainer = LetsSignTheme.colors.corInfoContainer
            corConteudo = LetsSignTheme.colors.corInfo
            iconePadrao = Icons.Filled.Info
        }
        TipoBanner.ALERTA -> {
            corContainer = LetsSignTheme.colors.corAvisoContainer
            corConteudo = LetsSignTheme.colors.corAviso
            iconePadrao = Icons.Filled.Warning
        }
        TipoBanner.ERRO -> {
            corContainer = LetsSignTheme.colors.corErroContainer
            corConteudo = LetsSignTheme.colors.corErro
            iconePadrao = Icons.Filled.Warning
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = corContainer, shape = FormaPequena)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icone ?: iconePadrao,
            contentDescription = null,
            tint = corConteudo,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = texto,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
            if (textoAcao != null && aoClicarAcao != null) {
                TextButton(
                    onClick = aoClicarAcao,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = textoAcao,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        color = corConteudo
                    )
                }
            }
        }
    }
}

@Composable
fun BannerOffline(
    modifier: Modifier = Modifier,
    mensagem: String = stringResource(R.string.msg_sem_conexao)
) {
    BannerInformativo(
        texto = mensagem,
        tipo = TipoBanner.ALERTA,
        icone = Icons.Filled.CloudOff,
        modifier = modifier
    )
}

@Preview(name = "Banner Informativo", showBackground = true)
@Composable
private fun BannerInformativoPreview() {
    LetsSignTheme {
        BannerInformativo(
            texto = "Entre para abrir o documento enviado a você.",
            tipo = TipoBanner.INFORMATIVO
        )
    }
}

@Preview(name = "Banner Alerta com Ação", showBackground = true)
@Composable
private fun BannerAlertaComAcaoPreview() {
    LetsSignTheme {
        BannerInformativo(
            texto = "Antes de enviar, cadastre quem vai assinar.",
            tipo = TipoBanner.ALERTA,
            textoAcao = "Cadastrar signatário",
            aoClicarAcao = {}
        )
    }
}

@Preview(name = "Banner Offline", showBackground = true)
@Composable
private fun BannerOfflinePreview() {
    LetsSignTheme {
        BannerOffline()
    }
}

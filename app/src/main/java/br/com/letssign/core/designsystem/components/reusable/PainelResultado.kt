package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.theme.FormaExtraGrande
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class TipoResultado {
    SUCESSO,
    ERRO,
    ALERTA
}

@Composable
fun PainelResultadoConteudo(
    tipo: TipoResultado,
    titulo: String,
    mensagem: String,
    textoAcaoPrincipal: String,
    aoClicarAcaoPrincipal: () -> Unit,
    modifier: Modifier = Modifier,
    textoAcaoSecundaria: String? = null,
    aoClicarAcaoSecundaria: (() -> Unit)? = null
) {
    val corIcone = when (tipo) {
        TipoResultado.SUCESSO -> LetsSignTheme.colors.corSucesso
        TipoResultado.ERRO -> LetsSignTheme.colors.corErro
        TipoResultado.ALERTA -> LetsSignTheme.colors.corAviso
    }

    val icone = when (tipo) {
        TipoResultado.SUCESSO -> Icons.Filled.CheckCircle
        TipoResultado.ERRO -> Icons.Filled.Error
        TipoResultado.ALERTA -> Icons.Filled.Warning
    }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corIcone,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = titulo,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = LetsSignTheme.colors.corTextoPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mensagem,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = LetsSignTheme.colors.corTextoSecundario,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        BotaoPrincipal(
            texto = textoAcaoPrincipal,
            onClick = aoClicarAcaoPrincipal
        )

        if (textoAcaoSecundaria != null && aoClicarAcaoSecundaria != null) {
            Spacer(modifier = Modifier.height(8.dp))
            BotaoTexto(
                texto = textoAcaoSecundaria,
                onClick = aoClicarAcaoSecundaria,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Painel de resultado modal
 */
@Composable
fun DialogResultado(
    tipo: TipoResultado,
    titulo: String,
    mensagem: String,
    textoAcaoPrincipal: String,
    aoClicarAcaoPrincipal: () -> Unit,
    aoDispensar: () -> Unit,
    modifier: Modifier = Modifier,
    textoAcaoSecundaria: String? = null,
    aoClicarAcaoSecundaria: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = aoDispensar) {
        Card(
            shape = FormaExtraGrande,
            colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie)
        ) {
            PainelResultadoConteudo(
                tipo = tipo,
                titulo = titulo,
                mensagem = mensagem,
                textoAcaoPrincipal = textoAcaoPrincipal,
                aoClicarAcaoPrincipal = aoClicarAcaoPrincipal,
                textoAcaoSecundaria = textoAcaoSecundaria,
                aoClicarAcaoSecundaria = aoClicarAcaoSecundaria,
                modifier = modifier
            )
        }
    }
}

@Preview(name = "Painel Sucesso", showBackground = true)
@Composable
private fun PainelSucessoPreview() {
    LetsSignTheme {
        PainelResultadoConteudo(
            tipo = TipoResultado.SUCESSO,
            titulo = "Documento enviado!",
            mensagem = "3 pessoas foram convidadas por e-mail e SMS.",
            textoAcaoPrincipal = "Acompanhar documento",
            aoClicarAcaoPrincipal = {}
        )
    }
}

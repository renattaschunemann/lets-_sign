package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun EstadoVazio(
    mensagem: String,
    modifier: Modifier = Modifier,
    icone: ImageVector = Icons.Filled.Description,
    textoAcao: String? = null,
    aoClicarAcao: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = LetsSignTheme.colors.corTextoSecundario,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = mensagem,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = LetsSignTheme.colors.corTextoSecundario,
            textAlign = TextAlign.Center
        )

        if (textoAcao != null && aoClicarAcao != null) {
            Spacer(modifier = Modifier.height(20.dp))
            BotaoSecundario(
                texto = textoAcao,
                onClick = aoClicarAcao
            )
        }
    }
}

@Composable
fun EstadoErro(
    mensagem: String,
    aoTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = LetsSignTheme.colors.corErro,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = mensagem,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = LetsSignTheme.colors.corTextoPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        BotaoPrincipal(
            texto = stringResource(R.string.btn_tentar_novamente),
            onClick = aoTentarNovamente
        )
    }
}

@Preview(name = "Estado Vazio", showBackground = true)
@Composable
private fun EstadoVazioPreview() {
    LetsSignTheme {
        EstadoVazio(
            mensagem = "Você ainda não enviou documentos. Toque em 'Novo documento' para começar.",
            textoAcao = "Novo documento",
            aoClicarAcao = {}
        )
    }
}

@Preview(name = "Estado de Erro", showBackground = true)
@Composable
private fun EstadoErroPreview() {
    LetsSignTheme {
        EstadoErro(
            mensagem = "Não foi possível iniciar. Verifique sua conexão e tente novamente.",
            aoTentarNovamente = {}
        )
    }
}

package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun Sig08ConclusaoAssinaturaScreen(
    aoIrParaInicio: () -> Unit,
    aoVisualizarComprovante: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LetsSignTheme.colors.corSucesso,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.sig08_titulo),
                style = MaterialTheme.typography.headlineSmall,
                color = LetsSignTheme.colors.corTextoPrincipal,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.sig08_subtitulo),
                style = MaterialTheme.typography.bodyLarge,
                color = LetsSignTheme.colors.corTextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotaoPrincipal(
                    texto = stringResource(R.string.sig08_btn_inicio),
                    onClick = aoIrParaInicio,
                    icone = Icons.Filled.Home
                )

                BotaoSecundario(
                    texto = stringResource(R.string.sig08_btn_baixar),
                    onClick = aoVisualizarComprovante,
                    icone = Icons.Filled.Description
                )
            }
        }
    }
}

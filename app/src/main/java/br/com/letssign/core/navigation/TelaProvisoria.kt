package br.com.letssign.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.logo.LogoPlaceholder
import br.com.letssign.core.designsystem.components.logo.LogoSize
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * Tela provisória estruturada para a FASE 1:
 * Exibe o ID e Nome de cada tela, permitindo navegar entre as 38 telas provisórias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaProvisoria(
    idTela: String,
    nomeTela: String,
    aoVoltar: (() -> Unit)? = null,
    acaoProxima: Pair<String, () -> Unit>? = null,
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$idTela — $nomeTela",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (aoVoltar != null) {
                        IconButton(onClick = aoVoltar) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_voltar)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie,
                    titleContentColor = LetsSignTheme.colors.corTextoPrincipal
                )
            )
        },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LetsSignTheme.colors.corFundo)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoPlaceholder(tamanho = LogoSize.MEDIO)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = idTela,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corPrimaria
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nomeTela,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corTextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.tela_provisoria_aviso),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = LetsSignTheme.colors.corTextoDesabilitado
            )

            if (acaoProxima != null) {
                Spacer(modifier = Modifier.height(32.dp))
                BotaoPrincipal(
                    texto = acaoProxima.first,
                    onClick = acaoProxima.second,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

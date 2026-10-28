package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig08VisualizadorLeituraScreen(
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Visualizar documento",
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = FormaMedia,
                colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Contrato de Prestação de Serviços — Somente Leitura",
                        style = MaterialTheme.typography.titleMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                    HorizontalDivider(color = LetsSignTheme.colors.corDivisor)
                    Text(
                        text = "Este documento é uma cópia autêntica e auditada. Todas as assinaturas coletadas possuem validade jurídica conforme MP 2.200-2/2001.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }
        }
    }
}

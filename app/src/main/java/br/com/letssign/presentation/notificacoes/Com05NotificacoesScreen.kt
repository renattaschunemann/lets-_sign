package br.com.letssign.presentation.notificacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.EstadoErro
import br.com.letssign.core.designsystem.components.reusable.EstadoVazio
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.shells.AdminBottomBar
import br.com.letssign.core.navigation.shells.DestinoAdmin
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Com05NotificacoesScreen(
    aoNavegarParaDocumentoAdmin: (String) -> Unit,
    aoNavegarParaDocumentoSignatario: (String) -> Unit,
    aoNavegarBottomNavAdmin: (DestinoAdmin) -> Unit,
    aoNavegarBottomNavSignatario: (DestinoSignatario) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Com05NotificacoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ehAdmin = uiState.perfil.equals("ADMIN", ignoreCase = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.com05_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                actions = {
                    if (uiState.quantidadeNaoLidas > 0) {
                        TextButton(onClick = viewModel::aoMarcarTodasComoLidas) {
                            Text(
                                text = stringResource(R.string.btn_marcar_todas_como_lidas),
                                color = LetsSignTheme.colors.corPrimaria
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            if (ehAdmin) {
                AdminBottomBar(
                    destinoAtual = DestinoAdmin.NOTIFICACOES,
                    aoNavegarPara = aoNavegarBottomNavAdmin,
                    quantidadeNaoLidas = uiState.quantidadeNaoLidas
                )
            } else {
                SignatarioBottomBar(
                    destinoAtual = DestinoSignatario.NOTIFICACOES,
                    aoNavegarPara = aoNavegarBottomNavSignatario,
                    quantidadeNotificacoes = uiState.quantidadeNaoLidas
                )
            }
        },
        containerColor = LetsSignTheme.colors.corFundo
    ) { paddingVal ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVal)
        ) {
            if (uiState.carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IndicadorCarregamentoCircular()
                }
            } else if (uiState.erro != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EstadoErro(
                        mensagem = uiState.erro ?: stringResource(R.string.msg_sem_conexao),
                        aoTentarNovamente = viewModel::carregarNotificacoes
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Filtros Todas / Não lidas
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !uiState.filtroApenasNaoLidas,
                                onClick = { viewModel.aoAlternarFiltroNaoLidas(false) },
                                label = { Text(stringResource(R.string.com05_filtro_todas)) }
                            )
                            FilterChip(
                                selected = uiState.filtroApenasNaoLidas,
                                onClick = { viewModel.aoAlternarFiltroNaoLidas(true) },
                                label = { Text(stringResource(R.string.com05_filtro_nao_lidas)) }
                            )
                        }
                    }

                    // Lista ou Estado Vazio
                    if (uiState.notificacoes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EstadoVazio(
                                    mensagem = stringResource(R.string.com05_vazio_titulo)
                                )
                            }
                        }
                    } else {
                        items(uiState.notificacoes, key = { it.id }) { notif ->
                            val descAcessivel = if (!notif.lida) {
                                stringResource(R.string.com05_cd_nao_lida) + ", " + notif.mensagem
                            } else {
                                notif.mensagem
                            }

                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics { contentDescription = descAcessivel }                                         .clickable {
                                            viewModel.aoMarcarComoLida(notif.id)
                                            notif.documentoId?.let { docId ->
                                                if (ehAdmin) {
                                                    aoNavegarParaDocumentoAdmin(docId)
                                                } else {
                                                    aoNavegarParaDocumentoSignatario(docId)
                                                }
                                            }
                                        },
                                    shape = FormaMedia,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!notif.lida) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.35f)
                                        else LetsSignTheme.colors.corSuperficie
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Ícone do tipo
                                        val icone = when (notif.tipoNotificacao) {
                                            "ASSINATURA_REALIZADA",
                                            "DOCUMENTO_CONCLUIDO" -> Icons.Filled.CheckCircle
                                            "RECUSA" -> Icons.Filled.Error
                                            else -> Icons.Filled.Notifications
                                        }

                                        Icon(
                                            imageVector = icone,
                                            contentDescription = null,
                                            tint = when (notif.tipoNotificacao) {
                                                "ASSINATURA_REALIZADA",
                                                "DOCUMENTO_CONCLUIDO" -> LetsSignTheme.colors.corSucesso
                                                "RECUSA" -> LetsSignTheme.colors.corErro
                                                else -> LetsSignTheme.colors.corPrimaria
                                            },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(top = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = notif.titulo,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = if (!notif.lida) FontWeight.Bold else FontWeight.Medium,
                                                    color = LetsSignTheme.colors.corTextoPrincipal
                                                )
                                                if (!notif.lida) {
                                                    // Marcador circular não lido
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(
                                                                LetsSignTheme.colors.corPrimaria,
                                                                shape = FormaTotal
                                                            )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = notif.mensagem,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LetsSignTheme.colors.corTextoSecundario
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val dataHoraFormatada = SimpleDateFormat(
                                                "dd/MM/yyyy 'às' HH:mm",
                                                Locale("pt", "BR")
                                            ).format(Date(notif.dataHoraUtc))

                                            Text(
                                                text = dataHoraFormatada,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LetsSignTheme.colors.corTextoDesabilitado
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

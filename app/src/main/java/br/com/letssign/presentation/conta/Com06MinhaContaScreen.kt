package br.com.letssign.presentation.conta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.DialogBasico
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.shells.AdminBottomBar
import br.com.letssign.core.navigation.shells.DestinoAdmin
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Com06MinhaContaScreen(
    aoSairParaLogin: () -> Unit,
    aoNavegarBottomNavAdmin: (DestinoAdmin) -> Unit,
    aoNavegarBottomNavSignatario: (DestinoSignatario) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Com06MinhaContaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ehAdmin = uiState.perfil.equals("ADMIN", ignoreCase = true)

    LaunchedEffect(uiState.logoutConcluido) {
        if (uiState.logoutConcluido) {
            aoSairParaLogin()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.com06_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            if (ehAdmin) {
                AdminBottomBar(
                    destinoAtual = DestinoAdmin.CONTA,
                    aoNavegarPara = aoNavegarBottomNavAdmin
                )
            } else {
                SignatarioBottomBar(
                    destinoAtual = DestinoSignatario.CONTA,
                    aoNavegarPara = aoNavegarBottomNavSignatario
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
            if (uiState.carregando || uiState.saindo) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IndicadorCarregamentoCircular()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Avatar grande com iniciais
                    val iniciais = uiState.nome
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                LetsSignTheme.colors.corPrimariaContainer,
                                shape = FormaTotal
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciais,
                            style = MaterialTheme.typography.headlineMedium,
                            color = LetsSignTheme.colors.corSobrePrimariaContainer
                        )
                    }

                    Text(
                        text = uiState.nome,
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )

                    // Cartão de Dados Pessoais
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FormaMedia,
                        colors = CardDefaults.cardColors(
                            containerColor = LetsSignTheme.colors.corSuperficie
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_nome),
                                valor = uiState.nome
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_email),
                                valor = uiState.email
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_telefone),
                                valor = uiState.telefone
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_perfil),
                                valor = if (ehAdmin) stringResource(R.string.com06_perfil_admin)
                                else stringResource(R.string.com06_perfil_signatario)
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_versao),
                                valor = uiState.versaoApp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botão Sair
                    BotaoSecundario(
                        texto = stringResource(R.string.btn_sair),
                        onClick = viewModel::aoSolicitarSair,
                        icone = Icons.AutoMirrored.Filled.Logout,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Dialog de confirmação de saída
            if (uiState.mostrandoDialogSair) {
                DialogBasico(
                    titulo = stringResource(R.string.com06_dialog_sair_titulo),
                    mensagem = stringResource(R.string.com06_dialog_sair_msg),
                    textoBotaoPrincipal = stringResource(R.string.btn_sair),
                    textoBotaoSecundario = stringResource(R.string.adm01_dialog_voltar),
                    aoConfirmar = viewModel::aoConfirmarSair,
                    aoCancelar = viewModel::aoCancelarSair,
                    aoDispensar = viewModel::aoCancelarSair
                )
            }
        }
    }
}

@Composable
private fun ItemDadoConta(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = LetsSignTheme.colors.corTextoDesabilitado
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = LetsSignTheme.colors.corTextoPrincipal
        )
    }
}

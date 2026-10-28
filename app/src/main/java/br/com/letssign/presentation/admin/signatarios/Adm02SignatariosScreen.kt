package br.com.letssign.presentation.admin.signatarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm02SignatariosScreen(
    aoNavegarParaNovoSignatario: () -> Unit,
    aoNavegarParaEditarSignatario: (String) -> Unit,
    aoNavegarBottomNav: (DestinoAdmin) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm02SignatariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.adm02_titulo),
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
            AdminBottomBar(
                destinoAtual = DestinoAdmin.SIGNATARIOS,
                aoNavegarPara = aoNavegarBottomNav,
                quantidadeNaoLidas = uiState.quantidadeNaoLidas
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = aoNavegarParaNovoSignatario,
                containerColor = LetsSignTheme.colors.corPrimaria,
                contentColor = LetsSignTheme.colors.corSobrePrimaria,
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.btn_cadastrar_signatario)) }
            )
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
                        aoTentarNovamente = viewModel::carregarSignatarios
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo de busca
                    item {
                        OutlinedTextField(
                            value = uiState.termoBusca,
                            onValueChange = viewModel::aoAlterarBusca,
                            placeholder = { Text(stringResource(R.string.adm02_busca_placeholder)) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = FormaMedia,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LetsSignTheme.colors.corSuperficie,
                                unfocusedContainerColor = LetsSignTheme.colors.corSuperficie,
                                focusedBorderColor = LetsSignTheme.colors.corPrimaria,
                                unfocusedBorderColor = LetsSignTheme.colors.corBorda
                            )
                        )
                    }

                    // Lista ou Estado Vazio
                    if (uiState.signatarios.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.termoBusca.isNotBlank()) {
                                    EstadoVazio(
                                        mensagem = stringResource(
                                            R.string.adm02_vazio_busca_formato,
                                            uiState.termoBusca
                                        ),
                                        textoAcao = stringResource(R.string.btn_limpar_filtro),
                                        aoClicarAcao = { viewModel.aoAlterarBusca("") }
                                    )
                                } else {
                                    EstadoVazio(
                                        mensagem = stringResource(R.string.adm02_vazio_titulo),
                                        textoAcao = stringResource(R.string.btn_cadastrar_signatario),
                                        aoClicarAcao = aoNavegarParaNovoSignatario
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.signatarios, key = { it.signatarioId }) { sig ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { aoNavegarParaEditarSignatario(sig.signatarioId) },
                                    shape = FormaMedia,
                                    colors = CardDefaults.cardColors(
                                        containerColor = LetsSignTheme.colors.corSuperficie
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar com iniciais
                                        val iniciais = sig.nome
                                            .split(" ")
                                            .filter { it.isNotBlank() }
                                            .take(2)
                                            .mapNotNull { it.firstOrNull()?.uppercase() }
                                            .joinToString("")

                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    LetsSignTheme.colors.corPrimariaContainer,
                                                    shape = FormaTotal
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = iniciais,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = LetsSignTheme.colors.corSobrePrimariaContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Nome, E-mail e Telefone (nunca data de nascimento)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sig.nome,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = LetsSignTheme.colors.corTextoPrincipal
                                            )
                                            Text(
                                                text = sig.email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LetsSignTheme.colors.corTextoSecundario
                                            )
                                            Text(
                                                text = sig.telefone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LetsSignTheme.colors.corTextoDesabilitado
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.aoExcluirSignatario(sig.signatarioId) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = stringResource(R.string.cd_excluir),
                                                tint = LetsSignTheme.colors.corTextoDesabilitado
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

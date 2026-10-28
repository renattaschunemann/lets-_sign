package br.com.letssign.presentation.admin.documentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.DialogBasico
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.EstadoVazio
import br.com.letssign.core.designsystem.components.logo.LogoPlaceholder
import br.com.letssign.core.designsystem.components.logo.LogoSize
import br.com.letssign.core.designsystem.components.reusable.CartaoDocumentoAdmin
import br.com.letssign.core.designsystem.components.reusable.SeloSituacaoDocumento
import br.com.letssign.core.navigation.shells.AdminBottomBar
import br.com.letssign.core.navigation.shells.DestinoAdmin
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.SituacaoDocumento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm01DocumentosScreen(
    aoNavegarParaNovoDocumento: () -> Unit,
    aoNavegarParaDocumento: (String) -> Unit,
    aoNavegarParaFalhaEnvio: (String) -> Unit,
    aoNavegarParaRascunho: (String) -> Unit,
    aoNavegarParaCadastrarSignatario: () -> Unit,
    aoNavegarBottomNav: (DestinoAdmin) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm01DocumentosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LogoPlaceholder(tamanho = LogoSize.PEQUENO)
                        Text(
                            text = stringResource(R.string.adm01_titulo),
                            style = MaterialTheme.typography.titleLarge,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            AdminBottomBar(
                destinoAtual = DestinoAdmin.DOCUMENTOS,
                aoNavegarPara = aoNavegarBottomNav,
                quantidadeNaoLidas = uiState.quantidadeNaoLidas
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = aoNavegarParaNovoDocumento,
                containerColor = LetsSignTheme.colors.corPrimaria,
                contentColor = LetsSignTheme.colors.corSobrePrimaria,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.btn_novo_documento)) }
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Resumo de contadores por situação (linha rolável)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SituacaoDocumento.entries.forEach { situacao ->
                                val count = uiState.contadores[situacao] ?: 0
                                Surface(
                                    shape = FormaMedia,
                                    color = LetsSignTheme.colors.corSuperficie,
                                    tonalElevation = LetsSignTheme.elevation.nivel1,
                                    modifier = Modifier.clickable {
                                        viewModel.aoSelecionarFiltro(
                                            when (situacao) {
                                                SituacaoDocumento.RASCUNHO -> FiltroDocumento.RASCUNHOS
                                                SituacaoDocumento.ENVIANDO -> FiltroDocumento.ENVIADOS
                                                SituacaoDocumento.FALHA_ENVIO -> FiltroDocumento.FALHA_ENVIO
                                                SituacaoDocumento.ENVIADO -> FiltroDocumento.ENVIADOS
                                                SituacaoDocumento.PARCIALMENTE_ASSINADO -> FiltroDocumento.PARCIALMENTE_ASSINADOS
                                                SituacaoDocumento.CONCLUIDO -> FiltroDocumento.CONCLUIDOS
                                                SituacaoDocumento.RECUSADO -> FiltroDocumento.RECUSADOS
                                                SituacaoDocumento.CANCELADO -> FiltroDocumento.CANCELADOS
                                                SituacaoDocumento.SUBSTITUIDO -> FiltroDocumento.TODOS
                                            }
                                        )
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SeloSituacaoDocumento(situacao = situacao)
                                        Text(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = LetsSignTheme.colors.corTextoPrincipal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Campo de busca por título
                    item {
                        OutlinedTextField(
                            value = uiState.termoBusca,
                            onValueChange = viewModel::aoAlterarBusca,
                            placeholder = { Text(stringResource(R.string.adm01_busca_placeholder)) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = FormaMedia,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LetsSignTheme.colors.corSuperficie,
                                unfocusedContainerColor = LetsSignTheme.colors.corSuperficie,
                                focusedBorderColor = LetsSignTheme.colors.corPrimaria,
                                unfocusedBorderColor = LetsSignTheme.colors.corBorda
                            )
                        )
                    }

                    // 3. Chips de filtro (rolagem horizontal)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_todos),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.TODOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.TODOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_rascunhos),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.RASCUNHOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.RASCUNHOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_enviados),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.ENVIADOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.ENVIADOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_parcialmente),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.PARCIALMENTE_ASSINADOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.PARCIALMENTE_ASSINADOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_concluidos),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.CONCLUIDOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.CONCLUIDOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_recusados),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.RECUSADOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.RECUSADOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_cancelados),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.CANCELADOS,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.CANCELADOS) }
                            )
                            FilterChipItem(
                                label = stringResource(R.string.adm01_filtro_falha),
                                selecionado = uiState.filtroSelecionado == FiltroDocumento.FALHA_ENVIO,
                                onClick = { viewModel.aoSelecionarFiltro(FiltroDocumento.FALHA_ENVIO) }
                            )
                        }
                    }

                    // 4. Banner caso não existam signatários cadastrados
                    if (!uiState.temSignatariosCadastrados) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                BannerAviso(
                                    mensagem = stringResource(R.string.adm01_banner_sem_signatarios),
                                    textoAcao = stringResource(R.string.btn_cadastrar_signatario),
                                    aoClicarAcao = aoNavegarParaCadastrarSignatario
                                )
                            }
                        }
                    }

                    // 5. Lista de Documentos ou Estado Vazio
                    if (uiState.documentos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.filtroSelecionado != FiltroDocumento.TODOS || uiState.termoBusca.isNotBlank()) {
                                    EstadoVazio(
                                        mensagem = stringResource(R.string.adm01_vazio_filtro_titulo),
                                        textoAcao = stringResource(R.string.btn_limpar_filtro),
                                        aoClicarAcao = viewModel::aoLimparFiltro
                                    )
                                } else {
                                    EstadoVazio(
                                        mensagem = stringResource(R.string.adm01_vazio_titulo),
                                        textoAcao = stringResource(R.string.btn_novo_documento),
                                        aoClicarAcao = aoNavegarParaNovoDocumento
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.documentos, key = { it.id }) { doc ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                CartaoDocumentoAdmin(
                                    titulo = doc.titulo,
                                    versao = doc.versao,
                                    situacao = doc.situacao,
                                    obrigatoriosAssinaram = doc.obrigatoriosAssinaram,
                                    totalObrigatorios = doc.totalObrigatorios,
                                    dataAtualizacao = doc.dataAtualizacaoFormatada,
                                    onClick = {
                                        when (doc.situacao) {
                                            SituacaoDocumento.FALHA_ENVIO -> aoNavegarParaFalhaEnvio(doc.id)
                                            SituacaoDocumento.RASCUNHO -> aoNavegarParaRascunho(doc.id)
                                            else -> aoNavegarParaDocumento(doc.id)
                                        }
                                    },
                                    onMenuClick = if (doc.situacao == SituacaoDocumento.RASCUNHO) {
                                        { viewModel.aoSolicitarExclusaoRascunho(doc) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Diálogo de confirmação de exclusão de rascunho
            if (uiState.rascunhoParaExcluir != null) {
                DialogBasico(
                    titulo = stringResource(R.string.adm01_dialog_excluir_rascunho_titulo),
                    mensagem = stringResource(R.string.adm01_dialog_excluir_rascunho_msg),
                    textoBotaoPrincipal = stringResource(R.string.btn_excluir_rascunho),
                    textoBotaoSecundario = stringResource(R.string.adm01_dialog_voltar),
                    aoConfirmar = viewModel::aoConfirmarExclusaoRascunho,
                    aoCancelar = viewModel::aoCancelarExclusaoRascunho,
                    aoDispensar = viewModel::aoCancelarExclusaoRascunho
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selecionado,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LetsSignTheme.colors.corPrimariaContainer,
            selectedLabelColor = LetsSignTheme.colors.corSobrePrimariaContainer
        )
    )
}

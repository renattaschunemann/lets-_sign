package br.com.letssign.presentation.admin.documentos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.BotaoTonal
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.LinhaParticipante
import br.com.letssign.core.designsystem.components.reusable.ModoLinhaParticipante
import br.com.letssign.core.designsystem.components.reusable.SeloSituacaoDocumento
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante
import br.com.letssign.domain.model.TipoOrdem
import br.com.letssign.presentation.admin.documentos.modals.Mod07CancelarDocumentoDialog
import br.com.letssign.presentation.admin.documentos.modals.Mod09DetalhesRecusaModal
import br.com.letssign.presentation.admin.documentos.modals.Mod11NovaVersaoModal

/**
 * ADM-11 — Acompanhamento do Documento.
 * Visão consolidada de progresso, status dos participantes, "Vez de" e ações de ciclo de vida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm11AcompanhamentoScreen(
    aoVoltar: () -> Unit,
    aoNavegarParaVisualizador: (versaoId: String) -> Unit,
    aoNavegarParaAuditoria: (versaoId: String) -> Unit,
    aoNavegarParaNovaVersaoCriada: (novoDocumentoId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm11AcompanhamentoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagemFeedback) {
        uiState.mensagemFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparFeedback()
        }
    }

    if (uiState.mostrarModalCancelar) {
        Mod07CancelarDocumentoDialog(
            aoConfirmar = { motivo, senha ->
                viewModel.cancelarDocumento(motivo, senha) {
                    viewModel.definirFeedback("Documento cancelado.")
                }
            },
            aoDispensar = { viewModel.fecharModalCancelar() },
            mensagemErro = uiState.erroCancelamento,
            carregando = uiState.carregando
        )
    }

    if (uiState.mostrarModalRecusa) {
        val partRecusado = uiState.documento?.participantes?.find { it.situacao == SituacaoParticipante.RECUSADO }
        val recusa = uiState.documento?.recusa
        Mod09DetalhesRecusaModal(
            nomeSignatario = partRecusado?.nomeSnapshot ?: "Signatário",
            dataHoraFormatada = "26/10/2026 às 15:42 (UTC)",
            justificativa = recusa?.observacao ?: "O signatário solicitou alteração nas cláusulas comerciais antes de assinar.",
            aoFechar = { viewModel.fecharModalRecusa() }
        )
    }

    if (uiState.mostrarModalNovaVersao) {
        Mod11NovaVersaoModal(
            aoConfirmar = {
                viewModel.criarNovaVersao { novoDocId ->
                    aoNavegarParaNovaVersaoCriada(novoDocId)
                }
            },
            aoDispensar = { viewModel.fecharModalNovaVersao() },
            carregando = uiState.carregando
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.adm11_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.wizard_btn_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LetsSignTheme.colors.corFundo,
        modifier = modifier
    ) { paddingValues ->
        if (uiState.carregando && uiState.documento == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                IndicadorCarregamentoCircular()
            }
        } else {
            val detalhe = uiState.documento ?: return@Scaffold

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Resumo do documento e situação
                item {
                    Surface(
                        shape = FormaMedia,
                        color = LetsSignTheme.colors.corSuperficie,
                        shadowElevation = LetsSignTheme.elevation.nivel1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detalhe.documento.titulo,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corTextoPrincipal,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "v${detalhe.versao.numeroVersao}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }

                            SeloSituacaoDocumento(situacao = detalhe.documento.situacaoAtual)

                            // Progresso de assinaturas
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.adm01_progresso_assinaturas,
                                            detalhe.assinadosObrigatorios,
                                            detalhe.totalObrigatorios
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                    Text(
                                        text = "${(detalhe.progressoPercentual * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LetsSignTheme.colors.corPrimaria
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { detalhe.progressoPercentual },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = LetsSignTheme.colors.corSucesso,
                                    trackColor = LetsSignTheme.colors.corDivisor
                                )
                            }
                        }
                    }
                }

                // Card 2: Ações Principais do Ciclo de Vida
                item {
                    Surface(
                        shape = FormaMedia,
                        color = LetsSignTheme.colors.corSuperficie,
                        shadowElevation = LetsSignTheme.elevation.nivel1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BotaoTonal(
                                    texto = stringResource(R.string.adm11_btn_ver_documento),
                                    aoClicar = { aoNavegarParaVisualizador(detalhe.versao.id) },
                                    icone = Icons.Default.Visibility,
                                    modifier = Modifier.weight(1f)
                                )
                                BotaoTonal(
                                    texto = stringResource(R.string.adm11_btn_trilha_auditoria),
                                    aoClicar = { aoNavegarParaAuditoria(detalhe.versao.id) },
                                    icone = Icons.Default.History,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Ações contextuais por status
                            when (detalhe.documento.situacaoAtual) {
                                SituacaoDocumento.RECUSADO -> {
                                    BotaoPrincipal(
                                        texto = stringResource(R.string.adm11_btn_ver_motivo_recusa),
                                        aoClicar = { viewModel.abrirModalRecusa() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    BotaoSecundario(
                                        texto = stringResource(R.string.adm11_btn_nova_versao),
                                        aoClicar = { viewModel.abrirModalNovaVersao() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                SituacaoDocumento.CANCELADO -> {
                                    BotaoPrincipal(
                                        texto = stringResource(R.string.adm11_btn_nova_versao),
                                        aoClicar = { viewModel.abrirModalNovaVersao() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                SituacaoDocumento.CONCLUIDO -> {
                                    BotaoPrincipal(
                                        texto = stringResource(R.string.adm11_btn_baixar_assinado),
                                        aoClicar = { viewModel.definirFeedback("Baixando documento assinado com certificado...") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    BotaoSecundario(
                                        texto = stringResource(R.string.adm11_btn_baixar_original),
                                        aoClicar = { viewModel.definirFeedback("Baixando PDF original...") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                SituacaoDocumento.ENVIADO,
                                SituacaoDocumento.PARCIALMENTE_ASSINADO -> {
                                    BotaoDestrutivo(
                                        texto = stringResource(R.string.adm11_btn_cancelar_documento),
                                        aoClicar = { viewModel.abrirModalCancelar() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                else -> Unit
                            }
                        }
                    }
                }

                // Card 3: Seção Signatários e Progresso
                item {
                    Text(
                        text = stringResource(R.string.adm11_secao_signatarios),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }

                items(detalhe.participantes.sortedBy { it.ordem }, key = { it.id }) { part ->
                    val isSequencial = detalhe.versao.tipoOrdem == TipoOrdem.SEQUENCIAL
                    val isVezAtual = isSequencial && part.situacao == SituacaoParticipante.PENDENTE
                    val isAguardando = isSequencial && part.situacao == SituacaoParticipante.AGUARDANDO_VEZ

                    Surface(
                        shape = FormaMedia,
                        color = LetsSignTheme.colors.corSuperficie,
                        shadowElevation = LetsSignTheme.elevation.nivel1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinhaParticipante(
                                nome = part.nomeSnapshot,
                                email = part.emailSnapshot,
                                papel = part.papel.name.lowercase().replaceFirstChar { it.uppercase() },
                                obrigatorio = part.obrigatorio,
                                ordem = part.ordem,
                                situacao = part.situacao,
                                modo = ModoLinhaParticipante.ACOMPANHAMENTO
                            )

                            // Badge de Vez de assinar ou Aguardando vez
                            if (isVezAtual) {
                                Surface(
                                    shape = FormaPequena,
                                    color = LetsSignTheme.colors.corAvisoContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corAviso)
                                ) {
                                    Text(
                                        text = stringResource(R.string.adm11_badge_vez_de),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LetsSignTheme.colors.corAviso,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (isAguardando) {
                                Surface(
                                    shape = FormaPequena,
                                    color = LetsSignTheme.colors.corNeutroContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.adm11_badge_aguardando),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LetsSignTheme.colors.corNeutro,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Ações por participante quando pendente
                            if (part.situacao == SituacaoParticipante.PENDENTE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { viewModel.reenviarConvite(part.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.adm11_btn_reenviar_convite),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Link de Assinatura", "https://letssign.app/doc/${detalhe.documento.id}?part=${part.id}")
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.definirFeedback(context.getString(R.string.adm11_sucesso_link_copiado))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.adm11_btn_copiar_link),
                                            style = MaterialTheme.typography.labelMedium
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

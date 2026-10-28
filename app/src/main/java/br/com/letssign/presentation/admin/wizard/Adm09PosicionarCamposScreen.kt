package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.BotaoTonal
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.presentation.admin.wizard.modals.Mod03AtribuirCampoModal
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog

/**
 * ADM-09 — Posicionar campos de assinatura sobre as páginas do PDF (Etapas 7–8).
 */
@Composable
fun Adm09PosicionarCamposScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaRevisao: () -> Unit,
    aoVoltarParaOrdem: () -> Unit,
    aoSairParaInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.mostrarModalSair) {
        Mod04SairAssistenteDialog(
            aoSalvarRascunho = { viewModel.salvarRascunhoManual(aoSairParaInicio) },
            aoDescartar = { viewModel.descartarRascunhoManual(aoSairParaInicio) },
            aoContinuarEditando = { viewModel.fecharModalSair() }
        )
    }

    if (uiState.mostrarModalAtribuirCampo && uiState.campoSelecionadoId != null) {
        val campoSel = uiState.campos.find { it.id == uiState.campoSelecionadoId }
        Mod03AtribuirCampoModal(
            participantes = uiState.participantesOrdenados,
            papeis = uiState.papelPorSignatario,
            campos = uiState.campos,
            signatarioAtualId = campoSel?.signatarioId,
            aoSelecionarSignatario = { novoSignatarioId ->
                uiState.campoSelecionadoId?.let { cid ->
                    viewModel.atribuirCampo(cid, novoSignatarioId)
                }
            },
            aoFechar = { viewModel.fecharModalAtribuirCampo() }
        )
    }

    WizardScaffold(
        etapaAtual = 7,
        tituloTopo = stringResource(R.string.adm09_titulo),
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaOrdem()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            if (viewModel.avancarEtapa()) {
                aoAvancarParaRevisao()
            }
        },
        podeAvancar = uiState.todosParticipantesTemCampo,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status de atribuição e botão Adicionar campo
            Surface(
                color = LetsSignTheme.colors.corSuperficie,
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.adm09_titulo),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )

                        BotaoTonal(
                            texto = stringResource(R.string.adm09_btn_adicionar_campo),
                            aoClicar = { viewModel.adicionarCampo() },
                            icone = Icons.Default.Add
                        )
                    }

                    // Banner de status dos campos
                    if (uiState.participantesSemCampo.isNotEmpty()) {
                        val nomesSemCampo = uiState.participantesSemCampo.mapNotNull { id ->
                            uiState.signatariosCadastrados.find { it.signatarioId == id }?.nome
                        }.joinToString(", ")

                        BannerAviso(
                            mensagem = stringResource(R.string.adm09_badge_pendencias_formato, nomesSemCampo)
                        )
                    } else {
                        Surface(
                            shape = FormaPequena,
                            color = LetsSignTheme.colors.corSucesso.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corSucesso)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LetsSignTheme.colors.corSucesso,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.adm09_todos_posicionados),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corSucesso
                                )
                            }
                        }
                    }
                }
            }

            // Visualizador PDF com camada de campos
            VisualizadorPdfCamadas(
                totalPaginas = uiState.pdfTotalPaginas.coerceAtLeast(1),
                paginaAtual = uiState.paginaAtualPdf,
                aoMudarPagina = { novaPagina -> viewModel.mudarPaginaPdf(novaPagina) },
                modo = ModoVisualizadorPdf.EDICAO,
                modifier = Modifier.fillMaxWidth(),
                camadaCampos = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.camposNaPaginaAtual.forEach { campo ->
                            val signatario = uiState.signatariosCadastrados.find { it.signatarioId == campo.signatarioId }
                            val isSelecionado = campo.id == uiState.campoSelecionadoId

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CampoAssinaturaSobreposto(
                                    nomeSignatario = signatario?.nome ?: stringResource(R.string.adm06_titulo),
                                    estado = if (isSelecionado) EstadoCampoAssinatura.SELECIONADO else EstadoCampoAssinatura.VAZIO,
                                    modifier = Modifier.clickable { viewModel.selecionarCampo(campo.id) }
                                )

                                // Barra flutuante de ações quando o campo está selecionado
                                if (isSelecionado) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = FormaMedia,
                                        color = LetsSignTheme.colors.corSuperficie,
                                        shadowElevation = LetsSignTheme.elevation.nivel2,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corBorda)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { viewModel.abrirModalAtribuirCampo() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(R.string.adm09_btn_atribuir_campo),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }

                                            TextButton(
                                                onClick = { viewModel.excluirCampo(campo.id) },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = LetsSignTheme.colors.corErro
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(R.string.adm09_btn_excluir_campo),
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
            )
        }
    }
}

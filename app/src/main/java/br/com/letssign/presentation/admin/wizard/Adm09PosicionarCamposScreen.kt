package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.BotaoTonal
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.presentation.admin.wizard.modals.Mod03AtribuirCampoModal
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog
import kotlin.math.roundToInt

/**
 * ADM-09 — Posicionar campos de assinatura sobre as páginas do PDF (Etapa 7).
 *
 * Funcionalidades:
 * - Adicionar campo de assinatura para o signatário;
 * - Escolher a página e arrastar o campo até a posição desejada via gestos de toque (drag);
 * - Mover entre páginas ou excluir campos antes do envio;
 * - Posicionar mais de um campo para o mesmo signatário (ex: locador e fiador);
 * - Salvar página, posição normalizada (normX, normY) e dimensões (normLargura, normAltura)
 *   preservando o posicionamento exato em qualquer dispositivo.
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
                    val papelPadrao = uiState.papelPorSignatario[novoSignatarioId] ?: PapelParticipante.CONTRATADO
                    viewModel.atribuirCampo(cid, novoSignatarioId, papelPadrao)
                }
            },
            aoAlterarPapel = { sigId, novoPapel ->
                viewModel.definirPapel(sigId, novoPapel)
                uiState.campoSelecionadoId?.let { cid ->
                    viewModel.definirPapelCampo(cid, novoPapel)
                }
            },
            aoFechar = { viewModel.fecharModalAtribuirCampo() }
        )
    }

    val campoSelecionado = uiState.campos.find { it.id == uiState.campoSelecionadoId }

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
            // Cabeçalho de status de atribuição e botão Adicionar campo
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
                        Column {
                            Text(
                                text = stringResource(R.string.adm09_titulo),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            Text(
                                text = "Arraste o campo na página para posicionar",
                                style = MaterialTheme.typography.bodySmall,
                                color = LetsSignTheme.colors.corTextoSecundario
                            )
                        }

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

            // Painel de ação flutuante para o campo atualmente selecionado
            if (campoSelecionado != null) {
                val signatarioSel = uiState.signatariosCadastrados.find { it.signatarioId == campoSelecionado.signatarioId }
                val papelSel = campoSelecionado.papel ?: uiState.papelPorSignatario[campoSelecionado.signatarioId]

                Card(
                    shape = FormaMedia,
                    colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                    elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Campo Selecionado: ${signatarioSel?.nome ?: "Signatário"}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Text(
                                    text = "Papel: ${papelSel?.rotulo ?: "Padrão"} • Pág. ${campoSelecionado.numeroPagina} • Pos: ${(campoSelecionado.normX * 100).toInt()}%, ${(campoSelecionado.normY * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corPrimaria
                                )
                            }

                            IconButton(
                                onClick = { viewModel.excluirCampo(campoSelecionado.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir campo",
                                    tint = LetsSignTheme.colors.corErro
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.abrirModalAtribuirCampo() },
                                modifier = Modifier.weight(1f),
                                shape = FormaPequena
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Atribuir / Papel",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (uiState.pdfTotalPaginas > 1) {
                                OutlinedButton(
                                    onClick = {
                                        val proximaPag = if (campoSelecionado.numeroPagina >= uiState.pdfTotalPaginas) 1 else campoSelecionado.numeroPagina + 1
                                        viewModel.moverCampoParaPagina(campoSelecionado.id, proximaPag)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = FormaPequena
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenWith,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mudar Página",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Visualizador PDF com camada sobreposta interativa de campos
            VisualizadorPdfCamadas(
                totalPaginas = uiState.pdfTotalPaginas.coerceAtLeast(1),
                paginaAtual = uiState.paginaAtualPdf,
                aoMudarPagina = { novaPagina -> viewModel.mudarPaginaPdf(novaPagina) },
                modo = ModoVisualizadorPdf.EDICAO,
                modifier = Modifier.fillMaxWidth(),
                camadaCampos = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val larguraTotalPx = constraints.maxWidth.toFloat()
                        val alturaTotalPx = constraints.maxHeight.toFloat()
                        val densidade = LocalDensity.current

                        uiState.camposNaPaginaAtual.forEach { campo ->
                            val signatario = uiState.signatariosCadastrados.find { it.signatarioId == campo.signatarioId }
                            val papelDoCampo = campo.papel ?: uiState.papelPorSignatario[campo.signatarioId]
                            val isSelecionado = campo.id == uiState.campoSelecionadoId

                            val larguraDp = with(densidade) { (larguraTotalPx * campo.normLargura).toDp() }.coerceIn(130.dp, 280.dp)
                            val alturaDp = with(densidade) { (alturaTotalPx * campo.normAltura).toDp() }.coerceIn(50.dp, 84.dp)

                            val posX = (campo.normX * larguraTotalPx)
                            val posY = (campo.normY * alturaTotalPx)

                            val textoExibicao = if (signatario != null) {
                                if (papelDoCampo != null) "${signatario.nome} (${papelDoCampo.rotulo})" else signatario.nome
                            } else {
                                stringResource(R.string.adm06_titulo)
                            }

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                                    .pointerInput(campo.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val novoNormX = campo.normX + (dragAmount.x / larguraTotalPx)
                                            val novoNormY = campo.normY + (dragAmount.y / alturaTotalPx)
                                            viewModel.atualizarPosicaoCampo(campo.id, novoNormX, novoNormY)
                                        }
                                    }
                                    .clickable { viewModel.selecionarCampo(campo.id) }
                            ) {
                                CampoAssinaturaSobreposto(
                                    nomeSignatario = textoExibicao,
                                    estado = if (isSelecionado) EstadoCampoAssinatura.SELECIONADO else EstadoCampoAssinatura.VAZIO,
                                    largura = larguraDp,
                                    altura = alturaDp
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

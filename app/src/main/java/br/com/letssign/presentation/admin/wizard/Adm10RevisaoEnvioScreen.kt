package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.reusable.BadgePapel
import br.com.letssign.core.designsystem.components.reusable.CampoAssinaturaSobreposto
import br.com.letssign.core.designsystem.components.reusable.EstadoCampoAssinatura
import br.com.letssign.core.designsystem.components.reusable.ModoVisualizadorPdf
import br.com.letssign.core.designsystem.components.reusable.VisualizadorPdfCamadas
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.domain.model.TipoOrdem
import br.com.letssign.presentation.admin.wizard.modals.Mod01ResultadoEnvioModal
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog
import kotlin.math.roundToInt

/**
 * ADM-10 — Revisão geral e disparo do documento para assinatura (Etapa 9).
 *
 * Apresenta:
 * 1. Documento final: PDF com todas as páginas e campos posicionados e dimensionados com coordenadas normalizadas;
 * 2. Lista de signatários que vão receber o documento, com dados, papéis e campos atribuídos;
 * 3. Campos e páginas de assinatura devidamente posicionados;
 * 4. Mensagem de convite personalizada por e-mail;
 * 5. Disparo com tratamento de resultado (Sucesso ou Orientação de Falha com retentativa idempotente sem duplicidades).
 */
@Composable
fun Adm10RevisaoEnvioScreen(
    viewModel: WizardNovoDocViewModel,
    aoFinalizarEnvioComSucesso: () -> Unit,
    aoVoltarParaCampos: () -> Unit,
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

    uiState.estadoModalResultado?.let { estado ->
        Mod01ResultadoEnvioModal(
            estado = estado,
            aoConcluirSucesso = {
                viewModel.fecharModalResultado()
                aoFinalizarEnvioComSucesso()
            },
            aoTentarNovamente = {
                viewModel.enviarDocumento()
            },
            aoVoltarInicio = {
                viewModel.fecharModalResultado()
                aoSairParaInicio()
            }
        )
    }

    WizardScaffold(
        etapaAtual = 9,
        tituloTopo = stringResource(R.string.adm10_titulo),
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaCampos()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = { viewModel.enviarDocumento() },
        podeAvancar = true,
        textoAvancar = stringResource(R.string.adm10_btn_enviar),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Resumo do Documento e Ordem
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                        Text(
                            text = stringResource(R.string.adm10_secao_resumo),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                    Text(
                        text = uiState.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )

                    val nomeArquivo = uiState.pdfNome ?: "documento.pdf"
                    Text(
                        text = stringResource(R.string.adm10_resumo_arquivo_formato, nomeArquivo, uiState.pdfTotalPaginas.coerceAtLeast(1)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )

                    Text(
                        text = stringResource(R.string.adm10_campos_total_formato, uiState.campos.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )

                    val tipoOrdemTexto = if (uiState.tipoOrdem == TipoOrdem.PARALELA) {
                        stringResource(R.string.adm08_opcao_paralela)
                    } else {
                        stringResource(R.string.adm08_opcao_sequencial)
                    }
                    Text(
                        text = "${stringResource(R.string.adm08_titulo)}: $tipoOrdemTexto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }

            // Card 2: Documento Final com Prévia do PDF e Campos de Assinatura Posicionados
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                        Text(
                            text = "Documento Final e Campos Posicionados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    Text(
                        text = "Revise as páginas do documento e a disposição exata dos campos de assinatura configurados para cada signatário.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )

                    HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                    // Visualizador do documento no modo de leitura com sobreposição de campos
                    VisualizadorPdfCamadas(
                        totalPaginas = uiState.pdfTotalPaginas.coerceAtLeast(1),
                        paginaAtual = uiState.paginaAtualPdf,
                        aoMudarPagina = { novaPagina -> viewModel.mudarPaginaPdf(novaPagina) },
                        modo = ModoVisualizadorPdf.LEITURA,
                        modifier = Modifier.fillMaxWidth(),
                        pdfPath = uiState.pdfPath,
                        tituloDocumento = uiState.titulo,
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

                                    val larguraDp = with(densidade) { (larguraTotalPx * campo.normLargura).toDp() }.coerceAtLeast(80.dp)
                                    val alturaDp = with(densidade) { (alturaTotalPx * campo.normAltura).toDp() }.coerceAtLeast(36.dp)

                                    val posX = (campo.normX * larguraTotalPx)
                                    val posY = (campo.normY * alturaTotalPx)

                                    val textoExibicao = if (signatario != null) {
                                        if (papelDoCampo != null) "${signatario.nome} (${papelDoCampo.rotulo})" else signatario.nome
                                    } else {
                                        stringResource(R.string.adm06_titulo)
                                    }

                                    Box(
                                        modifier = Modifier.offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                                    ) {
                                        CampoAssinaturaSobreposto(
                                            nomeSignatario = textoExibicao,
                                            estado = EstadoCampoAssinatura.VAZIO,
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

            // Card 3: Lista de Signatários Convocados e Detalhamento de Campos
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                        Text(
                            text = stringResource(R.string.adm10_secao_participantes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                    uiState.participantesOrdenados.forEachIndexed { index, part ->
                        val papel = uiState.papelPorSignatario[part.signatarioId] ?: PapelParticipante.CONTRATADO
                        val obrigatorio = uiState.obrigatorioPorSignatario[part.signatarioId] ?: true
                        val camposDoPart = uiState.campos.filter { it.signatarioId == part.signatarioId }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(LetsSignTheme.colors.corPrimaria, shape = FormaTotal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}º",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corSuperficie
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Text(
                                    text = "${part.email} • ${part.telefone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BadgePapel(papel = papel)
                                    Text(
                                        text = if (obrigatorio) stringResource(R.string.adm07_switch_obrigatorio) else stringResource(R.string.adm01_filtro_todos),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (obrigatorio) LetsSignTheme.colors.corTextoPrincipal else LetsSignTheme.colors.corTextoSecundario
                                    )
                                    Text(
                                        text = "• ${stringResource(R.string.mod03_qtd_campos_formato, camposDoPart.size)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LetsSignTheme.colors.corPrimaria
                                    )
                                }

                                if (camposDoPart.isNotEmpty()) {
                                    val detalheCampos = camposDoPart.joinToString(", ") { c ->
                                        val p = c.papel ?: papel
                                        "Pág. ${c.numeroPagina} (${p.rotulo})"
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Campos: $detalheCampos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                }
                            }
                        }

                        if (index < uiState.participantesOrdenados.size - 1) {
                            HorizontalDivider(
                                color = LetsSignTheme.colors.corDivisor.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Card 4: Mensagem que Acompanhará o Convite por E-mail
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                        Text(
                            text = stringResource(R.string.adm10_label_mensagem),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    Text(
                        text = stringResource(R.string.adm10_dica_mensagem),
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )

                    CampoTextoContornado(
                        valor = uiState.mensagemConvite,
                        aoMudarValor = { viewModel.atualizarMensagemConvite(it) },
                        rotulo = stringResource(R.string.adm10_label_mensagem),
                        dicaAjuda = stringResource(R.string.adm10_dica_mensagem),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Ações de Disparo
            BotaoPrincipal(
                texto = stringResource(R.string.adm10_btn_enviar),
                aoClicar = { viewModel.enviarDocumento() },
                icone = Icons.Default.Send,
                modifier = Modifier.fillMaxWidth()
            )

            BotaoSecundario(
                texto = stringResource(R.string.mod04_btn_salvar_rascunho),
                aoClicar = { viewModel.salvarRascunhoManual(aoSairParaInicio) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

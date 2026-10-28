package br.com.letssign.presentation.admin.wizard

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.BotaoTonal
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog
import br.com.letssign.presentation.admin.wizard.modals.Mod06ErroPdfDialog

/**
 * ADM-05 — Selecionar PDF (Etapa 3).
 */
@Composable
fun Adm05SelecionarPdfScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaSignatarios: () -> Unit,
    aoVoltarParaTitulo: () -> Unit,
    aoSairParaInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcherPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.validarECarregarPdf(context, it) }
    }

    if (uiState.erroPdf != null) {
        Mod06ErroPdfDialog(
            tipoErro = uiState.erroPdf!!,
            aoEscolherOutroArquivo = {
                viewModel.fecharModalErroPdf()
                launcherPdf.launch("application/pdf")
            }
        )
    }

    if (uiState.mostrarModalSair) {
        Mod04SairAssistenteDialog(
            aoSalvarRascunho = { viewModel.salvarRascunhoManual(aoSairParaInicio) },
            aoDescartar = { viewModel.descartarRascunhoManual(aoSairParaInicio) },
            aoContinuarEditando = { viewModel.fecharModalSair() }
        )
    }

    WizardScaffold(
        etapaAtual = 2,
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaTitulo()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            if (viewModel.avancarEtapa()) {
                aoAvancarParaSignatarios()
            }
        },
        podeAvancar = uiState.podeAvancarEtapa2,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.carregando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IndicadorCarregamentoCircular(
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (uiState.pdfPath == null) {
                // Card de upload
                Surface(
                    shape = FormaMedia,
                    color = LetsSignTheme.colors.corSuperficie,
                    border = BorderStroke(1.dp, LetsSignTheme.colors.corBorda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corPrimaria,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = stringResource(R.string.adm05_requisitos_pdf),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario,
                            textAlign = TextAlign.Center
                        )

                        BotaoTonal(
                            texto = stringResource(R.string.adm05_btn_escolher_pdf),
                            aoClicar = { launcherPdf.launch("application/pdf") }
                        )
                    }
                }

                BannerAviso(
                    texto = stringResource(R.string.adm05_aviso_obrigatorio)
                )
            } else {
                // Card do PDF carregado com sucesso
                Surface(
                    shape = FormaMedia,
                    color = LetsSignTheme.colors.corSuperficie,
                    shadowElevation = LetsSignTheme.elevation.nivel1,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF Carregado",
                                tint = LetsSignTheme.colors.corPrimaria,
                                modifier = Modifier.size(40.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.pdfNome ?: "documento.pdf",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                val tamanhoMb = String.format("%.1f MB", uiState.pdfTamanhoBytes / (1024.0 * 1024.0))
                                Text(
                                    text = stringResource(R.string.adm05_card_paginas_formato, uiState.pdfTotalPaginas, tamanhoMb),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }

                        Divider(color = LetsSignTheme.colors.corDivisor)

                        BotaoSecundario(
                            texto = stringResource(R.string.adm05_btn_substituir_pdf),
                            aoClicar = { launcherPdf.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

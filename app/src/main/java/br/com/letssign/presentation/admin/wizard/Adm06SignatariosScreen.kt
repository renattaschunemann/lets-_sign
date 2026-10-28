package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.BotaoTonal
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.presentation.admin.wizard.modals.Mod02NovoSignatarioModal
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog

/**
 * ADM-06 — Selecionar signatários (Etapa 4).
 */
@Composable
fun Adm06SignatariosScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaPapeis: () -> Unit,
    aoVoltarParaPdf: () -> Unit,
    aoSairParaInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.mostrarModalNovoSignatario) {
        Mod02NovoSignatarioModal(
            aoSalvar = { nome, email, telefone, dataNascimento ->
                viewModel.salvarNovoSignatarioRapido(nome, email, telefone, dataNascimento)
            },
            aoDispensar = { viewModel.fecharModalNovoSignatario() }
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
        etapaAtual = 3,
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaPdf()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            if (viewModel.avancarEtapa()) {
                aoAvancarParaPapeis()
            }
        },
        podeAvancar = uiState.podeAvancarEtapa3,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.adm06_selecionados_formato, uiState.signatariosSelecionadosIds.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )

                BotaoTonal(
                    texto = stringResource(R.string.adm06_btn_novo_signatario),
                    aoClicar = { viewModel.abrirModalNovoSignatario() },
                    icone = Icons.Default.Add
                )
            }

            // Campo de busca
            CampoTextoContornado(
                valor = uiState.buscaSignatarios,
                aoMudarValor = { viewModel.atualizarBuscaSignatarios(it) },
                rotulo = stringResource(R.string.adm06_busca_placeholder),
                iconeInicio = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corTextoSecundario
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.signatariosSelecionadosIds.isEmpty()) {
                BannerAviso(
                    mensagem = stringResource(R.string.adm06_aviso_minimo)
                )
            }

            // Lista de signatários cadastrados
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.signatariosFiltrados, key = { it.signatarioId }) { signatario ->
                    val isSelecionado = uiState.signatariosSelecionadosIds.contains(signatario.signatarioId)
                    Surface(
                        shape = FormaMedia,
                        color = if (isSelecionado) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.25f) else LetsSignTheme.colors.corSuperficie,
                        border = if (isSelecionado) BorderStroke(1.5.dp, LetsSignTheme.colors.corPrimaria) else BorderStroke(1.dp, LetsSignTheme.colors.corBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.alternarSelecaoSignatario(signatario.signatarioId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(
                                checked = isSelecionado,
                                onCheckedChange = { viewModel.alternarSelecaoSignatario(signatario.signatarioId) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LetsSignTheme.colors.corPrimaria
                                )
                            )

                            // Avatar com iniciais
                            val iniciais = signatario.nome
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .map { it.first().uppercaseChar() }
                                .joinToString("")
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color = LetsSignTheme.colors.corPrimariaContainer, shape = FormaTotal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = iniciais,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corSobrePrimariaContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = signatario.nome,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Text(
                                    text = signatario.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog

/**
 * ADM-07 — Definir papéis e obrigatoriedade (Etapa 5).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm07ObrigatoriedadeScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaOrdem: () -> Unit,
    aoVoltarParaSignatarios: () -> Unit,
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

    WizardScaffold(
        etapaAtual = 4,
        aoVoltar = {
            viewModel.voltarEtapa(aoSairParaInicio)
            aoVoltarParaSignatarios()
        },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            if (viewModel.avancarEtapa()) {
                aoAvancarParaOrdem()
            }
        },
        podeAvancar = uiState.podeAvancarEtapa4,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.podeAvancarEtapa4) {
                BannerAviso(
                    texto = stringResource(R.string.adm07_aviso_minimo_obrigatorio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.participantesOrdenados, key = { it.signatarioId }) { signatario ->
                    val papelAtual = uiState.papelPorSignatario[signatario.signatarioId] ?: PapelParticipante.CONTRATADO
                    val obrigatorio = uiState.obrigatorioPorSignatario[signatario.signatarioId] != false

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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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

                            Divider(color = LetsSignTheme.colors.corDivisor)

                            // Dropdown de Papel
                            var expandido by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandido,
                                onExpandedChange = { expandido = !expandido },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = when (papelAtual) {
                                        PapelParticipante.CONTRATANTE -> "Contratante"
                                        PapelParticipante.CONTRATADO -> "Contratado"
                                        PapelParticipante.TESTEMUNHA -> "Testemunha"
                                        PapelParticipante.INTERVENIENTE -> "Interveniente"
                                        PapelParticipante.OUTRO -> "Outro"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.adm07_label_papel)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandido,
                                    onDismissRequest = { expandido = false }
                                ) {
                                    PapelParticipante.values().forEach { papel ->
                                        val rotulo = when (papel) {
                                            PapelParticipante.CONTRATANTE -> "Contratante"
                                            PapelParticipante.CONTRATADO -> "Contratado"
                                            PapelParticipante.TESTEMUNHA -> "Testemunha"
                                            PapelParticipante.INTERVENIENTE -> "Interveniente"
                                            PapelParticipante.OUTRO -> "Outro"
                                        }
                                        DropdownMenuItem(
                                            text = { Text(rotulo) },
                                            onClick = {
                                                viewModel.definirPapel(signatario.signatarioId, papel)
                                                expandido = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Switch de Obrigatoriedade
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.adm07_switch_obrigatorio),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Switch(
                                    checked = obrigatorio,
                                    onCheckedChange = { viewModel.alternarObrigatoriedade(signatario.signatarioId, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LetsSignTheme.colors.corPrimaria,
                                        checkedTrackColor = LetsSignTheme.colors.corPrimariaContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

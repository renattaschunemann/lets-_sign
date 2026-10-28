package br.com.letssign.presentation.admin.wizard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.presentation.admin.wizard.modals.Mod04SairAssistenteDialog
import br.com.letssign.presentation.admin.wizard.modals.Mod05RetomarRascunhoDialog

/**
 * ADM-04 — Início do assistente e nome do documento (Etapas 1–2).
 */
@Composable
fun Adm04NovoDocTituloScreen(
    viewModel: WizardNovoDocViewModel,
    aoAvancarParaPdf: () -> Unit,
    aoSairParaInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.mostrarModalRetomarRascunho) {
        Mod05RetomarRascunhoDialog(
            aoRetomar = { viewModel.retomarRascunhoPendente() },
            aoIniciarNovo = { viewModel.descartarRascunhoPendenteEIniciarNovo() },
            aoDispensar = { viewModel.descartarRascunhoPendenteEIniciarNovo() }
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
        etapaAtual = 1,
        aoVoltar = { viewModel.voltarEtapa(aoSairParaInicio) },
        aoFechar = { viewModel.abrirModalSair() },
        aoAvancar = {
            if (viewModel.avancarEtapa()) {
                aoAvancarParaPdf()
            }
        },
        podeAvancar = uiState.podeAvancarEtapa1,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoTextoContornado(
                valor = uiState.titulo,
                aoMudarValor = { viewModel.atualizarTitulo(it) },
                rotulo = stringResource(R.string.adm04_label_titulo_doc),
                dicaAjuda = stringResource(R.string.adm04_dica_titulo_doc),
                ehErro = uiState.titulo.isBlank(),
                mensagemErro = if (uiState.titulo.isBlank()) stringResource(R.string.adm04_erro_titulo_vazio) else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

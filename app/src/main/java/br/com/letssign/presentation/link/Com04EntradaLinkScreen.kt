package br.com.letssign.presentation.link

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.reusable.EstadoErro
import br.com.letssign.core.designsystem.components.reusable.PainelResultadoConteudo
import br.com.letssign.core.designsystem.components.reusable.TipoResultado
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import kotlinx.coroutines.delay

@Composable
fun Com04EntradaLinkScreen(
    tokenOuId: String = "",
    onNavigateToLogin: () -> Unit,
    onNavigateToSignatarioHome: () -> Unit,
    onNavigateToDetalheDoc: (String) -> Unit,
    onNavigateToVisualizadorLeitura: (String) -> Unit,
    viewModel: EntradaLinkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tokenOuId) {
        viewModel.processarToken(tokenOuId)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EntradaLinkUiState.ValidoRedirecionando -> {
                delay(800)
                onNavigateToDetalheDoc(state.documentoId)
            }
            is EntradaLinkUiState.JaAssinado -> {
                delay(1500)
                onNavigateToVisualizadorLeitura(state.documentoId)
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LetsSignTheme.colors.corFundo)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is EntradaLinkUiState.Carregando,
            is EntradaLinkUiState.ValidoRedirecionando -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = LetsSignTheme.colors.corPrimaria,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))
                    Text(
                        text = stringResource(R.string.com04_abrindo_documento),
                        style = LetsSignTheme.typography.bodyLarge,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }

            is EntradaLinkUiState.BloqueioOutraConta -> {
                PainelResultadoConteudo(
                    tipo = TipoResultado.ALERTA,
                    titulo = "Acesso restrito",
                    mensagem = stringResource(R.string.com04_bloqueio_outra_conta),
                    textoAcaoPrincipal = stringResource(R.string.btn_sair_e_entrar_outra_conta),
                    aoClicarAcaoPrincipal = {
                        viewModel.deslogarESair(onNavigateToLogin)
                    },
                    textoAcaoSecundaria = stringResource(R.string.btn_ir_para_inicio),
                    aoClicarAcaoSecundaria = onNavigateToSignatarioHome
                )
            }

            is EntradaLinkUiState.DocumentoCancelado -> {
                PainelResultadoConteudo(
                    tipo = TipoResultado.ERRO,
                    titulo = "Documento indisponível",
                    mensagem = stringResource(R.string.com04_doc_cancelado),
                    textoAcaoPrincipal = stringResource(R.string.btn_ir_para_inicio),
                    aoClicarAcaoPrincipal = onNavigateToSignatarioHome
                )
            }

            is EntradaLinkUiState.JaAssinado -> {
                PainelResultadoConteudo(
                    tipo = TipoResultado.SUCESSO,
                    titulo = "Documento assinado",
                    mensagem = stringResource(R.string.com04_ja_assinado),
                    textoAcaoPrincipal = stringResource(R.string.btn_ir_para_inicio),
                    aoClicarAcaoPrincipal = onNavigateToSignatarioHome
                )
            }

            is EntradaLinkUiState.LinkInvalido -> {
                PainelResultadoConteudo(
                    tipo = TipoResultado.ERRO,
                    titulo = "Link indisponível",
                    mensagem = stringResource(R.string.com04_link_invalido),
                    textoAcaoPrincipal = stringResource(R.string.btn_ir_para_inicio),
                    aoClicarAcaoPrincipal = onNavigateToSignatarioHome
                )
            }

            is EntradaLinkUiState.ErroConexao -> {
                EstadoErro(
                    mensagem = state.mensagem,
                    aoTentarNovamente = { viewModel.processarToken(tokenOuId) }
                )
            }
        }
    }
}

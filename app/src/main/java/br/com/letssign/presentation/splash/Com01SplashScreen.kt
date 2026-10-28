package br.com.letssign.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
import br.com.letssign.core.designsystem.components.logo.LogoPlaceholder
import br.com.letssign.core.designsystem.components.logo.LogoSize
import br.com.letssign.core.designsystem.components.reusable.EstadoErro
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun Com01SplashScreen(
    tokenLinkPendente: String? = null,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSignatario: () -> Unit,
    onNavigateToLink: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tokenLinkPendente) {
        viewModel.verificarSessao(tokenLinkPendente)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.RedirecionarLogin -> onNavigateToLogin()
            is SplashUiState.RedirecionarAdmin -> onNavigateToAdmin()
            is SplashUiState.RedirecionarSignatario -> onNavigateToSignatario()
            is SplashUiState.RedirecionarLink -> onNavigateToLink(state.token)
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LetsSignTheme.colors.corFundo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(LetsSignTheme.spacing.esp24)
        ) {
            LogoPlaceholder(tamanho = LogoSize.GRANDE) // 160 x 160 dp

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp32))

            when (uiState) {
                is SplashUiState.Carregando,
                is SplashUiState.RedirecionarAdmin,
                is SplashUiState.RedirecionarSignatario,
                is SplashUiState.RedirecionarLogin,
                is SplashUiState.RedirecionarLink -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = LetsSignTheme.colors.corPrimaria,
                        strokeWidth = 4.dp
                    )
                }
                is SplashUiState.Erro -> {
                    EstadoErro(
                        mensagem = stringResource(R.string.com01_erro_iniciar),
                        aoTentarNovamente = { viewModel.verificarSessao(tokenLinkPendente) }
                    )
                }
            }
        }
    }
}

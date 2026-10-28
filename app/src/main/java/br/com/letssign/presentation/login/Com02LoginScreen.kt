package br.com.letssign.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerInformativo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.logo.LogoPlaceholder
import br.com.letssign.core.designsystem.components.logo.LogoSize
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.domain.model.PerfilUsuario

@Composable
fun Com02LoginScreen(
    vindoDeLink: Boolean = false,
    onNavigateToCadastroAdmin: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToSignatarioHome: () -> Unit,
    onNavigateToLink: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginSucessoPerfil) {
        val perfil = uiState.loginSucessoPerfil ?: return@LaunchedEffect
        if (vindoDeLink) {
            onNavigateToLink()
        } else {
            when (perfil) {
                PerfilUsuario.ADMIN -> onNavigateToAdminHome()
                PerfilUsuario.SIGNATARIO -> onNavigateToSignatarioHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LetsSignTheme.colors.corFundo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LetsSignTheme.spacing.esp16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp32))

        // Topo: LOGO — [INSERIR POSTERIORMENTE] em 120 x 48 dp
        LogoPlaceholder(tamanho = LogoSize.MEDIO)

        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp24))

        // Banner informativo condicional (variante "Vindo de link")
        if (vindoDeLink) {
            BannerInformativo(
                texto = stringResource(R.string.com02_banner_vindo_link),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))
        }

        // Campo E-mail
        CampoTextoContornado(
            valor = uiState.email,
            aoMudarValor = viewModel::onEmailChanged,
            rotulo = stringResource(R.string.com02_label_email),
            dicaAjuda = stringResource(R.string.com02_placeholder_email),
            mensagemErro = uiState.erroFormatoEmail,
            habilitado = !uiState.estaCarregando && !uiState.estaBloqueado,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

        // Campo Senha
        CampoTextoContornado(
            valor = uiState.senha,
            aoMudarValor = viewModel::onSenhaChanged,
            rotulo = stringResource(R.string.com02_label_senha),
            ehSenha = true,
            habilitado = !uiState.estaCarregando && !uiState.estaBloqueado,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.entrar()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Mensagens de erro de credenciais, conexão ou bloqueio
        val mensagemErro = uiState.erroCredenciais ?: uiState.erroConexao
        if (mensagemErro != null) {
            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp12))
            Text(
                text = mensagemErro,
                style = LetsSignTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corErro,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp24))

        // Botão principal "Entrar" (largura total)
        BotaoPrincipal(
            texto = stringResource(R.string.btn_entrar),
            onClick = {
                focusManager.clearFocus()
                viewModel.entrar()
            },
            carregando = uiState.estaCarregando,
            habilitado = !uiState.estaBloqueado,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

        // Botão secundário de texto "Criar conta de administrador"
        BotaoTexto(
            texto = stringResource(R.string.btn_criar_conta_admin),
            onClick = onNavigateToCadastroAdmin,
            habilitado = !uiState.estaCarregando
        )

        Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp32))
    }
}

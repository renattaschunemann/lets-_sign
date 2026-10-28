package br.com.letssign.presentation.cadastro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.logo.LogoPlaceholder
import br.com.letssign.core.designsystem.components.logo.LogoSize
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Com03CadastroAdminScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    viewModel: CadastroAdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val sucessoMsg = stringResource(R.string.com03_sucesso_conta_criada)

    LaunchedEffect(uiState.cadastroSucesso) {
        if (uiState.cadastroSucesso) {
            snackbarHostState.showSnackbar(sucessoMsg)
            delay(1000)
            onNavigateToAdminHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.com03_titulo),
                        style = LetsSignTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LetsSignTheme.colors.corFundo
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LetsSignTheme.spacing.esp16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // Logo centralizado 120 x 48 dp
            LogoPlaceholder(tamanho = LogoSize.MEDIO)

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp24))

            // Nome completo
            CampoTextoContornado(
                valor = uiState.nome,
                aoMudarValor = viewModel::onNomeChanged,
                rotulo = stringResource(R.string.com03_label_nome),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // E-mail
            CampoTextoContornado(
                valor = uiState.email,
                aoMudarValor = viewModel::onEmailChanged,
                rotulo = stringResource(R.string.com03_label_email),
                mensagemErro = if (uiState.erroEmailEmUso) stringResource(R.string.com03_erro_email_em_uso) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.erroEmailEmUso) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = LetsSignTheme.spacing.esp4),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = stringResource(R.string.com03_link_faca_login),
                        style = LetsSignTheme.typography.labelLarge.copy(color = LetsSignTheme.colors.corSecundaria),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // Telefone
            CampoTextoContornado(
                valor = uiState.telefone,
                aoMudarValor = viewModel::onTelefoneChanged,
                rotulo = stringResource(R.string.com03_label_telefone),
                dicaAjuda = "(11) 98765-4321",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // Senha
            CampoTextoContornado(
                valor = uiState.senha,
                aoMudarValor = viewModel::onSenhaChanged,
                rotulo = stringResource(R.string.com03_label_senha),
                ehSenha = true,
                mensagemErro = if (uiState.erroSenhaRequisitos) stringResource(R.string.com03_ajuda_senha) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            // Indicador de requisitos de senha visível antes de erro
            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp8))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.com03_ajuda_senha),
                    style = LetsSignTheme.typography.bodySmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
                Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp4))
                RequisitoSenhaItem(
                    texto = "Pelo menos 8 caracteres",
                    atendido = uiState.temPeloMenos8Caracteres
                )
                RequisitoSenhaItem(
                    texto = "Contém letras",
                    atendido = uiState.temLetras
                )
                RequisitoSenhaItem(
                    texto = "Contém números",
                    atendido = uiState.temNumeros
                )
            }

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // Confirmação de senha
            CampoTextoContornado(
                valor = uiState.confirmacaoSenha,
                aoMudarValor = viewModel::onConfirmacaoSenhaChanged,
                rotulo = stringResource(R.string.com03_label_confirmar_senha),
                ehSenha = true,
                mensagemErro = if (uiState.erroSenhasDiferentes) stringResource(R.string.com03_erro_senhas_diferentes) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.criarConta()
                }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp16))

            // Aceite de termos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onTermosChanged(!uiState.termosAceitos) }
                    .padding(vertical = LetsSignTheme.spacing.esp4)
            ) {
                Checkbox(
                    checked = uiState.termosAceitos,
                    onCheckedChange = viewModel::onTermosChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = LetsSignTheme.colors.corPrimaria,
                        checkmarkColor = LetsSignTheme.colors.corSobrePrimaria
                    )
                )
                Spacer(modifier = Modifier.width(LetsSignTheme.spacing.esp8))
                Text(
                    text = stringResource(R.string.com03_termos_uso),
                    style = LetsSignTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
            }

            uiState.erroConexao?.let { erro ->
                Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp12))
                Text(
                    text = erro,
                    style = LetsSignTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corErro,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp24))

            // Botão Criar conta
            BotaoPrincipal(
                texto = stringResource(R.string.btn_criar_conta),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.criarConta()
                },
                carregando = uiState.estaCarregando,
                habilitado = uiState.termosAceitos && uiState.senhaValida,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LetsSignTheme.spacing.esp32))
        }
    }
}

@Composable
private fun RequisitoSenhaItem(texto: String, atendido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (atendido) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (atendido) LetsSignTheme.colors.corSucesso else LetsSignTheme.colors.corTextoDesabilitado,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(LetsSignTheme.spacing.esp4))
        Text(
            text = texto,
            style = LetsSignTheme.typography.labelSmall,
            color = if (atendido) LetsSignTheme.colors.corSucesso else LetsSignTheme.colors.corTextoSecundario
        )
    }
}

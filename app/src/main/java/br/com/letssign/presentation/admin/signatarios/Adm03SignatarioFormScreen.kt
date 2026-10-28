package br.com.letssign.presentation.admin.signatarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerInformativo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm03SignatarioFormScreen(
    aoVoltar: () -> Unit,
    aoConcluirComSucesso: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm03SignatarioFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.salvoComSucesso) {
        if (uiState.salvoComSucesso) {
            aoConcluirComSucesso()
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val formatada = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            viewModel.aoAlterarDataNascimento(formatada)
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.btn_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text(stringResource(R.string.cd_voltar))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.ehEdicao) R.string.adm03_titulo_edicao
                            else R.string.adm03_titulo_novo
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
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
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LetsSignTheme.colors.corSuperficie,
                tonalElevation = LetsSignTheme.elevation.nivel2
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    BotaoPrincipal(
                        texto = stringResource(R.string.btn_salvar),
                        onClick = viewModel::aoSalvar,
                        carregando = uiState.salvando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = LetsSignTheme.colors.corFundo
    ) { paddingVal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVal)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner informativo
            BannerInformativo(
                texto = stringResource(R.string.adm03_banner_info)
            )

            // Erro geral se houver
            if (uiState.erroGeral != null) {
                Text(
                    text = uiState.erroGeral ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corErro
                )
            }

            // Campo Nome Completo
            CampoTextoContornado(
                valor = uiState.nome,
                aoMudarValor = viewModel::aoAlterarNome,
                rotulo = stringResource(R.string.adm03_label_nome),
                mensagemErro = uiState.erroNome,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo E-mail
            CampoTextoContornado(
                valor = uiState.email,
                aoMudarValor = viewModel::aoAlterarEmail,
                rotulo = stringResource(R.string.adm03_label_email),
                mensagemErro = uiState.erroEmail,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Telefone
            CampoTextoContornado(
                valor = uiState.telefone,
                aoMudarValor = viewModel::aoAlterarTelefone,
                rotulo = stringResource(R.string.adm03_label_telefone),
                mensagemErro = uiState.erroTelefone,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Data de Nascimento
            CampoTextoContornado(
                valor = uiState.dataNascimento,
                aoMudarValor = viewModel::aoAlterarDataNascimento,
                rotulo = stringResource(R.string.adm03_label_nascimento),
                mensagemErro = uiState.erroDataNascimento,
                iconeFim = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = stringResource(R.string.adm03_label_nascimento),
                            tint = LetsSignTheme.colors.corPrimaria
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Regra Senha / Edição
            if (uiState.ehEdicao) {
                Text(
                    text = stringResource(R.string.adm03_aviso_edicao_proximos),
                    style = MaterialTheme.typography.bodySmall,
                    color = LetsSignTheme.colors.corTextoDesabilitado
                )
                BotaoSecundario(
                    texto = stringResource(R.string.btn_redefinir_senha),
                    onClick = { /* Redefinir senha */ },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                if (uiState.emailJaExiste) {
                    Text(
                        text = stringResource(R.string.adm03_aviso_email_existente),
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                } else {
                    CampoTextoContornado(
                        valor = uiState.senha,
                        aoMudarValor = viewModel::aoAlterarSenha,
                        rotulo = stringResource(R.string.adm03_label_senha),
                        ehSenha = true,
                        dicaAjuda = stringResource(R.string.com03_ajuda_senha),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package br.com.letssign.presentation.admin.signatarios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Contacts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.common.contacts.AgendaContatosMockHelper
import br.com.letssign.core.designsystem.components.reusable.ModalSeletorContatosAgenda
import br.com.letssign.core.common.transformation.CepVisualTransformation
import br.com.letssign.core.common.transformation.CpfCnpjVisualTransformation
import br.com.letssign.core.common.transformation.DataNascimentoVisualTransformation
import br.com.letssign.core.common.transformation.TelefoneVisualTransformation
import br.com.letssign.core.designsystem.components.base.BannerInformativo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import java.time.Instant
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
    var mostrarModalContatosAgenda by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcherContato = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var nomeContato = ""
                var telefoneContato = ""
                var emailContato = ""
                var contactId = ""

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (idIndex != -1) contactId = cursor.getString(idIndex) ?: ""
                        if (nameIndex != -1) nomeContato = cursor.getString(nameIndex) ?: ""
                    }
                }

                if (contactId.isNotBlank()) {
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val numIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIndex != -1) {
                                telefoneContato = phoneCursor.getString(numIndex) ?: ""
                            }
                        }
                    }

                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { emailCursor ->
                        if (emailCursor.moveToFirst()) {
                            val mailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            if (mailIndex != -1) {
                                emailContato = emailCursor.getString(mailIndex) ?: ""
                            }
                        }
                    }
                }

                viewModel.preencherDadosImportados(
                    nome = nomeContato,
                    email = emailContato,
                    telefone = telefoneContato
                )
            } catch (_: Exception) {}
        }
    }

    val cpfCnpjTransformation = remember(uiState.tipoPessoa) {
        CpfCnpjVisualTransformation(ehCnpj = uiState.tipoPessoa == "PJ")
    }
    val telefoneTransformation = remember { TelefoneVisualTransformation() }
    val dataNascTransformation = remember { DataNascimentoVisualTransformation() }
    val cepTransformation = remember { CepVisualTransformation() }

    LaunchedEffect(uiState.salvoComSucesso) {
        if (uiState.salvoComSucesso) {
            aoConcluirComSucesso()
        }
    }

    if (mostrarModalContatosAgenda) {
        ModalSeletorContatosAgenda(
            aoSelecionarContato = { n, e, t ->
                viewModel.preencherDadosImportados(nome = n, email = e, telefone = t)
            },
            aoAbrirSeletorSistema = { launcherContato.launch(null) },
            aoFechar = { mostrarModalContatosAgenda = false }
        )
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
                            val formatada = localDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
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
                actions = {
                    br.com.letssign.core.designsystem.components.logo.LogoPlaceholder(
                        tamanho = br.com.letssign.core.designsystem.components.logo.LogoSize.PEQUENO,
                        modifier = Modifier.padding(end = 12.dp)
                    )
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
            BannerInformativo(
                texto = stringResource(R.string.adm03_banner_info)
            )

            BotaoSecundario(
                texto = "Importar dos Contatos do Celular",
                onClick = {
                    AgendaContatosMockHelper.inserirContatosNoDispositivoSeNecessario(context)
                    mostrarModalContatosAgenda = true
                },
                icone = Icons.Default.Contacts,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.erroGeral != null) {
                Text(
                    text = uiState.erroGeral ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corErro
                )
            }

            // 1. Tipo de Pessoa (PF / PJ) com RadioButtons
            Text(
                text = "Classificação do Signatário",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.aoAlterarTipoPessoa("PF") }
                ) {
                    RadioButton(
                        selected = uiState.tipoPessoa == "PF",
                        onClick = { viewModel.aoAlterarTipoPessoa("PF") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pessoa Física (CPF)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.aoAlterarTipoPessoa("PJ") }
                ) {
                    RadioButton(
                        selected = uiState.tipoPessoa == "PJ",
                        onClick = { viewModel.aoAlterarTipoPessoa("PJ") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pessoa Jurídica (CNPJ)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }
            }

            // 2. Campo CPF ou CNPJ com máscara
            CampoTextoContornado(
                valor = uiState.cpfCnpj,
                aoMudarValor = viewModel::aoAlterarCpfCnpj,
                rotulo = if (uiState.tipoPessoa == "PJ") "CNPJ *" else "CPF *",
                visualTransformation = cpfCnpjTransformation,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                mensagemErro = uiState.erroCpfCnpj,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Campo Nome Completo ou Razão Social
            CampoTextoContornado(
                valor = uiState.nome,
                aoMudarValor = viewModel::aoAlterarNome,
                rotulo = if (uiState.tipoPessoa == "PJ") "Razão Social *" else stringResource(R.string.adm03_label_nome),
                mensagemErro = uiState.erroNome,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Campo E-mail
            CampoTextoContornado(
                valor = uiState.email,
                aoMudarValor = viewModel::aoAlterarEmail,
                rotulo = stringResource(R.string.adm03_label_email),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                mensagemErro = uiState.erroEmail,
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Campo Telefone com máscara
            CampoTextoContornado(
                valor = uiState.telefone,
                aoMudarValor = viewModel::aoAlterarTelefone,
                rotulo = stringResource(R.string.adm03_label_telefone),
                visualTransformation = telefoneTransformation,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                mensagemErro = uiState.erroTelefone,
                modifier = Modifier.fillMaxWidth()
            )

            // 6. Campo Data de Nascimento com máscara e DatePicker
            CampoTextoContornado(
                valor = uiState.dataNascimento,
                aoMudarValor = viewModel::aoAlterarDataNascimento,
                rotulo = if (uiState.tipoPessoa == "PJ") "Data de Fundação *" else stringResource(R.string.adm03_label_nascimento),
                visualTransformation = dataNascTransformation,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
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

            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

            // 7. Seção de Endereço Completo
            Text(
                text = "Endereço Completo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LetsSignTheme.colors.corTextoPrincipal
            )

            // Campo CEP com busca automática
            CampoTextoContornado(
                valor = uiState.cep,
                aoMudarValor = viewModel::aoAlterarCep,
                rotulo = "CEP *",
                visualTransformation = cepTransformation,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                mensagemErro = uiState.erroCep,
                iconeFim = {
                    if (uiState.buscandoCep) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = LetsSignTheme.colors.corPrimaria
                        )
                    } else {
                        IconButton(onClick = { viewModel.buscarCep() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar CEP",
                                tint = LetsSignTheme.colors.corPrimaria
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Logradouro
            CampoTextoContornado(
                valor = uiState.logradouro,
                aoMudarValor = viewModel::aoAlterarLogradouro,
                rotulo = "Logradouro (Rua, Av, etc.)",
                modifier = Modifier.fillMaxWidth()
            )

            // Número e Complemento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTextoContornado(
                    valor = uiState.numero,
                    aoMudarValor = viewModel::aoAlterarNumero,
                    rotulo = "Número",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                CampoTextoContornado(
                    valor = uiState.complemento,
                    aoMudarValor = viewModel::aoAlterarComplemento,
                    rotulo = "Complemento",
                    modifier = Modifier.weight(1.5f)
                )
            }

            // Bairro
            CampoTextoContornado(
                valor = uiState.bairro,
                aoMudarValor = viewModel::aoAlterarBairro,
                rotulo = "Bairro",
                modifier = Modifier.fillMaxWidth()
            )

            // Cidade e Estado (UF)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTextoContornado(
                    valor = uiState.cidade,
                    aoMudarValor = viewModel::aoAlterarCidade,
                    rotulo = "Cidade",
                    modifier = Modifier.weight(2f)
                )

                CampoTextoContornado(
                    valor = uiState.estado,
                    aoMudarValor = viewModel::aoAlterarEstado,
                    rotulo = "UF",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

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

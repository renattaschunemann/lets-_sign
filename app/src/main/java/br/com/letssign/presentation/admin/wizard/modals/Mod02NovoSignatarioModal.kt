package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import br.com.letssign.R
import br.com.letssign.core.common.contacts.AgendaContatosMockHelper
import br.com.letssign.core.designsystem.components.reusable.ModalSeletorContatosAgenda
import br.com.letssign.core.common.cep.CepService
import br.com.letssign.core.common.transformation.CepVisualTransformation
import br.com.letssign.core.common.transformation.CpfCnpjVisualTransformation
import br.com.letssign.core.common.transformation.DataNascimentoVisualTransformation
import br.com.letssign.core.common.transformation.TelefoneVisualTransformation
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * MOD-02 — Modal de cadastro rápido de signatário dentro do wizard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mod02NovoSignatarioModal(
    aoSalvar: (
        nome: String,
        email: String,
        telefone: String,
        dataNascimento: String,
        tipoPessoa: String,
        cpfCnpj: String,
        cep: String,
        logradouro: String,
        numero: String,
        complemento: String,
        bairro: String,
        cidade: String,
        estado: String
    ) -> Unit,
    aoDispensar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tipoPessoa by remember { mutableStateOf("PF") }
    var cpfCnpj by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }

    var cep by remember { mutableStateOf("") }
    var logradouro by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var complemento by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var buscandoCep by remember { mutableStateOf(false) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarModalContatosAgenda by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcherContato = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var contactId = ""
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (idIndex != -1) contactId = cursor.getString(idIndex) ?: ""
                        if (nameIndex != -1) nome = cursor.getString(nameIndex) ?: ""
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
                                val raw = phoneCursor.getString(numIndex) ?: ""
                                telefone = raw.filter { it.isDigit() }.take(11)
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
                                email = emailCursor.getString(mailIndex) ?: ""
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    var erroCpfCnpj by remember { mutableStateOf<String?>(null) }
    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroDataNascimento by remember { mutableStateOf<String?>(null) }
    var erroCep by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val cpfCnpjTransformation = remember(tipoPessoa) {
        CpfCnpjVisualTransformation(ehCnpj = tipoPessoa == "PJ")
    }
    val telefoneTransformation = remember { TelefoneVisualTransformation() }
    val dataNascTransformation = remember { DataNascimentoVisualTransformation() }
    val cepTransformation = remember { CepVisualTransformation() }

    if (mostrarModalContatosAgenda) {
        ModalSeletorContatosAgenda(
            aoSelecionarContato = { n, e, t ->
                nome = n
                email = e
                telefone = t.filter { it.isDigit() }.take(11)
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
                            val formatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            dataNascimento = formatter.format(Date(millis))
                            erroDataNascimento = null
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = aoDispensar,
        title = {
            Text(
                text = stringResource(R.string.mod02_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        AgendaContatosMockHelper.inserirContatosNoDispositivoSeNecessario(context)
                        mostrarModalContatosAgenda = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormaMedia
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Importar dos Contatos do Celular")
                }

                // 1. RadioGroup: Pessoa Física vs Pessoa Jurídica
                Text(
                    text = "Tipo de Signatário",
                    style = MaterialTheme.typography.labelLarge,
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
                        modifier = Modifier.clickable {
                            tipoPessoa = "PF"
                            cpfCnpj = cpfCnpj.take(11)
                            erroCpfCnpj = null
                        }
                    ) {
                        RadioButton(
                            selected = tipoPessoa == "PF",
                            onClick = {
                                tipoPessoa = "PF"
                                cpfCnpj = cpfCnpj.take(11)
                                erroCpfCnpj = null
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pessoa Física",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            tipoPessoa = "PJ"
                            erroCpfCnpj = null
                        }
                    ) {
                        RadioButton(
                            selected = tipoPessoa == "PJ",
                            onClick = {
                                tipoPessoa = "PJ"
                                erroCpfCnpj = null
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pessoa Jurídica",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                }

                // 2. Campo CPF / CNPJ com máscara
                CampoTextoContornado(
                    valor = cpfCnpj,
                    aoMudarValor = {
                        val maxLen = if (tipoPessoa == "PJ") 14 else 11
                        cpfCnpj = it.filter { c -> c.isDigit() }.take(maxLen)
                        erroCpfCnpj = null
                    },
                    rotulo = if (tipoPessoa == "PJ") "CNPJ *" else "CPF *",
                    visualTransformation = cpfCnpjTransformation,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    mensagemErro = erroCpfCnpj,
                    ehErro = erroCpfCnpj != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Campo Nome / Razão Social
                CampoTextoContornado(
                    valor = nome,
                    aoMudarValor = {
                        nome = it
                        erroNome = null
                    },
                    rotulo = if (tipoPessoa == "PJ") "Razão Social *" else stringResource(R.string.adm03_label_nome),
                    mensagemErro = erroNome,
                    ehErro = erroNome != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Campo E-mail
                CampoTextoContornado(
                    valor = email,
                    aoMudarValor = {
                        email = it
                        erroEmail = null
                    },
                    rotulo = stringResource(R.string.adm03_label_email),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                    mensagemErro = erroEmail,
                    ehErro = erroEmail != null,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Campo Telefone com máscara
                CampoTextoContornado(
                    valor = telefone,
                    aoMudarValor = { telefone = it.filter { c -> c.isDigit() }.take(11) },
                    rotulo = stringResource(R.string.adm03_label_telefone),
                    visualTransformation = telefoneTransformation,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Campo Data de Nascimento com máscara e DatePicker
                CampoTextoContornado(
                    valor = dataNascimento,
                    aoMudarValor = {
                        dataNascimento = it.filter { c -> c.isDigit() }.take(8)
                        erroDataNascimento = null
                    },
                    rotulo = if (tipoPessoa == "PJ") "Data de Fundação *" else stringResource(R.string.adm03_label_nascimento),
                    visualTransformation = dataNascTransformation,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    iconeFim = {
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Selecionar data",
                                tint = LetsSignTheme.colors.corPrimaria
                            )
                        }
                    },
                    mensagemErro = erroDataNascimento,
                    ehErro = erroDataNascimento != null,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                // 7. Seção de Endereço Completo
                Text(
                    text = "Endereço Completo",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )

                // Campo CEP com busca
                CampoTextoContornado(
                    valor = cep,
                    aoMudarValor = {
                        val digitos = it.filter { c -> c.isDigit() }.take(8)
                        cep = digitos
                        erroCep = null
                        if (digitos.length == 8) {
                            coroutineScope.launch {
                                buscandoCep = true
                                val res = CepService.buscarCep(digitos)
                                buscandoCep = false
                                res.onSuccess { end ->
                                    if (end.logradouro.isNotBlank()) logradouro = end.logradouro
                                    if (end.complemento.isNotBlank()) complemento = end.complemento
                                    if (end.bairro.isNotBlank()) bairro = end.bairro
                                    if (end.cidade.isNotBlank()) cidade = end.cidade
                                    if (end.estado.isNotBlank()) estado = end.estado
                                }.onFailure { e ->
                                    erroCep = e.message ?: "Erro ao buscar CEP"
                                }
                            }
                        }
                    },
                    rotulo = "CEP *",
                    visualTransformation = cepTransformation,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    mensagemErro = erroCep,
                    ehErro = erroCep != null,
                    iconeFim = {
                        if (buscandoCep) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = LetsSignTheme.colors.corPrimaria
                            )
                        } else {
                            IconButton(onClick = {
                                if (cep.length == 8) {
                                    coroutineScope.launch {
                                        buscandoCep = true
                                        val res = CepService.buscarCep(cep)
                                        buscandoCep = false
                                        res.onSuccess { end ->
                                            if (end.logradouro.isNotBlank()) logradouro = end.logradouro
                                            if (end.complemento.isNotBlank()) complemento = end.complemento
                                            if (end.bairro.isNotBlank()) bairro = end.bairro
                                            if (end.cidade.isNotBlank()) cidade = end.cidade
                                            if (end.estado.isNotBlank()) estado = end.estado
                                        }.onFailure { e ->
                                            erroCep = e.message ?: "Erro ao buscar CEP"
                                        }
                                    }
                                }
                            }) {
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
                    valor = logradouro,
                    aoMudarValor = { logradouro = it },
                    rotulo = "Logradouro (Rua, Av.)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Número e Complemento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoTextoContornado(
                        valor = numero,
                        aoMudarValor = { numero = it },
                        rotulo = "Número",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    CampoTextoContornado(
                        valor = complemento,
                        aoMudarValor = { complemento = it },
                        rotulo = "Complemento",
                        modifier = Modifier.weight(1.5f)
                    )
                }

                // Bairro
                CampoTextoContornado(
                    valor = bairro,
                    aoMudarValor = { bairro = it },
                    rotulo = "Bairro",
                    modifier = Modifier.fillMaxWidth()
                )

                // Cidade e Estado (UF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoTextoContornado(
                        valor = cidade,
                        aoMudarValor = { cidade = it },
                        rotulo = "Cidade",
                        modifier = Modifier.weight(2f)
                    )

                    CampoTextoContornado(
                        valor = estado,
                        aoMudarValor = { estado = it.take(2).uppercase() },
                        rotulo = "UF",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            BotaoPrincipal(
                texto = stringResource(R.string.mod02_btn_salvar),
                aoClicar = {
                    var valido = true
                    if (tipoPessoa == "PF" && cpfCnpj.length != 11) {
                        erroCpfCnpj = "Informe os 11 dígitos do CPF."
                        valido = false
                    } else if (tipoPessoa == "PJ" && cpfCnpj.length != 14) {
                        erroCpfCnpj = "Informe os 14 dígitos do CNPJ."
                        valido = false
                    }

                    if (nome.isBlank() || !nome.trim().contains(" ")) {
                        erroNome = "Informe o nome completo ou razão social."
                        valido = false
                    }
                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                    if (!emailRegex.matches(email.trim())) {
                        erroEmail = "E-mail inválido."
                        valido = false
                    }

                    val dataFormatada = if (dataNascimento.length == 8) {
                        "${dataNascimento.substring(0, 2)}/${dataNascimento.substring(2, 4)}/${dataNascimento.substring(4)}"
                    } else {
                        dataNascimento
                    }

                    val partes = dataFormatada.trim().split("/")
                    if (partes.size != 3) {
                        erroDataNascimento = "Data no formato dd/mm/aaaa."
                        valido = false
                    } else {
                        try {
                            val dia = partes[0].toInt()
                            val mes = partes[1].toInt()
                            val ano = partes[2].toInt()
                            val data = LocalDate.of(ano, mes, dia)
                            val hoje = LocalDate.now()
                            if (data.isAfter(hoje)) {
                                erroDataNascimento = "Data futura inválida."
                                valido = false
                            } else if (tipoPessoa == "PF" && Period.between(data, hoje).years < 18) {
                                erroDataNascimento = "Signatário deve ter 18 anos ou mais (P03)."
                                valido = false
                            }
                        } catch (e: Exception) {
                            erroDataNascimento = "Data inválida."
                            valido = false
                        }
                    }

                    if (valido) {
                        aoSalvar(
                            nome.trim(),
                            email.trim().lowercase(),
                            telefone.trim(),
                            dataFormatada.trim(),
                            tipoPessoa,
                            cpfCnpj.trim(),
                            cep.trim(),
                            logradouro.trim(),
                            numero.trim(),
                            complemento.trim(),
                            bairro.trim(),
                            cidade.trim(),
                            estado.trim()
                        )
                    }
                }
            )
        },
        dismissButton = {
            BotaoTexto(
                texto = "Cancelar",
                aoClicar = aoDispensar
            )
        },
        shape = FormaMedia,
        containerColor = LetsSignTheme.colors.corSuperficie,
        modifier = modifier
    )
}

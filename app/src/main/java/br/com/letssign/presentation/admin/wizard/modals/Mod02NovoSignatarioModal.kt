package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
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
    aoSalvar: (nome: String, email: String, telefone: String, dataNascimento: String) -> Unit,
    aoDispensar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroDataNascimento by remember { mutableStateOf<String?>(null) }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
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
                CampoTextoContornado(
                    valor = nome,
                    aoMudarValor = {
                        nome = it
                        erroNome = null
                    },
                    rotulo = stringResource(R.string.adm03_label_nome),
                    mensagemErro = erroNome,
                    ehErro = erroNome != null,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoTextoContornado(
                    valor = email,
                    aoMudarValor = {
                        email = it
                        erroEmail = null
                    },
                    rotulo = stringResource(R.string.adm03_label_email),
                    mensagemErro = erroEmail,
                    ehErro = erroEmail != null,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoTextoContornado(
                    valor = telefone,
                    aoMudarValor = { telefone = it },
                    rotulo = stringResource(R.string.adm03_label_telefone),
                    modifier = Modifier.fillMaxWidth()
                )

                CampoTextoContornado(
                    valor = dataNascimento,
                    aoMudarValor = {
                        dataNascimento = it
                        erroDataNascimento = null
                    },
                    rotulo = stringResource(R.string.adm03_label_nascimento),
                    dicaAjuda = stringResource(R.string.adm03_placeholder_nascimento),
                    iconeFim = {
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Selecionar data de nascimento",
                                tint = LetsSignTheme.colors.corPrimaria
                            )
                        }
                    },
                    mensagemErro = erroDataNascimento,
                    ehErro = erroDataNascimento != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            BotaoPrincipal(
                texto = stringResource(R.string.mod02_btn_salvar),
                aoClicar = {
                    var valido = true
                    if (nome.isBlank() || !nome.trim().contains(" ")) {
                        erroNome = "Informe o nome completo."
                        valido = false
                    }
                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                    if (!emailRegex.matches(email.trim())) {
                        erroEmail = "E-mail inválido."
                        valido = false
                    }
                    val partes = dataNascimento.trim().split("/")
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
                            } else if (Period.between(data, hoje).years < 18) {
                                erroDataNascimento = "Signatário deve ter 18 anos ou mais (P03)."
                                valido = false
                            }
                        } catch (e: Exception) {
                            erroDataNascimento = "Data de nascimento inválida."
                            valido = false
                        }
                    }

                    if (valido) {
                        aoSalvar(nome.trim(), email.trim().lowercase(), telefone.trim(), dataNascimento.trim())
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

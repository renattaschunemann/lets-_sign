package br.com.letssign.presentation.signatario.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun Mod08RecusarAssinaturaDialog(
    aoConfirmarRecusa: (motivo: String, senha: String) -> Unit,
    aoFechar: () -> Unit,
    modifier: Modifier = Modifier,
    erro: String? = null
) {
    var motivo by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erroValidacao by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = aoFechar,
        shape = FormaMedia,
        containerColor = LetsSignTheme.colors.corSuperficie,
        title = {
            Text(
                text = stringResource(R.string.mod08_titulo),
                style = MaterialTheme.typography.titleMedium,
                color = LetsSignTheme.colors.corErro
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.mod08_aviso),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )

                CampoTextoContornado(
                    valor = motivo,
                    aoMudarValor = {
                        motivo = it
                        erroValidacao = null
                    },
                    rotulo = stringResource(R.string.mod08_label_motivo),
                    dicaAjuda = stringResource(R.string.mod08_placeholder_motivo)
                )

                CampoTextoContornado(
                    valor = senha,
                    aoMudarValor = {
                        senha = it
                        erroValidacao = null
                    },
                    rotulo = stringResource(R.string.mod08_label_senha),
                    ehSenha = true
                )

                val erroExibicao = erroValidacao ?: erro
                if (erroExibicao != null) {
                    Text(
                        text = erroExibicao,
                        style = MaterialTheme.typography.bodySmall,
                        color = LetsSignTheme.colors.corErro
                    )
                }
            }
        },
        confirmButton = {
            BotaoDestrutivo(
                texto = stringResource(R.string.mod08_btn_confirmar_recusa),
                onClick = {
                    if (motivo.isBlank()) {
                        erroValidacao = "Informe o motivo da recusa."
                    } else if (senha.isBlank()) {
                        erroValidacao = "Informe sua senha de acesso."
                    } else {
                        aoConfirmarRecusa(motivo.trim(), senha)
                    }
                }
            )
        },
        dismissButton = {
            BotaoTexto(
                texto = stringResource(R.string.btn_cancelar),
                onClick = aoFechar
            )
        },
        modifier = modifier
    )
}

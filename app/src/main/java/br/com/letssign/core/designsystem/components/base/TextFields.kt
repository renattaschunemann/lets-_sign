package br.com.letssign.core.designsystem.components.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * Campo de texto contornado com:
 * - Rótulo SEMPRE visível acima do campo
 * - Texto de ajuda
 * - Contador opcional
 * - Mensagem de erro anunciada ao TalkBack junto ao campo
 */
@Composable
fun CampoTextoContornado(
    valor: String,
    aoMudarValor: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    dicaAjuda: String? = null,
    mensagemErro: String? = null,
    ehErro: Boolean = mensagemErro != null,
    habilitado: Boolean = true,
    somenteLeitura: Boolean = false,
    limiteCaracteres: Int? = null,
    ehSenha: Boolean = false,
    iconeInicio: @Composable (() -> Unit)? = null,
    iconeFim: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var senhaVisivel by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Rótulo sempre visível acima do campo
        Text(
            text = rotulo,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = if (ehErro) LetsSignTheme.colors.corErro else LetsSignTheme.colors.corTextoPrincipal,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = valor,
            onValueChange = { novoValor ->
                if (limiteCaracteres == null || novoValor.length <= limiteCaracteres) {
                    aoMudarValor(novoValor)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    if (ehErro && mensagemErro != null) {
                        contentDescription = "$rotulo. Erro: $mensagemErro"
                    }
                },
            enabled = habilitado,
            readOnly = somenteLeitura,
            textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            shape = FormaPequena,
            isError = ehErro,
            visualTransformation = if (ehSenha && !senhaVisivel) PasswordVisualTransformation() else VisualTransformation.None,
            leadingIcon = iconeInicio,
            trailingIcon = {
                if (ehSenha) {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(
                                if (senhaVisivel) R.string.cd_senha_oculta else R.string.cd_senha_visivel
                            ),
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    }
                } else if (iconeFim != null) {
                    iconeFim()
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LetsSignTheme.colors.corFoco,
                unfocusedBorderColor = LetsSignTheme.colors.corBorda,
                errorBorderColor = LetsSignTheme.colors.corErro,
                disabledBorderColor = LetsSignTheme.colors.corDesabilitado,
                focusedTextColor = LetsSignTheme.colors.corTextoPrincipal,
                unfocusedTextColor = LetsSignTheme.colors.corTextoPrincipal,
                disabledTextColor = LetsSignTheme.colors.corTextoDesabilitado,
                focusedContainerColor = LetsSignTheme.colors.corSuperficie,
                unfocusedContainerColor = LetsSignTheme.colors.corSuperficie
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = !ehSenha
        )

        // Linha inferior com Erro / Dica de Ajuda e Contador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ehErro && mensagemErro != null) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corErro,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = mensagemErro,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = LetsSignTheme.colors.corErro
                    )
                }
            } else if (dicaAjuda != null) {
                Text(
                    text = dicaAjuda,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (limiteCaracteres != null) {
                Text(
                    text = "${valor.length}/$limiteCaracteres",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }
        }
    }
}

@Preview(name = "Campo Vazio", showBackground = true)
@Composable
private fun CampoVazioPreview() {
    LetsSignTheme {
        CampoTextoContornado(
            valor = "",
            aoMudarValor = {},
            rotulo = "E-mail",
            dicaAjuda = "Informe um e-mail válido"
        )
    }
}

@Preview(name = "Campo com Erro", showBackground = true)
@Composable
private fun CampoErroPreview() {
    LetsSignTheme {
        CampoTextoContornado(
            valor = "email_invalido",
            aoMudarValor = {},
            rotulo = "E-mail",
            mensagemErro = "Informe um e-mail válido, como nome@empresa.com."
        )
    }
}

@Preview(name = "Campo Senha com Contador", showBackground = true)
@Composable
private fun CampoSenhaPreview() {
    LetsSignTheme {
        CampoTextoContornado(
            valor = "12345678",
            aoMudarValor = {},
            rotulo = "Senha",
            ehSenha = true,
            limiteCaracteres = 20
        )
    }
}

@Preview(name = "Campo FontScale 2.0", fontScale = 2.0f, showBackground = true)
@Composable
private fun CampoFontScalePreview() {
    LetsSignTheme {
        CampoTextoContornado(
            valor = "Texto longo preenchido no campo",
            aoMudarValor = {},
            rotulo = "Título do documento",
            dicaAjuda = "Dê um nome que ajude os signatários a reconhecer o documento."
        )
    }
}

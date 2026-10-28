package br.com.letssign.core.designsystem.components.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import br.com.letssign.core.designsystem.theme.FormaGrande
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun BotaoPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    icone: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth(),
        enabled = habilitado && !carregando,
        shape = FormaTotal,
        colors = ButtonDefaults.buttonColors(
            containerColor = LetsSignTheme.colors.corPrimaria,
            contentColor = LetsSignTheme.colors.corSobrePrimaria,
            disabledContainerColor = LetsSignTheme.colors.corDesabilitado,
            disabledContentColor = LetsSignTheme.colors.corTextoDesabilitado
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LetsSignTheme.colors.corSobrePrimaria,
                strokeWidth = 2.dp
            )
        } else {
            if (icone != null) {
                Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(text = texto, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun BotaoSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    icone: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth(),
        enabled = habilitado && !carregando,
        shape = FormaTotal,
        border = BorderStroke(
            1.dp,
            if (habilitado) LetsSignTheme.colors.corBorda else LetsSignTheme.colors.corDesabilitado
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LetsSignTheme.colors.corTextoPrincipal,
            disabledContentColor = LetsSignTheme.colors.corTextoDesabilitado
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LetsSignTheme.colors.corPrimaria,
                strokeWidth = 2.dp
            )
        } else {
            if (icone != null) {
                Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(text = texto, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun BotaoDestrutivo(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth(),
        enabled = habilitado && !carregando,
        shape = FormaTotal,
        border = BorderStroke(
            1.dp,
            if (habilitado) LetsSignTheme.colors.corErro else LetsSignTheme.colors.corDesabilitado
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LetsSignTheme.colors.corErro,
            disabledContentColor = LetsSignTheme.colors.corTextoDesabilitado
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LetsSignTheme.colors.corErro,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = texto, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun BotaoTexto(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = habilitado && !carregando,
        colors = ButtonDefaults.textButtonColors(
            contentColor = LetsSignTheme.colors.corPrimaria,
            disabledContentColor = LetsSignTheme.colors.corTextoDesabilitado
        )
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = LetsSignTheme.colors.corPrimaria,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = texto, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun FabEstendido(
    texto: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandido: Boolean = true
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = FormaGrande,
        containerColor = LetsSignTheme.colors.corPrimariaContainer,
        contentColor = LetsSignTheme.colors.corSobrePrimariaContainer,
        icon = { Icon(icone, contentDescription = null) },
        text = { Text(text = texto, style = androidx.compose.material3.MaterialTheme.typography.labelLarge) },
        expanded = expandido
    )
}

@Composable
fun DialogBasico(
    titulo: String,
    mensagem: String,
    textoBotaoPrincipal: String,
    textoBotaoSecundario: String? = null,
    aoConfirmar: () -> Unit,
    aoCancelar: (() -> Unit)? = null,
    aoDispensar: () -> Unit = { aoCancelar?.invoke() }
) {
    AlertDialog(
        onDismissRequest = aoDispensar,
        title = {
            Text(
                text = titulo,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = LetsSignTheme.colors.corTextoPrincipal
            )
        },
        text = {
            Text(
                text = mensagem,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corTextoSecundario
            )
        },
        confirmButton = {
            BotaoPrincipal(
                texto = textoBotaoPrincipal,
                onClick = aoConfirmar
            )
        },
        dismissButton = if (textoBotaoSecundario != null && aoCancelar != null) {
            {
                BotaoTexto(
                    texto = textoBotaoSecundario,
                    onClick = aoCancelar
                )
            }
        } else null,
        containerColor = LetsSignTheme.colors.corSuperficie,
        shape = FormaMedia
    )
}

@Preview(name = "Botão Principal Padrão", showBackground = true)
@Composable
private fun BotaoPrincipalPreview() {
    LetsSignTheme {
        BotaoPrincipal(texto = "Avançar", onClick = {})
    }
}

@Preview(name = "Botão Principal Carregando", showBackground = true)
@Composable
private fun BotaoPrincipalCarregandoPreview() {
    LetsSignTheme {
        BotaoPrincipal(texto = "Avançar", onClick = {}, carregando = true)
    }
}

@Preview(name = "Botão Principal Desabilitado", showBackground = true)
@Composable
private fun BotaoPrincipalDesabilitadoPreview() {
    LetsSignTheme {
        BotaoPrincipal(texto = "Avançar", onClick = {}, habilitado = false)
    }
}

@Preview(name = "Botão Secundário e Destrutivo", showBackground = true)
@Composable
private fun BotoesSecundariosPreview() {
    LetsSignTheme {
        BotaoSecundario(texto = "Selecionar PDF", onClick = {})
    }
}

@Preview(name = "Botão FontScale 2.0", fontScale = 2.0f, showBackground = true)
@Composable
private fun BotaoFontScalePreview() {
    LetsSignTheme {
        BotaoPrincipal(texto = "Avançar para a próxima etapa", onClick = {})
    }
}

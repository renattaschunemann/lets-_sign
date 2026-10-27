package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoTexto
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * Diálogo de confirmação segura (consequências + caixa + senha):
 * Usado para ações críticas e irreversíveis (MOD-07 Cancelar Documento e MOD-08 Recusar Assinatura).
 */
@Composable
fun DialogConfirmacaoSegura(
    titulo: String,
    textoConsequencias: String,
    textoCheckbox: String,
    textoBotaoConfirmar: String,
    aoConfirmar: (senha: String) -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    mensagemErroSenha: String? = null,
    carregando: Boolean = false,
    campoObservacao: (@Composable () -> Unit)? = null
) {
    var caixaMarcada by remember { mutableStateOf(false) }
    var senha by remember { mutableStateOf("") }

    val podeConfirmar = caixaMarcada && senha.isNotBlank() && !carregando

    Dialog(
        onDismissRequest = aoVoltar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LetsSignTheme.colors.corFundo)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = titulo,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = LetsSignTheme.colors.corTextoPrincipal,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = textoConsequencias,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = LetsSignTheme.colors.corTextoSecundario
            )

            if (campoObservacao != null) {
                campoObservacao()
            }

            // Caixa de confirmação explícita
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = caixaMarcada,
                    onCheckedChange = { caixaMarcada = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = LetsSignTheme.colors.corPrimaria,
                        checkmarkColor = LetsSignTheme.colors.corSobrePrimaria
                    )
                )
                Text(
                    text = textoCheckbox,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoPrincipal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Campo senha
            CampoTextoContornado(
                valor = senha,
                aoMudarValor = { senha = it },
                rotulo = "Sua senha",
                ehSenha = true,
                mensagemErro = mensagemErroSenha,
                dicaAjuda = "Confirme sua senha para continuar."
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botões de ação
            BotaoDestrutivo(
                texto = textoBotaoConfirmar,
                onClick = { aoConfirmar(senha) },
                habilitado = podeConfirmar,
                carregando = carregando
            )

            BotaoTexto(
                texto = stringResource(R.string.cd_voltar),
                onClick = aoVoltar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Diálogo Confirmação Segura", showBackground = true)
@Composable
private fun DialogConfirmacaoPreview() {
    LetsSignTheme {
        DialogConfirmacaoSegura(
            titulo = "Cancelar documento",
            textoConsequencias = "Cancelar este documento encerra o fluxo. Assinaturas já feitas deixam de valer para esta versão e todos os convidados serão avisados.",
            textoCheckbox = "Entendo que o documento será cancelado e não poderá mais ser assinado",
            textoBotaoConfirmar = "Cancelar documento",
            aoConfirmar = {},
            aoVoltar = {}
        )
    }
}

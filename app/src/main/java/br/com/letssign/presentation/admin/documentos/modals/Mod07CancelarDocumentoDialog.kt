package br.com.letssign.presentation.admin.documentos.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BannerAviso
import br.com.letssign.core.designsystem.components.base.BotaoDestrutivo
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.theme.FormaGrande
import br.com.letssign.core.designsystem.theme.LetsSignTheme

/**
 * MOD-07 — Modal de confirmação segura para cancelar documento.
 * Exige justificativa e senha do administrador.
 */
@Composable
fun Mod07CancelarDocumentoDialog(
    aoConfirmar: (motivo: String, senhaAdmin: String) -> Unit,
    aoDispensar: () -> Unit,
    mensagemErro: String? = null,
    carregando: Boolean = false,
    modifier: Modifier = Modifier
) {
    var motivo by remember { mutableStateOf("") }
    var senhaAdmin by remember { mutableStateOf("") }
    var erroMotivo by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = aoDispensar) {
        Surface(
            shape = FormaGrande,
            color = LetsSignTheme.colors.corSuperficie,
            shadowElevation = LetsSignTheme.elevation.nivel3,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.mod07_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )

                BannerAviso(mensagem = stringResource(R.string.mod07_aviso))

                CampoTextoContornado(
                    valor = motivo,
                    aoMudarValor = {
                        motivo = it
                        erroMotivo = null
                    },
                    rotulo = stringResource(R.string.mod07_label_motivo),
                    dicaAjuda = stringResource(R.string.mod07_placeholder_motivo),
                    mensagemErro = erroMotivo,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoTextoContornado(
                    valor = senhaAdmin,
                    aoMudarValor = {
                        senhaAdmin = it
                        erroSenha = null
                    },
                    rotulo = stringResource(R.string.mod07_label_senha),
                    ehSenha = true,
                    mensagemErro = erroSenha ?: mensagemErro,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotaoSecundario(
                        texto = stringResource(R.string.wizard_btn_voltar),
                        aoClicar = aoDispensar,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoDestrutivo(
                        texto = stringResource(R.string.mod07_btn_confirmar_cancelamento),
                        aoClicar = {
                            var valido = true
                            if (motivo.isBlank()) {
                                erroMotivo = "Informe o motivo do cancelamento."
                                valido = false
                            }
                            if (senhaAdmin.isBlank()) {
                                erroSenha = "Informe sua senha de administrador."
                                valido = false
                            }
                            if (valido) {
                                aoConfirmar(motivo.trim(), senhaAdmin)
                            }
                        },
                        habilitado = !carregando,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

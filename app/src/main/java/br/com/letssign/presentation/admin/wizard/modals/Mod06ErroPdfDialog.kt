package br.com.letssign.presentation.admin.wizard.modals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.pdf.TipoErroPdf

/**
 * MOD-06 — Dialog de erro na validação do PDF (senha, tamanho, páginas ou corrompido).
 */
@Composable
fun Mod06ErroPdfDialog(
    tipoErro: TipoErroPdf,
    aoEscolherOutroArquivo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mensagemRes = when (tipoErro) {
        TipoErroPdf.SENHA_PROTEGIDA -> R.string.mod06_erro_senha
        TipoErroPdf.TAMANHO_EXCEDIDO -> R.string.mod06_erro_tamanho
        TipoErroPdf.PAGINAS_EXCEDIDAS -> R.string.mod06_erro_paginas
        TipoErroPdf.ARQUIVO_CORROMPIDO -> R.string.mod06_erro_corrompido
    }

    AlertDialog(
        onDismissRequest = aoEscolherOutroArquivo,
        title = {
            Text(
                text = stringResource(R.string.mod06_titulo),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = LetsSignTheme.colors.corErro
            )
        },
        text = {
            Text(
                text = stringResource(mensagemRes),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = LetsSignTheme.colors.corTextoSecundario
            )
        },
        confirmButton = {
            BotaoPrincipal(
                texto = stringResource(R.string.mod06_btn_outro_arquivo),
                aoClicar = aoEscolherOutroArquivo
            )
        },
        shape = FormaMedia,
        containerColor = LetsSignTheme.colors.corSuperficie,
        modifier = modifier
    )
}

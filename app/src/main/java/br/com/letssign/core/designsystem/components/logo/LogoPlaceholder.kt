package br.com.letssign.core.designsystem.components.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.letssign.R
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

enum class LogoSize(val width: Dp, val height: Dp) {
    GRANDE(188.dp, 64.dp),    // COM-01 Splash
    MEDIO(140.dp, 48.dp),     // COM-02 Login, COM-03 Cadastro
    PEQUENO(94.dp, 32.dp)     // Top App Bars em todas as telas
}

/**
 * Componente oficial de exibição da Logomarca LetsSign:
 * Alterna dinamicamente entre as versões oficiais para Tema Claro e Tema Escuro:
 * - Tema Claro: R.drawable.letssign_logo (letssign-logo: com texto escuro e slogan)
 * - Tema Escuro: R.drawable.letssign_logo_branco (letssign-logo-branco: com texto branco e ícone gradiente)
 */
@Composable
fun LogoPlaceholder(
    tamanho: LogoSize = LogoSize.MEDIO,
    modifier: Modifier = Modifier,
    usarVersaoEscura: Boolean = isSystemInDarkTheme()
) {
    val descricao = stringResource(R.string.logo_content_description)
    val logoResId = if (usarVersaoEscura) R.drawable.letssign_logo_branco else R.drawable.letssign_logo

    Box(
        modifier = modifier
            .size(tamanho.width, tamanho.height)
            .clip(FormaPequena)
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = descricao,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Logo Claro (letssign-logo)", showBackground = true)
@Composable
private fun LogoClaroPreview() {
    LetsSignTheme(darkTheme = false) {
        LogoPlaceholder(tamanho = LogoSize.MEDIO, usarVersaoEscura = false)
    }
}

@Preview(name = "Logo Escuro (letssign-logo-branco)", showBackground = true, backgroundColor = 0xFF12141C)
@Composable
private fun LogoEscuroPreview() {
    LetsSignTheme(darkTheme = true) {
        LogoPlaceholder(tamanho = LogoSize.MEDIO, usarVersaoEscura = true)
    }
}

@Preview(name = "Logo Grande 188x64", showBackground = true)
@Composable
private fun LogoGrandePreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.GRANDE)
    }
}

@Preview(name = "Logo Pequeno 94x32", showBackground = true)
@Composable
private fun LogoPequenoPreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.PEQUENO)
    }
}

@Preview(name = "Logo Médio FontScale 2.0", fontScale = 2.0f, showBackground = true)
@Composable
private fun LogoFontScalePreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.MEDIO)
    }
}

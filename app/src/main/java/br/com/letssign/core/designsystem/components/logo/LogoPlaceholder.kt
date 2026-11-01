package br.com.letssign.core.designsystem.components.logo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
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
 * Renderiza o asset oficial da LetsSign (R.drawable.logo_letssign)
 * com proporção preservada, cantos arredondados suaves e acessibilidade.
 */
@Composable
fun LogoPlaceholder(
    tamanho: LogoSize = LogoSize.MEDIO,
    modifier: Modifier = Modifier
) {
    val descricao = stringResource(R.string.logo_content_description)

    Box(
        modifier = modifier
            .size(tamanho.width, tamanho.height)
            .clip(FormaPequena)
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_letssign),
            contentDescription = descricao,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Logo Grande 160x160", showBackground = true)
@Composable
private fun LogoGrandePreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.GRANDE)
    }
}

@Preview(name = "Logo Médio 120x48", showBackground = true)
@Composable
private fun LogoMedioPreview() {
    LetsSignTheme {
        LogoPlaceholder(tamanho = LogoSize.MEDIO)
    }
}

@Preview(name = "Logo Pequeno 32x32", showBackground = true)
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

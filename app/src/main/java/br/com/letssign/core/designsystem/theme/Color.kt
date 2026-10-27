package br.com.letssign.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 34 Tokens de cor oficiais do LetsSign extraídos da especificação de design.
 * Nenhuma cor arbitrária deve ser utilizada fora destes tokens.
 */
@Immutable
data class LetsSignColors(
    val corPrimaria: Color = Color(0xFF293681),
    val corSobrePrimaria: Color = Color(0xFFFFFFFF),
    val corPrimariaContainer: Color = Color(0xFFE8EBFA),
    val corSobrePrimariaContainer: Color = Color(0xFF1B245E),
    val corSecundaria: Color = Color(0xFF4274D9),
    val corSobreSecundaria: Color = Color(0xFFFFFFFF),
    val corFundo: Color = Color(0xFFFFFFFF),
    val corFundoExterno: Color = Color(0xFFF4F6FB),
    val corSuperficie: Color = Color(0xFFFFFFFF),
    val corSuperficieVariante: Color = Color(0xFFEEF1F8),
    val corTextoPrincipal: Color = Color(0xFF171A24),
    val corTextoSecundario: Color = Color(0xFF5C6475),
    val corTextoDesabilitado: Color = Color(0xFF8C94A5),
    val corDesabilitado: Color = Color(0xFFE5E8EF),
    val corBorda: Color = Color(0xFFDCE2EE),
    val corDivisor: Color = Color(0xFFE5E9F2),
    val corOverlay: Color = Color(0xFF171A24),
    val corSombra: Color = Color(0xFF171A24),
    val corFoco: Color = Color(0xFF4274D9),
    val corSucesso: Color = Color(0xFF23845B),
    val corSucessoContainer: Color = Color(0xFFE1F4E9),
    val corAviso: Color = Color(0xFF946300),
    val corAvisoContainer: Color = Color(0xFFFFF1CC),
    val corErro: Color = Color(0xFFD64545),
    val corErroContainer: Color = Color(0xFFFCE8E8),
    val corInfo: Color = Color(0xFF293681),
    val corInfoContainer: Color = Color(0xFFE8EBFA),
    val corNeutro: Color = Color(0xFF5C6475),
    val corNeutroContainer: Color = Color(0xFFEEF1F8),
    val corBadge: Color = Color(0xFF293681),
    val corSobreBadge: Color = Color(0xFFFFFFFF),
    val corCampoAssinaturaVazio: Color = Color(0xFFF4F6FB),
    val corCampoAssinaturaSelecionado: Color = Color(0xFFE8EBFA),
    val corCampoAssinaturaAssinado: Color = Color(0xFFE1F4E9),
    val corCampoAssinaturaOutroParticipante: Color = Color(0xFFE5F5FA),
    val corPaginaPdf: Color = Color(0xFFFFFFFF),
    val corTintaAssinatura: Color = Color(0xFF293681)
)

val LocalLetsSignColors = staticCompositionLocalOf { LetsSignColors() }

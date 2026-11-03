package br.com.letssign.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 34 Tokens de cor oficiais do LetsSign extraídos da especificação de design.
 * Suporta Tema Claro (Light) e Tema Escuro (Dark).
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

val LetsSignLightColors = LetsSignColors()

val LetsSignDarkColors = LetsSignColors(
    corPrimaria = Color(0xFF5B86E5),
    corSobrePrimaria = Color(0xFFFFFFFF),
    corPrimariaContainer = Color(0xFF1E2866),
    corSobrePrimariaContainer = Color(0xFFE8EBFA),
    corSecundaria = Color(0xFF82B1FF),
    corSobreSecundaria = Color(0xFF0F1B52),
    corFundo = Color(0xFF12141C),
    corFundoExterno = Color(0xFF0D0F17),
    corSuperficie = Color(0xFF1A1D27),
    corSuperficieVariante = Color(0xFF242836),
    corTextoPrincipal = Color(0xFFF1F3FA),
    corTextoSecundario = Color(0xFFA5ADC0),
    corTextoDesabilitado = Color(0xFF6B7282),
    corDesabilitado = Color(0xFF2C313E),
    corBorda = Color(0xFF333948),
    corDivisor = Color(0xFF272C39),
    corOverlay = Color(0xFF000000),
    corSombra = Color(0xFF000000),
    corFoco = Color(0xFF82B1FF),
    corSucesso = Color(0xFF48BB78),
    corSucessoContainer = Color(0xFF1C3D2B),
    corAviso = Color(0xFFECC94B),
    corAvisoContainer = Color(0xFF443811),
    corErro = Color(0xFFFC8181),
    corErroContainer = Color(0xFF4E1D1D),
    corInfo = Color(0xFF63B3ED),
    corInfoContainer = Color(0xFF1A365D),
    corNeutro = Color(0xFFA0AEC0),
    corNeutroContainer = Color(0xFF2D3748),
    corBadge = Color(0xFF5B86E5),
    corSobreBadge = Color(0xFFFFFFFF),
    corCampoAssinaturaVazio = Color(0xFF242836),
    corCampoAssinaturaSelecionado = Color(0xFF1E2866),
    corCampoAssinaturaAssinado = Color(0xFF1C3D2B),
    corCampoAssinaturaOutroParticipante = Color(0xFF1E3340),
    corPaginaPdf = Color(0xFFFFFFFF),
    corTintaAssinatura = Color(0xFF1A237E)
)

val LocalLetsSignColors = staticCompositionLocalOf { LetsSignColors() }

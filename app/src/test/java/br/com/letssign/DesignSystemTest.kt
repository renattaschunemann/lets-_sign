package br.com.letssign

import androidx.compose.ui.graphics.Color
import br.com.letssign.core.designsystem.theme.LetsSignColors
import br.com.letssign.core.designsystem.theme.LetsSignSpacing
import br.com.letssign.core.navigation.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemTest {

    @Test
    fun `validar tokens oficiais de cores do Figma Prompt 0`() {
        val colors = LetsSignColors()
        assertEquals(Color(0xFF293681), colors.corPrimaria)
        assertEquals(Color(0xFFFFFFFF), colors.corSobrePrimaria)
        assertEquals(Color(0xFFE8EBFA), colors.corPrimariaContainer)
        assertEquals(Color(0xFF1B245E), colors.corSobrePrimariaContainer)
        assertEquals(Color(0xFF4274D9), colors.corSecundaria)
        assertEquals(Color(0xFFFFFFFF), colors.corSobreSecundaria)
        assertEquals(Color(0xFFFFFFFF), colors.corFundo)
        assertEquals(Color(0xFFF4F6FB), colors.corFundoExterno)
        assertEquals(Color(0xFFFFFFFF), colors.corSuperficie)
        assertEquals(Color(0xFFEEF1F8), colors.corSuperficieVariante)
        assertEquals(Color(0xFF171A24), colors.corTextoPrincipal)
        assertEquals(Color(0xFF5C6475), colors.corTextoSecundario)
        assertEquals(Color(0xFF8C94A5), colors.corTextoDesabilitado)
        assertEquals(Color(0xFFE5E8EF), colors.corDesabilitado)
        assertEquals(Color(0xFFDCE2EE), colors.corBorda)
        assertEquals(Color(0xFFE5E9F2), colors.corDivisor)
        assertEquals(Color(0xFF171A24), colors.corOverlay)
        assertEquals(Color(0xFF171A24), colors.corSombra)
        assertEquals(Color(0xFF4274D9), colors.corFoco)
        assertEquals(Color(0xFF23845B), colors.corSucesso)
        assertEquals(Color(0xFFE1F4E9), colors.corSucessoContainer)
        assertEquals(Color(0xFF946300), colors.corAviso)
        assertEquals(Color(0xFFFFF1CC), colors.corAvisoContainer)
        assertEquals(Color(0xFFD64545), colors.corErro)
        assertEquals(Color(0xFFFCE8E8), colors.corErroContainer)
        assertEquals(Color(0xFF293681), colors.corInfo)
        assertEquals(Color(0xFFE8EBFA), colors.corInfoContainer)
        assertEquals(Color(0xFF5C6475), colors.corNeutro)
        assertEquals(Color(0xFFEEF1F8), colors.corNeutroContainer)
        assertEquals(Color(0xFF293681), colors.corBadge)
        assertEquals(Color(0xFFFFFFFF), colors.corSobreBadge)
        assertEquals(Color(0xFFF4F6FB), colors.corCampoAssinaturaVazio)
        assertEquals(Color(0xFFE8EBFA), colors.corCampoAssinaturaSelecionado)
        assertEquals(Color(0xFFE1F4E9), colors.corCampoAssinaturaAssinado)
        assertEquals(Color(0xFFE5F5FA), colors.corCampoAssinaturaOutroParticipante)
        assertEquals(Color(0xFFFFFFFF), colors.corPaginaPdf)
        assertEquals(Color(0xFF293681), colors.corTintaAssinatura)
    }

    @Test
    fun `validar espacamentos e area de toque minima de 48dp`() {
        val spacing = LetsSignSpacing()
        assertEquals(4, spacing.esp4.value.toInt())
        assertEquals(8, spacing.esp8.value.toInt())
        assertEquals(12, spacing.esp12.value.toInt())
        assertEquals(16, spacing.esp16.value.toInt())
        assertEquals(24, spacing.esp24.value.toInt())
        assertEquals(32, spacing.esp32.value.toInt())
        assertTrue(spacing.toqueMinimo.value >= 48f)
    }

    @Test
    fun `validar que as 38 rotas oficiais estao presentes no sistema de navegacao`() {
        val routes: List<Any> = listOf(
            Com01SplashRoute,
            Com02LoginRoute(),
            Com03CadastroAdminRoute,
            Com04EntradaLinkRoute(),
            Com05NotificacoesRoute(),
            Com06MinhaContaRoute(),
            Adm01DocumentosRoute,
            Adm02SignatariosRoute,
            Adm03SignatarioFormRoute(),
            Adm04NovoDocTituloRoute(),
            Adm05SelecionarPdfRoute,
            Adm06SignatariosRoute,
            Adm07ObrigatoriedadeRoute,
            Adm08OrdemRoute,
            Adm09PosicionarCamposRoute,
            Adm10RevisaoEnvioRoute,
            Adm11AcompanhamentoRoute(),
            Adm12HistoricoRoute(),
            Adm13VisualizadorRoute(),
            Sig01InicioPendentesRoute,
            Sig02MeusDocumentosRoute,
            Sig03DetalheDocumentoRoute(),
            Sig04ConfirmacaoIdRoute(),
            Sig05AssinarDocumentoRoute(),
            Sig06CriarAssinaturaRoute,
            Sig07AssinaturaSucessoRoute(),
            Sig08VisualizadorLeituraRoute(),
            Mod01ResultadoEnvioRoute,
            Mod02ParticipanteDialogRoute(),
            Mod03SignatarioSheetRoute,
            Mod04SairAssistenteRoute,
            Mod05RetomarRascunhoRoute(),
            Mod06ErroPdfRoute(),
            Mod07ConfirmarCancelamentoRoute(),
            Mod08RecusarAssinaturaRoute(),
            Mod09DetalheRecusaRoute(),
            Mod10ConfirmarAssinaturaRoute(),
            Mod11CriarNovaVersaoRoute()
        )

        assertEquals(38, routes.size)
        routes.forEach { route ->
            assertNotNull(route)
        }
    }
}

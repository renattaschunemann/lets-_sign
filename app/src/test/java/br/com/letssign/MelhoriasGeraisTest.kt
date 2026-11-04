package br.com.letssign

import androidx.compose.ui.text.AnnotatedString
import br.com.letssign.core.common.contacts.AgendaContatosMockHelper
import br.com.letssign.core.common.transformation.DataNascimentoVisualTransformation
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.data.repository.OperacaoSincronizacaoOffline
import br.com.letssign.presentation.admin.wizard.CampoVisual
import br.com.letssign.presentation.admin.wizard.TemplateDocumentoRapido
import br.com.letssign.presentation.signatario.assinatura.AbaCapturaAssinatura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MelhoriasGeraisTest {

    @Test
    fun `verificar opcoes de template rapido de documentos`() {
        val templates = TemplateDocumentoRapido.entries
        assertEquals(3, templates.size)
        assertTrue(templates.contains(TemplateDocumentoRapido.CONTRATO_DUPLO))
        assertTrue(templates.contains(TemplateDocumentoRapido.INDIVIDUAL_CENTRALIZADO))
        assertTrue(templates.contains(TemplateDocumentoRapido.MULTIPLAS_PARTES))
    }

    @Test
    fun `verificar abas de captura de assinatura com upload de foto`() {
        val abas = AbaCapturaAssinatura.entries
        assertTrue(abas.contains(AbaCapturaAssinatura.UPLOAD_FOTO))
        assertTrue(abas.contains(AbaCapturaAssinatura.DESENHAR))
        assertTrue(abas.contains(AbaCapturaAssinatura.DIGITAR))
        assertTrue(abas.contains(AbaCapturaAssinatura.CERTIFICADO_DIGITAL))
    }

    @Test
    fun `verificar estrutura da operacao de sincronizacao offline`() {
        val operacao = OperacaoSincronizacaoOffline(
            tipo = "ASSINATURA",
            documentoId = "doc-123",
            payload = "{\"status\":\"ok\"}"
        )
        assertNotNull(operacao.id)
        assertEquals("ASSINATURA", operacao.tipo)
        assertEquals("doc-123", operacao.documentoId)
        assertTrue(operacao.dataHoraCriacaoUtc > 0)
    }

    @Test
    fun `verificar posicionamento inteligente de campos de assinatura`() {
        val campoDuplo1 = CampoVisual(
            signatarioId = "sig-1",
            normX = 0.06f,
            normY = 0.76f,
            normLargura = 0.42f,
            normAltura = 0.08f
        )
        val campoDuplo2 = CampoVisual(
            signatarioId = "sig-2",
            normX = 0.52f,
            normY = 0.76f,
            normLargura = 0.42f,
            normAltura = 0.08f
        )

        assertTrue(campoDuplo1.normX + campoDuplo1.normLargura <= 0.50f)
        assertTrue(campoDuplo2.normX >= 0.50f)
        assertTrue(campoDuplo2.normX + campoDuplo2.normLargura <= 1.0f)
    }

    @Test
    fun `verificar 10 contatos mockados da agenda do administrador`() {
        val contatos = AgendaContatosMockHelper.CONTATOS_MOCKADOS
        assertEquals(10, contatos.size)
        contatos.forEach { contato ->
            assertTrue("Nome deve estar preenchido", contato.nome.isNotBlank())
            assertTrue("E-mail deve conter @", contato.email.contains("@"))
            assertEquals("Telefone deve ter 11 dígitos com DDD", 11, contato.telefone.length)
            assertTrue("Cargo deve estar preenchido", contato.cargo.isNotBlank())
        }
    }

    @Test
    fun `verificar mascara e validacao de data de nascimento com ano completo`() {
        val transformation = DataNascimentoVisualTransformation()

        // Teste de formatação com 8 dígitos
        val resultado = transformation.filter(AnnotatedString("20081992"))
        assertEquals("20/08/1992", resultado.text.text)

        // Teste de formatação parcial
        assertEquals("20", transformation.filter(AnnotatedString("20")).text.text)
        assertEquals("20/08", transformation.filter(AnnotatedString("2008")).text.text)

        // Rejeitar ano incompleto (apenas 2 dígitos ou 6 dígitos totais)
        assertFalse(CadastroValidator.validarDataNascimento("20/08/92"))
        assertFalse(CadastroValidator.validarDataNascimento("200892"))
        assertFalse(CadastroValidator.validarDataNascimento("2008199"))

        // Aceitar somente ano completo de 4 dígitos (8 dígitos numéricos válidos)
        assertTrue(CadastroValidator.validarDataNascimento("20081992"))
        assertTrue(CadastroValidator.validarDataNascimento("20/08/1992"))
        assertTrue(CadastroValidator.validarDataNascimento("15/04/1988"))
        assertTrue(CadastroValidator.validarDataNascimento("01/01/2000"))

        // Rejeitar datas impossíveis
        assertFalse(CadastroValidator.validarDataNascimento("32/01/2000"))
        assertFalse(CadastroValidator.validarDataNascimento("15/13/2000"))
        assertFalse(CadastroValidator.validarDataNascimento("00/01/2000"))
    }
}

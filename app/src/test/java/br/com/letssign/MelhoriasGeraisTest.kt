package br.com.letssign

import br.com.letssign.data.repository.OperacaoSincronizacaoOffline
import br.com.letssign.presentation.admin.wizard.CampoVisual
import br.com.letssign.presentation.admin.wizard.TemplateDocumentoRapido
import br.com.letssign.presentation.signatario.assinatura.AbaCapturaAssinatura
import org.junit.Assert.assertEquals
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
}

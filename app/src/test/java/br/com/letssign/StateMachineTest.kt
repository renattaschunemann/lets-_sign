package br.com.letssign

import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante
import br.com.letssign.domain.statemachine.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class StateMachineTest {

    @Test
    fun `testar fluxo completo feliz do documento de Rascunho ate Concluido`() {
        var estado = SituacaoDocumento.RASCUNHO

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.IniciarEnvio)
        assertEquals(SituacaoDocumento.ENVIANDO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.ConfirmarEnvioSucesso)
        assertEquals(SituacaoDocumento.ENVIADO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.RegistrarPrimeiraAssinatura)
        assertEquals(SituacaoDocumento.PARCIALMENTE_ASSINADO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.RegistrarAssinaturaIntermediaria)
        assertEquals(SituacaoDocumento.PARCIALMENTE_ASSINADO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.RegistrarAssinaturaFinal)
        assertEquals(SituacaoDocumento.CONCLUIDO, estado)
    }

    @Test
    fun `testar fluxo de falha e retentativa de envio`() {
        var estado = SituacaoDocumento.RASCUNHO
        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.IniciarEnvio)
        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.FalharEnvio("Timeout"))
        assertEquals(SituacaoDocumento.FALHA_ENVIO, estado)

        // Retentar envio
        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.IniciarEnvio)
        assertEquals(SituacaoDocumento.ENVIANDO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.ConfirmarEnvioSucesso)
        assertEquals(SituacaoDocumento.ENVIADO, estado)
    }

    @Test
    fun `testar recusa de signatario obrigatorio e criacao de nova versao`() {
        var estado = SituacaoDocumento.ENVIADO
        estado = DocumentoStateMachine.transicionar(
            estado,
            EventoDocumento.RegistrarRecusaObrigatorio("part-123")
        )
        assertEquals(SituacaoDocumento.RECUSADO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.CriarNovaVersao(2))
        assertEquals(SituacaoDocumento.SUBSTITUIDO, estado)
    }

    @Test
    fun `testar cancelamento pelo Admin e criacao de nova versao`() {
        var estado = SituacaoDocumento.PARCIALMENTE_ASSINADO
        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.CancelarAdmin("Erro no valor"))
        assertEquals(SituacaoDocumento.CANCELADO, estado)

        estado = DocumentoStateMachine.transicionar(estado, EventoDocumento.CriarNovaVersao(2))
        assertEquals(SituacaoDocumento.SUBSTITUIDO, estado)
    }

    @Test
    fun `testar que transicoes invalidas do documento lancam IllegalStateException`() {
        // Não é permitido cancelar documento já concluído
        try {
            DocumentoStateMachine.transicionar(
                SituacaoDocumento.CONCLUIDO,
                EventoDocumento.CancelarAdmin("Tentar cancelar")
            )
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            // Sucesso esperado
        }

        // Não é permitido assinar rascunho
        try {
            DocumentoStateMachine.transicionar(
                SituacaoDocumento.RASCUNHO,
                EventoDocumento.RegistrarAssinaturaFinal
            )
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            // Sucesso esperado
        }
    }

    @Test
    fun `testar transicoes validas e invalidas da maquina de participantes`() {
        var estado = SituacaoParticipante.AGUARDANDO_VEZ

        estado = ParticipanteStateMachine.transicionar(estado, EventoParticipante.LiberarVez)
        assertEquals(SituacaoParticipante.PENDENTE, estado)

        estado = ParticipanteStateMachine.transicionar(estado, EventoParticipante.Assinar(System.currentTimeMillis()))
        assertEquals(SituacaoParticipante.ASSINADO, estado)

        // Participante já assinado não pode recusar
        try {
            ParticipanteStateMachine.transicionar(estado, EventoParticipante.Recusar("Mudei de ideia"))
            fail("Deveria ter lançado IllegalStateException")
        } catch (e: IllegalStateException) {
            // Sucesso esperado
        }

        // Teste de encerramento de participante opcional que não assinou
        val opcionalAguardando = SituacaoParticipante.AGUARDANDO_VEZ
        val encerrado = ParticipanteStateMachine.transicionar(opcionalAguardando, EventoParticipante.EncerrarFluxo)
        assertEquals(SituacaoParticipante.ENCERRADO, encerrado)
    }
}

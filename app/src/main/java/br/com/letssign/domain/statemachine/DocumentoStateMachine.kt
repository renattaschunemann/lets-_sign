package br.com.letssign.domain.statemachine

import br.com.letssign.domain.model.SituacaoDocumento

sealed interface EventoDocumento {
    data object IniciarEnvio : EventoDocumento
    data object ConfirmarEnvioSucesso : EventoDocumento
    data class FalharEnvio(val motivo: String) : EventoDocumento
    data object RegistrarPrimeiraAssinatura : EventoDocumento
    data object RegistrarAssinaturaIntermediaria : EventoDocumento
    data object RegistrarAssinaturaFinal : EventoDocumento
    data class RegistrarRecusaObrigatorio(val participanteId: String) : EventoDocumento
    data class CancelarAdmin(val motivo: String) : EventoDocumento
    data class CriarNovaVersao(val novaVersaoNumero: Int) : EventoDocumento
}

object DocumentoStateMachine {

    fun transicionar(situacaoAtual: SituacaoDocumento, evento: EventoDocumento): SituacaoDocumento {
        return when (situacaoAtual) {
            SituacaoDocumento.RASCUNHO -> when (evento) {
                is EventoDocumento.IniciarEnvio -> SituacaoDocumento.ENVIANDO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.ENVIANDO -> when (evento) {
                is EventoDocumento.ConfirmarEnvioSucesso -> SituacaoDocumento.ENVIADO
                is EventoDocumento.FalharEnvio -> SituacaoDocumento.FALHA_ENVIO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.FALHA_ENVIO -> when (evento) {
                is EventoDocumento.IniciarEnvio -> SituacaoDocumento.ENVIANDO
                is EventoDocumento.CancelarAdmin -> SituacaoDocumento.CANCELADO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.ENVIADO -> when (evento) {
                is EventoDocumento.RegistrarPrimeiraAssinatura -> SituacaoDocumento.PARCIALMENTE_ASSINADO
                is EventoDocumento.RegistrarAssinaturaFinal -> SituacaoDocumento.CONCLUIDO
                is EventoDocumento.RegistrarRecusaObrigatorio -> SituacaoDocumento.RECUSADO
                is EventoDocumento.CancelarAdmin -> SituacaoDocumento.CANCELADO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.PARCIALMENTE_ASSINADO -> when (evento) {
                is EventoDocumento.RegistrarAssinaturaIntermediaria -> SituacaoDocumento.PARCIALMENTE_ASSINADO
                is EventoDocumento.RegistrarAssinaturaFinal -> SituacaoDocumento.CONCLUIDO
                is EventoDocumento.RegistrarRecusaObrigatorio -> SituacaoDocumento.RECUSADO
                is EventoDocumento.CancelarAdmin -> SituacaoDocumento.CANCELADO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.RECUSADO -> when (evento) {
                is EventoDocumento.CriarNovaVersao -> SituacaoDocumento.SUBSTITUIDO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.CANCELADO -> when (evento) {
                is EventoDocumento.CriarNovaVersao -> SituacaoDocumento.SUBSTITUIDO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoDocumento.CONCLUIDO -> transicaoInvalida(situacaoAtual, evento)
            SituacaoDocumento.SUBSTITUIDO -> transicaoInvalida(situacaoAtual, evento)
        }
    }

    private fun transicaoInvalida(situacaoAtual: SituacaoDocumento, evento: EventoDocumento): Nothing {
        throw IllegalStateException(
            "Transição de estado inválida para Documento: Situação atual '$situacaoAtual' não aceita o evento '${evento::class.simpleName}'."
        )
    }
}

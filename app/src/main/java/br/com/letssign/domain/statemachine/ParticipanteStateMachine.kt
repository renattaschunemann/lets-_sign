package br.com.letssign.domain.statemachine

import br.com.letssign.domain.model.SituacaoParticipante

sealed interface EventoParticipante {
    data object LiberarVez : EventoParticipante
    data class Assinar(val dataHoraUtc: Long) : EventoParticipante
    data class Recusar(val motivo: String) : EventoParticipante
    data object EncerrarFluxo : EventoParticipante
}

object ParticipanteStateMachine {

    fun transicionar(situacaoAtual: SituacaoParticipante, evento: EventoParticipante): SituacaoParticipante {
        return when (situacaoAtual) {
            SituacaoParticipante.AGUARDANDO_VEZ -> when (evento) {
                is EventoParticipante.LiberarVez -> SituacaoParticipante.PENDENTE
                is EventoParticipante.EncerrarFluxo -> SituacaoParticipante.ENCERRADO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoParticipante.PENDENTE -> when (evento) {
                is EventoParticipante.Assinar -> SituacaoParticipante.ASSINADO
                is EventoParticipante.Recusar -> SituacaoParticipante.RECUSADO
                is EventoParticipante.EncerrarFluxo -> SituacaoParticipante.ENCERRADO
                else -> transicaoInvalida(situacaoAtual, evento)
            }

            SituacaoParticipante.ASSINADO -> transicaoInvalida(situacaoAtual, evento)
            SituacaoParticipante.RECUSADO -> transicaoInvalida(situacaoAtual, evento)
            SituacaoParticipante.ENCERRADO -> transicaoInvalida(situacaoAtual, evento)
        }
    }

    private fun transicaoInvalida(situacaoAtual: SituacaoParticipante, evento: EventoParticipante): Nothing {
        throw IllegalStateException(
            "Transição de estado inválida para Participante: Situação atual '$situacaoAtual' não aceita o evento '${evento::class.simpleName}'."
        )
    }
}

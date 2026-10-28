package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.NotificacaoDao
import br.com.letssign.data.local.entities.NotificacaoEntity
import br.com.letssign.domain.model.TipoNotificacao
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacaoRepository @Inject constructor(
    private val notificacaoDao: NotificacaoDao
) {

    suspend fun inicializarDadosDemoSeNecessario(usuarioId: String, perfil: String) {
        val agora = System.currentTimeMillis()

        if (perfil.equals("ADMIN", ignoreCase = true)) {
            val existente = notificacaoDao.buscarPorUsuarioSync(usuarioId)
            if (existente.isNotEmpty()) return

            val notificacoesAdmin = listOf(
                NotificacaoEntity(
                    id = "notif-adm-1",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-1",
                    tipoNotificacao = TipoNotificacao.ASSINATURA_REALIZADA.name,
                    titulo = "Contrato de Prestação de Serviços",
                    mensagem = "Maria Souza assinou 'Contrato de Prestação de Serviços'.",
                    lida = false,
                    dataHoraUtc = agora - 1800000 // 30 min atrás
                ),
                NotificacaoEntity(
                    id = "notif-adm-2",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-6",
                    tipoNotificacao = TipoNotificacao.RECUSA.name,
                    titulo = "Aditivo 02",
                    mensagem = "Recusado por Maria Souza em 26/10/2026 às 10:05.",
                    lida = true,
                    dataHoraUtc = agora - 7200000 // 2 horas atrás
                ),
                NotificacaoEntity(
                    id = "notif-adm-3",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-2",
                    tipoNotificacao = TipoNotificacao.LEMBRETE.name,
                    titulo = "Aditivo 02",
                    mensagem = "Não foi possível entregar o SMS para João Lima. Reenviar convite?",
                    lida = false,
                    dataHoraUtc = agora - 14400000 // 4 horas atrás
                ),
                NotificacaoEntity(
                    id = "notif-adm-4",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-5",
                    tipoNotificacao = TipoNotificacao.DOCUMENTO_CONCLUIDO.name,
                    titulo = "Contrato de Prestação de Serviços",
                    mensagem = "Documento concluído em 26/10/2026 às 15:42.",
                    lida = true,
                    dataHoraUtc = agora - 28800000 // 8 horas atrás
                )
            )
            notificacoesAdmin.forEach { notificacaoDao.inserir(it) }
        } else {
            val existente = notificacaoDao.buscarPorUsuarioSync(usuarioId)
            if (existente.isNotEmpty()) return

            val notificacoesSig = listOf(
                NotificacaoEntity(
                    id = "notif-sig-1",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-4",
                    tipoNotificacao = TipoNotificacao.SUA_VEZ.name,
                    titulo = "Aditivo 02",
                    mensagem = "Chegou a sua vez de assinar 'Aditivo 02'.",
                    lida = false,
                    dataHoraUtc = agora - 3600000
                ),
                NotificacaoEntity(
                    id = "notif-sig-2",
                    usuarioId = usuarioId,
                    documentoId = "doc-demo-1",
                    tipoNotificacao = TipoNotificacao.CONVITE_RECEBIDO.name,
                    titulo = "Contrato de Prestação de Serviços",
                    mensagem = "Você foi convidado para assinar 'Contrato de Prestação de Serviços'.",
                    lida = true,
                    dataHoraUtc = agora - 86400000
                )
            )
            notificacoesSig.forEach { notificacaoDao.inserir(it) }
        }
    }

    fun listarNotificacoes(usuarioId: String): Flow<List<NotificacaoEntity>> {
        return notificacaoDao.listarPorUsuario(usuarioId)
    }

    fun contarNaoLidas(usuarioId: String): Flow<Int> {
        return notificacaoDao.contarNaoLidas(usuarioId)
    }

    suspend fun marcarComoLida(notificacaoId: String) {
        notificacaoDao.marcarComoLida(notificacaoId)
    }

    suspend fun marcarTodasComoLidas(usuarioId: String) {
        notificacaoDao.marcarTodasComoLidas(usuarioId)
    }
}

package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.DocumentoDao
import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
import br.com.letssign.data.remote.RemoteDataSource
import br.com.letssign.domain.model.SituacaoDocumento
import br.com.letssign.domain.model.SituacaoParticipante
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LinkResolutionResult {
    data class ValidoParaAssinatura(val documentoId: String, val participanteId: String) : LinkResolutionResult
    data object BloqueioOutraConta : LinkResolutionResult
    data object DocumentoCancelado : LinkResolutionResult
    data class JaAssinado(val documentoId: String) : LinkResolutionResult
    data object LinkInvalidoOuExpirado : LinkResolutionResult
    data class ErroConexao(val mensagem: String) : LinkResolutionResult
}

@Singleton
class LinkResolverRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val documentoDao: DocumentoDao,
    private val participanteDocumentoDao: ParticipanteDocumentoDao
) {

    suspend fun resolverLink(token: String, emailUsuarioLogado: String?): LinkResolutionResult {
        if (token.isBlank()) return LinkResolutionResult.LinkInvalidoOuExpirado

        return try {
            val (documentoId, participanteId) = remoteDataSource.resolverLinkConvite(token)

            val docLocal = documentoDao.buscarPorId(documentoId)
            if (docLocal != null && docLocal.situacaoAtual == SituacaoDocumento.CANCELADO) {
                return LinkResolutionResult.DocumentoCancelado
            }

            val partLocal = participanteDocumentoDao.buscarPorId(participanteId)
            if (partLocal != null) {
                if (partLocal.situacao == SituacaoParticipante.ASSINADO) {
                    return LinkResolutionResult.JaAssinado(documentoId)
                }

                if (!emailUsuarioLogado.isNullOrBlank() &&
                    !partLocal.emailSnapshot.equals(emailUsuarioLogado.trim(), ignoreCase = true)
                ) {
                    return LinkResolutionResult.BloqueioOutraConta
                }
            }

            LinkResolutionResult.ValidoParaAssinatura(documentoId, participanteId)
        } catch (_: IOException) {
            LinkResolutionResult.ErroConexao("Sem conexão. Verifique a internet e tente novamente.")
        } catch (_: IllegalArgumentException) {
            LinkResolutionResult.LinkInvalidoOuExpirado
        } catch (e: Exception) {
            LinkResolutionResult.LinkInvalidoOuExpirado
        }
    }
}

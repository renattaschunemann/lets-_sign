package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.datastore.UsuarioSessao
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val remoteDataSource: RemoteDataSource,
    private val sessionManager: SessionManager
) {

    val sessaoAtiva: Flow<UsuarioSessao?> = sessionManager.sessaoFlow

    suspend fun autenticar(email: String, senha: String): Result<UsuarioEntity> {
        return try {
            val remoteUser = remoteDataSource.autenticar(email, senha)
            val usuarioLocal = UsuarioEntity(
                id = remoteUser.id,
                nome = remoteUser.nome,
                email = remoteUser.email,
                telefone = remoteUser.telefone,
                perfil = remoteUser.perfil,
                senhaHash = remoteUser.senhaHash,
                dataCriacaoUtc = System.currentTimeMillis()
            )
            // Salva ou atualiza no Room local
            val existente = usuarioDao.buscarPorId(usuarioLocal.id)
            if (existente == null) {
                usuarioDao.inserir(usuarioLocal)
            } else {
                usuarioDao.atualizar(usuarioLocal)
            }

            // Grava na sessão criptografada
            sessionManager.salvarSessao(
                UsuarioSessao(
                    usuarioId = usuarioLocal.id,
                    nome = usuarioLocal.nome,
                    email = usuarioLocal.email,
                    perfil = usuarioLocal.perfil,
                    tokenAcesso = "token-${usuarioLocal.id}"
                )
            )

            Result.success(usuarioLocal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cadastrarAdmin(
        nome: String,
        email: String,
        telefone: String,
        senha: String
    ): Result<UsuarioEntity> {
        return try {
            val remoteUser = remoteDataSource.cadastrarAdmin(nome, email, telefone, senha)
            val usuarioLocal = UsuarioEntity(
                id = remoteUser.id,
                nome = remoteUser.nome,
                email = remoteUser.email,
                telefone = remoteUser.telefone,
                perfil = remoteUser.perfil,
                senhaHash = remoteUser.senhaHash,
                dataCriacaoUtc = System.currentTimeMillis()
            )
            usuarioDao.inserir(usuarioLocal)

            sessionManager.salvarSessao(
                UsuarioSessao(
                    usuarioId = usuarioLocal.id,
                    nome = usuarioLocal.nome,
                    email = usuarioLocal.email,
                    perfil = usuarioLocal.perfil,
                    tokenAcesso = "token-${usuarioLocal.id}"
                )
            )

            Result.success(usuarioLocal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun encerrarSessao() {
        sessionManager.limparSessao()
    }
}

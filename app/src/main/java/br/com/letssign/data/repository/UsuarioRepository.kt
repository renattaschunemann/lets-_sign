package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
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
    private val sessionManager: SessionManager,
    private val participanteDao: ParticipanteDocumentoDao? = null
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
            // Salva ou atualiza no Room local evitando conflito com índice único de e-mail
            val porId = usuarioDao.buscarPorId(usuarioLocal.id)
            val porEmail = usuarioDao.buscarPorEmail(usuarioLocal.email)
            val idFinal = porId?.id ?: porEmail?.id ?: usuarioLocal.id
            val usuarioSalvar = usuarioLocal.copy(id = idFinal)

            if (porId != null || porEmail != null) {
                usuarioDao.atualizar(usuarioSalvar)
            } else {
                usuarioDao.inserir(usuarioSalvar)
            }

            // Grava na sessão criptografada
            sessionManager.salvarSessao(
                UsuarioSessao(
                    usuarioId = usuarioSalvar.id,
                    nome = usuarioSalvar.nome,
                    email = usuarioSalvar.email,
                    perfil = usuarioSalvar.perfil,
                    tokenAcesso = "token-${usuarioSalvar.id}"
                )
            )

            Result.success(usuarioSalvar)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cadastrarAdmin(
        nome: String,
        email: String,
        telefone: String,
        senha: String,
        cpf: String = "",
        dataNascimento: String = "",
        endereco: String = ""
    ): Result<UsuarioEntity> {
        return cadastrarUsuario(nome, email, telefone, senha, br.com.letssign.domain.model.PerfilUsuario.ADMIN, cpf, dataNascimento, endereco)
    }

    suspend fun cadastrarSignatario(
        nome: String,
        email: String,
        telefone: String,
        senha: String,
        cpf: String = "",
        dataNascimento: String = "",
        endereco: String = ""
    ): Result<UsuarioEntity> {
        return cadastrarUsuario(nome, email, telefone, senha, br.com.letssign.domain.model.PerfilUsuario.SIGNATARIO, cpf, dataNascimento, endereco)
    }

    suspend fun cadastrarUsuario(
        nome: String,
        email: String,
        telefone: String,
        senha: String,
        perfil: br.com.letssign.domain.model.PerfilUsuario,
        cpf: String = "",
        dataNascimento: String = "",
        endereco: String = "",
        profissao: String = ""
    ): Result<UsuarioEntity> {
        return try {
            val remoteUser = remoteDataSource.cadastrarUsuario(
                nome = nome,
                email = email,
                telefone = telefone,
                senhaHash = senha,
                perfil = perfil,
                cpf = cpf,
                dataNascimento = dataNascimento,
                endereco = endereco,
                profissao = profissao
            )
            val usuarioLocal = UsuarioEntity(
                id = remoteUser.id,
                nome = remoteUser.nome,
                email = remoteUser.email,
                telefone = remoteUser.telefone,
                perfil = remoteUser.perfil,
                senhaHash = remoteUser.senhaHash,
                dataCriacaoUtc = System.currentTimeMillis(),
                cpf = remoteUser.cpf,
                dataNascimento = remoteUser.dataNascimento,
                endereco = remoteUser.endereco,
                profissao = remoteUser.profissao
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

            // Se for signatário, vincula documentos atribuídos a este e-mail
            if (perfil == br.com.letssign.domain.model.PerfilUsuario.SIGNATARIO) {
                participanteDao?.vincularUsuarioPorEmail(usuarioLocal.id, usuarioLocal.email)
            }

            Result.success(usuarioLocal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun encerrarSessao() {
        sessionManager.limparSessao()
    }
}

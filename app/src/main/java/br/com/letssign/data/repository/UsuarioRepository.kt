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
            val porId = usuarioDao.buscarPorId(remoteUser.id)
            val porEmail = usuarioDao.buscarPorEmail(remoteUser.email)
            val idFinal = porId?.id ?: porEmail?.id ?: remoteUser.id

            val usuarioSalvar = UsuarioEntity(
                id = idFinal,
                nome = remoteUser.nome,
                email = remoteUser.email,
                telefone = remoteUser.telefone,
                perfil = remoteUser.perfil,
                senhaHash = remoteUser.senhaHash,
                dataCriacaoUtc = porId?.dataCriacaoUtc ?: porEmail?.dataCriacaoUtc ?: System.currentTimeMillis(),
                cpf = porId?.cpf?.ifBlank { remoteUser.cpf } ?: porEmail?.cpf?.ifBlank { remoteUser.cpf } ?: remoteUser.cpf,
                dataNascimento = porId?.dataNascimento?.ifBlank { remoteUser.dataNascimento } ?: porEmail?.dataNascimento?.ifBlank { remoteUser.dataNascimento } ?: remoteUser.dataNascimento,
                endereco = porId?.endereco?.ifBlank { remoteUser.endereco } ?: porEmail?.endereco?.ifBlank { remoteUser.endereco } ?: remoteUser.endereco,
                profissao = porId?.profissao?.ifBlank { remoteUser.profissao } ?: porEmail?.profissao?.ifBlank { remoteUser.profissao } ?: remoteUser.profissao
            )

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

            // Se for signatário, vincula documentos atribuídos a este e-mail
            if (usuarioSalvar.perfil == br.com.letssign.domain.model.PerfilUsuario.SIGNATARIO) {
                participanteDao?.vincularUsuarioPorEmail(email = usuarioSalvar.email, usuarioId = usuarioSalvar.id)
            }

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
                participanteDao?.vincularUsuarioPorEmail(email = usuarioLocal.email, usuarioId = usuarioLocal.id)
            }

            Result.success(usuarioLocal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun encerrarSessao() {
        sessionManager.limparSessao()
    }

    suspend fun atualizarPerfil(
        usuarioId: String,
        novoNome: String,
        novoEmail: String,
        novoTelefone: String,
        novoCpf: String = "",
        novaDataNascimento: String = "",
        novoEndereco: String = "",
        novaProfissao: String = ""
    ): Result<UsuarioEntity> {
        return try {
            val usuarioExistentePorEmail = usuarioDao.buscarPorEmail(novoEmail)
            if (usuarioExistentePorEmail != null && usuarioExistentePorEmail.id != usuarioId) {
                return Result.failure(IllegalArgumentException("Este e-mail já está em uso por outro usuário."))
            }

            val usuarioAtual = usuarioDao.buscarPorId(usuarioId) ?: usuarioExistentePorEmail
            val usuarioSalvar = if (usuarioAtual != null) {
                usuarioAtual.copy(
                    nome = novoNome,
                    email = novoEmail,
                    telefone = novoTelefone,
                    cpf = novoCpf,
                    dataNascimento = novaDataNascimento,
                    endereco = novoEndereco,
                    profissao = novaProfissao
                )
            } else {
                UsuarioEntity(
                    id = usuarioId.ifBlank { "usuario-$novoEmail" },
                    nome = novoNome,
                    email = novoEmail,
                    telefone = novoTelefone,
                    perfil = br.com.letssign.domain.model.PerfilUsuario.ADMIN,
                    senhaHash = "admin123",
                    dataCriacaoUtc = System.currentTimeMillis(),
                    cpf = novoCpf,
                    dataNascimento = novaDataNascimento,
                    endereco = novoEndereco,
                    profissao = novaProfissao
                )
            }

            if (usuarioAtual != null) {
                usuarioDao.atualizar(usuarioSalvar)
            } else {
                usuarioDao.inserir(usuarioSalvar)
            }

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
}

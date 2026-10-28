package br.com.letssign.data.repository

import br.com.letssign.core.common.text.TextNormalizer
import br.com.letssign.data.local.dao.AdminSignatarioDao
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.domain.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatarioRepository @Inject constructor(
    private val adminSignatarioDao: AdminSignatarioDao,
    private val usuarioDao: UsuarioDao
) {

    suspend fun inicializarDadosDemoSeNecessario(adminId: String) {
        val existente = adminSignatarioDao.buscarSignatarioPorEmail(adminId, "maria.souza@empresa.com")
        if (existente != null) return

        val agora = System.currentTimeMillis()
        val demoSignatarios = listOf(
            AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = "sig-maria-souza",
                nome = "Maria Souza",
                email = "maria.souza@empresa.com",
                telefone = "(11) 91234-5678",
                dataNascimento = "14/03/1988",
                dataVinculoUtc = agora
            ),
            AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = "sig-ana-lima",
                nome = "Ana Lima",
                email = "ana.lima@empresa.com",
                telefone = "(11) 92345-6789",
                dataNascimento = "20/07/1990",
                dataVinculoUtc = agora
            ),
            AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = "sig-joao-lima",
                nome = "João Lima",
                email = "joao.lima@email.com",
                telefone = "(11) 91234-5678",
                dataNascimento = "10/05/1992",
                dataVinculoUtc = agora
            ),
            AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = "sig-pedro-alves",
                nome = "Pedro Alves",
                email = "pedro.alves@empresa.com",
                telefone = "(11) 94567-8901",
                dataNascimento = "03/11/1985",
                dataVinculoUtc = agora
            ),
            AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = "sig-carlos-silva",
                nome = "Carlos Silva",
                email = "carlos.silva@empresa.com",
                telefone = "(11) 95678-9012",
                dataNascimento = "18/09/1995",
                dataVinculoUtc = agora
            )
        )
        adminSignatarioDao.inserirVarios(demoSignatarios)
    }

    fun listarSignatarios(adminId: String): Flow<List<AdminSignatarioEntity>> {
        return adminSignatarioDao.listarSignatariosDoAdmin(adminId)
    }

    suspend fun buscarSignatario(adminId: String, signatarioId: String): AdminSignatarioEntity? {
        return adminSignatarioDao.buscarSignatarioPorId(adminId, signatarioId)
    }

    suspend fun verificarEmailPossuiConta(email: String): Boolean {
        val emailNorm = email.trim().lowercase()
        return usuarioDao.buscarPorEmail(emailNorm) != null
    }

    suspend fun salvarSignatario(
        adminId: String,
        signatarioIdExistente: String?,
        nome: String,
        email: String,
        telefone: String,
        dataNascimento: String,
        senha: String?
    ): Result<Unit> {
        val nomeNormalizado = TextNormalizer.normalizar(nome)
        if (nomeNormalizado.isBlank() || !nome.trim().contains(" ")) {
            return Result.failure(IllegalArgumentException("Nome completo inválido"))
        }

        val emailNorm = email.trim().lowercase()
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(emailNorm)) {
            return Result.failure(IllegalArgumentException("E-mail com formato inválido"))
        }

        // Validação P03: data de nascimento
        val partesData = dataNascimento.trim().split("/")
        if (partesData.size != 3) {
            return Result.failure(IllegalArgumentException("Data de nascimento inválida (dd/mm/aaaa)"))
        }
        val dataNasc = try {
            val dia = partesData[0].toInt()
            val mes = partesData[1].toInt()
            val ano = partesData[2].toInt()
            LocalDate.of(ano, mes, dia)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Data de nascimento inválida"))
        }

        val hoje = LocalDate.now()
        if (dataNasc.isAfter(hoje)) {
            return Result.failure(IllegalArgumentException("Data de nascimento não pode ser futura"))
        }

        val idade = Period.between(dataNasc, hoje).years
        if (idade < 18) {
            return Result.failure(IllegalArgumentException("Signatário deve ser maior de 18 anos (P03)"))
        }

        if (signatarioIdExistente != null) {
            // Edição
            val existente = adminSignatarioDao.buscarSignatarioPorId(adminId, signatarioIdExistente)
                ?: return Result.failure(IllegalStateException("Signatário não encontrado"))
            val atualizado = existente.copy(
                nome = nome.trim(),
                email = emailNorm,
                telefone = telefone.trim(),
                dataNascimento = dataNascimento.trim()
            )
            adminSignatarioDao.atualizar(atualizado)
        } else {
            // Novo cadastro
            val usuarioExistente = usuarioDao.buscarPorEmail(emailNorm)
            val novoSignatarioId = usuarioExistente?.id ?: UUID.randomUUID().toString()

            val agora = System.currentTimeMillis()
            if (usuarioExistente == null) {
                // Cria conta de signatário
                val novoUsuario = UsuarioEntity(
                    id = novoSignatarioId,
                    nome = nome.trim(),
                    email = emailNorm,
                    telefone = telefone.trim(),
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = senha ?: "Senha123",
                    dataCriacaoUtc = agora
                )
                usuarioDao.inserir(novoUsuario)
            }

            val relacao = AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = novoSignatarioId,
                nome = nome.trim(),
                email = emailNorm,
                telefone = telefone.trim(),
                dataNascimento = dataNascimento.trim(),
                dataVinculoUtc = agora
            )
            adminSignatarioDao.inserir(relacao)
        }

        return Result.success(Unit)
    }

    suspend fun excluirSignatario(adminId: String, signatarioId: String): Result<Unit> {
        adminSignatarioDao.deletarSignatario(adminId, signatarioId)
        return Result.success(Unit)
    }
}

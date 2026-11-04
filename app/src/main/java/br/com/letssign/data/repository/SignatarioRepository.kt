package br.com.letssign.data.repository

import br.com.letssign.core.common.text.TextNormalizer
import br.com.letssign.data.local.dao.AdminSignatarioDao
import br.com.letssign.data.local.dao.UsuarioDao
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.data.local.entities.UsuarioEntity
import br.com.letssign.data.remote.RemoteDataSource
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
    private val usuarioDao: UsuarioDao,
    private val remoteDataSource: RemoteDataSource? = null
) {

    suspend fun inicializarDadosDemoSeNecessario(adminId: String) {
        try {
            val existente = adminSignatarioDao.buscarSignatarioPorEmail(adminId, "maria.souza@empresa.com")
            if (existente != null) return

            // Garante que o usuário Admin exista na tabela 'usuarios' com dados cadastrais completos
            if (usuarioDao.buscarPorId(adminId) == null) {
                usuarioDao.inserir(
                    UsuarioEntity(
                        id = adminId,
                        nome = if (adminId == "admin-carla-mendes") "Carla Mendes" else "Administrador",
                        email = if (adminId == "admin-carla-mendes") "carla@empresa.com.br" else "admin-$adminId@empresa.com",
                        telefone = "(11) 98765-4321",
                        perfil = PerfilUsuario.ADMIN,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = System.currentTimeMillis(),
                        cpf = "12345678901",
                        dataNascimento = "15041988",
                        endereco = "Av. Paulista, 1000, Bela Vista, São Paulo - SP",
                        profissao = "Administradora de Contratos"
                    )
                )
            }

            val agora = System.currentTimeMillis()
            val demoSignatarios = listOf(
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-maria-souza",
                    nome = "Maria Souza",
                    email = "maria.souza@empresa.com",
                    telefone = "(11) 91234-5678",
                    dataNascimento = "14/03/1988",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PF",
                    cpfCnpj = "23456789012",
                    cep = "01414-001",
                    logradouro = "Rua Oscar Freire",
                    numero = "350",
                    complemento = "Apto 42",
                    bairro = "Cerqueira César",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-joao-lima",
                    nome = "João Lima",
                    email = "joao.lima@email.com",
                    telefone = "(11) 91234-5678",
                    dataNascimento = "20/08/1992",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PF",
                    cpfCnpj = "98765432199",
                    cep = "01305-000",
                    logradouro = "Rua Augusta",
                    numero = "500",
                    complemento = "Conjunto 81",
                    bairro = "Consolação",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-ana-lima",
                    nome = "Ana Lima Consultoria Financeira Ltda",
                    email = "ana.lima@empresa.com",
                    telefone = "(11) 92345-6789",
                    dataNascimento = "20/07/2015",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PJ",
                    cpfCnpj = "12345678000195",
                    cep = "01419-001",
                    logradouro = "Alameda Santos",
                    numero = "800",
                    complemento = "Sala 12",
                    bairro = "Cerqueira César",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-techsolutions",
                    nome = "TechSolutions Inovações e Softwares S.A.",
                    email = "contato@techsolutions.com.br",
                    telefone = "(11) 93456-7890",
                    dataNascimento = "10/02/2012",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PJ",
                    cpfCnpj = "45678901000123",
                    cep = "04538-133",
                    logradouro = "Av. Brigadeiro Faria Lima",
                    numero = "3477",
                    complemento = "14º andar",
                    bairro = "Itaim Bibi",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-pedro-alves",
                    nome = "Pedro Alves",
                    email = "pedro.alves@empresa.com",
                    telefone = "(11) 94567-8901",
                    dataNascimento = "03/11/1985",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PF",
                    cpfCnpj = "45678901234",
                    cep = "01301-100",
                    logradouro = "Rua da Consolação",
                    numero = "1200",
                    complemento = "Bloco B",
                    bairro = "Consolação",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-carlos-silva",
                    nome = "Carlos Silva",
                    email = "carlos.silva@empresa.com",
                    telefone = "(11) 95678-9012",
                    dataNascimento = "18/09/1995",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PF",
                    cpfCnpj = "56789012345",
                    cep = "04538-000",
                    logradouro = "Av. Presidente Juscelino Kubitschek",
                    numero = "1800",
                    complemento = "",
                    bairro = "Vila Nova Conceição",
                    cidade = "São Paulo",
                    estado = "SP"
                ),
                AdminSignatarioEntity(
                    adminId = adminId,
                    signatarioId = "sig-global-logistics",
                    nome = "Global Logistics & Transportes Ltda",
                    email = "operacoes@globallogistics.com.br",
                    telefone = "(11) 96789-0123",
                    dataNascimento = "05/08/2018",
                    dataVinculoUtc = agora,
                    tipoPessoa = "PJ",
                    cpfCnpj = "78901234000156",
                    cep = "07170-350",
                    logradouro = "Rodovia Presidente Dutra",
                    numero = "KM 218",
                    complemento = "Galpão 4",
                    bairro = "Cumbica",
                    cidade = "Guarulhos",
                    estado = "SP"
                )
            )
            adminSignatarioDao.inserirVarios(demoSignatarios)

            // Garante que cada signatário de demonstração também exista na tabela 'usuarios' com todos os dados completos
            val usuariosSignatariosDemo = listOf(
                UsuarioEntity(
                    id = "sig-maria-souza",
                    nome = "Maria Souza",
                    email = "maria.souza@empresa.com",
                    telefone = "(11) 91234-5678",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "23456789012",
                    dataNascimento = "14031988",
                    endereco = "Rua Oscar Freire, 350, Apto 42, Cerqueira César, São Paulo - SP",
                    profissao = "Advogada Corporativa"
                ),
                UsuarioEntity(
                    id = "sig-joao-lima",
                    nome = "João Lima",
                    email = "joao.lima@email.com",
                    telefone = "(11) 91234-5678",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "98765432199",
                    dataNascimento = "20081992",
                    endereco = "Rua Augusta, 500, Conjunto 81, Consolação, São Paulo - SP",
                    profissao = "Analista de Operações"
                ),
                UsuarioEntity(
                    id = "sig-ana-lima",
                    nome = "Ana Lima Consultoria Financeira Ltda",
                    email = "ana.lima@empresa.com",
                    telefone = "(11) 92345-6789",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "12345678000195",
                    dataNascimento = "20072015",
                    endereco = "Alameda Santos, 800, Sala 12, Cerqueira César, São Paulo - SP",
                    profissao = "Consultoria Empresarial e Financeira"
                ),
                UsuarioEntity(
                    id = "sig-techsolutions",
                    nome = "TechSolutions Inovações e Softwares S.A.",
                    email = "contato@techsolutions.com.br",
                    telefone = "(11) 93456-7890",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "45678901000123",
                    dataNascimento = "10022012",
                    endereco = "Av. Brigadeiro Faria Lima, 3477, 14º andar, Itaim Bibi, São Paulo - SP",
                    profissao = "Desenvolvimento de Software e Cloud"
                ),
                UsuarioEntity(
                    id = "sig-pedro-alves",
                    nome = "Pedro Alves",
                    email = "pedro.alves@empresa.com",
                    telefone = "(11) 94567-8901",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "45678901234",
                    dataNascimento = "03111985",
                    endereco = "Rua da Consolação, 1200, Bloco B, Consolação, São Paulo - SP",
                    profissao = "Engenheiro Civil e Avaliador"
                ),
                UsuarioEntity(
                    id = "sig-carlos-silva",
                    nome = "Carlos Silva",
                    email = "carlos.silva@empresa.com",
                    telefone = "(11) 95678-9012",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "56789012345",
                    dataNascimento = "18091995",
                    endereco = "Av. Presidente Juscelino Kubitschek, 1800, Vila Nova Conceição, São Paulo - SP",
                    profissao = "Arquiteto de Soluções Cloud"
                ),
                UsuarioEntity(
                    id = "sig-global-logistics",
                    nome = "Global Logistics & Transportes Ltda",
                    email = "operacoes@globallogistics.com.br",
                    telefone = "(11) 96789-0123",
                    perfil = PerfilUsuario.SIGNATARIO,
                    senhaHash = "Senha123",
                    dataCriacaoUtc = agora,
                    cpf = "78901234000156",
                    dataNascimento = "05082018",
                    endereco = "Rodovia Presidente Dutra, KM 218, Galpão 4, Cumbica, Guarulhos - SP",
                    profissao = "Transporte e Logística Integrada"
                )
            )
            for (usuario in usuariosSignatariosDemo) {
                if (usuarioDao.buscarPorId(usuario.id) == null && usuarioDao.buscarPorEmail(usuario.email) == null) {
                    usuarioDao.inserir(usuario)
                }
            }
        } catch (e: Exception) {
            println("SignatarioRepository: aviso ao inicializar dados demo: ${e.message}")
        }
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
        senha: String?,
        tipoPessoa: String = "PF",
        cpfCnpj: String = "",
        cep: String = "",
        logradouro: String = "",
        numero: String = "",
        complemento: String = "",
        bairro: String = "",
        cidade: String = "",
        estado: String = ""
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
            // Edição local
            val existente = adminSignatarioDao.buscarSignatarioPorId(adminId, signatarioIdExistente)
                ?: return Result.failure(IllegalStateException("Signatário não encontrado"))
            val atualizado = existente.copy(
                nome = nome.trim(),
                email = emailNorm,
                telefone = telefone.trim(),
                dataNascimento = dataNascimento.trim(),
                tipoPessoa = tipoPessoa,
                cpfCnpj = cpfCnpj.trim(),
                cep = cep.trim(),
                logradouro = logradouro.trim(),
                numero = numero.trim(),
                complemento = complemento.trim(),
                bairro = bairro.trim(),
                cidade = cidade.trim(),
                estado = estado.trim()
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
                    dataCriacaoUtc = agora,
                    tipoPessoa = tipoPessoa,
                    cpfCnpj = cpfCnpj.trim(),
                    cep = cep.trim(),
                    logradouro = logradouro.trim(),
                    numero = numero.trim(),
                    complemento = complemento.trim(),
                    bairro = bairro.trim(),
                    cidade = cidade.trim(),
                    estado = estado.trim()
                )
                usuarioDao.inserir(novoUsuario)

                // Notifica persistência remota/REST se disponível
                try {
                    remoteDataSource?.cadastrarSignatario(
                        nome = nome.trim(),
                        email = emailNorm,
                        telefone = telefone.trim(),
                        senhaHash = senha ?: "Senha123",
                        dataNascimento = dataNascimento.trim()
                    )
                } catch (e: Exception) {
                    println("SignatarioRepository: aviso ao sincronizar signatario remotamente: ${e.message}")
                }
            }

            val relacao = AdminSignatarioEntity(
                adminId = adminId,
                signatarioId = novoSignatarioId,
                nome = nome.trim(),
                email = emailNorm,
                telefone = telefone.trim(),
                dataNascimento = dataNascimento.trim(),
                dataVinculoUtc = agora,
                tipoPessoa = tipoPessoa,
                cpfCnpj = cpfCnpj.trim(),
                cep = cep.trim(),
                logradouro = logradouro.trim(),
                numero = numero.trim(),
                complemento = complemento.trim(),
                bairro = bairro.trim(),
                cidade = cidade.trim(),
                estado = estado.trim()
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

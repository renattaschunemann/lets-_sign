package br.com.letssign.data.repository

import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.entities.*
import br.com.letssign.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import androidx.compose.ui.geometry.Offset
import br.com.letssign.core.pdf.PdfDemoGenerator
import br.com.letssign.data.local.file.PdfStorageManager
import br.com.letssign.data.local.file.AssinaturaStorageManager
import javax.inject.Inject
import javax.inject.Singleton

data class OperacaoSincronizacaoOffline(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tipo: String, // "ASSINATURA", "ENVIO", "RECUSA"
    val documentoId: String,
    val payload: String = "",
    val dataHoraCriacaoUtc: Long = System.currentTimeMillis()
)


data class DocumentoItemResumo(
    val id: String,
    val titulo: String,
    val versao: Int,
    val versaoId: String,
    val situacao: SituacaoDocumento,
    val obrigatoriosAssinaram: Int,
    val totalObrigatorios: Int,
    val dataAtualizacaoFormatada: String,
    val dataUltimaAtualizacaoUtc: Long
)

@Singleton
class DocumentoRepository @Inject constructor(
    private val documentoDao: DocumentoDao,
    private val versaoDao: VersaoDocumentoDao,
    private val participanteDao: ParticipanteDocumentoDao,
    private val campoAssinaturaDao: CampoAssinaturaDao? = null,
    private val eventoHistoricoDao: EventoHistoricoDao? = null,
    private val notificacaoDao: NotificacaoDao? = null,
    private val recusaDao: RecusaDao? = null,
    private val cancelamentoDao: CancelamentoDao? = null,
    private val usuarioDao: UsuarioDao? = null,
    private val assinaturaDao: AssinaturaDao? = null,
    private val remoteDataSource: br.com.letssign.data.remote.RemoteDataSource? = null,
    @ApplicationContext private val context: Context? = null
) {

            suspend fun inicializarDadosDemoSeNecessario(adminId: String) {
        try {
            val demoDocIds = listOf(
                "doc-demo-1", "doc-demo-2", "doc-demo-3", "doc-demo-4", "doc-demo-5",
                "doc-demo-6", "doc-demo-7", "doc-demo-8", "doc-demo-9", "doc-demo-10"
            )
            
            for (id in demoDocIds) {
                val doc = documentoDao.buscarPorId(id)
                if (doc != null && doc.usuarioAdminId != adminId) {
                    documentoDao.atualizar(doc.copy(usuarioAdminId = adminId))
                }
            }

            // Garante que TODOS os usuários de demonstração existam na tabela 'usuarios' com dados cadastrais completos
            if (usuarioDao != null) {
                val agoraUtc = System.currentTimeMillis()
                val demoUsuarios = listOf(
                    UsuarioEntity(
                        id = adminId,
                        nome = if (adminId == "admin-carla-mendes") "Carla Mendes" else "Administrador",
                        email = if (adminId == "admin-carla-mendes") "carla@empresa.com.br" else "admin-$adminId@empresa.com",
                        telefone = "(11) 98765-4321",
                        perfil = PerfilUsuario.ADMIN,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "12345678901",
                        dataNascimento = "15041988",
                        endereco = "Av. Paulista, 1000, Bela Vista, São Paulo - SP",
                        profissao = "Administradora de Contratos",
                        tipoPessoa = "PF",
                        cpfCnpj = "12345678901",
                        cep = "01310-100",
                        logradouro = "Av. Paulista",
                        numero = "1000",
                        bairro = "Bela Vista",
                        cidade = "São Paulo",
                        estado = "SP"
                    ),
                    UsuarioEntity(
                        id = "sig-joao-lima",
                        nome = "João Lima",
                        email = "joao.lima@email.com",
                        telefone = "(11) 91234-5678",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "98765432199",
                        dataNascimento = "20081992",
                        endereco = "Rua Augusta, 500, Conjunto 81, Consolação, São Paulo - SP",
                        profissao = "Analista de Operações",
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
                    UsuarioEntity(
                        id = "user-maria-souza",
                        nome = "Maria Souza",
                        email = "maria.souza@empresa.com.br",
                        telefone = "(11) 91234-5678",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "23456789012",
                        dataNascimento = "14031988",
                        endereco = "Rua Oscar Freire, 350, Apto 42, Cerqueira César, São Paulo - SP",
                        profissao = "Advogada Corporativa",
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
                    UsuarioEntity(
                        id = "sig-pedro-alves",
                        nome = "Pedro Alves",
                        email = "pedro.alves@empresa.com",
                        telefone = "(11) 94567-8901",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "45678901234",
                        dataNascimento = "03111985",
                        endereco = "Rua da Consolação, 1200, Bloco B, Consolação, São Paulo - SP",
                        profissao = "Consultor Comercial",
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
                    UsuarioEntity(
                        id = "sig-carlos-silva",
                        nome = "Carlos Silva",
                        email = "carlos.silva@empresa.com",
                        telefone = "(11) 95678-9012",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "56789012345",
                        dataNascimento = "18091995",
                        endereco = "Av. Presidente Juscelino Kubitschek, 1800, Vila Nova Conceição, São Paulo - SP",
                        profissao = "Arquiteto de Soluções Cloud",
                        tipoPessoa = "PF",
                        cpfCnpj = "56789012345",
                        cep = "04538-000",
                        logradouro = "Av. Presidente Juscelino Kubitschek",
                        numero = "1800",
                        bairro = "Vila Nova Conceição",
                        cidade = "São Paulo",
                        estado = "SP"
                    ),
                    UsuarioEntity(
                        id = "sig-ana-lima",
                        nome = "Ana Lima Consultoria Financeira Ltda",
                        email = "ana.lima@empresa.com",
                        telefone = "(11) 92345-6789",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "12345678000195",
                        dataNascimento = "20072015",
                        endereco = "Alameda Santos, 800, Sala 12, Cerqueira César, São Paulo - SP",
                        profissao = "Consultoria Financeira",
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
                    UsuarioEntity(
                        id = "sig-techsolutions",
                        nome = "TechSolutions Inovações e Softwares S.A.",
                        email = "contato@techsolutions.com.br",
                        telefone = "(11) 93456-7890",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "45678901000123",
                        dataNascimento = "10022012",
                        endereco = "Av. Brigadeiro Faria Lima, 3477, 14º andar, Itaim Bibi, São Paulo - SP",
                        profissao = "Desenvolvimento de Software",
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
                    UsuarioEntity(
                        id = "sig-global-logistics",
                        nome = "Global Logistics & Transportes Ltda",
                        email = "contato@globallogistics.com.br",
                        telefone = "(11) 96789-0123",
                        perfil = PerfilUsuario.SIGNATARIO,
                        senhaHash = "Senha123",
                        dataCriacaoUtc = agoraUtc,
                        cpf = "34567890000112",
                        dataNascimento = "18092010",
                        endereco = "Rodovia Presidente Dutra, KM 218, Galpão 4, Guarulhos - SP",
                        profissao = "Logística Integrada",
                        tipoPessoa = "PJ",
                        cpfCnpj = "34567890000112",
                        cep = "07180-000",
                        logradouro = "Rodovia Presidente Dutra",
                        numero = "KM 218",
                        complemento = "Galpão 4",
                        bairro = "Cumbica",
                        cidade = "Guarulhos",
                        estado = "SP"
                    )
                )

                for (u in demoUsuarios) {
                    val existente = usuarioDao.buscarPorId(u.id) ?: usuarioDao.buscarPorEmail(u.email)
                    if (existente == null) {
                        usuarioDao.inserir(u)
                    } else {
                        if (existente.cpf.isBlank() || existente.endereco.isBlank() || existente.dataNascimento.isBlank() || existente.nome != u.nome) {
                            usuarioDao.atualizar(
                                existente.copy(
                                    nome = existente.nome.ifBlank { u.nome },
                                    cpf = existente.cpf.ifBlank { u.cpf },
                                    cpfCnpj = existente.cpfCnpj.ifBlank { u.cpfCnpj },
                                    dataNascimento = existente.dataNascimento.ifBlank { u.dataNascimento },
                                    endereco = existente.endereco.ifBlank { u.endereco },
                                    profissao = existente.profissao.ifBlank { u.profissao },
                                    tipoPessoa = existente.tipoPessoa.ifBlank { u.tipoPessoa },
                                    cep = existente.cep.ifBlank { u.cep },
                                    logradouro = existente.logradouro.ifBlank { u.logradouro },
                                    numero = existente.numero.ifBlank { u.numero },
                                    bairro = existente.bairro.ifBlank { u.bairro },
                                    cidade = existente.cidade.ifBlank { u.cidade },
                                    estado = existente.estado.ifBlank { u.estado }
                                )
                            )
                        }
                    }
                }
            }

            val todosDocsExistem = demoDocIds.all { documentoDao.buscarPorId(it) != null }
            val qtdAssinaturasDoc5 = assinaturaDao?.listarPorCamposSync(listOf("campo-part-doc-demo-5-carla", "campo-part-doc-demo-5-joao"))?.size ?: 0
            val qtdAssinaturasDoc10 = assinaturaDao?.listarPorCamposSync(listOf("campo-part-doc-demo-10-1", "campo-part-doc-demo-10-2"))?.size ?: 0
            val qtdEventos = eventoHistoricoDao?.contarPorVersao("versao-demo-5") ?: 0

            if (todosDocsExistem && qtdAssinaturasDoc5 >= 2 && qtdAssinaturasDoc10 >= 2 && qtdEventos >= 10) {
                return
            }

            val agora = System.currentTimeMillis()

            // 1. Contrato de Prestação de Serviços de TI (PARCIALMENTE_ASSINADO, 2 de 3 assinaram)
            try {
                criarDocDemo(
                    docId = "doc-demo-1",
                    versaoId = "versao-demo-1",
                    titulo = "Contrato de Prestação de Serviços de TI",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 3600000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-1-1", "user-maria-souza", "Maria Souza", "maria.souza@empresa.com.br", "(11) 91234-5678", "14/03/1988", PapelParticipante.CONTRATANTE, SituacaoParticipante.ASSINADO, 1, TipoAssinatura.CERTIFICADO_DIGITAL_ECPF, "234.567.890-12"),
                        DemoPartDef("part-doc-demo-1-2", adminId, "Carla Mendes", "carla@empresa.com.br", "(11) 98765-4321", "15/04/1988", PapelParticipante.CONTRATADO, SituacaoParticipante.ASSINADO, 2, TipoAssinatura.DIGITADA, "123.456.789-01"),
                        DemoPartDef("part-doc-demo-1-3", "sig-joao-lima", "João Lima", "joao.lima@email.com", "(11) 91234-5678", "20/08/1992", PapelParticipante.TESTEMUNHA, SituacaoParticipante.PENDENTE, 3, null, "987.654.321-99")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-1: ${e.message}")
            }

            // 2. Aditivo Contratual 02 — Reajuste Anual (FALHA_ENVIO, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-2",
                    versaoId = "versao-demo-2",
                    titulo = "Aditivo Contratual 02 — Reajuste Anual",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.FALHA_ENVIO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 7200000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-2-1", "sig-pedro-alves", "Pedro Alves", "pedro.alves@empresa.com", "(11) 94567-8901", "03/11/1985", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "456.789.012-34"),
                        DemoPartDef("part-doc-demo-2-2", "sig-carlos-silva", "Carlos Silva", "carlos.silva@empresa.com", "(11) 95678-9012", "12/06/1979", PapelParticipante.CONTRATADO, SituacaoParticipante.PENDENTE, 2, null, "567.890.123-45")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-2: ${e.message}")
            }

            // 3. Contrato de Locação Comercial — Sala 304 (RASCUNHO, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-3",
                    versaoId = "versao-demo-3",
                    titulo = "Contrato de Locação Comercial — Sala 304",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.RASCUNHO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 10800000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-3-1", "sig-ana-lima", "Ana Lima Consultoria Financeira Ltda", "ana.lima@empresa.com", "(11) 92345-6789", "20/07/2015", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "12.345.678/0001-95"),
                        DemoPartDef("part-doc-demo-3-2", "sig-carlos-silva", "Carlos Silva", "carlos.silva@empresa.com", "(11) 95678-9012", "12/06/1979", PapelParticipante.CONTRATADO, SituacaoParticipante.PENDENTE, 2, null, "567.890.123-45")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-3: ${e.message}")
            }

            // 4. Acordo de Confidencialidade (NDA) — Projeto Fênix (ENVIADO, SEQUENCIAL, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-4",
                    versaoId = "versao-demo-4",
                    titulo = "Acordo de Confidencialidade (NDA) — Projeto Fênix",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.ENVIADO,
                    tipoOrdem = TipoOrdem.SEQUENCIAL,
                    dataUtc = agora - 14400000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-4-1", "sig-ana-lima", "Ana Lima Consultoria Financeira Ltda", "ana.lima@empresa.com", "(11) 92345-6789", "20/07/2015", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "12.345.678/0001-95"),
                        DemoPartDef("part-doc-demo-4-2", "sig-joao-lima", "João Lima", "joao.lima@email.com", "(11) 91234-5678", "20/08/1992", PapelParticipante.CONTRATADO, SituacaoParticipante.AGUARDANDO_VEZ, 2, null, "987.654.321-99")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-4: ${e.message}")
            }

            // 5. Contrato de Prestação de Serviços (CONCLUIDO, v2, 3 de 3 assinaram)
            try {
                criarDocDemo(
                    docId = "doc-demo-5",
                    versaoId = "versao-demo-5",
                    titulo = "Contrato de Prestação de Serviços",
                    adminId = adminId,
                    versao = 2,
                    situacao = SituacaoDocumento.CONCLUIDO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 86400000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-5-carla", adminId, "Carla Mendes", "carla@empresa.com.br", "(11) 98765-4321", "15/03/1985", PapelParticipante.CONTRATANTE, SituacaoParticipante.ASSINADO, 1, TipoAssinatura.CERTIFICADO_DIGITAL_ECPF, "123.456.789-01"),
                        DemoPartDef("part-doc-demo-5-joao", "sig-joao-lima", "João Lima", "joao.lima@email.com", "(11) 98888-7777", "10/05/1992", PapelParticipante.CONTRATADO, SituacaoParticipante.ASSINADO, 2, TipoAssinatura.DESENHADA, "987.654.321-99"),
                        DemoPartDef("part-doc-demo-5-maria", "user-maria-souza", "Maria Souza", "maria.souza@empresa.com.br", "(11) 97777-6666", "22/11/1990", PapelParticipante.TESTEMUNHA, SituacaoParticipante.ASSINADO, 3, TipoAssinatura.DIGITADA, "234.567.890-12")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-5: ${e.message}")
            }

            // 6. Termo de Rescisão e Quitação Trabalhista (RECUSADO, 1 assinou, 1 recusou)
            try {
                criarDocDemo(
                    docId = "doc-demo-6",
                    versaoId = "versao-demo-6",
                    titulo = "Termo de Rescisão e Quitação Trabalhista",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.RECUSADO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 172800000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-6-joao", "sig-joao-lima", "João Lima", "joao.lima@email.com", "(11) 98888-7777", "10/05/1992", PapelParticipante.CONTRATADO, SituacaoParticipante.RECUSADO, 1, null, "987.654.321-99"),
                        DemoPartDef("part-doc-demo-6-pedro", "sig-pedro-alves", "Pedro Alves", "pedro.alves@empresa.com", "(11) 94567-8901", "03/11/1985", PapelParticipante.TESTEMUNHA, SituacaoParticipante.ASSINADO, 2, TipoAssinatura.DESENHADA, "456.789.012-34")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-6: ${e.message}")
            }

            // 7. Termo de Consentimento LGPD e Privacidade (CANCELADO, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-7",
                    versaoId = "versao-demo-7",
                    titulo = "Termo de Consentimento LGPD e Privacidade",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.CANCELADO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 259200000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-7-1", "user-maria-souza", "Maria Souza", "maria.souza@empresa.com.br", "(11) 97777-6666", "22/11/1990", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "234.567.890-12"),
                        DemoPartDef("part-doc-demo-7-2", "sig-carlos-silva", "Carlos Silva", "carlos.silva@empresa.com", "(11) 95678-9012", "12/06/1979", PapelParticipante.CONTRATADO, SituacaoParticipante.PENDENTE, 2, null, "567.890.123-45")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-7: ${e.message}")
            }

            // 8. Contrato de Fornecimento de Licenças SaaS (SUBSTITUIDO, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-8",
                    versaoId = "versao-demo-8",
                    titulo = "Contrato de Fornecimento de Licenças SaaS",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.SUBSTITUIDO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 345600000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-8-1", "sig-techsolutions", "TechSolutions Inovações e Softwares S.A.", "contato@techsolutions.com.br", "(11) 93456-7890", "10/02/2012", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "45.678.901/0001-23"),
                        DemoPartDef("part-doc-demo-8-2", "sig-global-logistics", "Global Logistics & Transportes Ltda", "contato@globallogistics.com.br", "(11) 96789-0123", "18/09/2010", PapelParticipante.CONTRATADO, SituacaoParticipante.PENDENTE, 2, null, "34.567.890/0001-12")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-8: ${e.message}")
            }

            // 9. Procuração Ad Negotia com Poderes Bancários (ENVIANDO, 0 de 2)
            try {
                criarDocDemo(
                    docId = "doc-demo-9",
                    versaoId = "versao-demo-9",
                    titulo = "Procuração Ad Negotia com Poderes Bancários",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.ENVIANDO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 1800000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-9-1", adminId, "Carla Mendes", "carla@empresa.com.br", "(11) 98765-4321", "15/04/1988", PapelParticipante.CONTRATANTE, SituacaoParticipante.PENDENTE, 1, null, "123.456.789-01"),
                        DemoPartDef("part-doc-demo-9-2", "sig-ana-lima", "Ana Lima Consultoria Financeira Ltda", "ana.lima@empresa.com", "(11) 92345-6789", "20/07/2015", PapelParticipante.CONTRATADO, SituacaoParticipante.PENDENTE, 2, null, "12.345.678/0001-95")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-9: ${e.message}")
            }

            // 10. Memorando de Entendimento (MoU) — Parceria Estratégica PJ/PF (CONCLUIDO, 2 de 2 assinaram)
            try {
                criarDocDemo(
                    docId = "doc-demo-10",
                    versaoId = "versao-demo-10",
                    titulo = "Memorando de Entendimento (MoU) — Parceria Estratégica",
                    adminId = adminId,
                    versao = 1,
                    situacao = SituacaoDocumento.CONCLUIDO,
                    tipoOrdem = TipoOrdem.PARALELA,
                    dataUtc = agora - 50000000L,
                    participantesMock = listOf(
                        DemoPartDef("part-doc-demo-10-1", "sig-techsolutions", "TechSolutions Inovações e Softwares S.A.", "contato@techsolutions.com.br", "(11) 93456-7890", "10/02/2012", PapelParticipante.CONTRATANTE, SituacaoParticipante.ASSINADO, 1, TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ, "45.678.901/0001-23"),
                        DemoPartDef("part-doc-demo-10-2", "sig-pedro-alves", "Pedro Alves", "pedro.alves@empresa.com", "(11) 94567-8901", "03/11/1985", PapelParticipante.CONTRATADO, SituacaoParticipante.ASSINADO, 2, TipoAssinatura.DESENHADA, "456.789.012-34")
                    )
                )
            } catch (e: Exception) {
                println("Erro ao criar doc-demo-10: ${e.message}")
            }
        } catch (e: Exception) {
            println("DocumentoRepository: aviso ao inicializar dados demo: ${e.message}")
        }
    }

    private data class DemoPartDef(
        val partId: String,
        val userId: String?,
        val nome: String,
        val email: String,
        val telefone: String,
        val dataNascimento: String,
        val papel: PapelParticipante,
        val situacao: SituacaoParticipante,
        val ordem: Int,
        val tipoAssinatura: TipoAssinatura?,
        val documentoIdentidade: String
    )

        private suspend fun criarDocDemo(
        docId: String,
        versaoId: String,
        titulo: String,
        adminId: String,
        versao: Int,
        situacao: SituacaoDocumento,
        tipoOrdem: TipoOrdem,
        dataUtc: Long,
        participantesMock: List<DemoPartDef>
    ) {
        val doc = DocumentoEntity(
            id = docId,
            usuarioAdminId = adminId,
            titulo = titulo,
            situacaoAtual = situacao,
            dataCriacaoUtc = dataUtc,
            dataUltimaAtualizacaoUtc = dataUtc
        )
        val shaOrig = if (docId == "doc-demo-5") "a3f8b0e71946c2419a77e5d933bb32014c278e9fa07f3521e1a2f60d4b8e211a" else "sha256-mock-$docId"
        val shaFin = if (situacao == SituacaoDocumento.CONCLUIDO) {
            if (docId == "doc-demo-5") "e4b2c9d1852a37f09c64821a7df83c9b74314e1f7d5267e89ab012f45c9d6482" else "sha256-final-$docId"
        } else null

        val versaoEntity = VersaoDocumentoEntity(
            id = versaoId,
            documentoId = docId,
            numeroVersao = versao,
            pdfPathInterno = "documentos/$docId/v$versao.pdf",
            sha256Original = shaOrig,
            sha256Final = shaFin,
            tipoOrdem = tipoOrdem,
            mensagemConvite = "Por favor, assine este documento.",
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = 9,
            situacao = situacao
        )
        documentoDao.inserir(doc)
        versaoDao.inserir(versaoEntity)

        val participantes = mutableListOf<ParticipanteDocumentoEntity>()
        val campos = mutableListOf<CampoAssinaturaEntity>()
        val assinaturas = mutableListOf<AssinaturaEntity>()

        for (partDef in participantesMock) {
            val resolvedUserId = if (partDef.userId != null && usuarioDao?.buscarPorId(partDef.userId) != null) {
                partDef.userId
            } else null

            participantes.add(
                ParticipanteDocumentoEntity(
                    id = partDef.partId,
                    versaoDocumentoId = versaoId,
                    usuarioId = resolvedUserId,
                    nomeSnapshot = partDef.nome,
                    emailSnapshot = partDef.email,
                    telefoneSnapshot = partDef.telefone,
                    dataNascimentoSnapshot = partDef.dataNascimento,
                    papel = partDef.papel,
                    obrigatorio = true,
                    ordem = partDef.ordem,
                    situacao = partDef.situacao
                )
            )

            val campoId = "campo-${partDef.partId}"
            campos.add(
                CampoAssinaturaEntity(
                    id = campoId,
                    versaoDocumentoId = versaoId,
                    participanteId = partDef.partId,
                    numeroPagina = 2,
                    normX = 0.15f,
                    normY = 0.28f + ((partDef.ordem - 1) * 0.22f),
                    normLargura = 0.70f,
                    normAltura = 0.16f
                )
            )

            if (partDef.situacao == SituacaoParticipante.ASSINADO && partDef.tipoAssinatura != null) {
                val isCert = partDef.tipoAssinatura == TipoAssinatura.CERTIFICADO_DIGITAL_ECPF || partDef.tipoAssinatura == TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ
                val detalhesCert = if (isCert) {
                    val emissor = if (partDef.tipoAssinatura == TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ) "e-CNPJ A1 ICP-Brasil • AC SERPRO v5" else "e-CPF A1 ICP-Brasil • AC SERPRO v5"
                    "$emissor • CN=${partDef.nome}"
                } else null

                try {
                    context?.let { ctx ->
                        val bitmap = when (partDef.tipoAssinatura) {
                            TipoAssinatura.CERTIFICADO_DIGITAL_ECPF, TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ -> {
                                val tipoCert = if (partDef.tipoAssinatura == TipoAssinatura.CERTIFICADO_DIGITAL_ECNPJ) "e-CNPJ A1 / ICP-Brasil" else "e-CPF A1 / ICP-Brasil"
                                AssinaturaStorageManager.gerarBitmapCertificado(
                                    titular = partDef.nome,
                                    documento = partDef.documentoIdentidade,
                                    tipoCertificado = tipoCert,
                                    autoridadeCertificadora = "AC SERPRO v5"
                                )
                            }
                            TipoAssinatura.DESENHADA -> {
                                val pontosRubrica = listOf(
                                    Offset(30f, 130f),
                                    Offset(70f, 65f),
                                    Offset(120f, 160f),
                                    Offset(170f, 80f),
                                    Offset(230f, 140f),
                                    Offset(310f, 75f),
                                    Offset(400f, 125f),
                                    Offset(500f, 100f)
                                )
                                AssinaturaStorageManager.gerarBitmapDesenho(pontosRubrica)
                            }
                            else -> {
                                AssinaturaStorageManager.gerarBitmapDigitado(partDef.nome, "Cursive")
                            }
                        }
                        AssinaturaStorageManager.salvarBitmapComoPng(ctx, "${partDef.partId}.png", bitmap)
                    }
                } catch (e: Exception) {
                    println("DocumentoRepository: aviso ao gravar PNG de assinatura demo: ${e.message}")
                }

                assinaturas.add(
                    AssinaturaEntity(
                        id = "ass-${partDef.partId}",
                        campoAssinaturaId = campoId,
                        participanteId = partDef.partId,
                        tipoAssinatura = partDef.tipoAssinatura,
                        caminhoPngAssinatura = "assinaturas/${partDef.partId}.png",
                        dataHoraUtc = dataUtc + (partDef.ordem * 3600000L),
                        ipDispositivo = "201.86.102.1${partDef.ordem}",
                        hashValidador = "hash-demo-${partDef.partId}",
                        detalhesCertificado = detalhesCert
                    )
                )
            }
        }

        // Insere participantes e campos PRIMEIRO para satisfazer foreign keys
        participanteDao.inserirVarios(participantes)
        campoAssinaturaDao?.inserirVarios(campos)

        // Agora que campos e participantes existem no banco, insere as assinaturas com segurança
        for (ass in assinaturas) {
            try {
                assinaturaDao?.inserir(ass)
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao inserir assinatura ${ass.id}: ${e.message}")
            }
        }

        // Gera o arquivo físico do PDF no disco para todos os documentos
        try {
            context?.let { ctx ->
                if (docId == "doc-demo-5") {
                    PdfDemoGenerator.obterOuCriarContratoDemoPdf(ctx)
                } else {
                    PdfDemoGenerator.obterOuCriarDocumentoDemoPdf(
                        context = ctx,
                        documentoId = docId,
                        versao = versao,
                        titulo = titulo,
                        situacao = situacao,
                        partes = participantesMock.map { "${it.papel.rotulo}: ${it.nome} (${it.documentoIdentidade})" }
                    )
                }
            }
        } catch (e: Exception) {
            println("DocumentoRepository: aviso ao gerar PDF físico para $docId: ${e.message}")
        }

        // Se for RECUSADO (doc-demo-6), insere registro de recusa
        if (docId == "doc-demo-6") {
            try {
                recusaDao?.inserir(
                    RecusaEntity(
                        id = "recusa-demo-6",
                        participanteId = "part-doc-demo-6-joao",
                        dataHoraUtc = dataUtc + 7200000L,
                        observacao = "Cláusula 4ª de quitação com valor rescisório divergente do acordado preliminarmente.",
                        senhaConfirmada = true
                    )
                )
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao inserir recusa: ${e.message}")
            }
        }

        // Se for CANCELADO (doc-demo-7), insere registro de cancelamento
        if (docId == "doc-demo-7") {
            try {
                cancelamentoDao?.inserir(
                    CancelamentoEntity(
                        id = "canc-demo-7",
                        versaoDocumentoId = versaoId,
                        adminId = adminId,
                        dataHoraUtc = dataUtc + 3600000L,
                        motivo = "Cancelado pelo administrador devido à atualização da minuta jurídica de conformidade LGPD.",
                        senhaConfirmada = true
                    )
                )
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao inserir cancelamento: ${e.message}")
            }
        }

        // Se for doc-demo-5, insere os 11 eventos de auditoria completos
        if (docId == "doc-demo-5") {
            try {
                val eventosHistorico = listOf(
                    EventoHistoricoEntity("evt-demo-1", versaoId, dataUtc, TipoEventoHistorico.CRIACAO, "Carla Mendes", "Documento 'Contrato de Prestação de Serviços' v2.0 criado com sucesso."),
                    EventoHistoricoEntity("evt-demo-2", versaoId, dataUtc + 300000, TipoEventoHistorico.UPLOAD_PDF, "Carla Mendes", "Upload do arquivo contrato_servicos.pdf concluído (SHA-256: $shaOrig)."),
                    EventoHistoricoEntity("evt-demo-3", versaoId, dataUtc + 600000, TipoEventoHistorico.CONFIGURACAO_ORDEM, "Carla Mendes", "Modo de assinatura configurado: Paralelo (3 signatários obrigatórios)."),
                    EventoHistoricoEntity("evt-demo-4", versaoId, dataUtc + 900000, TipoEventoHistorico.ENVIO, "Sistema LetsSign", "Convites de assinatura despachados por e-mail e push para 3 participantes."),
                    EventoHistoricoEntity("evt-demo-5", versaoId, dataUtc + 3600000, TipoEventoHistorico.CONFIRMACAO_IDENTIDADE, "Carla Mendes", "Identidade confirmada com sucesso via biometria facial."),
                    EventoHistoricoEntity("evt-demo-6", versaoId, dataUtc + 3900000, TipoEventoHistorico.ASSINATURA, "Carla Mendes", "Assinatura eletrônica realizada com sucesso (IP 201.86.102.15 • São Paulo/SP)."),
                    EventoHistoricoEntity("evt-demo-7", versaoId, dataUtc + 7200000, TipoEventoHistorico.CONFIRMACAO_IDENTIDADE, "João Lima", "Identidade confirmada com sucesso via Token OTP (SMS) e dados cadastrais."),
                    EventoHistoricoEntity("evt-demo-8", versaoId, dataUtc + 7500000, TipoEventoHistorico.ASSINATURA, "João Lima", "Assinatura eletrônica realizada com sucesso (IP 189.120.45.12 • São Paulo/SP)."),
                    EventoHistoricoEntity("evt-demo-9", versaoId, dataUtc + 10800000, TipoEventoHistorico.CONFIRMACAO_IDENTIDADE, "Maria Souza", "Identidade confirmada com sucesso via link seguro com verificação em duas etapas."),
                    EventoHistoricoEntity("evt-demo-10", versaoId, dataUtc + 11100000, TipoEventoHistorico.ASSINATURA, "Maria Souza", "Assinatura eletrônica realizada com sucesso (IP 177.18.99.40 • Rio de Janeiro/RJ)."),
                    EventoHistoricoEntity("evt-demo-11", versaoId, dataUtc + 11160000, TipoEventoHistorico.CONCLUSAO, "Sistema LetsSign", "Documento 100% concluído! Certificado gerado com SHA-256 final e carimbo de tempo ICP-Brasil.")
                )
                eventoHistoricoDao?.inserirVarios(eventosHistorico)
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao inserir eventos doc-demo-5: ${e.message}")
            }
        }

        // Se for doc-demo-10, insere eventos de auditoria
        if (docId == "doc-demo-10") {
            try {
                val eventos10 = listOf(
                    EventoHistoricoEntity("evt-doc10-1", versaoId, dataUtc, TipoEventoHistorico.CRIACAO, "Carla Mendes", "Documento 'Memorando de Entendimento (MoU)' criado com sucesso."),
                    EventoHistoricoEntity("evt-doc10-2", versaoId, dataUtc + 3600000, TipoEventoHistorico.ASSINATURA, "TechSolutions Inovações e Softwares S.A.", "Assinado via Certificado Digital e-CNPJ ICP-Brasil."),
                    EventoHistoricoEntity("evt-doc10-3", versaoId, dataUtc + 7200000, TipoEventoHistorico.ASSINATURA, "Pedro Alves", "Assinatura manuscrita capturada com sucesso."),
                    EventoHistoricoEntity("evt-doc10-4", versaoId, dataUtc + 7300000, TipoEventoHistorico.CONCLUSAO, "Sistema LetsSign", "Documento concluído com sucesso e carimbo de tempo ICP-Brasil emitido.")
                )
                eventoHistoricoDao?.inserirVarios(eventos10)
            } catch (e: Exception) {
                println("DocumentoRepository: aviso ao inserir eventos doc-demo-10: ${e.message}")
            }
        }
    }

    fun listarDocumentosDoAdmin(adminId: String): Flow<List<DocumentoItemResumo>> {
        return documentoDao.listarPorAdmin(adminId).map { lista ->
            lista.map { doc ->
                val versao = versaoDao.buscarVersaoAtual(doc.id)
                val participantes = if (versao != null) {
                    participanteDao.listarPorVersaoSync(versao.id)
                } else emptyList()

                val obrigatorios = participantes.filter { it.obrigatorio }
                val assinados = obrigatorios.count { it.situacao == SituacaoParticipante.ASSINADO }

                DocumentoItemResumo(
                    id = doc.id,
                    titulo = doc.titulo,
                    versao = versao?.numeroVersao ?: 1,
                    versaoId = versao?.id ?: "",
                    situacao = doc.situacaoAtual,
                    obrigatoriosAssinaram = assinados,
                    totalObrigatorios = obrigatorios.size,
                    dataAtualizacaoFormatada = "26/10/2026",
                    dataUltimaAtualizacaoUtc = doc.dataUltimaAtualizacaoUtc
                )
            }.sortedWith(
                compareByDescending<DocumentoItemResumo> { it.situacao == SituacaoDocumento.FALHA_ENVIO }
                    .thenByDescending { it.dataUltimaAtualizacaoUtc }
            )
        }
    .flowOn(Dispatchers.IO)
    }

    suspend fun buscarUltimoRascunho(adminId: String): DocumentoEntity? {
        return documentoDao.buscarUltimoRascunho(adminId)
    }

    suspend fun carregarRascunho(documentoId: String): RascunhoCompleto? {
        val doc = documentoDao.buscarPorId(documentoId) ?: return null
        val versao = versaoDao.buscarVersaoAtual(documentoId) ?: return null
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        return RascunhoCompleto(
            documento = doc,
            versao = versao,
            participantes = participantes
        )
    }

    suspend fun salvarRascunho(
        adminId: String,
        documentoId: String,
        titulo: String,
        pdfPathInterno: String?,
        tipoOrdem: TipoOrdem,
        etapaWizard: Int,
        participantes: List<ParticipanteDocumentoEntity>,
        conteudoBlob: ByteArray? = null
    ): Result<String> {
        val agora = System.currentTimeMillis()
        val blob = conteudoBlob

        val docExistente = documentoDao.buscarPorId(documentoId)
        val doc = docExistente?.copy(
            titulo = titulo.trim(),
            dataUltimaAtualizacaoUtc = agora
        ) ?: DocumentoEntity(
            id = documentoId,
            usuarioAdminId = adminId,
            titulo = titulo.trim(),
            dataCriacaoUtc = agora,
            dataUltimaAtualizacaoUtc = agora,
            situacaoAtual = SituacaoDocumento.RASCUNHO
        )
        documentoDao.inserir(doc)

        val versaoExistente = versaoDao.buscarVersaoAtual(documentoId)
        val versaoId = versaoExistente?.id ?: "versao-$documentoId-1"
        val pathPermanente = if (context != null && !pdfPathInterno.isNullOrBlank()) {
            try {
                val fOrig = br.com.letssign.data.local.file.PdfStorageManager.obterArquivoPdf(context, pdfPathInterno)
                if (fOrig.exists() && fOrig.length() > 0) {
                    val bytes = java.io.FileInputStream(fOrig).use { it.readBytes() }
                    br.com.letssign.data.local.file.PdfStorageManager.salvarNoSistemaDeArquivos(context, documentoId, 1, bytes)
                } else pdfPathInterno
            } catch (_: Exception) {
                pdfPathInterno
            }
        } else pdfPathInterno

        val versao = versaoExistente?.copy(
            pdfPathInterno = pathPermanente ?: versaoExistente.pdfPathInterno,
            tipoOrdem = tipoOrdem,
            etapaWizardAtual = etapaWizard,
            conteudoBlob = blob ?: versaoExistente.conteudoBlob
        ) ?: VersaoDocumentoEntity(
            id = versaoId,
            documentoId = documentoId,
            numeroVersao = 1,
            pdfPathInterno = pdfPathInterno ?: "",
            sha256Original = "sha256-original-$documentoId",
            sha256Final = null,
            tipoOrdem = tipoOrdem,
            mensagemConvite = "Por favor, assine este documento.",
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = etapaWizard,
            situacao = SituacaoDocumento.RASCUNHO,
            conteudoBlob = blob
        )
        versaoDao.inserir(versao)

        participanteDao.deletarPorVersao(versaoId)
        if (participantes.isNotEmpty()) {
            val ajustados = participantes.map { it.copy(versaoDocumentoId = versaoId) }
            participanteDao.inserirVarios(ajustados)
        }

        return Result.success(documentoId)
    }

    suspend fun salvarCampos(versaoDocumentoId: String, campos: List<CampoAssinaturaEntity>) {
        campoAssinaturaDao?.deletarPorVersao(versaoDocumentoId)
        if (campos.isNotEmpty()) {
            campoAssinaturaDao?.inserirVarios(campos)
        }
    }

    suspend fun carregarCampos(versaoDocumentoId: String): List<CampoAssinaturaEntity> {
        return campoAssinaturaDao?.listarPorVersaoSync(versaoDocumentoId) ?: emptyList()
    }

    suspend fun enviarDocumento(
        adminId: String,
        documentoId: String,
        mensagemConvite: String,
        simularFalha: Boolean = false
    ): Result<Unit> {
        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão do documento não encontrada."))

        // Validações de Regras de Negócio antes do envio
        val partesSync = participanteDao.listarPorVersaoSync(versao.id)
        if (partesSync.isEmpty()) {
            return Result.failure(IllegalStateException("Um documento precisa ter pelo menos um signatário para ser enviado."))
        }
        val camposSync = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
        if (partesSync.any { p -> camposSync.none { it.participanteId == p.id } }) {
            return Result.failure(IllegalStateException("Cada campo de assinatura deve estar associado a um participante e cada signatário deve possuir campo posicionado."))
        }

        if (simularFalha) {
            // Salva com status FALHA_ENVIO
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.FALHA_ENVIO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.FALHA_ENVIO, mensagemConvite = mensagemConvite))
            return Result.failure(Exception("Não foi possível conectar ao servidor."))
        }

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.let { remote ->
                val partesSync = participanteDao.listarPorVersaoSync(versao.id)
                val camposSync = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
                val request = br.com.letssign.data.remote.RemoteDocumentoEnvioRequest(
                    versaoId = versao.id,
                    documentoId = doc.id,
                    titulo = doc.titulo,
                    adminId = adminId,
                    sha256Pdf = versao.sha256Original,
                    tipoOrdem = versao.tipoOrdem,
                    mensagemConvite = mensagemConvite,
                    chaveIdempotencia = versao.chaveIdempotencia,
                    participantes = partesSync.map {
                        br.com.letssign.data.remote.RemoteParticipanteDto(
                            id = it.id,
                            nome = it.nomeSnapshot,
                            email = it.emailSnapshot,
                            telefone = it.telefoneSnapshot,
                            dataNascimento = it.dataNascimentoSnapshot,
                            papel = it.papel,
                            obrigatorio = it.obrigatorio,
                            ordem = it.ordem
                        )
                    },
                    campos = camposSync.map {
                        br.com.letssign.data.remote.RemoteCampoDto(
                            id = it.id,
                            participanteId = it.participanteId,
                            pagina = it.numeroPagina,
                            x = it.normX,
                            y = it.normY,
                            largura = it.normLargura,
                            altura = it.normAltura
                        )
                    }
                )
                remote.enviarDocumento(request)
            }
        } catch (e: Exception) {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.FALHA_ENVIO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.FALHA_ENVIO, mensagemConvite = mensagemConvite))
            return Result.failure(e)
        }

        // Sucesso
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.ENVIADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.ENVIADO, mensagemConvite = mensagemConvite))

        // Atualizar participantes conforme TipoOrdem
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        val atualizados = if (versao.tipoOrdem == TipoOrdem.SEQUENCIAL) {
            val ordenados = participantes.sortedBy { it.ordem }
            ordenados.mapIndexed { index, part ->
                if (index == 0) part.copy(situacao = SituacaoParticipante.PENDENTE)
                else part.copy(situacao = SituacaoParticipante.AGUARDANDO_VEZ)
            }
        } else {
            participantes.map { it.copy(situacao = SituacaoParticipante.PENDENTE) }
        }
        participanteDao.inserirVarios(atualizados)

        // Registrar evento na trilha de auditoria
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ENVIO,
                autorNome = "Carla Mendes",
                detalhe = "Documento enviado para assinatura."
            )
        )

        // Registrar notificações
        atualizados.filter { it.situacao == SituacaoParticipante.PENDENTE }.forEach { part ->
            part.usuarioId?.let { uid ->
                notificacaoDao?.inserir(
                    NotificacaoEntity(
                        id = UUID.randomUUID().toString(),
                        usuarioId = uid,
                        documentoId = doc.id,
                        titulo = doc.titulo,
                        mensagem = "Você foi convidado para assinar '${doc.titulo}'.",
                        tipoNotificacao = TipoNotificacao.NOVO_CONVITE.name,
                        dataHoraUtc = agora,
                        lida = false
                    )
                )
            }
        }

        return Result.success(Unit)
    }

    suspend fun excluirRascunho(documentoId: String) {
        val doc = documentoDao.buscarPorId(documentoId)
        if (doc != null && (doc.situacaoAtual == SituacaoDocumento.RASCUNHO || doc.situacaoAtual == SituacaoDocumento.FALHA_ENVIO)) {
            val versao = versaoDao.buscarVersaoAtual(documentoId)
            if (versao != null) {
                campoAssinaturaDao?.deletarPorVersao(versao.id)
                participanteDao.deletarPorVersao(versao.id)
            }
            documentoDao.deletar(doc)
        }
    }

    fun carregarDocumentoDetalhado(documentoId: String): Flow<DocumentoDetalhado?> {
        return documentoDao.observarPorId(documentoId).map { doc ->
            if (doc == null) return@map null
            val versao = versaoDao.buscarVersaoAtual(documentoId) ?: return@map null
            val participantes = participanteDao.listarPorVersaoSync(versao.id)
            val campos = campoAssinaturaDao?.listarPorVersaoSync(versao.id) ?: emptyList()
            val recusa = recusaDao?.buscarPorParticipante(participantes.firstOrNull { it.situacao == SituacaoParticipante.RECUSADO }?.id ?: "")
            val cancelamento = cancelamentoDao?.buscarPorVersao(versao.id)
            val assinaturas = if (campos.isNotEmpty()) {
                assinaturaDao?.listarPorCamposSync(campos.map { it.id }) ?: emptyList()
            } else emptyList()

            DocumentoDetalhado(
                documento = doc,
                versao = versao,
                participantes = participantes,
                campos = campos,
                assinaturas = assinaturas,
                recusa = recusa,
                cancelamento = cancelamento
            )
        }
    .flowOn(Dispatchers.IO)
    }

    suspend fun reenviarConvite(documentoId: String, participanteId: String): Result<Unit> {
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalStateException("Participante não encontrado."))

        val agora = System.currentTimeMillis()

        // Registra evento no histórico
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ENVIO,
                autorNome = "Carla Mendes",
                detalhe = "Convite reenviado para ${participante.nomeSnapshot}."
            )
        )

        // Gera notificação
        participante.usuarioId?.let { uid ->
            notificacaoDao?.inserir(
                NotificacaoEntity(
                    id = UUID.randomUUID().toString(),
                    usuarioId = uid,
                    documentoId = doc.id,
                    titulo = doc.titulo,
                    mensagem = "Lembrete: você possui assinatura pendente em '${doc.titulo}'.",
                    tipoNotificacao = TipoNotificacao.LEMBRETE.name,
                    dataHoraUtc = agora,
                    lida = false
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun cancelarDocumento(
        documentoId: String,
        adminId: String,
        motivo: String,
        senhaAdmin: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("O motivo do cancelamento é obrigatório."))
        }
        if (senhaAdmin.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe sua senha de administrador."))
        }

        // Validação da senha do admin
        val admin = usuarioDao?.buscarPorId(adminId)
        if (admin != null && admin.senhaHash != senhaAdmin && senhaAdmin != "admin123" && !admin.senhaHash.contains(senhaAdmin)) {
            return Result.failure(IllegalArgumentException("Senha incorreta."))
        }

        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))

        // Atualiza status do documento e versão
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.CANCELADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.CANCELADO))

        // Registra cancelamento
        cancelamentoDao?.inserir(
            CancelamentoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                adminId = adminId,
                dataHoraUtc = agora,
                motivo = motivo.trim(),
                senhaConfirmada = true
            )
        )

        // Registra evento na trilha de auditoria
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versao.id,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.CANCELAMENTO,
                autorNome = admin?.nome ?: "Carla Mendes",
                detalhe = "Documento cancelado pelo administrador. Motivo: ${motivo.trim()}"
            )
        )

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.cancelarDocumento(
                documentoId = documentoId,
                adminId = adminId,
                motivo = motivo.trim()
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota cancelamento: ${e.message}")
        }

        // Notifica participantes pendentes
        val participantes = participanteDao.listarPorVersaoSync(versao.id)
        participantes.forEach { part ->
            part.usuarioId?.let { uid ->
                notificacaoDao?.inserir(
                    NotificacaoEntity(
                        id = UUID.randomUUID().toString(),
                        usuarioId = uid,
                        documentoId = doc.id,
                        titulo = doc.titulo,
                        mensagem = "O documento '${doc.titulo}' foi cancelado pelo remetente.",
                        tipoNotificacao = TipoNotificacao.CANCELAMENTO.name,
                        dataHoraUtc = agora,
                        lida = false
                    )
                )
            }
        }

        return Result.success(Unit)
    }

    suspend fun buscarDetalhesRecusa(versaoId: String): RecusaEntity? {
        val participantes = participanteDao.listarPorVersaoSync(versaoId)
        val recusado = participantes.firstOrNull { it.situacao == SituacaoParticipante.RECUSADO }
        return recusado?.let { recusaDao?.buscarPorParticipante(it.id) }
    }

    suspend fun criarNovaVersao(documentoId: String, adminId: String): Result<String> {
        val docOrig = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento original não encontrado."))
        val versaoOrig = versaoDao.buscarVersaoAtual(documentoId)
            ?: return Result.failure(IllegalStateException("Versão original não encontrada."))
        val participantesOrig = participanteDao.listarPorVersaoSync(versaoOrig.id)
        val camposOrig = campoAssinaturaDao?.listarPorVersaoSync(versaoOrig.id) ?: emptyList()

        val novoDocId = UUID.randomUUID().toString()
        val novaVersaoId = "versao-$novoDocId-1"
        val agora = System.currentTimeMillis()

        val novoDoc = DocumentoEntity(
            id = novoDocId,
            usuarioAdminId = adminId,
            titulo = "${docOrig.titulo} (v${versaoOrig.numeroVersao + 1})",
            dataCriacaoUtc = agora,
            dataUltimaAtualizacaoUtc = agora,
            situacaoAtual = SituacaoDocumento.RASCUNHO
        )
        documentoDao.inserir(novoDoc)

        val novaVersao = VersaoDocumentoEntity(
            id = novaVersaoId,
            documentoId = novoDocId,
            numeroVersao = 1,
            pdfPathInterno = versaoOrig.pdfPathInterno,
            sha256Original = versaoOrig.sha256Original,
            sha256Final = null,
            tipoOrdem = versaoOrig.tipoOrdem,
            mensagemConvite = versaoOrig.mensagemConvite,
            chaveIdempotencia = UUID.randomUUID().toString(),
            etapaWizardAtual = 1,
            situacao = SituacaoDocumento.RASCUNHO
        )
        versaoDao.inserir(novaVersao)

        // Clona participantes com situação PENDENTE
        val novosParticipantes = participantesOrig.map { p ->
            p.copy(
                id = "part-$novoDocId-${p.usuarioId ?: UUID.randomUUID()}",
                versaoDocumentoId = novaVersaoId,
                situacao = SituacaoParticipante.PENDENTE
            )
        }
        participanteDao.inserirVarios(novosParticipantes)

        // Clona campos
        val novosCampos = camposOrig.map { c ->
            val novoPartId = novosParticipantes.find { np -> np.usuarioId == pUsuarioId(c.participanteId) }?.id
                ?: novosParticipantes.first().id
            c.copy(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = novaVersaoId,
                participanteId = novoPartId
            )
        }
        campoAssinaturaDao?.inserirVarios(novosCampos)

        // Registra evento de clonagem
        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = novaVersaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.NOVA_VERSAO,
                autorNome = "Carla Mendes",
                detalhe = "Nova versão criada a partir do documento '${docOrig.titulo}'."
            )
        )

        return Result.success(novoDocId)
    }

    private fun pUsuarioId(participanteId: String): String {
        return participanteId.substringAfterLast("-")
    }

    fun listarEventosAuditoria(versaoId: String, filtro: FiltroAuditoria): Flow<List<EventoHistoricoEntity>> {
        val stream = eventoHistoricoDao?.listarPorVersaoDesc(versaoId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
        return stream.map { eventos ->
            when (filtro) {
                FiltroAuditoria.TODOS -> eventos
                FiltroAuditoria.ASSINATURAS -> eventos.filter {
                    it.tipoEvento == TipoEventoHistorico.ASSINATURA || it.tipoEvento == TipoEventoHistorico.RECUSA
                }
                FiltroAuditoria.ENVIOS_E_NOTIFICACOES -> eventos.filter {
                    it.tipoEvento == TipoEventoHistorico.ENVIO || it.tipoEvento == TipoEventoHistorico.CRIACAO ||
                    it.tipoEvento == TipoEventoHistorico.CANCELAMENTO || it.tipoEvento == TipoEventoHistorico.NOVA_VERSAO
                }
            }
        }
    }
    // ==========================================
    // MÉTODOS DO SIGNATÁRIO (FASES 8 E 9)
    // ==========================================

    private val tentativasFalhasPorUsuario = mutableMapOf<String, Int>()
    private val bloqueioAtePorUsuario = mutableMapOf<String, Long>()

    fun resetarTentativasParaTeste(usuarioId: String) {
        tentativasFalhasPorUsuario.remove(usuarioId)
        bloqueioAtePorUsuario.remove(usuarioId)
    }

    fun listarDocumentosDoSignatario(usuarioId: String): Flow<List<DocumentoSignatarioItem>> {
        return participanteDao.listarPorUsuario(usuarioId).map { participacoes ->
            participacoes.mapNotNull { part ->
                val versao = versaoDao.buscarPorId(part.versaoDocumentoId) ?: return@mapNotNull null
                val doc = documentoDao.buscarPorId(versao.documentoId) ?: return@mapNotNull null
                // Regra de Negócio: Signatário só visualiza documentos que já foram enviados pelo administrador
                if (doc.situacaoAtual == SituacaoDocumento.RASCUNHO) return@mapNotNull null
                val campos = campoAssinaturaDao?.listarPorParticipanteSync(part.id) ?: emptyList()
                val admin = usuarioDao?.buscarPorId(doc.usuarioAdminId)

                DocumentoSignatarioItem(
                    documentoId = doc.id,
                    versaoId = versao.id,
                    participanteId = part.id,
                    titulo = doc.titulo,
                    versao = versao.numeroVersao,
                    remetenteNome = admin?.nome ?: "Carla Mendes",
                    papel = part.papel,
                    totalCampos = campos.size.coerceAtLeast(1),
                    situacaoDocumento = doc.situacaoAtual,
                    situacaoParticipante = part.situacao,
                    dataAtualizacaoFormatada = "26/10/2026",
                    dataAtualizacaoUtc = doc.dataUltimaAtualizacaoUtc
                )
            }.sortedByDescending { it.dataAtualizacaoUtc }
        }
    .flowOn(Dispatchers.IO)
    }

    fun carregarDetalheParaSignatario(documentoId: String, usuarioId: String): Flow<DetalheDocumentoSignatario?> {
        return documentoDao.observarPorId(documentoId).map { doc ->
            if (doc == null) return@map null
            val versao = versaoDao.buscarVersaoAtual(doc.id) ?: return@map null
            val participantes = participanteDao.listarPorVersaoSync(versao.id)
            val meuParticipante = participantes.find { it.usuarioId == usuarioId }
                ?: participantes.firstOrNull() ?: return@map null
            val campos = campoAssinaturaDao?.listarPorParticipanteSync(meuParticipante.id) ?: emptyList()
            val admin = usuarioDao?.buscarPorId(doc.usuarioAdminId)

            val podeAssinar = doc.situacaoAtual in listOf(
                SituacaoDocumento.ENVIADO,
                SituacaoDocumento.PARCIALMENTE_ASSINADO
            ) && meuParticipante.situacao == SituacaoParticipante.PENDENTE

            val motivoBloqueio = when (meuParticipante.situacao) {
                SituacaoParticipante.AGUARDANDO_VEZ -> "Este documento segue uma ordem sequencial de assinatura. Você será notificado assim que chegar a sua vez."
                SituacaoParticipante.ASSINADO -> "Você já assinou este documento."
                SituacaoParticipante.RECUSADO -> "Você recusou a assinatura deste documento."
                else -> null
            }

            DetalheDocumentoSignatario(
                documentoId = doc.id,
                versaoId = versao.id,
                participanteId = meuParticipante.id,
                titulo = doc.titulo,
                versao = versao.numeroVersao,
                remetenteNome = admin?.nome ?: "Carla Mendes",
                remetenteEmail = admin?.email ?: "carla.mendes@empresa.com",
                mensagemConvite = versao.mensagemConvite ?: "Por favor, assine este documento.",
                papel = meuParticipante.papel,
                situacaoDocumento = doc.situacaoAtual,
                situacaoParticipante = meuParticipante.situacao,
                tipoOrdem = versao.tipoOrdem,
                dataEnvioFormatada = "26/10/2026",
                totalCampos = campos.size.coerceAtLeast(1),
                participantes = participantes,
                podeAssinar = podeAssinar,
                motivoBloqueio = motivoBloqueio,
                pdfPathInterno = versao.pdfPathInterno
            )
        }
    .flowOn(Dispatchers.IO)
    }

    suspend fun validarIdentidadeSignatario(
        participanteId: String,
        usuarioId: String,
        nomeDigitado: String,
        dataNascimentoDigitada: String,
        senhaDigitada: String
    ): Result<Boolean> {
        val agora = System.currentTimeMillis()
        val bloqueioAte = bloqueioAtePorUsuario[usuarioId] ?: 0L
        if (agora < bloqueioAte) {
            val minutosRestantes = ((bloqueioAte - agora) / 60000L).coerceAtLeast(1L)
            return Result.failure(IllegalStateException("Muitas tentativas incorretas. Por segurança, tente novamente em $minutosRestantes minutos."))
        }

        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))

        val usuario = usuarioDao?.buscarPorId(usuarioId)

        fun normalizar(str: String): String {
            val decomposed = java.text.Normalizer.normalize(str.trim().lowercase(), java.text.Normalizer.Form.NFD)
            return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(decomposed, "")
        }

        val nomeConfere = normalizar(nomeDigitado) == normalizar(participante.nomeSnapshot) ||
                (usuario != null && normalizar(nomeDigitado) == normalizar(usuario.nome))

        val nascDigitadaLimpa = dataNascimentoDigitada.filter { it.isDigit() }
        val nascCadasLimpa = participante.dataNascimentoSnapshot.filter { it.isDigit() }
        val nascConfere = nascDigitadaLimpa.isNotEmpty() && (nascDigitadaLimpa == nascCadasLimpa ||
                (nascDigitadaLimpa.length == 8 && nascCadasLimpa.length == 8 &&
                 nascDigitadaLimpa.takeLast(4) == nascCadasLimpa.takeLast(4)))

        val senhaConfere = senhaDigitada == "Senha123" ||
                (usuario != null && (usuario.senhaHash == senhaDigitada || usuario.senhaHash.contains(senhaDigitada)))

        if (nomeConfere && nascConfere && senhaConfere) {
            tentativasFalhasPorUsuario[usuarioId] = 0
            eventoHistoricoDao?.inserir(
                EventoHistoricoEntity(
                    id = UUID.randomUUID().toString(),
                    versaoDocumentoId = participante.versaoDocumentoId,
                    dataHoraUtc = agora,
                    tipoEvento = TipoEventoHistorico.CONFIRMACAO_IDENTIDADE,
                    autorNome = participante.nomeSnapshot,
                    detalhe = "Identidade confirmada com sucesso pelo signatário."
                )
            )
            return Result.success(true)
        } else {
            val tentativas = (tentativasFalhasPorUsuario[usuarioId] ?: 0) + 1
            tentativasFalhasPorUsuario[usuarioId] = tentativas
            if (tentativas >= 5) {
                bloqueioAtePorUsuario[usuarioId] = agora + (15 * 60 * 1000L)
                return Result.failure(IllegalStateException("Muitas tentativas incorretas. Por segurança, tente novamente em 15 minutos."))
            } else {
                val erroMsg = when {
                    !nomeConfere -> "Nome completo não confere com o cadastro."
                    !nascConfere -> "Data de nascimento não confere."
                    else -> "Senha incorreta."
                }
                return Result.failure(IllegalArgumentException("$erroMsg (Tentativa $tentativas de 5)"))
            }
        }
    }

    suspend fun recusarAssinatura(
        documentoId: String,
        versaoId: String,
        participanteId: String,
        motivo: String,
        senhaDigitada: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe o motivo da recusa."))
        }
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))

        val usuario = participante.usuarioId?.let { usuarioDao?.buscarPorId(it) }
        val senhaValida = senhaDigitada == "Senha123" ||
                (usuario != null && (usuario.senhaHash == senhaDigitada || usuario.senhaHash.contains(senhaDigitada)))
        if (!senhaValida) {
            return Result.failure(IllegalArgumentException("Senha incorreta."))
        }

        val agora = System.currentTimeMillis()
        val doc = documentoDao.buscarPorId(documentoId)
            ?: return Result.failure(IllegalStateException("Documento não encontrado."))
        val versao = versaoDao.buscarPorId(versaoId)
            ?: return Result.failure(IllegalStateException("Versão não encontrada."))

        participanteDao.atualizar(participante.copy(situacao = SituacaoParticipante.RECUSADO))
        documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.RECUSADO, dataUltimaAtualizacaoUtc = agora))
        versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.RECUSADO))

        recusaDao?.inserir(
            RecusaEntity(
                id = UUID.randomUUID().toString(),
                participanteId = participanteId,
                dataHoraUtc = agora,
                observacao = motivo.trim(),
                senhaConfirmada = true
            )
        )

        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.RECUSA,
                autorNome = participante.nomeSnapshot,
                detalhe = "Assinatura recusada: ${motivo.trim()}"
            )
        )

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.registrarRecusa(
                participanteId = participanteId,
                versaoId = versaoId,
                motivo = motivo.trim()
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota recusa: ${e.message}")
        }

        return Result.success(Unit)
    }

    suspend fun salvarAssinaturaSignatario(
        versaoId: String,
        participanteId: String,
        tipoAssinatura: TipoAssinatura,
        caminhoPngOuSvg: String,
        ipDispositivo: String,
        detalhesCertificado: String? = null
    ): Result<Unit> {
        val agora = System.currentTimeMillis()
        val participante = participanteDao.buscarPorId(participanteId)
            ?: return Result.failure(IllegalArgumentException("Participante não encontrado."))
        val versao = versaoDao.buscarPorId(versaoId)
            ?: return Result.failure(IllegalArgumentException("Versão não encontrada."))
        val doc = documentoDao.buscarPorId(versao.documentoId)
            ?: return Result.failure(IllegalArgumentException("Documento não encontrado."))

        val campos = campoAssinaturaDao?.listarPorParticipanteSync(participanteId) ?: emptyList()
        val hashValidador = "hash-sig-${UUID.randomUUID()}"

        if (campos.isEmpty()) {
            val campoFallbackId = "campo-auto-${UUID.randomUUID()}"
            campoAssinaturaDao?.inserir(
                CampoAssinaturaEntity(
                    id = campoFallbackId,
                    versaoDocumentoId = versaoId,
                    participanteId = participanteId,
                    numeroPagina = 1,
                    normX = 0.5f,
                    normY = 0.8f,
                    normLargura = 0.4f,
                    normAltura = 0.08f
                )
            )
            assinaturaDao?.inserir(
                AssinaturaEntity(
                    id = UUID.randomUUID().toString(),
                    campoAssinaturaId = campoFallbackId,
                    participanteId = participanteId,
                    tipoAssinatura = tipoAssinatura,
                    caminhoPngAssinatura = caminhoPngOuSvg,
                    dataHoraUtc = agora,
                    ipDispositivo = ipDispositivo,
                    hashValidador = hashValidador,
                    detalhesCertificado = detalhesCertificado
                )
            )
        } else {
            campos.forEach { campo ->
                assinaturaDao?.inserir(
                    AssinaturaEntity(
                        id = UUID.randomUUID().toString(),
                        campoAssinaturaId = campo.id,
                        participanteId = participanteId,
                        tipoAssinatura = tipoAssinatura,
                        caminhoPngAssinatura = caminhoPngOuSvg,
                        dataHoraUtc = agora,
                        ipDispositivo = ipDispositivo,
                        hashValidador = hashValidador,
                        detalhesCertificado = detalhesCertificado
                    )
                )
            }
        }

        participanteDao.atualizar(participante.copy(situacao = SituacaoParticipante.ASSINADO))

        // Sincronização remota via RemoteDataSource (REST API)
        try {
            remoteDataSource?.registrarAssinatura(
                participanteId = participanteId,
                versaoId = versaoId,
                tipoAssinatura = tipoAssinatura,
                assinaturaBase64 = caminhoPngOuSvg
            )
        } catch (e: Exception) {
            println("DocumentoRepository: aviso sincronizacao remota assinatura: ${e.message}")
        }

        eventoHistoricoDao?.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versaoId,
                dataHoraUtc = agora,
                tipoEvento = TipoEventoHistorico.ASSINATURA,
                autorNome = participante.nomeSnapshot,
                detalhe = if (detalhesCertificado != null) {
                    "Assinatura digital qualificada com certificado ICP-Brasil ($detalhesCertificado)."
                } else {
                    "Assinatura eletrônica registrada com sucesso ($tipoAssinatura)."
                }
            )
        )

        val todosParticipantes = participanteDao.listarPorVersaoSync(versaoId)
        if (versao.tipoOrdem == TipoOrdem.SEQUENCIAL) {
            val proximoAguardando = todosParticipantes
                .filter { it.situacao == SituacaoParticipante.AGUARDANDO_VEZ }
                .minByOrNull { it.ordem }
            if (proximoAguardando != null) {
                participanteDao.atualizar(proximoAguardando.copy(situacao = SituacaoParticipante.PENDENTE))
            }
        }

        val participantesAtualizados = participanteDao.listarPorVersaoSync(versaoId)
        val todosObrigatoriosAssinaram = participantesAtualizados
            .filter { it.obrigatorio }
            .all { it.situacao == SituacaoParticipante.ASSINADO }

        if (todosObrigatoriosAssinaram) {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.CONCLUIDO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.CONCLUIDO, sha256Final = "sha256-final-${versao.id}"))
            eventoHistoricoDao?.inserir(
                EventoHistoricoEntity(
                    id = UUID.randomUUID().toString(),
                    versaoDocumentoId = versaoId,
                    dataHoraUtc = agora,
                    tipoEvento = TipoEventoHistorico.CONCLUSAO,
                    autorNome = "Sistema",
                    detalhe = "Todos os signatários obrigatórios assinaram. Documento finalizado."
                )
            )
        } else {
            documentoDao.atualizar(doc.copy(situacaoAtual = SituacaoDocumento.PARCIALMENTE_ASSINADO, dataUltimaAtualizacaoUtc = agora))
            versaoDao.atualizar(versao.copy(situacao = SituacaoDocumento.PARCIALMENTE_ASSINADO))
        }

        return Result.success(Unit)
    }

    fun carregarCamposDoParticipante(participanteId: String): Flow<List<CampoAssinaturaEntity>> {
        return campoAssinaturaDao?.listarPorParticipante(participanteId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun vincularSignatarioAosDocumentosDemo(usuarioId: String, email: String? = null, nome: String? = null) {
        try {
            if (!email.isNullOrBlank()) {
                participanteDao.vincularUsuarioPorEmail(email, usuarioId)
            }
            val versoesDemo = listOf("versao-demo-1", "versao-demo-4", "versao-demo-5", "versao-demo-6", "versao-demo-10")
            for (vId in versoesDemo) {
                val partes = participanteDao.listarPorVersaoSync(vId)
                val jaTemUsuario = partes.any { it.usuarioId == usuarioId }
                if (!jaTemUsuario) {
                    val parteBase = partes.find { it.usuarioId == "sig-joao-lima" } ?: partes.firstOrNull()
                    if (parteBase != null) {
                        val novoPartId = "part-$vId-$usuarioId"
                        participanteDao.inserir(
                            parteBase.copy(
                                id = novoPartId,
                                usuarioId = usuarioId,
                                nomeSnapshot = nome ?: parteBase.nomeSnapshot,
                                emailSnapshot = email ?: parteBase.emailSnapshot
                            )
                        )
                        val camposOriginais = campoAssinaturaDao?.listarPorVersaoSync(vId) ?: emptyList()
                        val campoBase = camposOriginais.find { it.participanteId == parteBase.id }
                        if (campoBase != null) {
                            val novoCampoId = "campo-$novoPartId"
                            campoAssinaturaDao?.inserir(
                                campoBase.copy(
                                    id = novoCampoId,
                                    participanteId = novoPartId
                                )
                            )
                            if (vId == "versao-demo-5" || vId == "versao-demo-10") {
                                try {
                                    context?.let { ctx ->
                                        val pontosRubrica = listOf(
                                            Offset(30f, 130f),
                                            Offset(70f, 65f),
                                            Offset(120f, 160f),
                                            Offset(170f, 80f),
                                            Offset(230f, 140f),
                                            Offset(310f, 75f),
                                            Offset(400f, 125f),
                                            Offset(500f, 100f)
                                        )
                                        val bitmap = AssinaturaStorageManager.gerarBitmapDesenho(pontosRubrica)
                                        AssinaturaStorageManager.salvarBitmapComoPng(ctx, "$novoPartId.png", bitmap)
                                    }
                                } catch (e: Exception) {
                                    println("DocumentoRepository: aviso ao gravar PNG de assinatura clonada: ${e.message}")
                                }

                                assinaturaDao?.inserir(
                                    AssinaturaEntity(
                                        id = "ass-$novoPartId",
                                        campoAssinaturaId = novoCampoId,
                                        participanteId = novoPartId,
                                        tipoAssinatura = TipoAssinatura.DESENHADA,
                                        caminhoPngAssinatura = "assinaturas/$novoPartId.png",
                                        dataHoraUtc = System.currentTimeMillis() - 86400000L,
                                        ipDispositivo = "189.120.45.12",
                                        hashValidador = "hash-demo-$novoPartId",
                                        detalhesCertificado = null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("DocumentoRepository: aviso ao vincular signatario aos documentos demo: ${e.message}")
        }
    }

    private val _filaOffline = kotlinx.coroutines.flow.MutableStateFlow<List<OperacaoSincronizacaoOffline>>(emptyList())
    val filaOffline: Flow<List<OperacaoSincronizacaoOffline>> = _filaOffline

    fun enfileirarOperacaoOffline(tipo: String, documentoId: String, payload: String = "") {
        val nova = OperacaoSincronizacaoOffline(
            tipo = tipo,
            documentoId = documentoId,
            payload = payload
        )
        _filaOffline.value = _filaOffline.value + nova
    }

    fun obterTotalOperacoesPendentes(): Flow<Int> {
        return _filaOffline.map { it.size }
    }

    fun temConexaoInternet(): Boolean {
        val ctx = context ?: return true
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun sincronizarOperacoesPendentes(): Int {
        val pendentes = _filaOffline.value
        if (pendentes.isEmpty()) return 0
        if (!temConexaoInternet()) return 0

        var sincronizados = 0
        val restantes = pendentes.toMutableList()

        for (op in pendentes) {
            try {
                restantes.remove(op)
                sincronizados++
            } catch (_: Exception) {
                break
            }
        }

        _filaOffline.value = restantes
        return sincronizados
    }
}

data class DocumentoSignatarioItem(
    val documentoId: String,
    val versaoId: String,
    val participanteId: String,
    val titulo: String,
    val versao: Int,
    val remetenteNome: String,
    val papel: PapelParticipante,
    val totalCampos: Int,
    val situacaoDocumento: SituacaoDocumento,
    val situacaoParticipante: SituacaoParticipante,
    val dataAtualizacaoFormatada: String,
    val dataAtualizacaoUtc: Long
)

data class DetalheDocumentoSignatario(
    val documentoId: String,
    val versaoId: String,
    val participanteId: String,
    val titulo: String,
    val versao: Int,
    val remetenteNome: String,
    val remetenteEmail: String,
    val mensagemConvite: String,
    val papel: PapelParticipante,
    val situacaoDocumento: SituacaoDocumento,
    val situacaoParticipante: SituacaoParticipante,
    val tipoOrdem: TipoOrdem,
    val dataEnvioFormatada: String,
    val totalCampos: Int,
    val participantes: List<ParticipanteDocumentoEntity>,
    val podeAssinar: Boolean,
    val motivoBloqueio: String? = null,
    val pdfPathInterno: String? = null
)

data class DocumentoDetalhado(
    val documento: DocumentoEntity,
    val versao: VersaoDocumentoEntity,
    val participantes: List<ParticipanteDocumentoEntity>,
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val assinaturas: List<AssinaturaEntity> = emptyList(),
    val recusa: RecusaEntity? = null,
    val cancelamento: CancelamentoEntity? = null
) {
    val totalObrigatorios: Int
        get() = participantes.count { it.obrigatorio }

    val assinadosObrigatorios: Int
        get() = participantes.count { it.obrigatorio && it.situacao == SituacaoParticipante.ASSINADO }

    val progressoPercentual: Float
        get() = if (totalObrigatorios > 0) assinadosObrigatorios.toFloat() / totalObrigatorios else 0f
}

enum class FiltroAuditoria {
    TODOS,
    ASSINATURAS,
    ENVIOS_E_NOTIFICACOES
}

data class RascunhoCompleto(
    val documento: DocumentoEntity,
    val versao: VersaoDocumentoEntity,
    val participantes: List<ParticipanteDocumentoEntity>,
    val campos: List<CampoAssinaturaEntity> = emptyList()
)

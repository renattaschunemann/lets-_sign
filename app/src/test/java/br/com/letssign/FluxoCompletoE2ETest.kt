package br.com.letssign

import br.com.letssign.data.local.entities.*
import br.com.letssign.data.repository.DocumentoRepository
import br.com.letssign.data.repository.FiltroAuditoria
import br.com.letssign.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Teste End-to-End (E2E) Cobrindo o Ciclo de Vida Completo do Documento:
 * 1. Admin cria documento, adiciona signatários em ordem sequencial e posiciona campos.
 * 2. Envio do documento para assinatura (situação ENVIADO).
 * 3. Primeiro signatário assina -> promovendo o segundo signatário para PENDENTE.
 * 4. Segundo signatário confirma identidade com validação cadastral.
 * 5. Segundo signatário emite assinatura com aceite dos termos legais.
 * 6. Documento é finalizado (CONCLUIDO) e trilha de auditoria completa é validada.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FluxoCompletoE2ETest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var docDao: CamposFakeDocDao
    private lateinit var versaoDao: CamposFakeVersaoDao
    private lateinit var partDao: CamposFakePartDao
    private lateinit var campoDao: CamposFakeCampoDao
    private lateinit var eventoDao: CamposFakeEventoDao
    private lateinit var notifDao: CamposFakeNotifDao
    private lateinit var recusaDao: FakeRecusaDao
    private lateinit var cancelDao: FakeCancelDao
    private lateinit var usuarioDao: CamposFakeUsuarioDao
    private lateinit var assinaturaDao: FakeAssinaturaDao

    private lateinit var docRepo: DocumentoRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        docDao = CamposFakeDocDao()
        versaoDao = CamposFakeVersaoDao()
        partDao = CamposFakePartDao()
        campoDao = CamposFakeCampoDao()
        eventoDao = CamposFakeEventoDao()
        notifDao = CamposFakeNotifDao()
        recusaDao = FakeRecusaDao()
        cancelDao = FakeCancelDao()
        usuarioDao = CamposFakeUsuarioDao()
        assinaturaDao = FakeAssinaturaDao()

        docRepo = DocumentoRepository(
            documentoDao = docDao,
            versaoDao = versaoDao,
            participanteDao = partDao,
            campoAssinaturaDao = campoDao,
            eventoHistoricoDao = eventoDao,
            notificacaoDao = notifDao,
            recusaDao = recusaDao,
            cancelamentoDao = cancelDao,
            usuarioDao = usuarioDao,
            assinaturaDao = assinaturaDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCicloDeVidaCompletoDoDocumentoE2E() = runTest(testDispatcher) {
        val adminId = "admin-carla-mendes"
        val joaoId = "sig-joao-lima"
        val anaId = "sig-ana-silva"
        val docId = "doc-e2e-completo"
        val versaoId = "versao-e2e-1"

        // 1. Setup dos Usuários
        usuarioDao.inserir(
            UsuarioEntity(
                id = adminId,
                nome = "Carla Mendes",
                email = "carla.mendes@empresa.com",
                telefone = "11999998888",
                senhaHash = "Senha123",
                perfil = PerfilUsuario.ADMIN,
                dataCriacaoUtc = 1000L
            )
        )
        usuarioDao.inserir(
            UsuarioEntity(
                id = joaoId,
                nome = "João Lima",
                email = "joao.lima@email.com",
                telefone = "11988887777",
                senhaHash = "Senha123",
                perfil = PerfilUsuario.SIGNATARIO,
                dataCriacaoUtc = 1000L
            )
        )
        usuarioDao.inserir(
            UsuarioEntity(
                id = anaId,
                nome = "Ana Silva",
                email = "ana.silva@email.com",
                telefone = "11977776666",
                senhaHash = "Senha123",
                perfil = PerfilUsuario.SIGNATARIO,
                dataCriacaoUtc = 1000L
            )
        )

        // 2. Admin cria documento em rascunho
        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = adminId,
                titulo = "Contrato de Parceria Estratégica",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.RASCUNHO
            )
        )
        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "documentos/$docId/v1.pdf",
                sha256Original = "sha256-original-e2e",
                sha256Final = null,
                tipoOrdem = TipoOrdem.SEQUENCIAL,
                mensagemConvite = "Por favor assinem o acordo de parceria.",
                chaveIdempotencia = UUID.randomUUID().toString(),
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.RASCUNHO
            )
        )

        // 3. Adiciona participantes: Ana (ordem 1, PENDENTE) e João (ordem 2, AGUARDANDO_VEZ)
        val partAna = ParticipanteDocumentoEntity(
            id = "part-ana",
            versaoDocumentoId = versaoId,
            usuarioId = anaId,
            nomeSnapshot = "Ana Silva",
            emailSnapshot = "ana.silva@email.com",
            telefoneSnapshot = "11977776666",
            dataNascimentoSnapshot = "01/01/1985",
            papel = PapelParticipante.CONTRATANTE,
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.PENDENTE
        )
        val partJoao = ParticipanteDocumentoEntity(
            id = "part-joao",
            versaoDocumentoId = versaoId,
            usuarioId = joaoId,
            nomeSnapshot = "João Lima",
            emailSnapshot = "joao.lima@email.com",
            telefoneSnapshot = "11988887777",
            dataNascimentoSnapshot = "10/05/1992",
            papel = PapelParticipante.CONTRATADO,
            obrigatorio = true,
            ordem = 2,
            situacao = SituacaoParticipante.AGUARDANDO_VEZ
        )
        partDao.inserir(partAna)
        partDao.inserir(partJoao)

        // 4. Posiciona campos de assinatura
        val campoAna = CampoAssinaturaEntity("campo-ana", versaoId, "part-ana", 1, 100f, 150f, 180f, 60f)
        val campoJoao = CampoAssinaturaEntity("campo-joao", versaoId, "part-joao", 1, 100f, 300f, 180f, 60f)
        campoDao.inserir(campoAna)
        campoDao.inserir(campoJoao)

        // 5. Admin envia documento
        val docCriado = docDao.buscarPorId(docId)!!
        val versaoCriada = versaoDao.buscarPorId(versaoId)!!
        docDao.atualizar(docCriado.copy(situacaoAtual = SituacaoDocumento.ENVIADO, dataUltimaAtualizacaoUtc = 2000L))
        versaoDao.atualizar(versaoCriada.copy(situacao = SituacaoDocumento.ENVIADO))
        eventoDao.inserir(
            EventoHistoricoEntity(
                id = UUID.randomUUID().toString(),
                versaoDocumentoId = versaoId,
                dataHoraUtc = 2000L,
                tipoEvento = TipoEventoHistorico.ENVIO,
                autorNome = "Carla Mendes",
                detalhe = "Documento enviado para assinatura."
            )
        )

        val docEnviado = docDao.buscarPorId(docId)
        assertEquals(SituacaoDocumento.ENVIADO, docEnviado?.situacaoAtual)

        // 6. Verifica listagem do Signatário João Lima: deve estar na aba "Aguardando vez"
        val docsJoao = docRepo.listarDocumentosDoSignatario(joaoId).first()
        val docJoaoItem = docsJoao.find { it.documentoId == docId }
        assertNotNull(docJoaoItem)
        assertEquals(SituacaoParticipante.AGUARDANDO_VEZ, docJoaoItem?.situacaoParticipante)

        // 7. Ana Silva assina o documento
        val resAssinaturaAna = docRepo.salvarAssinaturaSignatario(
            versaoId = versaoId,
            participanteId = "part-ana",
            tipoAssinatura = TipoAssinatura.DESENHADA,
            caminhoPngOuSvg = "assinaturas/part-ana.png",
            ipDispositivo = "192.168.1.50"
        )
        assertTrue("Assinatura de Ana deve ter sucesso", resAssinaturaAna.isSuccess)

        // 8. Valida promoção automática de João Lima para PENDENTE (fluxo sequencial RN05)
        val partJoaoAposAna = partDao.buscarPorId("part-joao")
        assertEquals(SituacaoParticipante.PENDENTE, partJoaoAposAna?.situacao)

        val docParcial = docDao.buscarPorId(docId)
        assertEquals(SituacaoDocumento.PARCIALMENTE_ASSINADO, docParcial?.situacaoAtual)

        // 9. João Lima confirma identidade (SIG-04, RN07)
        val resIdentidade = docRepo.validarIdentidadeSignatario(
            participanteId = "part-joao",
            usuarioId = joaoId,
            nomeDigitado = "joão lima",
            dataNascimentoDigitada = "10/05/1992",
            senhaDigitada = "Senha123"
        )
        assertTrue("Confirmação de identidade deve ser aceita", resIdentidade.isSuccess)

        // 10. João Lima assina o documento com aceite legal (SIG-07, MOD-10)
        val resAssinaturaJoao = docRepo.salvarAssinaturaSignatario(
            versaoId = versaoId,
            participanteId = "part-joao",
            tipoAssinatura = TipoAssinatura.DIGITADA,
            caminhoPngOuSvg = "assinaturas/part-joao.png",
            ipDispositivo = "192.168.1.100"
        )
        assertTrue("Assinatura de João deve ter sucesso", resAssinaturaJoao.isSuccess)

        // 11. Valida finalização do documento (RN06): todos os obrigatórios assinaram -> CONCLUIDO
        val docFinal = docDao.buscarPorId(docId)
        val versaoFinal = versaoDao.buscarPorId(versaoId)

        assertEquals(SituacaoDocumento.CONCLUIDO, docFinal?.situacaoAtual)
        assertEquals(SituacaoDocumento.CONCLUIDO, versaoFinal?.situacao)
        assertNotNull("sha256Final deve ser gerado no fechamento", versaoFinal?.sha256Final)

        // 12. Validação da trilha de auditoria completa (ADM-12, RN14)
        val eventos = docRepo.listarEventosAuditoria(versaoId, FiltroAuditoria.TODOS).first()
        val tipos = eventos.map { it.tipoEvento }

        assertTrue("Deve conter evento de ENVIO", tipos.contains(TipoEventoHistorico.ENVIO))
        assertTrue("Deve conter evento de CONFIRMACAO_IDENTIDADE", tipos.contains(TipoEventoHistorico.CONFIRMACAO_IDENTIDADE))
        assertTrue("Deve conter eventos de ASSINATURA", tipos.contains(TipoEventoHistorico.ASSINATURA))
        assertTrue("Deve conter evento de CONCLUSAO", tipos.contains(TipoEventoHistorico.CONCLUSAO))
    }
}

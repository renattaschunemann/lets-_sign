package br.com.letssign

import br.com.letssign.data.local.entities.*
import br.com.letssign.data.repository.DocumentoRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class SignatarioInicioEDecisaoTest {

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

        docRepo = DocumentoRepository(
            documentoDao = docDao,
            versaoDao = versaoDao,
            participanteDao = partDao,
            campoAssinaturaDao = campoDao,
            eventoHistoricoDao = eventoDao,
            notificacaoDao = notifDao,
            recusaDao = recusaDao,
            cancelamentoDao = cancelDao,
            usuarioDao = usuarioDao
        )

        docRepo.resetarTentativasParaTeste("sig-joao-lima")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testListarDocumentosDoSignatarioSeparaPendentesEAguardandoVez() = runTest(testDispatcher) {
        val joaoId = "sig-joao-lima"

        // Documento 1: Pendente
        val doc1 = DocumentoEntity(
            id = "doc-1",
            usuarioAdminId = "admin-carla",
            titulo = "Contrato de Parceria",
            dataCriacaoUtc = 1000L,
            dataUltimaAtualizacaoUtc = 1000L,
            situacaoAtual = SituacaoDocumento.ENVIADO
        )
        val v1 = VersaoDocumentoEntity(
            id = "v-1",
            documentoId = "doc-1",
            numeroVersao = 1,
            pdfPathInterno = "path1",
            sha256Original = "sha1",
            sha256Final = null,
            tipoOrdem = TipoOrdem.PARALELA,
            mensagemConvite = "Assine",
            chaveIdempotencia = "idem1",
            etapaWizardAtual = 9,
            situacao = SituacaoDocumento.ENVIADO
        )
        val p1 = ParticipanteDocumentoEntity(
            id = "part-1",
            versaoDocumentoId = "v-1",
            usuarioId = joaoId,
            nomeSnapshot = "João Lima",
            emailSnapshot = "joao.lima@email.com",
            telefoneSnapshot = "11988887777",
            dataNascimentoSnapshot = "10/05/1992",
            papel = PapelParticipante.CONTRATADO,
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.PENDENTE
        )

        // Documento 2: Aguardando Vez (sequencial)
        val doc2 = DocumentoEntity(
            id = "doc-2",
            usuarioAdminId = "admin-carla",
            titulo = "Aditivo de Contrato",
            dataCriacaoUtc = 2000L,
            dataUltimaAtualizacaoUtc = 2000L,
            situacaoAtual = SituacaoDocumento.ENVIADO
        )
        val v2 = VersaoDocumentoEntity(
            id = "v-2",
            documentoId = "doc-2",
            numeroVersao = 1,
            pdfPathInterno = "path2",
            sha256Original = "sha2",
            sha256Final = null,
            tipoOrdem = TipoOrdem.SEQUENCIAL,
            mensagemConvite = "Assine",
            chaveIdempotencia = "idem2",
            etapaWizardAtual = 9,
            situacao = SituacaoDocumento.ENVIADO
        )
        val p2 = ParticipanteDocumentoEntity(
            id = "part-2",
            versaoDocumentoId = "v-2",
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

        docDao.inserir(doc1)
        docDao.inserir(doc2)
        versaoDao.inserir(v1)
        versaoDao.inserir(v2)
        partDao.inserir(p1)
        partDao.inserir(p2)

        val lista = docRepo.listarDocumentosDoSignatario(joaoId).first()
        assertEquals(2, lista.size)

        val pendentes = lista.filter { it.situacaoParticipante == SituacaoParticipante.PENDENTE }
        val aguardando = lista.filter { it.situacaoParticipante == SituacaoParticipante.AGUARDANDO_VEZ }

        assertEquals(1, pendentes.size)
        assertEquals("Contrato de Parceria", pendentes.first().titulo)

        assertEquals(1, aguardando.size)
        assertEquals("Aditivo de Contrato", aguardando.first().titulo)
    }

    @Test
    fun testCarregarDetalheParaSignatarioOrdemSequencialBloqueiaQuandoAguardandoVez() = runTest(testDispatcher) {
        val joaoId = "sig-joao-lima"
        val doc = DocumentoEntity(
            id = "doc-seq",
            usuarioAdminId = "admin-carla",
            titulo = "Acordo Comercial",
            dataCriacaoUtc = 1000L,
            dataUltimaAtualizacaoUtc = 1000L,
            situacaoAtual = SituacaoDocumento.ENVIADO
        )
        val v = VersaoDocumentoEntity(
            id = "v-seq",
            documentoId = "doc-seq",
            numeroVersao = 1,
            pdfPathInterno = "path",
            sha256Original = "sha",
            sha256Final = null,
            tipoOrdem = TipoOrdem.SEQUENCIAL,
            mensagemConvite = "Favor assinar",
            chaveIdempotencia = "idem",
            etapaWizardAtual = 9,
            situacao = SituacaoDocumento.ENVIADO
        )

        val part1 = ParticipanteDocumentoEntity(
            id = "p1",
            versaoDocumentoId = "v-seq",
            usuarioId = "user-1",
            nomeSnapshot = "Ana Silva",
            emailSnapshot = "ana@email.com",
            telefoneSnapshot = "11911111111",
            dataNascimentoSnapshot = "01/01/1985",
            papel = PapelParticipante.CONTRATANTE,
            obrigatorio = true,
            ordem = 1,
            situacao = SituacaoParticipante.PENDENTE
        )
        val part2 = ParticipanteDocumentoEntity(
            id = "p2",
            versaoDocumentoId = "v-seq",
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

        docDao.inserir(doc)
        versaoDao.inserir(v)
        partDao.inserir(part1)
        partDao.inserir(part2)

        val detalhe = docRepo.carregarDetalheParaSignatario("doc-seq", joaoId).first()
        assertNotNull(detalhe)
        assertEquals(false, detalhe!!.podeAssinar)
        assertNotNull(detalhe.motivoBloqueio)
        assertTrue(detalhe.motivoBloqueio!!.contains("ordem sequencial"))
    }

    @Test
    fun testConfirmacaoIdentidadeComSucesso() = runTest(testDispatcher) {
        val joaoId = "sig-joao-lima"
        val participanteId = "part-identidade"
        partDao.inserir(
            ParticipanteDocumentoEntity(
                id = participanteId,
                versaoDocumentoId = "v-id",
                usuarioId = joaoId,
                nomeSnapshot = "João Lima",
                emailSnapshot = "joao.lima@email.com",
                telefoneSnapshot = "11988887777",
                dataNascimentoSnapshot = "10/05/1992",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.PENDENTE
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

        // Variação de acentos/caixa no nome e data com barras
        val res = docRepo.validarIdentidadeSignatario(
            participanteId = participanteId,
            usuarioId = joaoId,
            nomeDigitado = "  joao lima ",
            dataNascimentoDigitada = "10051992",
            senhaDigitada = "Senha123"
        )

        assertTrue(res.isSuccess)
        assertEquals(true, res.getOrNull())

        // Verifica que evento de confirmação de identidade foi registrado
        val eventos = eventoDao.eventos
        assertTrue(eventos.any { it.tipoEvento == TipoEventoHistorico.CONFIRMACAO_IDENTIDADE })
    }

    @Test
    fun testConfirmacaoIdentidade5TentativasEBloqueio15Minutos() = runTest(testDispatcher) {
        val joaoId = "sig-joao-lima"
        val participanteId = "part-bloqueio"
        partDao.inserir(
            ParticipanteDocumentoEntity(
                id = participanteId,
                versaoDocumentoId = "v-bloq",
                usuarioId = joaoId,
                nomeSnapshot = "João Lima",
                emailSnapshot = "joao.lima@email.com",
                telefoneSnapshot = "11988887777",
                dataNascimentoSnapshot = "10/05/1992",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.PENDENTE
            )
        )

        // Tentativas 1 a 4 devem falhar com indicação de tentativa
        for (i in 1..4) {
            val res = docRepo.validarIdentidadeSignatario(
                participanteId = participanteId,
                usuarioId = joaoId,
                nomeDigitado = "Nome Errado",
                dataNascimentoDigitada = "01011990",
                senhaDigitada = "errada"
            )
            assertTrue("Tentativa $i deveria falhar", res.isFailure)
            val msg = res.exceptionOrNull()?.message ?: ""
            assertTrue(msg.contains("Tentativa $i de 5"))
        }

        // 5ª tentativa deve acionar o bloqueio temporário
        val res5 = docRepo.validarIdentidadeSignatario(
            participanteId = participanteId,
            usuarioId = joaoId,
            nomeDigitado = "Nome Errado",
            dataNascimentoDigitada = "01011990",
            senhaDigitada = "errada"
        )
        assertTrue(res5.isFailure)
        val msgBloqueio = res5.exceptionOrNull()?.message ?: ""
        assertTrue(msgBloqueio.contains("15 minutos"))

        // Próxima tentativa mesmo com dados certos deve ser rejeitada por estar bloqueado
        val resAposBloqueio = docRepo.validarIdentidadeSignatario(
            participanteId = participanteId,
            usuarioId = joaoId,
            nomeDigitado = "João Lima",
            dataNascimentoDigitada = "10/05/1992",
            senhaDigitada = "Senha123"
        )
        assertTrue(resAposBloqueio.isFailure)
        assertTrue(resAposBloqueio.exceptionOrNull()?.message?.contains("segurança") == true)
    }

    @Test
    fun testRecusarAssinaturaComMotivoESenhaAtualizaDocumentoERegistraAuditoria() = runTest(testDispatcher) {
        val joaoId = "sig-joao-lima"
        val docId = "doc-recusa"
        val versaoId = "v-recusa"
        val partId = "p-recusa"

        docDao.inserir(
            DocumentoEntity(
                id = docId,
                usuarioAdminId = "admin-carla",
                titulo = "Contrato de Fornecimento",
                dataCriacaoUtc = 1000L,
                dataUltimaAtualizacaoUtc = 1000L,
                situacaoAtual = SituacaoDocumento.ENVIADO
            )
        )
        versaoDao.inserir(
            VersaoDocumentoEntity(
                id = versaoId,
                documentoId = docId,
                numeroVersao = 1,
                pdfPathInterno = "path",
                sha256Original = "sha",
                sha256Final = null,
                tipoOrdem = TipoOrdem.PARALELA,
                mensagemConvite = "Assine",
                chaveIdempotencia = "idem",
                etapaWizardAtual = 9,
                situacao = SituacaoDocumento.ENVIADO
            )
        )
        partDao.inserir(
            ParticipanteDocumentoEntity(
                id = partId,
                versaoDocumentoId = versaoId,
                usuarioId = joaoId,
                nomeSnapshot = "João Lima",
                emailSnapshot = "joao.lima@email.com",
                telefoneSnapshot = "11988887777",
                dataNascimentoSnapshot = "10/05/1992",
                papel = PapelParticipante.CONTRATADO,
                obrigatorio = true,
                ordem = 1,
                situacao = SituacaoParticipante.PENDENTE
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

        // Falha com motivo vazio
        val resVazio = docRepo.recusarAssinatura(docId, versaoId, partId, "", "Senha123")
        assertTrue(resVazio.isFailure)

        // Falha com senha errada
        val resSenhaErrada = docRepo.recusarAssinatura(docId, versaoId, partId, "Cláusula 4 abusiva", "SenhaIncorreta")
        assertTrue(resSenhaErrada.isFailure)

        // Sucesso
        val resSucesso = docRepo.recusarAssinatura(docId, versaoId, partId, "Cláusula 4 com taxa indevida", "Senha123")
        assertTrue(resSucesso.isSuccess)

        // Verifica situações
        val partAtualizado = partDao.buscarPorId(partId)
        assertEquals(SituacaoParticipante.RECUSADO, partAtualizado?.situacao)

        val docAtualizado = docDao.buscarPorId(docId)
        assertEquals(SituacaoDocumento.RECUSADO, docAtualizado?.situacaoAtual)

        val versaoAtualizada = versaoDao.buscarPorId(versaoId)
        assertEquals(SituacaoDocumento.RECUSADO, versaoAtualizada?.situacao)

        // Verifica registro de recusa
        val recusa = recusaDao.buscarPorParticipante(partId)
        assertNotNull(recusa)
        assertEquals("Cláusula 4 com taxa indevida", recusa?.observacao)

        // Verifica auditoria
        val eventos = eventoDao.eventos
        assertTrue(eventos.any { it.tipoEvento == TipoEventoHistorico.RECUSA })
    }
}

package br.com.letssign

import br.com.letssign.data.remote.FakeRemoteDataSource
import br.com.letssign.data.remote.RemoteCampoDto
import br.com.letssign.data.remote.RemoteDocumentoEnvioRequest
import br.com.letssign.data.remote.RemoteParticipanteDto
import br.com.letssign.domain.model.PapelParticipante
import br.com.letssign.domain.model.TipoOrdem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FakeRemoteDataSourceTest {

    private lateinit var fakeServer: FakeRemoteDataSource

    @Before
    fun setUp() {
        fakeServer = FakeRemoteDataSource(latenciaSimuladaMs = 0L)
    }

    @Test
    fun `testar autenticacao com sucesso do usuario padrao Carla Mendes`() = runBlocking {
        val user = fakeServer.autenticar("carla@empresa.com.br", "Senha123")
        assertEquals("Carla Mendes", user.nome)
        assertEquals("admin-carla-mendes", user.id)
    }

    @Test
    fun `testar regra P01 unicidade global de email no cadastro`() = runBlocking {
        try {
            fakeServer.cadastrarAdmin("Outra Carla", "carla@empresa.com.br", "11999999999", "Senha123")
            fail("Deveria ter lançado IllegalStateException para email duplicado")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("P01") == true)
        }
    }

    @Test
    fun `testar regra RN05 idempotencia de envio com a mesma chave`() = runBlocking {
        val chaveIdempotencia = UUID.randomUUID().toString()
        val request = RemoteDocumentoEnvioRequest(
            versaoId = "versao-1",
            documentoId = "doc-1",
            titulo = "Contrato de Prestação de Serviços",
            adminId = "admin-carla-mendes",
            sha256Pdf = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            tipoOrdem = TipoOrdem.SEQUENCIAL,
            mensagemConvite = "Por favor assine o contrato",
            chaveIdempotencia = chaveIdempotencia,
            participantes = listOf(
                RemoteParticipanteDto(
                    id = "part-1",
                    nome = "João Lima",
                    email = "joao.lima@email.com",
                    telefone = "11912345678",
                    dataNascimento = "1990-05-15",
                    papel = PapelParticipante.CONTRATADO,
                    obrigatorio = true,
                    ordem = 1
                )
            ),
            campos = listOf(
                RemoteCampoDto(
                    id = "campo-1",
                    participanteId = "part-1",
                    pagina = 1,
                    x = 0.2f,
                    y = 0.8f,
                    largura = 0.4f,
                    altura = 0.1f
                )
            )
        )

        val resposta1 = fakeServer.enviarDocumento(request)
        val resposta2 = fakeServer.enviarDocumento(request)

        // Devem retornar exatamente o mesmo envio e mesma resposta
        assertEquals(resposta1.envioId, resposta2.envioId)
        assertEquals(resposta1.chaveIdempotencia, resposta2.chaveIdempotencia)
        assertEquals(resposta1.dataHoraUtc, resposta2.dataHoraUtc)
    }

    @Test
    fun `testar confirmacao de identidade com regra P04 normalizacao e regra P05 5 tentativas`() = runBlocking {
        // Enviar documento primeiro para cadastrar o participante
        val request = RemoteDocumentoEnvioRequest(
            versaoId = "versao-2",
            documentoId = "doc-2",
            titulo = "Termo Aditivo",
            adminId = "admin-carla-mendes",
            sha256Pdf = "hash123",
            tipoOrdem = TipoOrdem.PARALELA,
            mensagemConvite = "Assinatura",
            chaveIdempotencia = UUID.randomUUID().toString(),
            participantes = listOf(
                RemoteParticipanteDto(
                    id = "part-ana",
                    nome = "Ana Cláudia Silva",
                    email = "ana.silva@email.com",
                    telefone = "11988887777",
                    dataNascimento = "1988-10-20",
                    papel = PapelParticipante.CONTRATANTE,
                    obrigatorio = true,
                    ordem = 1
                )
            ),
            campos = listOf(
                RemoteCampoDto("c-ana", "part-ana", 1, 0.1f, 0.1f, 0.3f, 0.1f)
            )
        )
        fakeServer.enviarDocumento(request)

        // Teste de normalização P04: nome com acentos e maiúsculas deve bater com "ana claudia silva"
        val resultadoNormalizado = fakeServer.validarIdentidadeSignatario(
            participanteId = "part-ana",
            nome = "  ANA claudia SILVA  ",
            email = "  ANA.SILVA@EMAIL.COM  ",
            dataNascimento = "1988-10-20"
        )
        assertTrue(resultadoNormalizado.sucesso)

        // Testar 5 erros consecutivos para gerar bloqueio de 15 minutos (P05)
        for (i in 1..4) {
            val erro = fakeServer.validarIdentidadeSignatario("part-ana", "Nome Incorreto", "ana@email.com", "1900-01-01")
            assertFalse(erro.sucesso)
            assertFalse(erro.bloqueado)
            assertEquals(5 - i, erro.tentativasRestantes)
        }

        // 5ª tentativa falha -> Bloqueio ativado
        val quintoErro = fakeServer.validarIdentidadeSignatario("part-ana", "Nome Incorreto", "ana@email.com", "1900-01-01")
        assertFalse(quintoErro.sucesso)
        assertTrue(quintoErro.bloqueado)
        assertTrue(quintoErro.tempoBloqueioRestanteMs > 0)
    }
}

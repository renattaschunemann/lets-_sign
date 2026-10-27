package br.com.letssign.data.remote

import br.com.letssign.core.common.text.TextNormalizer
import br.com.letssign.domain.model.*
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID

data class RemoteUsuarioDto(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String,
    val perfil: PerfilUsuario,
    val senhaHash: String
)

data class RemoteDocumentoEnvioRequest(
    val versaoId: String,
    val documentoId: String,
    val titulo: String,
    val adminId: String,
    val sha256Pdf: String,
    val tipoOrdem: TipoOrdem,
    val mensagemConvite: String,
    val chaveIdempotencia: String,
    val participantes: List<RemoteParticipanteDto>,
    val campos: List<RemoteCampoDto>
)

data class RemoteParticipanteDto(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String,
    val dataNascimento: String,
    val papel: PapelParticipante,
    val obrigatorio: Boolean,
    val ordem: Int
)

data class RemoteCampoDto(
    val id: String,
    val participanteId: String,
    val pagina: Int,
    val x: Float,
    val y: Float,
    val largura: Float,
    val altura: Float
)

data class RemoteEnvioResponse(
    val envioId: String,
    val chaveIdempotencia: String,
    val status: String,
    val dataHoraUtc: Long,
    val linksConvite: Map<String, String> // participanteId -> link
)

data class RemoteConfirmacaoIdentidadeResult(
    val sucesso: Boolean,
    val bloqueado: Boolean = false,
    val tempoBloqueioRestanteMs: Long = 0,
    val tentativasRestantes: Int = 0,
    val mensagemErro: String? = null
)

interface RemoteDataSource {
    suspend fun autenticar(email: String, senhaHash: String): RemoteUsuarioDto
    suspend fun cadastrarAdmin(nome: String, email: String, telefone: String, senhaHash: String): RemoteUsuarioDto
    suspend fun enviarDocumento(request: RemoteDocumentoEnvioRequest): RemoteEnvioResponse
    suspend fun resolverLinkConvite(token: String): Pair<String, String> // Retorna Pair(documentoId, participanteId)
    suspend fun validarIdentidadeSignatario(
        participanteId: String,
        nome: String,
        email: String,
        dataNascimento: String
    ): RemoteConfirmacaoIdentidadeResult
    suspend fun registrarAssinatura(
        participanteId: String,
        versaoId: String,
        tipoAssinatura: TipoAssinatura,
        assinaturaBase64: String
    ): Boolean
    suspend fun registrarRecusa(
        participanteId: String,
        versaoId: String,
        motivo: String
    ): Boolean
    suspend fun cancelarDocumento(
        documentoId: String,
        adminId: String,
        motivo: String
    ): Boolean
}

class FakeRemoteDataSource(
    var latenciaSimuladaMs: Long = 300L,
    var simularFalhaRede: Boolean = false
) : RemoteDataSource {

    // Tabelas simuladas em memória
    private val usuariosDb = mutableMapOf<String, RemoteUsuarioDto>()
    private val enviosPorChaveIdempotencia = mutableMapOf<String, RemoteEnvioResponse>()
    private val linksConviteParaParticipante = mutableMapOf<String, Pair<String, String>>() // token -> (docId, participanteId)
    private val tentativasIdentidade = mutableMapOf<String, Pair<Int, Long>>() // participanteId -> (tentativasFalhas, timestampBloqueio)
    private val participantesCadastrados = mutableMapOf<String, RemoteParticipanteDto>()
    private val versoesDb = mutableMapOf<String, RemoteDocumentoEnvioRequest>()
    private val situacaoDocumentos = mutableMapOf<String, SituacaoDocumento>()

    init {
        // Usuário padrão de demonstração Carla Mendes (Admin)
        val carla = RemoteUsuarioDto(
            id = "admin-carla-mendes",
            nome = "Carla Mendes",
            email = "carla@empresa.com.br",
            telefone = "11987654321",
            perfil = PerfilUsuario.ADMIN,
            senhaHash = "Senha123"
        )
        usuariosDb[carla.email] = carla

        // Usuário padrão de demonstração João Lima (Signatário)
        val joao = RemoteUsuarioDto(
            id = "sig-joao-lima",
            nome = "João Lima",
            email = "joao.lima@email.com",
            telefone = "11912345678",
            perfil = PerfilUsuario.SIGNATARIO,
            senhaHash = "Senha123"
        )
        usuariosDb[joao.email] = joao
    }

    private suspend fun simularRede() {
        if (latenciaSimuladaMs > 0) {
            delay(latenciaSimuladaMs)
        }
        if (simularFalhaRede) {
            throw IOException("Falha simulada de conexão com o servidor LetsSign (E01/E22).")
        }
    }

    override suspend fun autenticar(email: String, senhaHash: String): RemoteUsuarioDto {
        simularRede()
        val emailNormalizado = email.trim().lowercase()
        val usuario = usuariosDb[emailNormalizado]
            ?: throw IllegalArgumentException("E-mail não cadastrado (E02).")

        if (usuario.senhaHash != senhaHash) {
            throw IllegalArgumentException("Senha incorreta (E02).")
        }
        return usuario
    }

    override suspend fun cadastrarAdmin(
        nome: String,
        email: String,
        telefone: String,
        senhaHash: String
    ): RemoteUsuarioDto {
        simularRede()
        val emailNorm = email.trim().lowercase()
        // Regra P01: Unicidade global de e-mails
        if (usuariosDb.containsKey(emailNorm)) {
            throw IllegalStateException("E-mail já cadastrado no sistema (P01/E06).")
        }

        val novoUsuario = RemoteUsuarioDto(
            id = UUID.randomUUID().toString(),
            nome = nome.trim(),
            email = emailNorm,
            telefone = telefone.trim(),
            perfil = PerfilUsuario.ADMIN,
            senhaHash = senhaHash
        )
        usuariosDb[emailNorm] = novoUsuario
        return novoUsuario
    }

    override suspend fun enviarDocumento(request: RemoteDocumentoEnvioRequest): RemoteEnvioResponse {
        simularRede()

        // Regra RN05 / RF05: Idempotência de envio
        enviosPorChaveIdempotencia[request.chaveIdempotencia]?.let { envioExistente ->
            return envioExistente
        }

        // Regra RN02: Mínimo 1 obrigatório
        val temObrigatorio = request.participantes.any { it.obrigatorio }
        if (!temObrigatorio) {
            throw IllegalArgumentException("O documento deve conter ao menos 1 signatário obrigatório (RN02/E19).")
        }

        // Regra RN04: Todos devem ter campos de assinatura
        val idsComCampo = request.campos.map { it.participanteId }.toSet()
        val todosTemCampo = request.participantes.all { it.id in idsComCampo }
        if (!todosTemCampo) {
            throw IllegalArgumentException("Todos os participantes devem ter ao menos 1 campo no PDF (RN04/E21).")
        }

        // Gerar tokens e links de convite
        val links = mutableMapOf<String, String>()
        request.participantes.forEach { participante ->
            participantesCadastrados[participante.id] = participante
            val token = UUID.randomUUID().toString().replace("-", "").take(16)
            linksConviteParaParticipante[token] = Pair(request.documentoId, participante.id)
            links[participante.id] = "https://links.letssign.com.br/convite/$token"
        }

        versoesDb[request.versaoId] = request
        situacaoDocumentos[request.documentoId] = SituacaoDocumento.ENVIADO

        val response = RemoteEnvioResponse(
            envioId = UUID.randomUUID().toString(),
            chaveIdempotencia = request.chaveIdempotencia,
            status = "ENVIADO",
            dataHoraUtc = System.currentTimeMillis(),
            linksConvite = links
        )
        enviosPorChaveIdempotencia[request.chaveIdempotencia] = response
        return response
    }

    override suspend fun resolverLinkConvite(token: String): Pair<String, String> {
        simularRede()
        return linksConviteParaParticipante[token]
            ?: throw IllegalArgumentException("Link de convite inválido ou expirado (E07/E08).")
    }

    override suspend fun validarIdentidadeSignatario(
        participanteId: String,
        nome: String,
        email: String,
        dataNascimento: String
    ): RemoteConfirmacaoIdentidadeResult {
        simularRede()

        val participante = participantesCadastrados[participanteId]
            ?: return RemoteConfirmacaoIdentidadeResult(
                sucesso = false,
                mensagemErro = "Participante não encontrado."
            )

        val agora = System.currentTimeMillis()
        val tentativas = tentativasIdentidade[participanteId] ?: Pair(0, 0L)
        val (falhas, bloqueioAte) = tentativas

        // Regra P05: Bloqueio de 15 minutos
        if (agora < bloqueioAte) {
            val restante = bloqueioAte - agora
            return RemoteConfirmacaoIdentidadeResult(
                sucesso = false,
                bloqueado = true,
                tempoBloqueioRestanteMs = restante,
                mensagemErro = "Limite de 5 tentativas excedido. Bloqueado por mais ${restante / 1000}s (P05/E24)."
            )
        }

        // Regra P04: Normalização estrita
        val nomeOk = TextNormalizer.saoEquivalentes(nome, participante.nome)
        val emailOk = TextNormalizer.saoEquivalentes(email, participante.email)
        val dataOk = dataNascimento.trim() == participante.dataNascimento.trim()

        if (nomeOk && emailOk && dataOk) {
            // Sucesso: zera tentativas
            tentativasIdentidade.remove(participanteId)
            return RemoteConfirmacaoIdentidadeResult(sucesso = true)
        } else {
            val novasFalhas = falhas + 1
            if (novasFalhas >= 5) {
                val novoBloqueio = agora + (15 * 60 * 1000L) // 15 minutos
                tentativasIdentidade[participanteId] = Pair(0, novoBloqueio)
                return RemoteConfirmacaoIdentidadeResult(
                    sucesso = false,
                    bloqueado = true,
                    tempoBloqueioRestanteMs = 15 * 60 * 1000L,
                    mensagemErro = "Limite de 5 tentativas excedido. Bloqueado por 15 minutos (P05/E24)."
                )
            } else {
                tentativasIdentidade[participanteId] = Pair(novasFalhas, 0L)
                return RemoteConfirmacaoIdentidadeResult(
                    sucesso = false,
                    tentativasRestantes = 5 - novasFalhas,
                    mensagemErro = "Dados divergentes. Você tem mais ${5 - novasFalhas} tentativa(s) (P05/E23)."
                )
            }
        }
    }

    override suspend fun registrarAssinatura(
        participanteId: String,
        versaoId: String,
        tipoAssinatura: TipoAssinatura,
        assinaturaBase64: String
    ): Boolean {
        simularRede()
        val versao = versoesDb[versaoId]
            ?: throw IllegalStateException("Versão do documento não encontrada.")

        val docId = versao.documentoId
        val situacaoAtual = situacaoDocumentos[docId] ?: SituacaoDocumento.ENVIADO

        // Regra RN08: Revalidação no ato da assinatura
        if (situacaoAtual == SituacaoDocumento.CANCELADO || situacaoAtual == SituacaoDocumento.RECUSADO) {
            throw IllegalStateException("O documento não pode ser assinado pois foi cancelado ou recusado (RN08/E25/E26).")
        }

        return true
    }

    override suspend fun registrarRecusa(
        participanteId: String,
        versaoId: String,
        motivo: String
    ): Boolean {
        simularRede()
        val versao = versoesDb[versaoId]
            ?: throw IllegalStateException("Versão do documento não encontrada.")

        val participante = participantesCadastrados[participanteId]
            ?: throw IllegalStateException("Participante não encontrado.")

        val docId = versao.documentoId

        // Regra RN09: Recusa de Obrigatório encerra documento como RECUSADO
        // Recusa de Opcional mantém o documento
        if (participante.obrigatorio) {
            situacaoDocumentos[docId] = SituacaoDocumento.RECUSADO
        }
        return true
    }

    override suspend fun cancelarDocumento(
        documentoId: String,
        adminId: String,
        motivo: String
    ): Boolean {
        simularRede()
        situacaoDocumentos[documentoId] = SituacaoDocumento.CANCELADO
        return true
    }
}

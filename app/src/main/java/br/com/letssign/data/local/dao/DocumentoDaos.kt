package br.com.letssign.data.local.dao

import androidx.room.*
import br.com.letssign.data.local.entities.*
import br.com.letssign.domain.model.SituacaoDocumento
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(documento: DocumentoEntity)

    @Update
    suspend fun atualizar(documento: DocumentoEntity)

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE usuarioAdminId = :adminId ORDER BY dataUltimaAtualizacaoUtc DESC")
    fun listarPorAdmin(adminId: String): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE usuarioAdminId = :adminId AND situacaoAtual = 'RASCUNHO' ORDER BY dataUltimaAtualizacaoUtc DESC LIMIT 1")
    suspend fun buscarUltimoRascunho(adminId: String): DocumentoEntity?

    @Delete
    suspend fun deletar(documento: DocumentoEntity)
}

@Dao
interface VersaoDocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(versao: VersaoDocumentoEntity)

    @Update
    suspend fun atualizar(versao: VersaoDocumentoEntity)

    @Query("SELECT * FROM versoes_documento WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): VersaoDocumentoEntity?

    @Query("SELECT * FROM versoes_documento WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<VersaoDocumentoEntity?>

    @Query("SELECT * FROM versoes_documento WHERE documentoId = :documentoId ORDER BY numeroVersao DESC LIMIT 1")
    suspend fun buscarVersaoAtual(documentoId: String): VersaoDocumentoEntity?

    @Query("SELECT * FROM versoes_documento WHERE documentoId = :documentoId ORDER BY numeroVersao DESC LIMIT 1")
    fun observarVersaoAtual(documentoId: String): Flow<VersaoDocumentoEntity?>

    @Query("SELECT * FROM versoes_documento WHERE documentoId = :documentoId ORDER BY numeroVersao ASC")
    fun listarVersoesDoDocumento(documentoId: String): Flow<List<VersaoDocumentoEntity>>
}

@Dao
interface ParticipanteDocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(participante: ParticipanteDocumentoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirVarios(participantes: List<ParticipanteDocumentoEntity>)

    @Update
    suspend fun atualizar(participante: ParticipanteDocumentoEntity)

    @Query("SELECT * FROM participantes_documento WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): ParticipanteDocumentoEntity?

    @Query("SELECT * FROM participantes_documento WHERE versaoDocumentoId = :versaoDocumentoId ORDER BY ordem ASC")
    fun listarPorVersao(versaoDocumentoId: String): Flow<List<ParticipanteDocumentoEntity>>

    @Query("SELECT * FROM participantes_documento WHERE versaoDocumentoId = :versaoDocumentoId ORDER BY ordem ASC")
    suspend fun listarPorVersaoSync(versaoDocumentoId: String): List<ParticipanteDocumentoEntity>

    @Query("SELECT * FROM participantes_documento WHERE usuarioId = :usuarioId ORDER BY id DESC")
    fun listarPorUsuario(usuarioId: String): Flow<List<ParticipanteDocumentoEntity>>

    @Query("DELETE FROM participantes_documento WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("DELETE FROM participantes_documento WHERE versaoDocumentoId = :versaoDocumentoId")
    suspend fun deletarPorVersao(versaoDocumentoId: String)
}

@Dao
interface CampoAssinaturaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(campo: CampoAssinaturaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirVarios(campos: List<CampoAssinaturaEntity>)

    @Update
    suspend fun atualizar(campo: CampoAssinaturaEntity)

    @Delete
    suspend fun deletar(campo: CampoAssinaturaEntity)

    @Query("DELETE FROM campos_assinatura WHERE id = :id")
    suspend fun deletarPorId(id: String)

    @Query("SELECT * FROM campos_assinatura WHERE versaoDocumentoId = :versaoDocumentoId")
    fun listarPorVersao(versaoDocumentoId: String): Flow<List<CampoAssinaturaEntity>>

    @Query("SELECT * FROM campos_assinatura WHERE versaoDocumentoId = :versaoDocumentoId AND numeroPagina = :numeroPagina")
    fun listarPorVersaoEPagina(versaoDocumentoId: String, numeroPagina: Int): Flow<List<CampoAssinaturaEntity>>

    @Query("SELECT * FROM campos_assinatura WHERE participanteId = :participanteId")
    fun listarPorParticipante(participanteId: String): Flow<List<CampoAssinaturaEntity>>

    @Query("SELECT * FROM campos_assinatura WHERE participanteId = :participanteId")
    suspend fun listarPorParticipanteSync(participanteId: String): List<CampoAssinaturaEntity>
}

@Dao
interface AssinaturaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(assinatura: AssinaturaEntity)

    @Query("SELECT * FROM assinaturas WHERE campoAssinaturaId = :campoAssinaturaId LIMIT 1")
    suspend fun buscarPorCampo(campoAssinaturaId: String): AssinaturaEntity?

    @Query("SELECT * FROM assinaturas WHERE participanteId = :participanteId")
    fun listarPorParticipante(participanteId: String): Flow<List<AssinaturaEntity>>
}

@Dao
interface RecusaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(recusa: RecusaEntity)

    @Query("SELECT * FROM recusas WHERE participanteId = :participanteId LIMIT 1")
    suspend fun buscarPorParticipante(participanteId: String): RecusaEntity?
}

@Dao
interface CancelamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(cancelamento: CancelamentoEntity)

    @Query("SELECT * FROM cancelamentos WHERE versaoDocumentoId = :versaoDocumentoId LIMIT 1")
    suspend fun buscarPorVersao(versaoDocumentoId: String): CancelamentoEntity?
}

@Dao
interface EnvioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(envio: EnvioEntity)

    @Query("SELECT * FROM envios WHERE chaveIdempotencia = :chaveIdempotencia LIMIT 1")
    suspend fun buscarPorChaveIdempotencia(chaveIdempotencia: String): EnvioEntity?
}

@Dao
interface NotificacaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(notificacao: NotificacaoEntity)

    @Query("SELECT * FROM notificacoes WHERE usuarioId = :usuarioId ORDER BY dataHoraUtc DESC")
    fun listarPorUsuario(usuarioId: String): Flow<List<NotificacaoEntity>>

    @Query("SELECT * FROM notificacoes WHERE usuarioId = :usuarioId ORDER BY dataHoraUtc DESC")
    suspend fun buscarPorUsuarioSync(usuarioId: String): List<NotificacaoEntity>

    @Query("UPDATE notificacoes SET lida = 1 WHERE id = :id")
    suspend fun marcarComoLida(id: String)

    @Query("UPDATE notificacoes SET lida = 1 WHERE usuarioId = :usuarioId AND lida = 0")
    suspend fun marcarTodasComoLidas(usuarioId: String)

    @Query("SELECT COUNT(*) FROM notificacoes WHERE usuarioId = :usuarioId AND lida = 0")
    fun contarNaoLidas(usuarioId: String): Flow<Int>
}

@Dao
interface EventoHistoricoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(evento: EventoHistoricoEntity)

    @Query("SELECT * FROM eventos_historico WHERE versaoDocumentoId = :versaoDocumentoId ORDER BY dataHoraUtc ASC")
    fun listarPorVersaoAsc(versaoDocumentoId: String): Flow<List<EventoHistoricoEntity>>

    @Query("SELECT * FROM eventos_historico WHERE versaoDocumentoId = :versaoDocumentoId ORDER BY dataHoraUtc DESC")
    fun listarPorVersaoDesc(versaoDocumentoId: String): Flow<List<EventoHistoricoEntity>>
}

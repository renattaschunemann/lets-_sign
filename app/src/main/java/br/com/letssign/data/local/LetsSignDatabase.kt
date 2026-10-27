package br.com.letssign.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.letssign.data.local.converters.Converters
import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.entities.*

@Database(
    entities = [
        UsuarioEntity::class,
        AdminSignatarioEntity::class,
        DocumentoEntity::class,
        VersaoDocumentoEntity::class,
        ParticipanteDocumentoEntity::class,
        CampoAssinaturaEntity::class,
        AssinaturaEntity::class,
        RecusaEntity::class,
        CancelamentoEntity::class,
        EnvioEntity::class,
        NotificacaoEntity::class,
        EventoHistoricoEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LetsSignDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun adminSignatarioDao(): AdminSignatarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun versaoDocumentoDao(): VersaoDocumentoDao
    abstract fun participanteDocumentoDao(): ParticipanteDocumentoDao
    abstract fun campoAssinaturaDao(): CampoAssinaturaDao
    abstract fun assinaturaDao(): AssinaturaDao
    abstract fun recusaDao(): RecusaDao
    abstract fun cancelamentoDao(): CancelamentoDao
    abstract fun envioDao(): EnvioDao
    abstract fun notificacaoDao(): NotificacaoDao
    abstract fun eventoHistoricoDao(): EventoHistoricoDao

    companion object {
        const val DATABASE_NAME = "letssign_database.db"
    }
}

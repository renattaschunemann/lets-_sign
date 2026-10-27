package br.com.letssign.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import br.com.letssign.data.local.LetsSignDatabase
import br.com.letssign.data.local.dao.*
import br.com.letssign.data.local.datastore.SessionManager
import br.com.letssign.data.local.security.CryptoManager
import br.com.letssign.data.local.security.SecurityCipher
import br.com.letssign.data.remote.FakeRemoteDataSource
import br.com.letssign.data.remote.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LetsSignDatabase {
        return Room.databaseBuilder(
            context,
            LetsSignDatabase::class.java,
            LetsSignDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideUsuarioDao(db: LetsSignDatabase): UsuarioDao = db.usuarioDao()

    @Provides
    fun provideAdminSignatarioDao(db: LetsSignDatabase): AdminSignatarioDao = db.adminSignatarioDao()

    @Provides
    fun provideDocumentoDao(db: LetsSignDatabase): DocumentoDao = db.documentoDao()

    @Provides
    fun provideVersaoDocumentoDao(db: LetsSignDatabase): VersaoDocumentoDao = db.versaoDocumentoDao()

    @Provides
    fun provideParticipanteDocumentoDao(db: LetsSignDatabase): ParticipanteDocumentoDao = db.participanteDocumentoDao()

    @Provides
    fun provideCampoAssinaturaDao(db: LetsSignDatabase): CampoAssinaturaDao = db.campoAssinaturaDao()

    @Provides
    fun provideAssinaturaDao(db: LetsSignDatabase): AssinaturaDao = db.assinaturaDao()

    @Provides
    fun provideRecusaDao(db: LetsSignDatabase): RecusaDao = db.recusaDao()

    @Provides
    fun provideCancelamentoDao(db: LetsSignDatabase): CancelamentoDao = db.cancelamentoDao()

    @Provides
    fun provideEnvioDao(db: LetsSignDatabase): EnvioDao = db.envioDao()

    @Provides
    fun provideNotificacaoDao(db: LetsSignDatabase): NotificacaoDao = db.notificacaoDao()

    @Provides
    fun provideEventoHistoricoDao(db: LetsSignDatabase): EventoHistoricoDao = db.eventoHistoricoDao()
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("letssign_secure_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideSecurityCipher(): SecurityCipher {
        return CryptoManager()
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        dataStore: DataStore<Preferences>,
        cipher: SecurityCipher
    ): SessionManager {
        return SessionManager(dataStore, cipher)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRemoteDataSource(): RemoteDataSource {
        return FakeRemoteDataSource(
            latenciaSimuladaMs = 300L,
            simularFalhaRede = false
        )
    }
}

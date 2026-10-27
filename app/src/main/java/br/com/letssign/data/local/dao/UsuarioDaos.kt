package br.com.letssign.data.local.dao

import androidx.room.*
import br.com.letssign.data.local.entities.AdminSignatarioEntity
import br.com.letssign.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: UsuarioEntity)

    @Update
    suspend fun atualizar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    fun buscarPorEmailFlow(email: String): Flow<UsuarioEntity?>
}

@Dao
interface AdminSignatarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(relacao: AdminSignatarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirVarios(relacoes: List<AdminSignatarioEntity>)

    @Query("SELECT * FROM admin_signatarios WHERE adminId = :adminId ORDER BY nome ASC")
    fun listarSignatariosDoAdmin(adminId: String): Flow<List<AdminSignatarioEntity>>

    @Query("SELECT * FROM admin_signatarios WHERE adminId = :adminId AND email = :email LIMIT 1")
    suspend fun buscarSignatarioPorEmail(adminId: String, email: String): AdminSignatarioEntity?

    @Query("SELECT * FROM admin_signatarios WHERE adminId = :adminId AND signatarioId = :signatarioId LIMIT 1")
    suspend fun buscarSignatarioPorId(adminId: String, signatarioId: String): AdminSignatarioEntity?

    @Query("DELETE FROM admin_signatarios WHERE adminId = :adminId AND signatarioId = :signatarioId")
    suspend fun deletarSignatario(adminId: String, signatarioId: String)
}

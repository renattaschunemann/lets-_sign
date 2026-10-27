package br.com.letssign.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.letssign.domain.model.PerfilUsuario

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String,
    val perfil: PerfilUsuario,
    val senhaHash: String,
    val dataCriacaoUtc: Long
)

@Entity(
    tableName = "admin_signatarios",
    primaryKeys = ["adminId", "signatarioId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["adminId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["signatarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["adminId"]),
        Index(value = ["signatarioId"]),
        Index(value = ["adminId", "email"], unique = true) // Regra P03: unicidade de email por Admin
    ]
)
data class AdminSignatarioEntity(
    val adminId: String,
    val signatarioId: String,
    val nome: String,
    val email: String,
    val telefone: String,
    val dataNascimento: String,
    val dataVinculoUtc: Long
)

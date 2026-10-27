package br.com.letssign.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.letssign.domain.model.*

@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioAdminId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioAdminId"]),
        Index(value = ["situacaoAtual"])
    ]
)
data class DocumentoEntity(
    @PrimaryKey
    val id: String,
    val usuarioAdminId: String,
    val titulo: String,
    val dataCriacaoUtc: Long,
    val dataUltimaAtualizacaoUtc: Long,
    val situacaoAtual: SituacaoDocumento
)

@Entity(
    tableName = "versoes_documento",
    foreignKeys = [
        ForeignKey(
            entity = DocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentoId"]),
        Index(value = ["chaveIdempotencia"], unique = true)
    ]
)
data class VersaoDocumentoEntity(
    @PrimaryKey
    val id: String,
    val documentoId: String,
    val numeroVersao: Int,
    val pdfPathInterno: String,
    val sha256Original: String,
    val sha256Final: String? = null,
    val tipoOrdem: TipoOrdem,
    val mensagemConvite: String,
    val chaveIdempotencia: String,
    val etapaWizardAtual: Int,
    val situacao: SituacaoDocumento
)

@Entity(
    tableName = "participantes_documento",
    foreignKeys = [
        ForeignKey(
            entity = VersaoDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoDocumentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["versaoDocumentoId"]),
        Index(value = ["usuarioId"]),
        Index(value = ["versaoDocumentoId", "emailSnapshot"], unique = true) // Regra RN01: unicidade no documento
    ]
)
data class ParticipanteDocumentoEntity(
    @PrimaryKey
    val id: String,
    val versaoDocumentoId: String,
    val usuarioId: String? = null,
    val nomeSnapshot: String,
    val emailSnapshot: String,
    val telefoneSnapshot: String,
    val dataNascimentoSnapshot: String,
    val papel: PapelParticipante,
    val obrigatorio: Boolean,
    val ordem: Int,
    val situacao: SituacaoParticipante
)

@Entity(
    tableName = "campos_assinatura",
    foreignKeys = [
        ForeignKey(
            entity = VersaoDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoDocumentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipanteDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["participanteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["versaoDocumentoId"]),
        Index(value = ["participanteId"])
    ]
)
data class CampoAssinaturaEntity(
    @PrimaryKey
    val id: String,
    val versaoDocumentoId: String,
    val participanteId: String,
    val numeroPagina: Int,
    val normX: Float,
    val normY: Float,
    val normLargura: Float,
    val normAltura: Float
)

@Entity(
    tableName = "assinaturas",
    foreignKeys = [
        ForeignKey(
            entity = CampoAssinaturaEntity::class,
            parentColumns = ["id"],
            childColumns = ["campoAssinaturaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipanteDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["participanteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["campoAssinaturaId"], unique = true),
        Index(value = ["participanteId"])
    ]
)
data class AssinaturaEntity(
    @PrimaryKey
    val id: String,
    val campoAssinaturaId: String,
    val participanteId: String,
    val tipoAssinatura: TipoAssinatura,
    val caminhoPngAssinatura: String,
    val dataHoraUtc: Long,
    val ipDispositivo: String,
    val hashValidador: String
)

@Entity(
    tableName = "recusas",
    foreignKeys = [
        ForeignKey(
            entity = ParticipanteDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["participanteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["participanteId"], unique = true)
    ]
)
data class RecusaEntity(
    @PrimaryKey
    val id: String,
    val participanteId: String,
    val dataHoraUtc: Long,
    val observacao: String,
    val senhaConfirmada: Boolean
)

@Entity(
    tableName = "cancelamentos",
    foreignKeys = [
        ForeignKey(
            entity = VersaoDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoDocumentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["adminId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["versaoDocumentoId"], unique = true),
        Index(value = ["adminId"])
    ]
)
data class CancelamentoEntity(
    @PrimaryKey
    val id: String,
    val versaoDocumentoId: String,
    val adminId: String,
    val dataHoraUtc: Long,
    val motivo: String,
    val senhaConfirmada: Boolean
)

@Entity(
    tableName = "envios",
    foreignKeys = [
        ForeignKey(
            entity = VersaoDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoDocumentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["versaoDocumentoId"]),
        Index(value = ["chaveIdempotencia"], unique = true)
    ]
)
data class EnvioEntity(
    @PrimaryKey
    val id: String,
    val versaoDocumentoId: String,
    val chaveIdempotencia: String,
    val dataHoraUtc: Long,
    val statusEnvio: String,
    val respostaServidorJson: String
)

@Entity(
    tableName = "notificacoes",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["documentoId"]),
        Index(value = ["dataHoraUtc"])
    ]
)
data class NotificacaoEntity(
    @PrimaryKey
    val id: String,
    val usuarioId: String,
    val documentoId: String? = null,
    val titulo: String,
    val mensagem: String,
    val tipoNotificacao: String,
    val dataHoraUtc: Long,
    val lida: Boolean
)

@Entity(
    tableName = "eventos_historico",
    foreignKeys = [
        ForeignKey(
            entity = VersaoDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoDocumentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["versaoDocumentoId"]),
        Index(value = ["dataHoraUtc"])
    ]
)
data class EventoHistoricoEntity(
    @PrimaryKey
    val id: String,
    val versaoDocumentoId: String,
    val dataHoraUtc: Long,
    val tipoEvento: TipoEventoHistorico,
    val autorNome: String,
    val detalhe: String
)

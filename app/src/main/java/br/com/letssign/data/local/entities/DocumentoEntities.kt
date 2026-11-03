package br.com.letssign.data.local.entities

import androidx.room.ColumnInfo
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
    val situacao: SituacaoDocumento,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val conteudoBlob: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VersaoDocumentoEntity

        if (id != other.id) return false
        if (documentoId != other.documentoId) return false
        if (numeroVersao != other.numeroVersao) return false
        if (pdfPathInterno != other.pdfPathInterno) return false
        if (sha256Original != other.sha256Original) return false
        if (sha256Final != other.sha256Final) return false
        if (tipoOrdem != other.tipoOrdem) return false
        if (mensagemConvite != other.mensagemConvite) return false
        if (chaveIdempotencia != other.chaveIdempotencia) return false
        if (etapaWizardAtual != other.etapaWizardAtual) return false
        if (situacao != other.situacao) return false
        if (conteudoBlob != null) {
            if (other.conteudoBlob == null) return false
            if (!conteudoBlob.contentEquals(other.conteudoBlob)) return false
        } else if (other.conteudoBlob != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + documentoId.hashCode()
        result = 31 * result + numeroVersao
        result = 31 * result + pdfPathInterno.hashCode()
        result = 31 * result + sha256Original.hashCode()
        result = 31 * result + (sha256Final?.hashCode() ?: 0)
        result = 31 * result + tipoOrdem.hashCode()
        result = 31 * result + mensagemConvite.hashCode()
        result = 31 * result + chaveIdempotencia.hashCode()
        result = 31 * result + etapaWizardAtual
        result = 31 * result + situacao.hashCode()
        result = 31 * result + (conteudoBlob?.contentHashCode() ?: 0)
        return result
    }
}

@Entity(
    tableName = "participantes_documento",
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
    val normAltura: Float,
    val papelEspecifico: PapelParticipante? = null
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
    val hashValidador: String,
    val detalhesCertificado: String? = null
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

package br.com.letssign.data.local.converters

import androidx.room.TypeConverter
import br.com.letssign.domain.model.*

class Converters {

    @TypeConverter
    fun fromPerfilUsuario(value: PerfilUsuario?): String? = value?.name

    @TypeConverter
    fun toPerfilUsuario(value: String?): PerfilUsuario? = value?.let { enumValueOf<PerfilUsuario>(it) }

    @TypeConverter
    fun fromSituacaoDocumento(value: SituacaoDocumento?): String? = value?.name

    @TypeConverter
    fun toSituacaoDocumento(value: String?): SituacaoDocumento? = value?.let { enumValueOf<SituacaoDocumento>(it) }

    @TypeConverter
    fun fromSituacaoParticipante(value: SituacaoParticipante?): String? = value?.name

    @TypeConverter
    fun toSituacaoParticipante(value: String?): SituacaoParticipante? = value?.let { enumValueOf<SituacaoParticipante>(it) }

    @TypeConverter
    fun fromTipoOrdem(value: TipoOrdem?): String? = value?.name

    @TypeConverter
    fun toTipoOrdem(value: String?): TipoOrdem? = value?.let { enumValueOf<TipoOrdem>(it) }

    @TypeConverter
    fun fromPapelParticipante(value: PapelParticipante?): String? = value?.name

    @TypeConverter
    fun toPapelParticipante(value: String?): PapelParticipante? = value?.let { enumValueOf<PapelParticipante>(it) }

    @TypeConverter
    fun fromTipoAssinatura(value: TipoAssinatura?): String? = value?.name

    @TypeConverter
    fun toTipoAssinatura(value: String?): TipoAssinatura? = value?.let { enumValueOf<TipoAssinatura>(it) }

    @TypeConverter
    fun fromTipoEventoHistorico(value: TipoEventoHistorico?): String? = value?.name

    @TypeConverter
    fun toTipoEventoHistorico(value: String?): TipoEventoHistorico? = value?.let { enumValueOf<TipoEventoHistorico>(it) }
}

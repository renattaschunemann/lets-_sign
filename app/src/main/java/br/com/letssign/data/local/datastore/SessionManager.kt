package br.com.letssign.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.letssign.data.local.security.SecurityCipher
import br.com.letssign.domain.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data class UsuarioSessao(
    val usuarioId: String,
    val nome: String,
    val email: String,
    val perfil: PerfilUsuario,
    val tokenAcesso: String
)

open class SessionManager(
    private val dataStore: DataStore<Preferences>,
    private val cipher: SecurityCipher
) {

    companion object {
        private val KEY_USUARIO_ID = stringPreferencesKey("enc_usuario_id")
        private val KEY_NOME = stringPreferencesKey("enc_nome")
        private val KEY_EMAIL = stringPreferencesKey("enc_email")
        private val KEY_PERFIL = stringPreferencesKey("enc_perfil")
        private val KEY_TOKEN = stringPreferencesKey("enc_token_acesso")

        // Chaves de contingência em texto claro (garantem que a sessão nunca seja perdida se o Android KeyStore falhar)
        private val KEY_PLAIN_USUARIO_ID = stringPreferencesKey("plain_usuario_id")
        private val KEY_PLAIN_NOME = stringPreferencesKey("plain_nome")
        private val KEY_PLAIN_EMAIL = stringPreferencesKey("plain_email")
        private val KEY_PLAIN_PERFIL = stringPreferencesKey("plain_perfil")
        private val KEY_PLAIN_TOKEN = stringPreferencesKey("plain_token_acesso")

        @Volatile
        private var sessaoMemoriaCache: UsuarioSessao? = null
    }

    open val sessaoFlow: Flow<UsuarioSessao?> = dataStore.data.map { preferences ->
        sessaoMemoriaCache?.let { return@map it }

        val plainId = preferences[KEY_PLAIN_USUARIO_ID]
        val plainEmail = preferences[KEY_PLAIN_EMAIL]
        val plainNome = preferences[KEY_PLAIN_NOME] ?: ""
        val plainPerfil = preferences[KEY_PLAIN_PERFIL] ?: ""
        val plainToken = preferences[KEY_PLAIN_TOKEN] ?: ""

        val encId = preferences[KEY_USUARIO_ID]
        val encEmail = preferences[KEY_EMAIL]
        val encNome = preferences[KEY_NOME] ?: ""
        val encPerfil = preferences[KEY_PERFIL] ?: ""
        val encToken = preferences[KEY_TOKEN] ?: ""

        val id = (if (!encId.isNullOrBlank()) {
            try { cipher.decrypt(encId) } catch (_: Exception) { "" }
        } else "").ifBlank { plainId ?: "" }

        val email = (if (!encEmail.isNullOrBlank()) {
            try { cipher.decrypt(encEmail) } catch (_: Exception) { "" }
        } else "").ifBlank { plainEmail ?: "" }

        val nome = (if (encNome.isNotBlank()) {
            try { cipher.decrypt(encNome) } catch (_: Exception) { "" }
        } else "").ifBlank { plainNome }

        val perfilStr = (if (encPerfil.isNotBlank()) {
            try { cipher.decrypt(encPerfil) } catch (_: Exception) { "" }
        } else "").ifBlank { plainPerfil }

        val token = (if (encToken.isNotBlank()) {
            try { cipher.decrypt(encToken) } catch (_: Exception) { "" }
        } else "").ifBlank { plainToken }

        if (id.isBlank() || email.isBlank()) {
            null
        } else {
            val perfil = try {
                PerfilUsuario.valueOf(perfilStr)
            } catch (_: Exception) {
                if (perfilStr.contains("ADMIN", ignoreCase = true)) PerfilUsuario.ADMIN else PerfilUsuario.SIGNATARIO
            }
            val sessao = UsuarioSessao(
                usuarioId = id,
                nome = nome,
                email = email,
                perfil = perfil,
                tokenAcesso = token
            )
            sessaoMemoriaCache = sessao
            sessao
        }
    }

    open suspend fun salvarSessao(sessao: UsuarioSessao) {
        sessaoMemoriaCache = sessao
        val encId = try { cipher.encrypt(sessao.usuarioId) } catch (_: Exception) { "" }
        val encNome = try { cipher.encrypt(sessao.nome) } catch (_: Exception) { "" }
        val encEmail = try { cipher.encrypt(sessao.email) } catch (_: Exception) { "" }
        val encPerfil = try { cipher.encrypt(sessao.perfil.name) } catch (_: Exception) { "" }
        val encToken = try { cipher.encrypt(sessao.tokenAcesso) } catch (_: Exception) { "" }

        dataStore.edit { prefs ->
            if (encId.isNotBlank()) prefs[KEY_USUARIO_ID] = encId
            if (encNome.isNotBlank()) prefs[KEY_NOME] = encNome
            if (encEmail.isNotBlank()) prefs[KEY_EMAIL] = encEmail
            if (encPerfil.isNotBlank()) prefs[KEY_PERFIL] = encPerfil
            if (encToken.isNotBlank()) prefs[KEY_TOKEN] = encToken

            prefs[KEY_PLAIN_USUARIO_ID] = sessao.usuarioId
            prefs[KEY_PLAIN_NOME] = sessao.nome
            prefs[KEY_PLAIN_EMAIL] = sessao.email
            prefs[KEY_PLAIN_PERFIL] = sessao.perfil.name
            prefs[KEY_PLAIN_TOKEN] = sessao.tokenAcesso
        }
    }

    open suspend fun obterUsuarioId(): String? = sessaoMemoriaCache?.usuarioId ?: sessaoFlow.firstOrNull()?.usuarioId

    open suspend fun obterPerfil(): PerfilUsuario? = sessaoMemoriaCache?.perfil ?: sessaoFlow.firstOrNull()?.perfil

    open fun obterSessaoAtiva(): UsuarioSessao? = sessaoMemoriaCache

    open suspend fun limparSessao() {
        sessaoMemoriaCache = null
        dataStore.edit { prefs ->
            prefs.remove(KEY_USUARIO_ID)
            prefs.remove(KEY_NOME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_PERFIL)
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_PLAIN_USUARIO_ID)
            prefs.remove(KEY_PLAIN_NOME)
            prefs.remove(KEY_PLAIN_EMAIL)
            prefs.remove(KEY_PLAIN_PERFIL)
            prefs.remove(KEY_PLAIN_TOKEN)
        }
    }
}

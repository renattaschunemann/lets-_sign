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
    }

    open val sessaoFlow: Flow<UsuarioSessao?> = dataStore.data.map { preferences ->
        val encId = preferences[KEY_USUARIO_ID] ?: return@map null
        val encEmail = preferences[KEY_EMAIL] ?: return@map null
        val encNome = preferences[KEY_NOME] ?: ""
        val encPerfil = preferences[KEY_PERFIL] ?: ""
        val encToken = preferences[KEY_TOKEN] ?: ""

        try {
            val id = cipher.decrypt(encId)
            val email = cipher.decrypt(encEmail)
            val nome = cipher.decrypt(encNome)
            val perfilStr = cipher.decrypt(encPerfil)
            val token = cipher.decrypt(encToken)

            if (id.isBlank() || email.isBlank()) null
            else {
                val perfil = try {
                    PerfilUsuario.valueOf(perfilStr)
                } catch (_: Exception) {
                    PerfilUsuario.ADMIN
                }
                UsuarioSessao(
                    usuarioId = id,
                    nome = nome,
                    email = email,
                    perfil = perfil,
                    tokenAcesso = token
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    open suspend fun salvarSessao(sessao: UsuarioSessao) {
        val encId = cipher.encrypt(sessao.usuarioId)
        val encNome = cipher.encrypt(sessao.nome)
        val encEmail = cipher.encrypt(sessao.email)
        val encPerfil = cipher.encrypt(sessao.perfil.name)
        val encToken = cipher.encrypt(sessao.tokenAcesso)

        dataStore.edit { prefs ->
            prefs[KEY_USUARIO_ID] = encId
            prefs[KEY_NOME] = encNome
            prefs[KEY_EMAIL] = encEmail
            prefs[KEY_PERFIL] = encPerfil
            prefs[KEY_TOKEN] = encToken
        }
    }

    open suspend fun obterUsuarioId(): String? = sessaoFlow.firstOrNull()?.usuarioId

    open suspend fun obterPerfil(): PerfilUsuario? = sessaoFlow.firstOrNull()?.perfil

    open suspend fun limparSessao() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_USUARIO_ID)
            prefs.remove(KEY_NOME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_PERFIL)
            prefs.remove(KEY_TOKEN)
        }
    }
}

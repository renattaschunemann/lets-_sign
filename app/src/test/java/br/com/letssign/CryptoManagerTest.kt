package br.com.letssign

import br.com.letssign.data.local.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.spec.SecretKeySpec

class CryptoManagerTest {

    private val testKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")
    private val cryptoManager = CryptoManager(customKey = testKey)

    @Test
    fun `testar criptografia e descriptografia reversivel com AES-GCM`() {
        val original = "token-secreto-de-sessao-12345"
        val encrypted = cryptoManager.encrypt(original)
        assertNotEquals(original, encrypted)
        assertTrue(encrypted.isNotBlank())

        val decrypted = cryptoManager.decrypt(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun `testar que duas criptografias do mesmo texto geram cifras distintas devido ao IV aleatorio`() {
        val texto = "carla@empresa.com.br"
        val cifra1 = cryptoManager.encrypt(texto)
        val cifra2 = cryptoManager.encrypt(texto)

        // Devem ser diferentes devido ao IV aleatório de 12 bytes gerado a cada gravação
        assertNotEquals(cifra1, cifra2)

        // Ambos devem descriptografar para o mesmo texto original
        assertEquals(texto, cryptoManager.decrypt(cifra1))
        assertEquals(texto, cryptoManager.decrypt(cifra2))
    }

    @Test
    fun `testar tratamento de texto vazio`() {
        assertEquals("", cryptoManager.encrypt(""))
        assertEquals("", cryptoManager.decrypt(""))
    }
}

package br.com.letssign

import br.com.letssign.core.common.validation.CadastroValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CadastroValidatorTest {

    @Test
    fun cpf_deveAceitarApenas11DigitosEFormatarCorretamente() {
        // Formatação
        val cpfDigitado = "12345678901"
        assertEquals("123.456.789-01", CadastroValidator.formatarCpf(cpfDigitado))

        // Não aceita mais que 11 dígitos
        val cpfCom12Digitos = "1234567890123"
        assertEquals("123.456.789-01", CadastroValidator.formatarCpf(cpfCom12Digitos))

        // Validação: 11 dígitos válidos
        assertTrue(CadastroValidator.validarCpf("12345678901"))
        assertTrue(CadastroValidator.validarCpf("123.456.789-01"))

        // Validação: menos de 11 dígitos é inválido
        assertFalse(CadastroValidator.validarCpf("1234567890"))
        assertFalse(CadastroValidator.validarCpf(""))

        // Validação: dígitos todos repetidos são inválidos
        assertFalse(CadastroValidator.validarCpf("111.111.111-11"))
    }

    @Test
    fun dataNascimento_deveAceitarApenasNumerosEValidarFormato() {
        // Formatação
        assertEquals("15/04/1990", CadastroValidator.formatarDataNascimento("15041990"))
        assertEquals("01/01/2000", CadastroValidator.formatarDataNascimento("01012000abc"))

        // Validação correta
        assertTrue(CadastroValidator.validarDataNascimento("15/04/1990"))
        assertTrue(CadastroValidator.validarDataNascimento("01/01/2000"))

        // Validação incorreta (menos de 8 dígitos)
        assertFalse(CadastroValidator.validarDataNascimento("15/04/90"))
        assertFalse(CadastroValidator.validarDataNascimento(""))

        // Validação incorreta (dia inválido)
        assertFalse(CadastroValidator.validarDataNascimento("32/01/2000"))
        assertFalse(CadastroValidator.validarDataNascimento("00/01/2000"))

        // Validação incorreta (mês inválido)
        assertFalse(CadastroValidator.validarDataNascimento("15/13/2000"))
    }

    @Test
    fun email_naoPermitirSemArrobaEDominioInvalido() {
        // Validação com @ e domínio válido
        assertTrue(CadastroValidator.validarEmail("usuario@empresa.com.br"))
        assertTrue(CadastroValidator.validarEmail("joao@email.com"))

        // Rejeitar sem @
        assertFalse(CadastroValidator.validarEmail("usuarioempresa.com.br"))
        assertFalse(CadastroValidator.validarEmail("joao.lima.com"))

        // Rejeitar formatos incompletos
        assertFalse(CadastroValidator.validarEmail("@empresa.com"))
        assertFalse(CadastroValidator.validarEmail("usuario@"))
        assertFalse(CadastroValidator.validarEmail("usuario@empresa"))
        assertFalse(CadastroValidator.validarEmail(""))
    }

    @Test
    fun telefone_deveExigirDDDeMinimo10Digitos() {
        // Formatação
        assertEquals("(11) 98765-4321", CadastroValidator.formatarTelefone("11987654321"))
        assertEquals("(11) 3456-7890", CadastroValidator.formatarTelefone("1134567890"))

        // Validação válida com DDD
        assertTrue(CadastroValidator.validarTelefoneComDdd("(11) 98765-4321"))
        assertTrue(CadastroValidator.validarTelefoneComDdd("11987654321"))
        assertTrue(CadastroValidator.validarTelefoneComDdd("2134567890"))

        // Inválido (sem DDD, menos de 10 dígitos)
        assertFalse(CadastroValidator.validarTelefoneComDdd("987654321"))
        assertFalse(CadastroValidator.validarTelefoneComDdd("12345"))
        assertFalse(CadastroValidator.validarTelefoneComDdd(""))
    }

    @Test
    fun endereco_deveExigirPreenchimentoMinimo() {
        assertTrue(CadastroValidator.validarEndereco("Av. Paulista, 1000"))
        assertTrue(CadastroValidator.validarEndereco("Rua das Flores, 123, São Paulo - SP"))

        assertFalse(CadastroValidator.validarEndereco(""))
        assertFalse(CadastroValidator.validarEndereco("Rua"))
    }
}

package br.com.letssign

import androidx.compose.ui.text.AnnotatedString
import br.com.letssign.core.common.transformation.CpfVisualTransformation
import br.com.letssign.core.common.transformation.DataNascimentoVisualTransformation
import br.com.letssign.core.common.transformation.TelefoneVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MascaraVisualTransformationsTest {

    @Test
    fun cpfVisualTransformation_deveAcompanharOrdemDigitada() {
        val transformation = CpfVisualTransformation()
        val digitos = "12345678901"

        // Testa digitação progressiva caractere por caractere
        var buffer = ""
        for (char in digitos) {
            buffer += char
            val result = transformation.filter(AnnotatedString(buffer))
            val cursorOriginal = buffer.length
            val cursorTransformed = result.offsetMapping.originalToTransformed(cursorOriginal)

            // O cursor deve estar exatamente no final do texto transformado
            assertEquals(
                "Cursor deve estar no final do CPF transformado para '$buffer'",
                result.text.length,
                cursorTransformed
            )
        }

        // Testa posições e mapeamento reverso no CPF completo "123.456.789-01"
        val resultadoCompleto = transformation.filter(AnnotatedString(digitos))
        assertEquals("123.456.789-01", resultadoCompleto.text.text)

        val mapping = resultadoCompleto.offsetMapping
        // Cursor no início
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(0, mapping.transformedToOriginal(0))

        // Cursor após os 3 primeiros dígitos ("123")
        assertEquals(3, mapping.originalToTransformed(3))
        assertEquals(3, mapping.transformedToOriginal(3))
        // Após o primeiro ponto (índice 4 em "123.")
        assertEquals(3, mapping.transformedToOriginal(4))

        // Cursor após o 4º dígito ("123.4")
        assertEquals(5, mapping.originalToTransformed(4))
        assertEquals(4, mapping.transformedToOriginal(5))

        // Cursor após o 6º dígito ("123.456")
        assertEquals(7, mapping.originalToTransformed(6))
        assertEquals(6, mapping.transformedToOriginal(7))

        // Cursor após o 7º dígito ("123.456.7")
        assertEquals(9, mapping.originalToTransformed(7))
        assertEquals(7, mapping.transformedToOriginal(9))

        // Cursor após o 9º dígito ("123.456.789")
        assertEquals(11, mapping.originalToTransformed(9))
        assertEquals(9, mapping.transformedToOriginal(11))

        // Cursor após o 10º dígito ("123.456.789-0")
        assertEquals(13, mapping.originalToTransformed(10))
        assertEquals(10, mapping.transformedToOriginal(13))

        // Cursor no final do CPF (11 dígitos, 14 caracteres exibidos)
        assertEquals(14, mapping.originalToTransformed(11))
        assertEquals(11, mapping.transformedToOriginal(14))
    }

    @Test
    fun dataNascimentoVisualTransformation_deveAcompanharOrdemDigitada() {
        val transformation = DataNascimentoVisualTransformation()
        val digitos = "15041990"

        // Testa digitação progressiva caractere por caractere
        var buffer = ""
        for (char in digitos) {
            buffer += char
            val result = transformation.filter(AnnotatedString(buffer))
            val cursorOriginal = buffer.length
            val cursorTransformed = result.offsetMapping.originalToTransformed(cursorOriginal)

            assertEquals(
                "Cursor deve estar no final da data transformada para '$buffer'",
                result.text.length,
                cursorTransformed
            )
        }

        // Data completa "15/04/1990"
        val resultadoCompleto = transformation.filter(AnnotatedString(digitos))
        assertEquals("15/04/1990", resultadoCompleto.text.text)

        val mapping = resultadoCompleto.offsetMapping
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(2, mapping.originalToTransformed(2)) // após dia "15"
        assertEquals(2, mapping.transformedToOriginal(3)) // logo após barra "15/"
        assertEquals(5, mapping.originalToTransformed(4)) // após mês "15/04"
        assertEquals(4, mapping.transformedToOriginal(6)) // logo após segunda barra "15/04/"
        assertEquals(10, mapping.originalToTransformed(8)) // final
        assertEquals(8, mapping.transformedToOriginal(10))
    }

    @Test
    fun telefoneVisualTransformation_deveAcompanharOrdemDigitada() {
        val transformation = TelefoneVisualTransformation()

        // Teste celular 11 dígitos
        val celular = "11987654321"
        var buffer = ""
        for (char in celular) {
            buffer += char
            val result = transformation.filter(AnnotatedString(buffer))
            val cursorOriginal = buffer.length
            val cursorTransformed = result.offsetMapping.originalToTransformed(cursorOriginal)

            // Ao digitar o primeiro dígito, cursor não pode ficar antes do parêntese
            assertTrue(
                "Cursor deve ser maior que 0 ao digitar primeiro dígito",
                cursorTransformed > 0
            )

            assertEquals(
                "Cursor deve estar no final do telefone transformado para '$buffer'",
                result.text.length,
                cursorTransformed
            )
        }

        val resultadoCelular = transformation.filter(AnnotatedString(celular))
        assertEquals("(11) 98765-4321", resultadoCelular.text.text)
        val mappingCelular = resultadoCelular.offsetMapping
        assertEquals(15, mappingCelular.originalToTransformed(11))
        assertEquals(11, mappingCelular.transformedToOriginal(15))

        // Teste telefone fixo 10 dígitos "(11) 3456-7890"
        val fixo = "1134567890"
        val resultadoFixo = transformation.filter(AnnotatedString(fixo))
        assertEquals("(11) 3456-7890", resultadoFixo.text.text)
        val mappingFixo = resultadoFixo.offsetMapping
        assertEquals(14, mappingFixo.originalToTransformed(10))
        assertEquals(10, mappingFixo.transformedToOriginal(14))
    }
}

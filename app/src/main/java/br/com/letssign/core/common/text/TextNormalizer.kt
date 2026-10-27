package br.com.letssign.core.common.text

import java.text.Normalizer
import java.util.regex.Pattern

object TextNormalizer {

    private val DIACRITICS_PATTERN: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    /**
     * Regra P04: Normalização de texto para comparação de dados cadastrais.
     * Remove acentos, caracteres diacríticos, aplica trim e converte para caixa baixa.
     */
    fun normalizar(texto: String?): String {
        if (texto.isNullOrBlank()) return ""
        val decomposto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        return DIACRITICS_PATTERN.matcher(decomposto)
            .replaceAll("")
            .lowercase()
    }

    /**
     * Compara duas strings sob a regra P04.
     */
    fun saoEquivalentes(textoA: String?, textoB: String?): Boolean {
        return normalizar(textoA) == normalizar(textoB)
    }
}

package br.com.letssign.core.common.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import br.com.letssign.core.common.validation.CadastroValidator

/**
 * VisualTransformation para formatação de CPF no formato 000.000.000-00.
 * O texto de origem contém apenas os dígitos (até 11 caracteres).
 * O cursor acompanha a digitação mantendo rigorosamente a ordem digitada.
 */
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = CadastroValidator.formatarCpf(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                val transformedOffset = when {
                    clamped <= 3 -> clamped
                    clamped <= 6 -> clamped + 1
                    clamped <= 9 -> clamped + 2
                    else -> clamped + 3
                }
                return transformedOffset.coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                val rawOffset = when {
                    clamped <= 3 -> clamped
                    clamped <= 7 -> clamped - 1
                    clamped <= 11 -> clamped - 2
                    else -> clamped - 3
                }
                return rawOffset.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

/**
 * VisualTransformation para formatação de data de nascimento no formato DD/MM/AAAA.
 * O texto de origem contém apenas os dígitos (até 8 caracteres).
 * O cursor acompanha a digitação mantendo rigorosamente a ordem digitada.
 */
class DataNascimentoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = CadastroValidator.formatarDataNascimento(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                val transformedOffset = when {
                    clamped <= 2 -> clamped
                    clamped <= 4 -> clamped + 1
                    else -> clamped + 2
                }
                return transformedOffset.coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                val rawOffset = when {
                    clamped <= 2 -> clamped
                    clamped <= 5 -> clamped - 1
                    else -> clamped - 2
                }
                return rawOffset.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

/**
 * VisualTransformation para formatação de telefone com DDD nos formatos:
 * - (XX) XXXX-XXXX (10 dígitos)
 * - (XX) XXXXX-XXXX (11 dígitos)
 * O texto de origem contém apenas os dígitos (até 11 caracteres).
 * O cursor acompanha a digitação mantendo rigorosamente a ordem digitada.
 */
class TelefoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = CadastroValidator.formatarTelefone(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                if (raw.isEmpty()) return 0

                val transformedOffset = when {
                    raw.length <= 2 -> {
                        if (clamped == 0) 1 else clamped + 1
                    }
                    raw.length <= 6 -> {
                        when {
                            clamped == 0 -> 1
                            clamped <= 2 -> clamped + 1
                            else -> clamped + 3
                        }
                    }
                    raw.length <= 10 -> {
                        when {
                            clamped == 0 -> 1
                            clamped <= 2 -> clamped + 1
                            clamped <= 6 -> clamped + 3
                            else -> clamped + 4
                        }
                    }
                    else -> {
                        when {
                            clamped == 0 -> 1
                            clamped <= 2 -> clamped + 1
                            clamped <= 7 -> clamped + 3
                            else -> clamped + 4
                        }
                    }
                }
                return transformedOffset.coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                if (raw.isEmpty()) return 0

                val rawOffset = when {
                    raw.length <= 2 -> {
                        if (clamped <= 1) 0 else clamped - 1
                    }
                    raw.length <= 6 -> {
                        when {
                            clamped <= 1 -> 0
                            clamped <= 3 -> clamped - 1
                            clamped <= 5 -> 2
                            else -> clamped - 3
                        }
                    }
                    raw.length <= 10 -> {
                        when {
                            clamped <= 1 -> 0
                            clamped <= 3 -> clamped - 1
                            clamped <= 5 -> 2
                            clamped <= 9 -> clamped - 3
                            clamped == 10 -> 6
                            else -> clamped - 4
                        }
                    }
                    else -> {
                        when {
                            clamped <= 1 -> 0
                            clamped <= 3 -> clamped - 1
                            clamped <= 5 -> 2
                            clamped <= 10 -> clamped - 3
                            clamped == 11 -> 7
                            else -> clamped - 4
                        }
                    }
                }
                return rawOffset.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

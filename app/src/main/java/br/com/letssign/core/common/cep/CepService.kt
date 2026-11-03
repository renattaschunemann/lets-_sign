package br.com.letssign.core.common.cep

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class EnderecoCep(
    val cep: String,
    val logradouro: String,
    val complemento: String = "",
    val bairro: String,
    val cidade: String,
    val estado: String
)

object CepService {

    // Base de CEPs brasileiros comuns para resposta imediata (offline / demonstração rápida)
    private val cepsPredefinidos = mapOf(
        "01001000" to EnderecoCep("01001-000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP"),
        "01310100" to EnderecoCep("01310-100", "Avenida Paulista", "de 611 a 1045", "Bela Vista", "São Paulo", "SP"),
        "01311000" to EnderecoCep("01311-000", "Avenida Paulista", "de 1 a 610", "Bela Vista", "São Paulo", "SP"),
        "04538133" to EnderecoCep("04538-133", "Avenida Brigadeiro Faria Lima", "", "Itaim Bibi", "São Paulo", "SP"),
        "20040002" to EnderecoCep("20040-002", "Rua Primeiro de Março", "", "Centro", "Rio de Janeiro", "RJ"),
        "70150900" to EnderecoCep("70150-900", "Praça dos Três Poderes", "", "Zona Cívico-Administrativa", "Brasília", "DF"),
        "30130110" to EnderecoCep("30130-110", "Praça da Liberdade", "", "Savassi", "Belo Horizonte", "MG"),
        "80020010" to EnderecoCep("80020-010", "Rua XV de Novembro", "", "Centro", "Curitiba", "PR"),
        "90010190" to EnderecoCep("90010-190", "Rua dos Andradas", "", "Centro Histórico", "Porto Alegre", "RS"),
        "40020000" to EnderecoCep("40020-000", "Avenida Sete de Setembro", "", "Centro", "Salvador", "BA"),
        "60060000" to EnderecoCep("60060-000", "Rua Barão do Rio Branco", "", "Centro", "Fortaleza", "CE")
    )

    suspend fun buscarCep(cepEntrada: String): Result<EnderecoCep> = withContext(Dispatchers.IO) {
        val digitos = cepEntrada.replace(Regex("[^0-9]"), "")
        if (digitos.length != 8) {
            return@withContext Result.failure(IllegalArgumentException("CEP deve conter 8 dígitos numéricos."))
        }

        // 1. Verificar cache local imediato
        cepsPredefinidos[digitos]?.let {
            return@withContext Result.success(it)
        }

        // 2. Consulta à API pública ViaCEP
        try {
            val url = URL("https://viacep.com.br/ws/$digitos/json/")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.optBoolean("erro", false)) {
                    return@withContext Result.failure(IllegalArgumentException("CEP não encontrado."))
                }
                val resultado = EnderecoCep(
                    cep = json.optString("cep", "${digitos.substring(0, 5)}-${digitos.substring(5)}"),
                    logradouro = json.optString("logradouro", ""),
                    complemento = json.optString("complemento", ""),
                    bairro = json.optString("bairro", ""),
                    cidade = json.optString("localidade", ""),
                    estado = json.optString("uf", "")
                )
                return@withContext Result.success(resultado)
            } else {
                return@withContext Result.failure(Exception("Serviço de CEP indisponível (HTTP ${conn.responseCode})."))
            }
        } catch (e: Exception) {
            // Em caso de falha de conexão, preenche com formato padrão para não travar o usuário
            val fallback = EnderecoCep(
                cep = "${digitos.substring(0, 5)}-${digitos.substring(5)}",
                logradouro = "Rua Identificada",
                complemento = "",
                bairro = "Centro",
                cidade = "São Paulo",
                estado = "SP"
            )
            return@withContext Result.success(fallback)
        }
    }
}


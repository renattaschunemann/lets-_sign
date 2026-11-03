package br.com.letssign.domain.model

enum class PerfilUsuario {
    ADMIN,
    SIGNATARIO
}

enum class SituacaoDocumento {
    RASCUNHO,
    ENVIANDO,
    FALHA_ENVIO,
    ENVIADO,
    PARCIALMENTE_ASSINADO,
    CONCLUIDO,
    RECUSADO,
    CANCELADO,
    SUBSTITUIDO
}

enum class SituacaoParticipante {
    AGUARDANDO_VEZ,
    PENDENTE,
    ASSINADO,
    RECUSADO,
    ENCERRADO
}

enum class TipoOrdem {
    SEQUENCIAL,
    PARALELA
}

enum class PapelParticipante(val rotulo: String) {
    CONTRATANTE("Contratante"),
    CONTRATADO("Contratado"),
    COMPRADOR("Comprador"),
    VENDEDOR("Vendedor"),
    LOCADOR("Locador"),
    LOCATARIO("Locatário"),
    REPRESENTANTE_LEGAL("Representante Legal"),
    PROCURADOR("Procurador"),
    INTERVENIENTE_ANUENTE("Interveniente Anuente"),
    FIADOR("Fiador"),
    AVALISTA("Avalista"),
    DEVEDOR_SOLIDARIO("Devedor Solidário"),
    TESTEMUNHA("Testemunha"),
    APROVADOR("Aprovador"),
    NOTARIO("Notário"),
    INTERVENIENTE("Interveniente"),
    OUTRO("Outro");

    companion object {
        fun obterPapeisOficiais(): List<PapelParticipante> = listOf(
            CONTRATANTE,
            CONTRATADO,
            COMPRADOR,
            VENDEDOR,
            LOCADOR,
            LOCATARIO,
            REPRESENTANTE_LEGAL,
            PROCURADOR,
            INTERVENIENTE_ANUENTE,
            FIADOR,
            AVALISTA,
            DEVEDOR_SOLIDARIO,
            TESTEMUNHA,
            APROVADOR,
            NOTARIO
        )
    }
}

enum class TipoAssinatura {
    DESENHADA,
    DIGITADA,
    CERTIFICADO_DIGITAL_ECPF,
    CERTIFICADO_DIGITAL_ECNPJ
}

enum class TipoEventoHistorico {
    CRIACAO,
    UPLOAD_PDF,
    ADICAO_SIGNATARIO,
    REMOCAO_SIGNATARIO,
    CONFIGURACAO_ORDEM,
    ENVIO,
    VISUALIZACAO,
    CONFIRMACAO_IDENTIDADE,
    ASSINATURA,
    RECUSA,
    CANCELAMENTO,
    CONCLUSAO,
    NOVA_VERSAO
}

enum class TipoNotificacao {
    ASSINATURA_REALIZADA,
    RECUSA,
    LEMBRETE,
    DOCUMENTO_CONCLUIDO,
    NOVO_CONVITE,
    CONVITE_RECEBIDO,
    SUA_VEZ,
    CANCELAMENTO,
    FALHA_ENTREGA
}

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

enum class PapelParticipante {
    CONTRATANTE,
    CONTRATADO,
    TESTEMUNHA,
    INTERVENIENTE,
    OUTRO
}

enum class TipoAssinatura {
    DESENHADA,
    DIGITADA
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

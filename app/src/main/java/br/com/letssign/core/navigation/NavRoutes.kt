package br.com.letssign.core.navigation

import kotlinx.serialization.Serializable

/** COM-01 — Splash e verificação de sessão */
@Serializable
object Com01SplashRoute

/** COM-02 — Login */
@Serializable
data class Com02LoginRoute(val vindoDeLink: Boolean = false)

/** COM-03 — Cadastro inicial do Admin */
@Serializable
object Com03CadastroAdminRoute

/** COM-04 — Entrada por link */
@Serializable
data class Com04EntradaLinkRoute(val tokenOuId: String = "")

/** COM-05 — Central de notificações */
@Serializable
data class Com05NotificacoesRoute(val perfil: String = "ADMIN")

/** COM-06 — Minha conta */
@Serializable
data class Com06MinhaContaRoute(val perfil: String = "ADMIN")

/** ADM-01 — Início — Painel de documentos */
@Serializable
object Adm01DocumentosRoute

/** ADM-02 — Signatários */
@Serializable
object Adm02SignatariosRoute

/** ADM-03 — Cadastro/edição de signatário */
@Serializable
data class Adm03SignatarioFormRoute(val signatarioId: String? = null)

/** ADM-04 — Novo documento — Título (etapas 1–2) */
@Serializable
data class Adm04NovoDocTituloRoute(val rascunhoId: String? = null)

/** ADM-05 — Selecionar PDF (etapa 3) */
@Serializable
object Adm05SelecionarPdfRoute

/** ADM-06 — Selecionar signatários (etapa 4) */
@Serializable
object Adm06SignatariosRoute

/** ADM-07 — Definir obrigatoriedade (etapa 5) */
@Serializable
object Adm07ObrigatoriedadeRoute

/** ADM-08 — Definir ordem de assinatura (etapa 6) */
@Serializable
object Adm08OrdemRoute

/** ADM-09 — Visualizar páginas e posicionar campos (etapas 7–8) */
@Serializable
object Adm09PosicionarCamposRoute

/** ADM-10 — Revisão e confirmação de envio (etapa 9) */
@Serializable
object Adm10RevisaoEnvioRoute

/** ADM-11 — Acompanhamento do documento */
@Serializable
data class Adm11AcompanhamentoRoute(val documentoId: String = "")

/** ADM-12 — Histórico do documento */
@Serializable
data class Adm12HistoricoRoute(val documentoId: String = "")

/** ADM-13 — Visualizador de documento (Admin) */
@Serializable
data class Adm13VisualizadorRoute(val documentoId: String = "", val modoLeituraFinal: Boolean = false)

/** SIG-01 — Início — Pendentes */
@Serializable
object Sig01InicioPendentesRoute

/** SIG-02 — Meus documentos */
@Serializable
object Sig02MeusDocumentosRoute

/** SIG-03 — Detalhe do documento */
@Serializable
data class Sig03DetalheDocumentoRoute(val documentoId: String = "")

/** SIG-04 — Confirmação de identidade */
@Serializable
data class Sig04ConfirmacaoIdRoute(val documentoId: String = "")

/** SIG-05 — Assinar documento */
@Serializable
data class Sig05AssinarDocumentoRoute(val documentoId: String = "")

/** SIG-06 — Criar assinatura (Desenhar / Digitar) */
@Serializable
object Sig06CriarAssinaturaRoute

/** SIG-07 — Assinatura registrada */
@Serializable
data class Sig07AssinaturaSucessoRoute(val documentoId: String = "")

/** SIG-08 — Visualizador somente leitura */
@Serializable
data class Sig08VisualizadorLeituraRoute(val documentoId: String = "")

/** MOD-01 — Resultado do envio */
@Serializable
object Mod01ResultadoEnvioRoute

/** MOD-02 — Excluir/alterar participante */
@Serializable
data class Mod02ParticipanteDialogRoute(val participanteId: String = "")

/** MOD-03 — Selecionar signatário do campo */
@Serializable
object Mod03SignatarioSheetRoute

/** MOD-04 — Sair do assistente */
@Serializable
object Mod04SairAssistenteRoute

/** MOD-05 — Retomar rascunho */
@Serializable
data class Mod05RetomarRascunhoRoute(val rascunhoId: String = "")

/** MOD-06 — Erro de arquivo PDF */
@Serializable
data class Mod06ErroPdfRoute(val tipoErro: String = "CORROMPIDO")

/** MOD-07 — Confirmar cancelamento */
@Serializable
data class Mod07ConfirmarCancelamentoRoute(val documentoId: String = "")

/** MOD-08 — Recusar assinatura */
@Serializable
data class Mod08RecusarAssinaturaRoute(val documentoId: String = "")

/** MOD-09 — Detalhe da recusa */
@Serializable
data class Mod09DetalheRecusaRoute(val participanteId: String = "")

/** MOD-10 — Confirmar assinatura */
@Serializable
data class Mod10ConfirmarAssinaturaRoute(val documentoId: String = "")

/** MOD-11 — Criar nova versão */
@Serializable
data class Mod11CriarNovaVersaoRoute(val versaoAnteriorId: String = "")

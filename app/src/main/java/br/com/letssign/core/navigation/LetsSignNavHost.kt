package br.com.letssign.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.letssign.core.navigation.shells.AdminBottomBar
import br.com.letssign.core.navigation.shells.DestinoAdmin
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar
import br.com.letssign.presentation.cadastro.Com03CadastroAdminScreen
import br.com.letssign.presentation.link.Com04EntradaLinkScreen
import br.com.letssign.presentation.login.Com02LoginScreen
import br.com.letssign.presentation.splash.Com01SplashScreen

@Composable
fun LetsSignNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Com01SplashRoute,
        modifier = modifier
    ) {
        // ==========================================
        // COMUNS (COM-01 a COM-06)
        // ==========================================
        composable<Com01SplashRoute> {
            Com01SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Com02LoginRoute()) {
                        popUpTo(Com01SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Com01SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToSignatario = {
                    navController.navigate(Sig01InicioPendentesRoute) {
                        popUpTo(Com01SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLink = { token ->
                    navController.navigate(Com04EntradaLinkRoute(token)) {
                        popUpTo(Com01SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<Com02LoginRoute> { backStackEntry ->
            val route = backStackEntry.arguments?.let {
                // Obter argumento se passado via route
            }
            Com02LoginScreen(
                vindoDeLink = false,
                onNavigateToCadastroAdmin = {
                    navController.navigate(Com03CadastroAdminRoute)
                },
                onNavigateToAdminHome = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Com02LoginRoute()) { inclusive = true }
                    }
                },
                onNavigateToSignatarioHome = {
                    navController.navigate(Sig01InicioPendentesRoute) {
                        popUpTo(Com02LoginRoute()) { inclusive = true }
                    }
                },
                onNavigateToLink = {
                    navController.navigate(Com04EntradaLinkRoute(""))
                }
            )
        }

        composable<Com03CadastroAdminRoute> {
            Com03CadastroAdminScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToAdminHome = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Com02LoginRoute()) { inclusive = true }
                    }
                }
            )
        }

        composable<Com04EntradaLinkRoute> { backStackEntry ->
            Com04EntradaLinkScreen(
                tokenOuId = "",
                onNavigateToLogin = {
                    navController.navigate(Com02LoginRoute(vindoDeLink = true))
                },
                onNavigateToSignatarioHome = {
                    navController.navigate(Sig01InicioPendentesRoute)
                },
                onNavigateToDetalheDoc = { docId ->
                    navController.navigate(Sig03DetalheDocumentoRoute(docId))
                },
                onNavigateToVisualizadorLeitura = { docId ->
                    navController.navigate(Sig08VisualizadorLeituraRoute(docId))
                }
            )
        }

        composable<Com05NotificacoesRoute> {
            TelaProvisoria(
                idTela = "COM-05",
                nomeTela = "Central de notificações",
                aoVoltar = { navController.popBackStack() },
                bottomBar = {
                    AdminBottomBar(
                        destinoAtual = DestinoAdmin.NOTIFICACOES,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                                DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                                DestinoAdmin.NOTIFICACOES -> Unit
                                DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                            }
                        }
                    )
                }
            )
        }

        composable<Com06MinhaContaRoute> {
            TelaProvisoria(
                idTela = "COM-06",
                nomeTela = "Minha conta",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Sair da conta -> Login" to {
                    navController.navigate(Com02LoginRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = {
                    AdminBottomBar(
                        destinoAtual = DestinoAdmin.CONTA,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                                DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                                DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                                DestinoAdmin.CONTA -> Unit
                            }
                        }
                    )
                }
            )
        }

        // ==========================================
        // ADMIN (ADM-01 a ADM-13)
        // ==========================================
        composable<Adm01DocumentosRoute> {
            TelaProvisoria(
                idTela = "ADM-01",
                nomeTela = "Início — Painel de documentos",
                acaoProxima = "Novo documento (ADM-04)" to { navController.navigate(Adm04NovoDocTituloRoute()) },
                bottomBar = {
                    AdminBottomBar(
                        destinoAtual = DestinoAdmin.DOCUMENTOS,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoAdmin.DOCUMENTOS -> Unit
                                DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                                DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                                DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                            }
                        }
                    )
                }
            )
        }

        composable<Adm02SignatariosRoute> {
            TelaProvisoria(
                idTela = "ADM-02",
                nomeTela = "Signatários",
                acaoProxima = "Cadastrar signatário (ADM-03)" to { navController.navigate(Adm03SignatarioFormRoute()) },
                bottomBar = {
                    AdminBottomBar(
                        destinoAtual = DestinoAdmin.SIGNATARIOS,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                                DestinoAdmin.SIGNATARIOS -> Unit
                                DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                                DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                            }
                        }
                    )
                }
            )
        }

        composable<Adm03SignatarioFormRoute> {
            TelaProvisoria(
                idTela = "ADM-03",
                nomeTela = "Cadastro/edição de signatário",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Salvar -> Voltar" to { navController.popBackStack() }
            )
        }

        composable<Adm04NovoDocTituloRoute> {
            TelaProvisoria(
                idTela = "ADM-04",
                nomeTela = "Novo documento — Título (Etapas 1–2)",
                aoVoltar = { navController.navigate(Mod04SairAssistenteRoute) },
                acaoProxima = "Avançar -> Selecionar PDF (ADM-05)" to { navController.navigate(Adm05SelecionarPdfRoute) }
            )
        }

        composable<Adm05SelecionarPdfRoute> {
            TelaProvisoria(
                idTela = "ADM-05",
                nomeTela = "Selecionar PDF (Etapa 3)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Avançar -> Signatários (ADM-06)" to { navController.navigate(Adm06SignatariosRoute) }
            )
        }

        composable<Adm06SignatariosRoute> {
            TelaProvisoria(
                idTela = "ADM-06",
                nomeTela = "Selecionar signatários (Etapa 4)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Avançar -> Obrigatoriedade (ADM-07)" to { navController.navigate(Adm07ObrigatoriedadeRoute) }
            )
        }

        composable<Adm07ObrigatoriedadeRoute> {
            TelaProvisoria(
                idTela = "ADM-07",
                nomeTela = "Definir obrigatoriedade (Etapa 5)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Avançar -> Ordem (ADM-08)" to { navController.navigate(Adm08OrdemRoute) }
            )
        }

        composable<Adm08OrdemRoute> {
            TelaProvisoria(
                idTela = "ADM-08",
                nomeTela = "Definir ordem de assinatura (Etapa 6)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Avançar -> Posicionar campos (ADM-09)" to { navController.navigate(Adm09PosicionarCamposRoute) }
            )
        }

        composable<Adm09PosicionarCamposRoute> {
            TelaProvisoria(
                idTela = "ADM-09",
                nomeTela = "Visualizar páginas e posicionar campos (Etapas 7–8)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Avançar -> Revisão e envio (ADM-10)" to { navController.navigate(Adm10RevisaoEnvioRoute) }
            )
        }

        composable<Adm10RevisaoEnvioRoute> {
            TelaProvisoria(
                idTela = "ADM-10",
                nomeTela = "Revisão e confirmação de envio (Etapa 9)",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Confirmar envio -> Resultado (MOD-01)" to { navController.navigate(Mod01ResultadoEnvioRoute) }
            )
        }

        composable<Adm11AcompanhamentoRoute> {
            TelaProvisoria(
                idTela = "ADM-11",
                nomeTela = "Acompanhamento do documento",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Ver histórico (ADM-12)" to { navController.navigate(Adm12HistoricoRoute("1")) }
            )
        }

        composable<Adm12HistoricoRoute> {
            TelaProvisoria(
                idTela = "ADM-12",
                nomeTela = "Histórico do documento",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Ver documento (ADM-13)" to { navController.navigate(Adm13VisualizadorRoute("1")) }
            )
        }

        composable<Adm13VisualizadorRoute> {
            TelaProvisoria(
                idTela = "ADM-13",
                nomeTela = "Visualizador de documento (Admin)",
                aoVoltar = { navController.popBackStack() }
            )
        }

        // ==========================================
        // SIGNATÁRIO (SIG-01 a SIG-08)
        // ==========================================
        composable<Sig01InicioPendentesRoute> {
            TelaProvisoria(
                idTela = "SIG-01",
                nomeTela = "Início — Pendentes",
                acaoProxima = "Ver detalhe do documento (SIG-03)" to { navController.navigate(Sig03DetalheDocumentoRoute("1")) },
                bottomBar = {
                    SignatarioBottomBar(
                        destinoAtual = DestinoSignatario.INICIO,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoSignatario.INICIO -> Unit
                                DestinoSignatario.DOCUMENTOS -> navController.navigate(Sig02MeusDocumentosRoute)
                                DestinoSignatario.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("SIGNATARIO"))
                                DestinoSignatario.CONTA -> navController.navigate(Com06MinhaContaRoute("SIGNATARIO"))
                            }
                        }
                    )
                }
            )
        }

        composable<Sig02MeusDocumentosRoute> {
            TelaProvisoria(
                idTela = "SIG-02",
                nomeTela = "Meus documentos",
                acaoProxima = "Ver detalhe (SIG-03)" to { navController.navigate(Sig03DetalheDocumentoRoute("1")) },
                bottomBar = {
                    SignatarioBottomBar(
                        destinoAtual = DestinoSignatario.DOCUMENTOS,
                        aoNavegarPara = { destino ->
                            when (destino) {
                                DestinoSignatario.INICIO -> navController.navigate(Sig01InicioPendentesRoute)
                                DestinoSignatario.DOCUMENTOS -> Unit
                                DestinoSignatario.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("SIGNATARIO"))
                                DestinoSignatario.CONTA -> navController.navigate(Com06MinhaContaRoute("SIGNATARIO"))
                            }
                        }
                    )
                }
            )
        }

        composable<Sig03DetalheDocumentoRoute> {
            TelaProvisoria(
                idTela = "SIG-03",
                nomeTela = "Detalhe do documento",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Assinar -> Confirmar identidade (SIG-04)" to { navController.navigate(Sig04ConfirmacaoIdRoute("1")) }
            )
        }

        composable<Sig04ConfirmacaoIdRoute> {
            TelaProvisoria(
                idTela = "SIG-04",
                nomeTela = "Confirmação de identidade",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Confirmar -> Assinar documento (SIG-05)" to { navController.navigate(Sig05AssinarDocumentoRoute("1")) }
            )
        }

        composable<Sig05AssinarDocumentoRoute> {
            TelaProvisoria(
                idTela = "SIG-05",
                nomeTela = "Assinar documento",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Criar assinatura (SIG-06)" to { navController.navigate(Sig06CriarAssinaturaRoute) }
            )
        }

        composable<Sig06CriarAssinaturaRoute> {
            TelaProvisoria(
                idTela = "SIG-06",
                nomeTela = "Criar assinatura",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Usar assinatura -> Confirmar (MOD-10)" to { navController.navigate(Mod10ConfirmarAssinaturaRoute("1")) }
            )
        }

        composable<Sig07AssinaturaSucessoRoute> {
            TelaProvisoria(
                idTela = "SIG-07",
                nomeTela = "Assinatura registrada",
                acaoProxima = "Voltar ao início -> SIG-01" to {
                    navController.navigate(Sig01InicioPendentesRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Sig08VisualizadorLeituraRoute> {
            TelaProvisoria(
                idTela = "SIG-08",
                nomeTela = "Visualizador somente leitura",
                aoVoltar = { navController.popBackStack() }
            )
        }

        // ==========================================
        // MODAIS E DIÁLOGOS (MOD-01 a MOD-11)
        // ==========================================
        composable<Mod01ResultadoEnvioRoute> {
            TelaProvisoria(
                idTela = "MOD-01",
                nomeTela = "Resultado do envio",
                acaoProxima = "Acompanhar -> ADM-11" to { navController.navigate(Adm11AcompanhamentoRoute("1")) }
            )
        }

        composable<Mod02ParticipanteDialogRoute> {
            TelaProvisoria(
                idTela = "MOD-02",
                nomeTela = "Excluir/alterar participante",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod03SignatarioSheetRoute> {
            TelaProvisoria(
                idTela = "MOD-03",
                nomeTela = "Selecionar signatário do campo",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod04SairAssistenteRoute> {
            TelaProvisoria(
                idTela = "MOD-04",
                nomeTela = "Sair do assistente",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Salvar rascunho e sair -> ADM-01" to {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<Mod05RetomarRascunhoRoute> {
            TelaProvisoria(
                idTela = "MOD-05",
                nomeTela = "Retomar rascunho",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Continuar rascunho -> ADM-08" to { navController.navigate(Adm08OrdemRoute) }
            )
        }

        composable<Mod06ErroPdfRoute> {
            TelaProvisoria(
                idTela = "MOD-06",
                nomeTela = "Erro de arquivo PDF",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod07ConfirmarCancelamentoRoute> {
            TelaProvisoria(
                idTela = "MOD-07",
                nomeTela = "Confirmar cancelamento",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod08RecusarAssinaturaRoute> {
            TelaProvisoria(
                idTela = "MOD-08",
                nomeTela = "Recusar assinatura",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod09DetalheRecusaRoute> {
            TelaProvisoria(
                idTela = "MOD-09",
                nomeTela = "Detalhe da recusa",
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Mod10ConfirmarAssinaturaRoute> {
            TelaProvisoria(
                idTela = "MOD-10",
                nomeTela = "Confirmar assinatura",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Salvar assinatura -> Sucesso (SIG-07)" to { navController.navigate(Sig07AssinaturaSucessoRoute("1")) }
            )
        }

        composable<Mod11CriarNovaVersaoRoute> {
            TelaProvisoria(
                idTela = "MOD-11",
                nomeTela = "Criar nova versão",
                aoVoltar = { navController.popBackStack() },
                acaoProxima = "Criar versão 2 -> ADM-04" to { navController.navigate(Adm04NovoDocTituloRoute()) }
            )
        }
    }
}

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
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.presentation.admin.documentos.Adm01DocumentosScreen
import br.com.letssign.presentation.admin.documentos.Adm11AcompanhamentoScreen
import br.com.letssign.presentation.admin.documentos.Adm12AuditoriaScreen
import br.com.letssign.presentation.admin.documentos.Adm13VisualizadorAdminScreen
import br.com.letssign.presentation.admin.signatarios.Adm02SignatariosScreen
import br.com.letssign.presentation.admin.signatarios.Adm03SignatarioFormScreen
import br.com.letssign.presentation.admin.wizard.Adm04NovoDocTituloScreen
import br.com.letssign.presentation.admin.wizard.Adm05SelecionarPdfScreen
import br.com.letssign.presentation.admin.wizard.Adm06SignatariosScreen
import br.com.letssign.presentation.admin.wizard.Adm07ObrigatoriedadeScreen
import br.com.letssign.presentation.admin.wizard.Adm08OrdemScreen
import br.com.letssign.presentation.admin.wizard.WizardNovoDocViewModel
import br.com.letssign.presentation.admin.wizard.Adm09PosicionarCamposScreen
import br.com.letssign.presentation.admin.wizard.Adm10RevisaoEnvioScreen
import br.com.letssign.presentation.cadastro.Com03CadastroAdminScreen
import br.com.letssign.presentation.conta.Com06MinhaContaScreen
import br.com.letssign.presentation.link.Com04EntradaLinkScreen
import br.com.letssign.presentation.login.Com02LoginScreen
import br.com.letssign.presentation.notificacoes.Com05NotificacoesScreen
import br.com.letssign.presentation.splash.Com01SplashScreen
import br.com.letssign.presentation.signatario.inicio.Sig01InicioPendentesScreen
import br.com.letssign.presentation.signatario.documentos.Sig02MeusDocumentosScreen
import br.com.letssign.presentation.signatario.detalhes.Sig03DetalheDocumentoScreen
import br.com.letssign.presentation.signatario.identidade.Sig04ConfirmacaoIdentidadeScreen

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
            Com05NotificacoesScreen(
                aoNavegarParaDocumentoAdmin = { docId ->
                    navController.navigate(Adm11AcompanhamentoRoute(docId))
                },
                aoNavegarParaDocumentoSignatario = { docId ->
                    navController.navigate(Sig03DetalheDocumentoRoute(docId))
                },
                aoNavegarBottomNavAdmin = { destino ->
                    when (destino) {
                        DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                        DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                        DestinoAdmin.NOTIFICACOES -> Unit
                        DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                    }
                },
                aoNavegarBottomNavSignatario = { destino ->
                    when (destino) {
                        DestinoSignatario.INICIO -> navController.navigate(Sig01InicioPendentesRoute)
                        DestinoSignatario.DOCUMENTOS -> navController.navigate(Sig02MeusDocumentosRoute)
                        DestinoSignatario.NOTIFICACOES -> Unit
                        DestinoSignatario.CONTA -> navController.navigate(Com06MinhaContaRoute("SIGNATARIO"))
                    }
                }
            )
        }

        composable<Com06MinhaContaRoute> {
            Com06MinhaContaScreen(
                aoSairParaLogin = {
                    navController.navigate(Com02LoginRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                aoNavegarBottomNavAdmin = { destino ->
                    when (destino) {
                        DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                        DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                        DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                        DestinoAdmin.CONTA -> Unit
                    }
                },
                aoNavegarBottomNavSignatario = { destino ->
                    when (destino) {
                        DestinoSignatario.INICIO -> navController.navigate(Sig01InicioPendentesRoute)
                        DestinoSignatario.DOCUMENTOS -> navController.navigate(Sig02MeusDocumentosRoute)
                        DestinoSignatario.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("SIGNATARIO"))
                        DestinoSignatario.CONTA -> Unit
                    }
                }
            )
        }

        // ==========================================
        // ADMIN (ADM-01 a ADM-13)
        // ==========================================
        composable<Adm01DocumentosRoute> {
            Adm01DocumentosScreen(
                aoNavegarParaNovoDocumento = { navController.navigate(Adm04NovoDocTituloRoute()) },
                aoNavegarParaDocumento = { docId -> navController.navigate(Adm11AcompanhamentoRoute(docId)) },
                aoNavegarParaFalhaEnvio = { docId -> navController.navigate(Adm10RevisaoEnvioRoute) },
                aoNavegarParaRascunho = { docId -> navController.navigate(Adm04NovoDocTituloRoute(docId)) },
                aoNavegarParaCadastrarSignatario = { navController.navigate(Adm03SignatarioFormRoute()) },
                aoNavegarBottomNav = { destino ->
                    when (destino) {
                        DestinoAdmin.DOCUMENTOS -> Unit
                        DestinoAdmin.SIGNATARIOS -> navController.navigate(Adm02SignatariosRoute)
                        DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                        DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                    }
                }
            )
        }

        composable<Adm02SignatariosRoute> {
            Adm02SignatariosScreen(
                aoNavegarParaNovoSignatario = { navController.navigate(Adm03SignatarioFormRoute()) },
                aoNavegarParaEditarSignatario = { id -> navController.navigate(Adm03SignatarioFormRoute(id)) },
                aoNavegarBottomNav = { destino ->
                    when (destino) {
                        DestinoAdmin.DOCUMENTOS -> navController.navigate(Adm01DocumentosRoute)
                        DestinoAdmin.SIGNATARIOS -> Unit
                        DestinoAdmin.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("ADMIN"))
                        DestinoAdmin.CONTA -> navController.navigate(Com06MinhaContaRoute("ADMIN"))
                    }
                }
            )
        }

        composable<Adm03SignatarioFormRoute> {
            Adm03SignatarioFormScreen(
                aoVoltar = { navController.popBackStack() },
                aoConcluirComSucesso = { navController.popBackStack() }
            )
        }

        composable<Adm04NovoDocTituloRoute> { backStackEntry ->
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(backStackEntry)
            Adm04NovoDocTituloScreen(
                viewModel = wizardViewModel,
                aoAvancarParaPdf = { navController.navigate(Adm05SelecionarPdfRoute) },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm05SelecionarPdfRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm05SelecionarPdfScreen(
                viewModel = wizardViewModel,
                aoAvancarParaSignatarios = { navController.navigate(Adm06SignatariosRoute) },
                aoVoltarParaTitulo = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm06SignatariosRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm06SignatariosScreen(
                viewModel = wizardViewModel,
                aoAvancarParaPapeis = { navController.navigate(Adm07ObrigatoriedadeRoute) },
                aoVoltarParaPdf = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm07ObrigatoriedadeRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm07ObrigatoriedadeScreen(
                viewModel = wizardViewModel,
                aoAvancarParaOrdem = { navController.navigate(Adm08OrdemRoute) },
                aoVoltarParaSignatarios = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm08OrdemRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm08OrdemScreen(
                viewModel = wizardViewModel,
                aoAvancarParaCampos = { navController.navigate(Adm09PosicionarCamposRoute) },
                aoVoltarParaPapeis = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm09PosicionarCamposRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm09PosicionarCamposScreen(
                viewModel = wizardViewModel,
                aoAvancarParaRevisao = { navController.navigate(Adm10RevisaoEnvioRoute) },
                aoVoltarParaOrdem = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm10RevisaoEnvioRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry<Adm04NovoDocTituloRoute>() }.getOrNull() ?: backStackEntry
            }
            val wizardViewModel: WizardNovoDocViewModel = hiltViewModel(parentEntry)
            Adm10RevisaoEnvioScreen(
                viewModel = wizardViewModel,
                aoFinalizarEnvioComSucesso = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                },
                aoVoltarParaCampos = { navController.popBackStack() },
                aoSairParaInicio = {
                    navController.navigate(Adm01DocumentosRoute) {
                        popUpTo(Adm01DocumentosRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<Adm11AcompanhamentoRoute> {
            Adm11AcompanhamentoScreen(
                aoVoltar = { navController.popBackStack() },
                aoNavegarParaVisualizador = { versaoId -> navController.navigate(Adm13VisualizadorRoute(versaoId)) },
                aoNavegarParaAuditoria = { versaoId -> navController.navigate(Adm12HistoricoRoute(versaoId)) },
                aoNavegarParaNovaVersaoCriada = { novoDocId ->
                    navController.navigate(Adm04NovoDocTituloRoute(novoDocId))
                }
            )
        }

        composable<Adm12HistoricoRoute> {
            Adm12AuditoriaScreen(
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable<Adm13VisualizadorRoute> {
            Adm13VisualizadorAdminScreen(
                aoVoltar = { navController.popBackStack() }
            )
        }

        // ==========================================
        // SIGNATÁRIO (SIG-01 a SIG-08)
        // ==========================================
        composable<Sig01InicioPendentesRoute> {
            Sig01InicioPendentesScreen(
                aoNavegarParaDetalhes = { docId -> navController.navigate(Sig03DetalheDocumentoRoute(docId)) },
                aoNavegarParaAssinar = { docId -> navController.navigate(Sig04ConfirmacaoIdRoute(docId)) },
                aoNavegarBottomNav = { destino ->
                    when (destino) {
                        DestinoSignatario.INICIO -> Unit
                        DestinoSignatario.DOCUMENTOS -> navController.navigate(Sig02MeusDocumentosRoute)
                        DestinoSignatario.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("SIGNATARIO"))
                        DestinoSignatario.CONTA -> navController.navigate(Com06MinhaContaRoute("SIGNATARIO"))
                    }
                }
            )
        }

        composable<Sig02MeusDocumentosRoute> {
            Sig02MeusDocumentosScreen(
                aoNavegarParaDetalhes = { docId -> navController.navigate(Sig03DetalheDocumentoRoute(docId)) },
                aoNavegarParaAssinar = { docId -> navController.navigate(Sig04ConfirmacaoIdRoute(docId)) },
                aoNavegarBottomNav = { destino ->
                    when (destino) {
                        DestinoSignatario.INICIO -> navController.navigate(Sig01InicioPendentesRoute)
                        DestinoSignatario.DOCUMENTOS -> Unit
                        DestinoSignatario.NOTIFICACOES -> navController.navigate(Com05NotificacoesRoute("SIGNATARIO"))
                        DestinoSignatario.CONTA -> navController.navigate(Com06MinhaContaRoute("SIGNATARIO"))
                    }
                }
            )
        }

        composable<Sig03DetalheDocumentoRoute> {
            Sig03DetalheDocumentoScreen(
                aoVoltar = { navController.popBackStack() },
                aoNavegarParaAssinar = { docId -> navController.navigate(Sig04ConfirmacaoIdRoute(docId)) },
                aoNavegarParaVisualizar = { docId -> navController.navigate(Sig08VisualizadorLeituraRoute(docId)) },
                aoRecusaConcluida = { navController.popBackStack() }
            )
        }

        composable<Sig04ConfirmacaoIdRoute> {
            Sig04ConfirmacaoIdentidadeScreen(
                aoVoltar = { navController.popBackStack() },
                aoIdentidadeConfirmada = { docId -> navController.navigate(Sig05AssinarDocumentoRoute(docId)) }
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

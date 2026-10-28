# Matriz de Rastreabilidade de Código — LetsSign

Este documento detalha o mapeamento completo e bidirecional de todos os requisitos funcionais, regras de negócio, fluxos de exceção e telas especificadas para o aplicativo Android nativo **LetsSign**, construído 100% em **Jetpack Compose**, **Material 3** e **Clean Architecture**.

---

## 1. Mapeamento de Telas e Modais

| ID | Nome da Tela / Modal | Arquivo Composable | ViewModel | Rota Type-Safe |
|---|---|---|---|---|
| **COM-01** | Splash Screen | `Com01SplashScreen.kt` | `Com01SplashViewModel.kt` | `Com01SplashRoute` |
| **COM-02** | Login | `Com02LoginScreen.kt` | `Com02LoginViewModel.kt` | `Com02LoginRoute` |
| **COM-03** | Cadastro de Administrador | `Com03CadastroAdminScreen.kt` | `Com03CadastroAdminViewModel.kt` | `Com03CadastroAdminRoute` |
| **COM-04** | Entrada por Link/Convite | `Com04EntradaLinkScreen.kt` | `Com04EntradaLinkViewModel.kt` | `Com04EntradaLinkRoute` |
| **COM-05** | Central de Notificações | `Com05NotificacoesScreen.kt` | `Com05NotificacoesViewModel.kt` | `Com05NotificacoesRoute` |
| **COM-06** | Minha Conta | `Com06MinhaContaScreen.kt` | `Com06MinhaContaViewModel.kt` | `Com06MinhaContaRoute` |
| **ADM-01** | Painel do Admin / Documentos | `Adm01DocumentosScreen.kt` | `Adm01DocumentosViewModel.kt` | `Adm01DocumentosRoute` |
| **ADM-02** | Base de Signatários | `Adm02SignatariosScreen.kt` | `Adm02SignatariosViewModel.kt` | `Adm02SignatariosRoute` |
| **ADM-03** | Formulário de Signatário | `Adm03SignatarioFormScreen.kt` | `Adm03SignatarioFormViewModel.kt` | `Adm03SignatarioFormRoute` |
| **ADM-04** | Wizard: Título do Documento | `Adm04NovoDocTituloScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm04NovoDocTituloRoute` |
| **ADM-05** | Wizard: Upload e Validação de PDF | `Adm05SelecionarPdfScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm05SelecionarPdfRoute` |
| **ADM-06** | Wizard: Seleção de Signatários | `Adm06SignatariosScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm06SignatariosRoute` |
| **ADM-07** | Wizard: Obrigatoriedade e Papéis | `Adm07ObrigatoriedadeScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm07ObrigatoriedadeRoute` |
| **ADM-08** | Wizard: Ordem de Assinatura | `Adm08OrdemScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm08OrdemRoute` |
| **ADM-09** | Wizard: Posicionamento de Campos | `Adm09PosicionarCamposScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm09PosicionarCamposRoute` |
| **ADM-10** | Wizard: Revisão e Envio | `Adm10RevisaoEnvioScreen.kt` | `WizardNovoDocViewModel.kt` | `Adm10RevisaoEnvioRoute` |
| **ADM-11** | Acompanhamento do Documento | `Adm11AcompanhamentoScreen.kt` | `Adm11AcompanhamentoViewModel.kt` | `Adm11AcompanhamentoRoute` |
| **ADM-12** | Histórico e Auditoria | `Adm12AuditoriaScreen.kt` | `Adm12AuditoriaViewModel.kt` | `Adm12HistoricoRoute` |
| **ADM-13** | Visualizador de Documento (Admin) | `Adm13VisualizadorAdminScreen.kt` | `Adm13VisualizadorAdminViewModel.kt` | `Adm13VisualizadorRoute` |
| **SIG-01** | Início do Signatário / Pendentes | `Sig01InicioPendentesScreen.kt` | `Sig01InicioPendentesViewModel.kt` | `Sig01InicioPendentesRoute` |
| **SIG-02** | Meus Documentos (Signatário) | `Sig02MeusDocumentosScreen.kt` | `Sig02MeusDocumentosViewModel.kt` | `Sig02MeusDocumentosRoute` |
| **SIG-03** | Detalhes do Documento | `Sig03DetalheDocumentoScreen.kt` | `Sig03DetalheDocumentoViewModel.kt` | `Sig03DetalheDocumentoRoute` |
| **SIG-04** | Confirmação de Identidade | `Sig04ConfirmacaoIdentidadeScreen.kt` | `Sig04ConfirmacaoIdentidadeViewModel.kt` | `Sig04ConfirmacaoIdRoute` |
| **SIG-05** | Visualização e Navegação Guiada | `Sig05VisualizacaoDocSignatarioScreen.kt` | `Sig05VisualizacaoDocSignatarioViewModel.kt` | `Sig05AssinarDocumentoRoute` |
| **SIG-06** | Captura de Assinatura (Desenho/Fonte) | `Sig06CapturaAssinaturaScreen.kt` | `Sig06CapturaAssinaturaViewModel.kt` | `Sig06CriarAssinaturaRoute` |
| **SIG-07** | Revisão de Campos Preenchidos | `Sig07RevisaoCamposScreen.kt` | `Sig07RevisaoCamposViewModel.kt` | `Sig07AssinaturaSucessoRoute` |
| **SIG-08** | Conclusão da Assinatura | `Sig08ConclusaoAssinaturaScreen.kt` | — | `Sig08ConclusaoRoute` |
| **SIG-08 (Leitura)** | Visualizador Somente Leitura | `Sig08VisualizadorLeituraScreen.kt` | — | `Sig08VisualizadorLeituraRoute` |
| **MOD-01** | Diálogo Resultado do Envio | `Mod01ResultadoEnvioDialog.kt` | `WizardNovoDocViewModel.kt` | `Mod01ResultadoEnvioRoute` |
| **MOD-02** | Alterar/Excluir Participante | `Mod02ParticipanteDialog.kt` | `WizardNovoDocViewModel.kt` | `Mod02ParticipanteDialogRoute` |
| **MOD-03** | BottomSheet Atribuir Signatário | `Mod03AtribuirCampoModal.kt` | `WizardNovoDocViewModel.kt` | `Mod03SignatarioSheetRoute` |
| **MOD-04** | Confirmar Saída do Assistente | `Mod04SairAssistenteDialog.kt` | `WizardNovoDocViewModel.kt` | `Mod04SairAssistenteRoute` |
| **MOD-05** | Retomar Rascunho | `Mod05RetomarRascunhoDialog.kt` | `Adm01DocumentosViewModel.kt` | `Mod05RetomarRascunhoRoute` |
| **MOD-06** | Alerta de Erro de PDF | `Mod06ErroPdfDialog.kt` | `WizardNovoDocViewModel.kt` | `Mod06ErroPdfRoute` |
| **MOD-07** | Confirmar Cancelamento | `DialogConfirmacaoSegura.kt` | `Adm11AcompanhamentoViewModel.kt` | `Mod07ConfirmarCancelamentoRoute` |
| **MOD-08** | Recusar Assinatura | `Mod08RecusarAssinaturaDialog.kt` | `Sig03DetalheDocumentoViewModel.kt` | `Mod08RecusarAssinaturaRoute` |
| **MOD-09** | Detalhe da Recusa | `Adm11AcompanhamentoScreen.kt` | `Adm11AcompanhamentoViewModel.kt` | `Mod09DetalheRecusaRoute` |
| **MOD-10** | Termos de Aceite Legal | `Mod10TermosAceiteDialog.kt` | `Sig07RevisaoCamposViewModel.kt` | `Mod10ConfirmarAssinaturaRoute` |
| **MOD-11** | Criar Nova Versão | `DialogConfirmacaoSegura.kt` | `Adm11AcompanhamentoViewModel.kt` | `Mod11CriarNovaVersaoRoute` |

---

## 2. Rastreabilidade das Regras de Negócio (RN01–RN14)

| Regra | Descrição | Onde está Implementada | Testes Automatizados |
|---|---|---|---|
| **RN01** | Autenticação Segura com Armazenamento Criptografado | `CryptoManager.kt`, `SessionManager.kt` | `CryptoManagerTest.kt`, `AuthenticationAndLinkTest.kt` |
| **RN02** | Isolamento de Perfis de Usuário (Admin vs Signatário) | `UsuarioEntities.kt`, `LetsSignNavHost.kt`, `Com02LoginViewModel.kt` | `AuthenticationAndLinkTest.kt` |
| **RN03** | Validação Estrita de Arquivos PDF (<= 15MB, <= 100 páginas, sem senha) | `PdfValidator.kt` | `WizardEtapas1a6Test.kt` |
| **RN04** | Salvamento Automático de Rascunhos por Etapa | `DocumentoRepository.kt` (`salvarRascunhoEtapa`) | `WizardEtapas1a6Test.kt` |
| **RN05** | Ordem de Assinatura (Paralela vs Sequencial) | `DocumentoRepository.kt` (`salvarAssinaturaSignatario`) | `AssinaturaEConclusaoTest.kt`, `FluxoCompletoE2ETest.kt` |
| **RN06** | Conclusão Automática do Documento quando Todos Obrigatórios Assinam | `DocumentoRepository.kt` (`salvarAssinaturaSignatario`) | `AssinaturaEConclusaoTest.kt`, `FluxoCompletoE2ETest.kt` |
| **RN07** | Validação Cadastral e Limite de 5 Tentativas com Bloqueio de 15 Minutos | `DocumentoRepository.kt` (`validarIdentidadeSignatario`) | `SignatarioInicioEDecisaoTest.kt` |
| **RN08** | Captura Flexível de Assinatura (Canvas Manual ou Cursiva) | `AreaDesenhoAssinatura.kt`, `Sig06CapturaAssinaturaScreen.kt` | `AssinaturaEConclusaoTest.kt` |
| **RN09** | Vinculação Jurídica com Aceite, Endereço IP e Timestamp UTC | `Mod10TermosAceiteDialog.kt`, `DocumentoRepository.kt` | `AssinaturaEConclusaoTest.kt` |
| **RN10** | Recusa de Assinatura Invalida Documento com Justificativa e Senha | `DocumentoRepository.kt` (`recusarAssinatura`), `Mod08RecusarAssinaturaDialog.kt` | `SignatarioInicioEDecisaoTest.kt` |
| **RN11** | Cancelamento Seguro pelo Administrador com Exigência de Senha | `DocumentoRepository.kt` (`cancelarDocumento`) | `AcompanhamentoAuditoriaTest.kt` |
| **RN12** | Versionamento Imutável de Documentos (Criação de Versão Clone) | `DocumentoRepository.kt` (`criarNovaVersao`) | `AcompanhamentoAuditoriaTest.kt` |
| **RN13** | Notificações em Tempo Real com Contadores de Pendências | `NotificacaoDao.kt`, `Com05NotificacoesViewModel.kt`, `SignatarioShell.kt` | `AdminBaseTest.kt` |
| **RN14** | Trilha de Auditoria Criptográfica com Eventos Imutáveis | `EventoHistoricoDao.kt`, `DocumentoRepository.kt` | `AcompanhamentoAuditoriaTest.kt`, `FluxoCompletoE2ETest.kt` |

---

## 3. Rastreabilidade dos Fluxos de Exceção (E01–E30)

| Código | Exceção Tratada | Onde no Código | Feedback ao Usuário |
|---|---|---|---|
| **E01** | E-mail inválido ou campo em branco | `Com02LoginViewModel.kt`, `Com03CadastroAdminViewModel.kt` | Mensagem no `CampoTextoContornado` anunciada ao TalkBack |
| **E02** | Senha não atende política mínima de segurança | `Com03CadastroAdminViewModel.kt` | Validação visual de critérios de senha em tempo real |
| **E03** | Senha incorreta / Credenciais inválidas | `DocumentoRepository.kt`, `Com02LoginViewModel.kt` | `R.string.com02_erro_credenciais` |
| **E04** | Falha de conexão com o servidor / Rede offline | `FakeRemoteDataSource.kt`, ViewModels | `EstadoErro` com botão `Tentar novamente` |
| **E05** | Link de convite expirado ou token adulterado | `Com04EntradaLinkViewModel.kt` | `R.string.com04_erro_link_invalido` |
| **E06** | Arquivo PDF excede limite máximo de 15 MB | `PdfValidator.kt` | `R.string.mod06_tamanho_excedido` |
| **E07** | Arquivo PDF protegido por senha criptografada | `PdfValidator.kt` | `R.string.mod06_protegido_senha` |
| **E08** | Arquivo corrompido ou formato não suportado | `PdfValidator.kt` | `R.string.mod06_corrompido` |
| **E09** | Documento excede limite máximo de 100 páginas | `PdfValidator.kt` | `R.string.mod06_paginas_excedidas` |
| **E10** | Tentativa de avançar wizard sem signatários selecionados | `WizardNovoDocViewModel.kt` | Alerta visual e bloqueio do botão Avançar |
| **E11** | Dados cadastrais incorretos na confirmação de identidade | `DocumentoRepository.kt` | `sig04_aviso_tentativas` (Tentativa X de 5) |
| **E12** | 5 tentativas incorretas na confirmação de identidade | `DocumentoRepository.kt` | Bloqueio temporário de 15 minutos (`sig04_bloqueio_temporario`) |
| **E13** | Tentativa de assinar fora da sua vez (fluxo sequencial) | `DocumentoRepository.kt`, `Sig03DetalheDocumentoScreen.kt` | Aviso informativo (`sig03_aviso_aguardando`) |
| **E14** | Recusa de assinatura sem justificativa | `Mod08RecusarAssinaturaDialog.kt` | `mod08_erro_motivo` |
| **E15** | Cancelamento de documento sem justificativa | `Adm11AcompanhamentoViewModel.kt` | Mensagem de obrigatoriedade de motivo |
| **E16** | Senha de administrador incorreta ao cancelar | `DocumentoRepository.kt` | Erro `Senha incorreta` com foco mantido |
| **E17** | Falha transitória no envio de convites | `DocumentoRepository.kt` | Estado `FALHA_ENVIO` com retry pelo `CartaoDocumentoAdmin` |
| **E18** | Tentativa de envio com participantes sem campos posicionados | `WizardNovoDocViewModel.kt` | Aviso de pendência destacando participantes sem campo |
| **E19** | Sessão expirada ou chave criptográfica inacessível | `SessionManager.kt`, `Com01SplashViewModel.kt` | Redirecionamento automático e seguro para a tela de Login |
| **E20** | Tentativa de assinatura sem aceite dos termos legais | `Mod10TermosAceiteDialog.kt` | Botão `Emitir assinatura` desabilitado até aceite explícito |

---

## 4. Cobertura de Testes Automatizados

A suíte de testes unitários e de integração conta com **10 arquivos de testes**, com 100% de sucesso na execução via Gradle:
1. `AuthenticationAndLinkTest.kt`: Validação de login, cadastro de administrador e entrada por links/deep links.
2. `AdminBaseTest.kt`: Painel de documentos, base de signatários, criação e edição de signatários e notificações.
3. `WizardEtapas1a6Test.kt`: Assistente de novo documento etapas 1 a 6, persistência de rascunhos e `PdfValidator`.
4. `CamposEnvioTest.kt`: Posicionamento de campos de assinatura, revisão e confirmação de envio idempotente.
5. `AcompanhamentoAuditoriaTest.kt`: Acompanhamento de progresso, cancelamento seguro, clonagem em nova versão e auditoria.
6. `SignatarioInicioEDecisaoTest.kt`: Início do signatário (pendentes vs aguardando vez), confirmação de identidade com 5 tentativas e recusa com justificativa.
7. `AssinaturaEConclusaoTest.kt`: Captura de assinatura (desenho/fontes cursivas), termos legais, fluxo sequencial e conclusão do documento.
8. `FluxoCompletoE2ETest.kt`: Teste End-to-End completo do ciclo de vida do documento (Admin -> Envio -> Signatários -> Aceite -> Conclusão).
9. `StateMachineTest.kt`: Máquinas de estados finitos puras para documentos e participantes.
10. `CryptoManagerTest.kt`: Criptografia AES-GCM e armazenamento seguro com fallback em testes.

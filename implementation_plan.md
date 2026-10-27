# PLANO DE IMPLEMENTAÇÃO — LETSSIGN (FASE 0)

> **Documento de Planejamento Geral (Sprint 1 · Etapa 3)**  
> **Status:** Aguardando Aprovação  
> **Fontes da Verdade:** `docs/LetsSign_Etapa1_Arquitetura_de_Informacao.md` e `docs/LetsSign_Prompts_FigmaMake.txt`  
> **Regras Permanentes:** `.agent/rules/letssign-regras.md`

---

## 1. ESTRUTURA DE PASTAS DO PROJETO (`:app`)

Namespace e applicationId: `br.com.letssign`  
Arquitetura em camadas: UI (Compose) → Domain (UseCases, State Machines) → Data (Room, DataStore, FakeRemoteDataSource).

```
br.com.letssign
├── core
│   ├── common
│   │   ├── extensions/          # Extensões úteis (Context, Date, Flow)
│   │   ├── formatters/          # Formatadores de data/hora (dd/MM/yyyy HH:mm) e moeda
│   │   ├── hashing/             # Gerador SHA-256 de streams e arquivos
│   │   ├── result/              # Result<T> e tratamento unificado de erros
│   │   └── text/                # Normalizador P04 (remover acentos, trim, lowercase)
│   ├── designsystem
│   │   ├── theme/
│   │   │   ├── Color.kt         # LetsSignColors com os 34 tokens de cor oficiais
│   │   │   ├── Shape.kt         # Formas: Pequena (8dp), Média (12dp), Grande (16dp), Extra Grande (28dp), Total (Pílula)
│   │   │   ├── Spacing.kt       # Espaçamentos: esp-4, esp-8, esp-12, esp-16, esp-24, esp-32 (dp)
│   │   │   ├── Elevation.kt     # Elevações: nível 0 a 3 com corSombra
│   │   │   ├── Type.kt          # Escala tipográfica Roboto M3 (Display, Headline, Title, Body, Label em sp)
│   │   │   └── Theme.kt         # LetsSignTheme com CompositionLocalProvider
│   │   ├── components
│   │   │   ├── base/            # Botões (com loading), Campos Contornados (label fixo + erro), Banners, Snackbar, Dialogs
│   │   │   ├── logo/            # LogoPlaceholder (160x160, 120x48, 32x32 com borda tracejada)
│   │   │   ├── feedback/        # Skeleton, Indicador Circular, Banner Offline, Painel de Resultado
│   │   │   ├── pdf/             # PdfVisualizer (PdfRenderer com cache LRU, zoom, pinch, páginas)
│   │   │   ├── signature/       # Canvas de desenho + Seletor Cursivo (4 fontes OFL)
│   │   │   └── reusable/        # Os 16 componentes da seção 14.2 / Figma Make bloco 8
│   │   └── badges/              # SeloSituacao (8 para documento, 5 para participante)
│   └── navigation
│       ├── NavRoutes.kt         # Objetos e classes @Serializable com ID de cada tela
│       ├── LetsSignNavHost.kt   # NavHost central ligando as 38 rotas type-safe
│       └── shells/              # Shells com BottomNavigation para Admin e Signatário
├── data
│   ├── local
│   │   ├── database/
│   │   │   ├── LetsSignDatabase.kt # Room database (exportSchema = true, versão 1)
│   │   │   ├── converters/         # Converters para Enums, Date/Instant e Listas
│   │   │   ├── dao/                # 11 DAOs da seção 10 (Usuario, Documento, Versao, Participante, etc.)
│   │   │   └── entities/           # 12 Entidades Room mapeadas
│   │   ├── datastore/
│   │   │   ├── EncryptedDataStore.kt # DataStore protegido com chave AES-GCM no Android Keystore
│   │   │   └── SessionPreferences.kt # Preferências de sessão e links pendentes
│   │   └── file/
│   │       └── InternalStorageManager.kt # Cópia via SAF para files/documentos/{id}/v{n}.pdf
├── data
│   ├── remote
│   │   ├── api/                 # Interfaces RemoteDataSource (*RemoteDataSource)
│   │   └── fake/                # FakeRemoteDataSource simulando servidor local (RT05, P01, RF05, P05)
│   └── repository
│       ├── UsuarioRepositoryImpl.kt
│       ├── DocumentoRepositoryImpl.kt       # Filtro RN07 estrito pelo usuário logado
│       ├── WizardDocumentoRepositoryImpl.kt
│       ├── SignatarioDocumentoRepositoryImpl.kt
│       ├── NotificacaoRepositoryImpl.kt
│       ├── HistoricoRepositoryImpl.kt
│       └── SessaoRepositoryImpl.kt
├── domain
│   ├── model/                  # Modelos de domínio imutáveis
│   ├── statemachine/           # Funções puras das máquinas de estado (Documento e Participante)
│   └── usecase/                # Casos de uso de autenticação, wizard, envio, assinatura e recusa
└── feature
    ├── common/                 # Telas comuns (COM-01 a COM-06)
    ├── admin/                  # Telas do Admin (ADM-01 a ADM-13)
    ├── signer/                 # Telas do Signatário (SIG-01 a SIG-08)
    └── modal/                  # Diálogos e folhas modais (MOD-01 a MOD-11)
```

---

## 2. TABELA COMPLETA DAS 38 TELAS (TELA → ROTA → COMPOSABLE → VIEWMODEL → FASE)

Cada rota implementa `@Serializable` com o ID explícito e KDoc formal:

| ID | Nome da Tela | Rota Type-Safe | Composable Principal | ViewModel (Hilt) | Fase Impl. |
|---|---|---|---|---|---|
| **COM-01** | Splash e verificação de sessão | `Com01SplashRoute` | `Com01SplashScreen` | `SplashViewModel` | Fase 1 (prov.) / Fase 3 |
| **COM-02** | Login | `Com02LoginRoute` | `Com02LoginScreen` | `LoginViewModel` | Fase 1 (prov.) / Fase 3 |
| **COM-03** | Cadastro inicial do Admin | `Com03CadastroAdminRoute` | `Com03CadastroAdminScreen` | `CadastroAdminViewModel` | Fase 1 (prov.) / Fase 3 |
| **COM-04** | Entrada por link | `Com04EntradaLinkRoute` | `Com04EntradaLinkScreen` | `EntradaLinkViewModel` | Fase 1 (prov.) / Fase 3 |
| **COM-05** | Central de notificações | `Com05NotificacoesRoute` | `Com05NotificacoesScreen` | `NotificacoesViewModel` | Fase 1 (prov.) / Fase 3 |
| **COM-06** | Minha conta | `Com06MinhaContaRoute` | `Com06MinhaContaScreen` | `MinhaContaViewModel` | Fase 1 (prov.) / Fase 3 |
| **ADM-01** | Início — Painel de documentos | `Adm01DocumentosRoute` | `Adm01DocumentosScreen` | `AdmDocumentosViewModel` | Fase 1 (prov.) / Fase 4 |
| **ADM-02** | Signatários | `Adm02SignatariosRoute` | `Adm02SignatariosScreen` | `AdmSignatariosViewModel` | Fase 1 (prov.) / Fase 4 |
| **ADM-03** | Cadastro/edição de signatário | `Adm03SignatarioFormRoute` | `Adm03SignatarioFormScreen` | `AdmSignatarioFormViewModel` | Fase 1 (prov.) / Fase 4 |
| **ADM-04** | Novo doc — Título (Etapas 1–2) | `Adm04NovoDocTituloRoute` | `Adm04NovoDocTituloScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **ADM-05** | Selecionar PDF (Etapa 3) | `Adm05SelecionarPdfRoute` | `Adm05SelecionarPdfScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **ADM-06** | Selecionar signatários (Etapa 4) | `Adm06SignatariosRoute` | `Adm06SignatariosScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **ADM-07** | Definir obrigatoriedade (Etapa 5)| `Adm07ObrigatoriedadeRoute` | `Adm07ObrigatoriedadeScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **ADM-08** | Definir ordem (Etapa 6) | `Adm08OrdemRoute` | `Adm08OrdemScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **ADM-09** | Posicionar campos (Etapas 7–8) | `Adm09PosicionarCamposRoute` | `Adm09PosicionarCamposScreen`| `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 6 |
| **ADM-10** | Revisão e envio (Etapa 9) | `Adm10RevisaoEnvioRoute` | `Adm10RevisaoEnvioScreen` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 6 |
| **ADM-11** | Acompanhamento do documento | `Adm11AcompanhamentoRoute` | `Adm11AcompanhamentoScreen` | `AcompanhamentoViewModel` | Fase 1 (prov.) / Fase 7 |
| **ADM-12** | Histórico do documento | `Adm12HistoricoRoute` | `Adm12HistoricoScreen` | `HistoricoDocViewModel` | Fase 1 (prov.) / Fase 7 |
| **ADM-13** | Visualizador de documento (Admin)| `Adm13VisualizadorRoute` | `Adm13VisualizadorScreen` | `VisualizadorAdminViewModel` | Fase 1 (prov.) / Fase 7 |
| **SIG-01** | Início — Pendentes | `Sig01InicioPendentesRoute` | `Sig01InicioPendentesScreen` | `SigPendentesViewModel` | Fase 1 (prov.) / Fase 8 |
| **SIG-02** | Meus documentos | `Sig02MeusDocumentosRoute` | `Sig02MeusDocumentosScreen` | `SigMeusDocumentosViewModel`| Fase 1 (prov.) / Fase 8 |
| **SIG-03** | Detalhe do documento | `Sig03DetalheDocumentoRoute` | `Sig03DetalheDocumentoScreen` | `SigDetalheDocViewModel` | Fase 1 (prov.) / Fase 8 |
| **SIG-04** | Confirmação de identidade | `Sig04ConfirmacaoIdRoute` | `Sig04ConfirmacaoIdScreen` | `SigConfirmacaoIdViewModel` | Fase 1 (prov.) / Fase 8 |
| **SIG-05** | Assinar documento | `Sig05AssinarDocumentoRoute`| `Sig05AssinarDocumentoScreen`| `SigAssinaturaFlowViewModel`| Fase 1 (prov.) / Fase 9 |
| **SIG-06** | Criar assinatura (Desenhar/Digitar)| `Sig06CriarAssinaturaRoute`| `Sig06CriarAssinaturaScreen`| `SigAssinaturaFlowViewModel`| Fase 1 (prov.) / Fase 9 |
| **SIG-07** | Assinatura registrada | `Sig07AssinaturaSucessoRoute`| `Sig07AssinaturaSucessoScreen`| `SigAssinaturaFlowViewModel`| Fase 1 (prov.) / Fase 9 |
| **SIG-08** | Visualizador somente leitura | `Sig08VisualizadorLeituraRoute`| `Sig08VisualizadorLeituraScreen`| `SigVisualizadorLeituraViewModel`| Fase 1 (prov.) / Fase 9 |
| **MOD-01** | Resultado do envio | `Mod01ResultadoEnvioRoute` | `Mod01ResultadoEnvioDialog` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 6 |
| **MOD-02** | Excluir/alterar participante | `Mod02ParticipanteDialogRoute`| `Mod02ParticipanteDialog` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **MOD-03** | Selecionar signatário do campo | `Mod03SignatarioSheetRoute` | `Mod03SignatarioSheet` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 6 |
| **MOD-04** | Sair do assistente | `Mod04SairAssistenteRoute` | `Mod04SairAssistenteDialog` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **MOD-05** | Retomar rascunho | `Mod05RetomarRascunhoRoute` | `Mod05RetomarRascunhoDialog` | `AdmDocumentosViewModel` | Fase 1 (prov.) / Fase 5 |
| **MOD-06** | Erro de arquivo PDF | `Mod06ErroPdfRoute` | `Mod06ErroPdfDialog` | `WizardDocumentoViewModel` | Fase 1 (prov.) / Fase 5 |
| **MOD-07** | Confirmar cancelamento | `Mod07ConfirmarCancelamentoRoute`| `Mod07ConfirmarCancelamentoDialog`| `AcompanhamentoViewModel` | Fase 1 (prov.) / Fase 7 |
| **MOD-08** | Recusar assinatura | `Mod08RecusarAssinaturaRoute`| `Mod08RecusarAssinaturaDialog` | `SigDetalheDocViewModel` | Fase 1 (prov.) / Fase 8 |
| **MOD-09** | Detalhe da recusa | `Mod09DetalheRecusaRoute` | `Mod09DetalheRecusaSheet` | `AcompanhamentoViewModel` | Fase 1 (prov.) / Fase 7 |
| **MOD-10** | Confirmar assinatura | `Mod10ConfirmarAssinaturaRoute`| `Mod10ConfirmarAssinaturaDialog`| `SigAssinaturaFlowViewModel`| Fase 1 (prov.) / Fase 9 |
| **MOD-11** | Criar nova versão | `Mod11CriarNovaVersaoRoute` | `Mod11CriarNovaVersaoDialog` | `AcompanhamentoViewModel` | Fase 1 (prov.) / Fase 7 |

---

## 3. MODELO DE DADOS, DAOS E REPOSITÓRIOS

### 3.1 Entidades Room (com `@Entity` e chaves estrangeiras)
1. **`UsuarioEntity`**: id (PK, String UUID), nome, email (Index único), telefone, perfil (ADMIN / SIGNATARIO), senhaHash, dataCriacaoUtc.
2. **`AdminSignatarioEntity`**: adminId (FK), signatarioId (FK), nome, email, telefone, dataNascimento, dataVinculoUtc (PK composta adminId + signatarioId).
3. **`DocumentoEntity`**: id (PK), usuarioAdminId (FK), titulo, dataCriacaoUtc, dataUltimaAtualizacaoUtc, situacaoAtual.
4. **`VersaoDocumentoEntity`**: id (PK), documentoId (FK), numeroVersao, pdfPathInterno, sha256Original, sha256Final, tipoOrdem (SEQUENCIAL/PARALELA), mensagemConvite, chaveIdempotencia, etapaWizardAtual, situacao.
5. **`ParticipanteDocumentoEntity`**: id (PK), versaoDocumentoId (FK), usuarioId (FK opcional), nomeSnapshot, emailSnapshot, telefoneSnapshot, dataNascimentoSnapshot, papel, obrigatorio (Boolean), ordem (Int), situacao (AGUARDANDO_VEZ, PENDENTE, ASSINADO, RECUSADO, ENCERRADO).
6. **`CampoAssinaturaEntity`**: id (PK), versaoDocumentoId (FK), participanteId (FK), numeroPagina, normX, normY, normLargura, normAltura (coordenadas 0.0–1.0).
7. **`AssinaturaEntity`**: id (PK), campoAssinaturaId (FK), participanteId (FK), tipoAssinatura (DESENHADA/DIGITADA), caminhoPngAssinatura, dataHoraUtc, ipDispositivo, hashValidador.
8. **`RecusaEntity`**: id (PK), participanteId (FK), dataHoraUtc, observacao, senhaConfirmada (Boolean).
9. **`CancelamentoEntity`**: id (PK), versaoDocumentoId (FK), adminId (FK), dataHoraUtc, motivo, senhaConfirmada (Boolean).
10. **`EnvioEntity`**: id (PK), versaoDocumentoId (FK), chaveIdempotencia, dataHoraUtc, statusEnvio, respostaServidorJson.
11. **`NotificacaoEntity`**: id (PK), usuarioId (FK), documentoId (FK), titulo, mensagem, tipoNotificacao, dataHoraUtc, lida (Boolean).
12. **`EventoHistoricoEntity`**: id (PK), versaoDocumentoId (FK), dataHoraUtc, tipoEvento, autorNome, detalhe. *(Append-only, sem update/delete)*.

### 3.2 DAOs e Repositórios
- **DAOs**: `UsuarioDao`, `DocumentoDao`, `VersaoDocumentoDao`, `ParticipanteDao`, `CampoAssinaturaDao`, `AssinaturaDao`, `RecusaDao`, `CancelamentoDao`, `EnvioDao`, `NotificacaoDao`, `EventoHistoricoDao`.
- **Filtro RN07**: Todas as queries de documentos, participantes e histórico executadas pelo Admin são filtradas estritamente por `usuarioAdminId == sessao.usuarioId`. As do Signatário filtram por `participante.emailSnapshot == sessao.email` ou `usuarioId == sessao.usuarioId`.

---

## 4. MAPA DAS MÁQUINAS DE ESTADO

Implementadas como funções puras em Kotlin (sem dependência de Android):

### 4.1 Documento (`DocumentoStateMachine.kt`)
- `transicionarDocumento(situacaoAtual, evento): SituacaoDocumento`
- **Regras:**
  - `RASCUNHO` + `EnviarSucesso` → `ENVIADO`
  - `RASCUNHO` + `EnviarFalha` → `FALHA_ENVIO`
  - `FALHA_ENVIO` + `ReenviarSucesso` → `ENVIADO`
  - `ENVIADO` + `AssinaturaObrigatorio` → `PARCIALMENTE_ASSINADO` (ou `CONCLUIDO` se era o único)
  - `PARCIALMENTE_ASSINADO` + `UltimaAssinaturaObrigatoria` → `CONCLUIDO`
  - `ENVIADO`/`PARCIALMENTE_ASSINADO` + `RecusaObrigatorio` → `RECUSADO`
  - `ENVIADO`/`PARCIALMENTE_ASSINADO` + `RecusaOpcional` → Mantém status atual (libera a vez se sequencial)
  - `ENVIADO`/`PARCIALMENTE_ASSINADO` + `AdminCancela` → `CANCELADO`
  - `RECUSADO`/`CANCELADO` + `NovaVersaoEnviada` → `SUBSTITUIDO`

### 4.2 Participante (`ParticipanteStateMachine.kt`)
- `transicionarParticipante(situacaoAtual, evento, tipoOrdem, participantesRestantes): SituacaoParticipante`
- **Regras:**
  - Em fluxo paralelo: todos iniciam em `PENDENTE`.
  - Em fluxo sequencial: o 1º inicia em `PENDENTE`, os demais em `AGUARDANDO_VEZ`.
  - Quando participante N assina ou recusa (sendo opcional): participante N+1 transiciona de `AGUARDANDO_VEZ` para `PENDENTE`.
  - Quando o documento transiciona para `CONCLUIDO`: participantes opcionais pendentes transicionam para `ENCERRADO`.
  - Quando o documento transiciona para `CANCELADO` ou `RECUSADO` (por outro): participantes pendentes/aguardando transicionam para `ENCERRADO`.

---

## 5. DESIGN SYSTEM & COMPONENTES

### 5.1 Tokens de Cor (`LetsSignColors.kt`)
34 tokens camelCase mapeados a partir do bloco 3:
- `corPrimaria` (#293681), `corSobrePrimaria` (#FFFFFF), `corPrimariaContainer` (#E8EBFA), `corSobrePrimariaContainer` (#1B245E)
- `corSecundaria` (#4274D9), `corSobreSecundaria` (#FFFFFF)
- `corFundo` (#FFFFFF), `corFundoExterno` (#F4F6FB)
- `corSuperficie` (#FFFFFF), `corSuperficieVariante` (#EEF1F8)
- `corTextoPrincipal` (#171A24), `corTextoSecundario` (#5C6475), `corTextoDesabilitado` (#8C94A5)
- `corDesabilitado` (#E5E8EF), `corBorda` (#DCE2EE), `corDivisor` (#E5E9F2)
- `corOverlay` (#171A24), `corSombra` (#171A24), `corFoco` (#4274D9)
- `corSucesso` (#23845B), `corSucessoContainer` (#E1F4E9)
- `corAviso` (#946300), `corAvisoContainer` (#FFF1CC)
- `corErro` (#D64545), `corErroContainer` (#FCE8E8)
- `corInfo` (#293681), `corInfoContainer` (#E8EBFA)
- `corNeutro` (#5C6475), `corNeutroContainer` (#EEF1F8)
- `corBadge` (#293681), `corSobreBadge` (#FFFFFF)
- `corCampoAssinaturaVazio` (#F4F6FB), `corCampoAssinaturaSelecionado` (#E8EBFA)
- `corCampoAssinaturaAssinado` (#E1F4E9), `corCampoAssinaturaOutroParticipante` (#E5F5FA)
- `corPaginaPdf` (#FFFFFF), `corTintaAssinatura` (#293681)

### 5.2 Tipografia (Roboto, sp)
- `Display Small` (36/44), `Headline Small` (24/32), `Title Large` (22/28), `Title Medium` (16/24, 500)
- `Body Large` (16/24), `Body Medium` (14/20), `Label Large` (14/20, 500), `Label Medium` (12/16, 500), `Label Small` (11/16)
- Suporte estrito a `fontScale 2.0` (quebra de linha automática e sem cortes).

### 5.3 Os 16 Componentes da Seção 14.2
1. Barra de navegação inferior (Admin e Signatário, com badges e indicador em pílula)
2. Indicador de etapas do wizard (1–9 com estados concluída, atual e futura)
3. Selo de situação (8 para documento, 5 para participante, texto + ícone)
4. Cartão de documento (variantes Admin e Signatário)
5. Linha de participante (avatar, nome, papel, obrigatoriedade, ordem, selo, ações)
6. Visualizador de PDF com camada de campos (modos edição, assinatura e leitura)
7. Campo de assinatura sobreposto (VAZIO, SELECIONADO, ASSINADO, OUTRO_PARTICIPANTE)
8. Contador/badge de pendências (círculo com acessibilidade)
9. Diálogo de confirmação segura (consequências + checkbox + senha)
10. Diálogo/painel de resultado (sucesso/erro com ícones e até 2 ações)
11. Estado vazio (ícone grande neutro + texto + ação opcional)
12. Estado de erro com "Tentar novamente"
13. Lista de pendências com atalhos (cartão aviso com links diretos)
14. Item de linha do tempo (marcador, linha vertical, data/hora, evento, detalhe)
15. Bottom sheet de seleção (alça, título, lista tocável)
16. Área de desenho + seletor de estilo de texto (Canvas com limpar + 4 fontes cursivas)

---

## 6. BIBLIOTECAS E VERSÕES (libs.versions.toml)

Configuração com versões ESTÁVEIS e compatíveis:

```toml
[versions]
agp = "8.7.2"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
lifecycle = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.10.01"
navigationCompose = "2.8.3"
serialization = "1.7.3"
hilt = "2.52"
hiltNavigationCompose = "1.2.0"
room = "2.6.1"
datastore = "1.1.1"
pdfbox = "2.0.27.0"
coroutines = "1.9.0"
turbine = "1.2.0"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
pdfbox-android = { group = "com.tom-roush", name = "pdfbox-android", version.ref = "pdfbox" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
```

---

## 7. CRITÉRIOS DE ACEITE DE CADA FASE

- **FASE 1 — Projeto Base e Design System**:
  - Projeto Gradle compila perfeitamente sem warnings críticos.
  - Design system implementado com CompositionLocal e todos os 34 tokens de cor.
  - 16 componentes reutilizáveis e LogoPlaceholder criados com `@Preview` (inclusive `fontScale 2.0`).
  - NavHost com 38 rotas type-safe navegáveis em telas provisórias "ID — Nome".
  - Shells de bottom navigation Admin e Signatário funcionais.

- **FASE 2 — Dados e Servidor Simulado**:
  - 12 entidades Room, 11 DAOs com queries testadas in-memory.
  - DataStore criptografado via Android Keystore com persistência de sessão.
  - Máquinas de estado puras com 100% de cobertura de testes unitários (recusa opcional, encerramento de pendentes, etc.).
  - FakeRemoteDataSource funcional aplicando idempotência, atraso configurável e validação cadastral.
  - Seed de debug com Carla Mendes, signatários e documentos de teste.

- **FASE 3 — Entrada e Conta (COM-01 a COM-06)**:
  - Splash redireciona corretamente por perfil/sessão.
  - Login com validação, autofill e bloqueio temporário.
  - Cadastro do Admin com indicador de senha e aceite de termos.
  - Entrada por link tratando todos os estados da ficha COM-04.
  - Notificações com filtro e Minha Conta com logout e limpeza de dados locais.

- **FASE 4 — Admin Base (ADM-01 a ADM-03)**:
  - Painel de documentos com contadores, busca, filtros e destaque de "Falha no envio".
  - Gestão de signatários com busca e sem exibir data de nascimento na listagem.
  - Cadastro/edição de signatário com DatePicker e validação P03.

- **FASE 5 — Wizard Etapas 1–6 (ADM-04 a ADM-08, MOD-02, MOD-04, MOD-05, MOD-06)**:
  - Grafo aninhado de wizard com ViewModel compartilhado e autosave por etapa.
  - Validação estrita de PDF (tamanho, páginas, senha, integridade) com exibição do MOD-06.
  - Seleção de participantes sem duplicidade e controle transacional no MOD-02.
  - Validação de obrigatoriedade (mínimo 1) e ordenação por drag e botões.

- **FASE 6 — Campos e Envio (ADM-09, MOD-03, ADM-10, MOD-01)**:
  - Visualizador com PdfRenderer, cache LRU, zoom e controle de páginas.
  - Posicionamento de campos delimitados à página e conversão normalizada (0–1).
  - Revisão com bloqueio de envio em caso de pendências.
  - Disparo de envio idempotente com exibição do MOD-01.

- **FASE 7 — Acompanhamento (ADM-11 a ADM-13, MOD-07, MOD-09, MOD-11)**:
  - Acompanhamento com progresso em tempo real, "Vez de" e ações por status.
  - Trilha de auditoria (ADM-12) com filtros e inversão cronológica.
  - Visualizador de leitura (ADM-13) e cancelamento seguro com senha (MOD-07).
  - Clonagem e criação de nova versão (MOD-11).

- **FASE 8 — Signatário Início e Decisão (SIG-01 a SIG-04, MOD-08)**:
  - Início com contadores anunciados e separação de pendentes vs aguardando vez.
  - Confirmação de identidade estrita (5 tentativas, bloqueio 15 min, normalização P04).
  - Recusa de assinatura com justificativa e senha (MOD-08).

- **FASE 9 — Assinatura (SIG-05 a SIG-08, MOD-10)**:
  - Assinatura guiada com navegação entre campos.
  - Captura de assinatura por desenho no Canvas ou tipografia cursiva.
  - Aceite de termos no MOD-10 e geração do PDF final composto via PDFBox.
  - Teste ponta a ponta: envio pelo Admin → assinaturas sequenciais → conclusão e PDF final no ADM-13.

- **FASE 10 — Qualidade e Rastreabilidade**:
  - Matriz de rastreabilidade em `docs/rastreabilidade_codigo.md`.
  - Tratamento de todas as exceções E01 a E30.
  - Auditoria de acessibilidade (TalkBack e fontScale 2.0).
  - Build e testes verdes: `./gradlew assembleDebug testDebugUnitTest lintDebug`.

---

## 8. PONTOS A CONFIRMAR (AMBIGUIDADES / ALINHAMENTO)

1. **Tokens de Cor [PREENCHER]**: Na especificação do Figma Make fornecida, todos os 34 tokens de cor vieram preenchidos com seus valores hexadecimais (ex.: `cor-primaria = #293681`, `cor-secundaria = #4274D9`, etc.). Não há tokens pendentes de preenchimento. Confirmamos que utilizaremos exatamente estes valores definidos.
2. **Fontes Cursivas (SIG-06)**: Utilizaremos as fontes cursivas OFL do Google Fonts empacotadas em `res/font/`: *Dancing Script*, *Great Vibes*, *Allura* e *Sacramento*.
3. **App Link / Deep Link (COM-04)**: O domínio padrão configurado via BuildConfig será `links.letssign.com.br`, com fallback para intent-filter customizado `letssign://documento/{id}` para testes locais via `adb`.

---
*Fim do Implementation Plan — FASE 0.*

# REGRAS PERMANENTES — LETSSIGN

## Fidelidade
- 38 telas com IDs estáveis: 6 COM, 13 ADM, 8 SIG, 11 MOD. Não renomeie, não funda, não crie telas, campos ou funcionalidades fora da Etapa 1.
- Sugestões S01–S10 (seção 13) NÃO são implementadas.
- Nomes de entidades e situações seguem a seção 10 (Usuario, AdminSignatario, Documento, VersaoDocumento, ParticipanteDocumento, CampoAssinatura, Assinatura, Recusa, Cancelamento, Envio, Notificacao, EventoHistorico).

## Stack
- Kotlin; UI 100% Jetpack Compose + Material 3 (tema claro, Roboto).
- Gradle Kotlin DSL + Version Catalog (gradle/libs.versions.toml), versões ESTÁVEIS mais recentes compatíveis com o Android Studio atual (nada alpha/beta).
- applicationId/namespace: br.com.letssign · minSdk 26 · compileSdk/targetSdk = API estável mais recente · JVM 17.
- MVVM em camadas UI → Domain (casos de uso) → Data (repositórios). Hilt + KSP (sem kapt).
- Room (com Flow, exportSchema = true) + DataStore. Sessão em DataStore criptografado com Android Keystore (NÃO usar EncryptedSharedPreferences, descontinuada).
- Navigation Compose com rotas type-safe (Kotlin Serialization). Coroutines/Flow; estado de tela em StateFlow<UiState> imutável.
- PDF: PdfRenderer (exibição) + PDFBox-Android (com.tom-roush:pdfbox-android) para validação e composição. Arquivos via Storage Access Framework.
- Testes: JUnit, kotlinx-coroutines-test, Turbine, Room in-memory, Compose UI Test.

## Estrutura (módulo único :app)
br.com.letssign
├── core/designsystem  tema, tokens, tipografia, formas, componentes, LogoPlaceholder
├── core/navigation    rotas, NavHost, shells de bottom navigation
├── core/common        utilitários (hash, datas, normalização de texto)
├── data/local         Room, DataStore, arquivos internos
├── data/remote        interfaces RemoteDataSource + FakeRemoteDataSource
├── data/repository    repositórios da seção 10.3
├── domain             modelos, casos de uso, máquinas de estado
└── feature/<área>     cada tela = Screen (stateless) + ViewModel (Hilt) + UiState
Cada rota contém o ID da tela (ex.: Sig03DetalheDocumentoRoute) e KDoc "/** SIG-03 — Detalhe do documento */".

## Servidor (RT05)
Não há backend nesta sprint. Toda dependência de servidor passa por interfaces *RemoteDataSource, implementadas por um FakeRemoteDataSource que simula o servidor no próprio aparelho, aplicando: e-mail único (P01/P03), idempotência por chaveIdempotencia (RF05), RN05, RN06, RN08, RN09 com normalização P04 e limite P05 (5 tentativas, bloqueio 15 min), RN10 (seção 1.1), P06, P09, P14 (PDF final). Atraso e falha configuráveis. Trocar o Fake por REST não pode exigir mudança em telas ou ViewModels.

## Design
- LetsSignColors com um campo por token do bloco "3. TOKENS DE COR" de docs/LetsSign_Prompts_FigmaMake.txt (camelCase: corPrimaria, corCampoAssinaturaVazio...). Telas usam só tokens; nunca Color(0x...) direto.
- Token ainda "[PREENCHER]": não invente nem sugira paleta. Use a cor baseline do Material 3 do papel equivalente só para compilar, marque "// TODO [PREENCHER] nome-do-token" e liste no walkthrough.
- Logo: LogoPlaceholder com borda tracejada, fundo corSuperficieVariante e texto "LOGO", tamanhos 160×160 (COM-01), 120×48 (COM-02, COM-03) e 32×32 dp (top app bar de ADM-01 e SIG-01), contentDescription "LOGO — [INSERIR POSTERIORMENTE]". Não desenhe nem troque por ícone; a logo real entrará depois sem mudar o layout.
- Grid 8 dp, margens 16 dp, toque ≥ 48 dp. Bottom navigation só nas telas raiz; oculta no wizard (ADM-04 a ADM-10), no fluxo de assinatura (SIG-04 → SIG-05 → MOD-10 → SIG-07) e em telas empilhadas.

## Textos
Toda microcopy em res/values/strings.xml (plurais e parâmetros %1$s), copiada LITERALMENTE da Etapa 1/Etapa 2. Nenhum texto fixo em Kotlin. Interface em português do Brasil; datas dd/MM/yyyy, horas HH:mm no fuso do aparelho; banco em UTC.

## Acessibilidade (WCAG 2.1 AA)
Nunca informação só por cor (texto + ícone); contentDescription em ícones de ação; alternativa sem gesto para arrastar, desenhar e reordenar; rótulos visíveis e erros junto ao campo anunciados ao TalkBack; layout funcional com fontScale 2.0.

## Segurança
Senha nunca em texto puro nem em log; RN07 aplicada no repositório (filtro pelo usuário da sessão); COM-04 nunca mostra dados do documento a quem não é o destinatário; sair (COM-06) limpa dados e PDFs do usuário; PDFs em files/documentos/{documentoId}/v{n}.pdf com SHA-256.

## Forma de trabalhar
- Trabalhe UMA fase por vez. Em cada fase: gere implementation_plan.md e a lista de tarefas, aguarde minha aprovação, implemente, valide e PARE.
- Fim de cada fase: ./gradlew assembleDebug, testDebugUnitTest e lintDebug verdes + walkthrough com o que foi feito, IDs de telas cobertos, regras atendidas, pendências e capturas do emulador.
- Um commit por fase: "fase N: resumo". Não altere o que foi feito em fases anteriores sem pedir. Nunca apague docs/.

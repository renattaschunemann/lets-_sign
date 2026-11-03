# LetsSign Mobile — Plataforma de Assinatura Eletrônica

Aplicativo nativo para **Android** desenvolvido em **Kotlin** e **Jetpack Compose** para a criação, posicionamento, disparo, assinatura e auditoria de documentos eletrônicos com validade jurídica, em conformidade com a MP nº 2.200-2/2001 e a Lei Federal nº 14.063/2020.

---

## 🛠️ Tecnologias e Arquitetura

- **Linguagem & Plataforma:** Kotlin 2.0+ nativo para Android (Min SDK 26, Target SDK 34).
- **Interface Declarativa:** Jetpack Compose com Material Design 3.
- **Identidade Visual:** Suporte nativo completo a **Tema Claro (Light Mode)** e **Tema Escuro (Dark Mode)**, com alternância dinâmica da logo institucional (`letssign-logo` para tema claro e `letssign-logo-branco` para tema escuro).
- **Arquitetura:** Clean Architecture (Domain, Data, Presentation) com injeção de dependência via **Hilt**.
- **Persistência Local (SSOT):** **Room Database (SQLite)** com versionamento e migração destrutiva segura em desenvolvimento.
- **Persistência Híbrida de PDFs:**
  1. *Sistema de Arquivos:* Gravação física no `filesDir` com registro de metadados no SQLite (`PdfStorageManager.salvarNoSistemaDeArquivos`).
  2. *Armazenamento Direto no Banco (BLOB):* Campo `@ColumnInfo(typeAffinity = ColumnInfo.BLOB) val conteudoBlob: ByteArray?` em `VersaoDocumentoEntity`.
- **Preparado para APIs REST:** Repositórios estruturados com injeção de `RemoteDataSource? = null`, garantindo que futuras integrações de rede não quebrem o funcionamento local.
- **Segurança Criptográfica:** Criptografia AES-GCM via `AndroidKeyStore`, integridade por hash **SHA-256** e proteção contra duplicidade por chave de idempotência (`UUID`).

---

## 👥 Perfis de Acesso e Credenciais de Demonstração

O aplicativo conta com atalhos de preenchimento rápido (1 toque) na tela de login para facilitar os testes:

### 1. Administrador (Gestor de Contratos)
- **E-mail:** `carla@empresa.com.br`
- **Senha:** `admin123`
- **Responsabilidades:** Cadastrar/editar signatários, criar novos fluxos de documentos, selecionar PDF, classificar papéis, posicionar campos de assinatura, disparar convites e acompanhar a auditoria.

### 2. Signatário (Assinante)
- **E-mail:** `joao.lima@email.com`
- **Senha:** `signatario123`
- **Responsabilidades:** Consultar documentos pendentes/assinados, ler as cláusulas do PDF, desenhar assinatura na tela (canvas touch) ou escolher estilo cursivo tipográfico, concordar com os termos de integridade e assinar.

---

## 🚀 Funcionalidades Principais (Sprint 1)

### 1. Cadastro e Gestão de Signatários (ADM-02 e ADM-03)
- Listagem, busca e filtro de signatários ativos.
- Cadastro de novo signatário com validação de campos obrigatórios:
  - **Nome Completo**
  - **E-mail** (credencial de login e envio)
  - **Telefone**
  - **Senha** (credencial de assinatura)
- Edição de signatários existentes sem quebra de snapshot histórico em contratos já criados.

### 2. Assistente de Cadastro de Documento (ADM-04 a ADM-10)
- **Etapa 1:** Definição do título do documento.
- **Etapa 2:** Seleção de arquivo PDF via SAF (*Storage Access Framework*) com validação de integridade, cálculo do hash SHA-256 e contagem real de páginas via `PdfRenderer`. Tratamento para arquivos corrompidos ou com senha com opção de recarga.
- **Etapa 3:** Seleção de múltiplos signatários (mínimo obrigatório de 1 signatário).
- **Etapa 4:** Definição de obrigatoriedade de assinatura e classificação de papéis oficiais:
  - *Contratante, Contratado, Comprador, Vendedor, Locador, Locatário, Representante Legal, Procurador, Interveniente Anuente, Fiador, Avalista, Devedor Solidário, Testemunha, Aprovador e Notário*.
- **Etapa 5:** Configuração do fluxo de assinatura:
  - **Paralelo:** Todos os participantes recebem o convite simultaneamente.
  - **Sequencial:** Ordem estrita com reordenação interativa (1º, 2º, 3º...).

### 3. Posicionamento Visual de Assinaturas (ADM-09)
- **Navegação de Páginas:** Visualizador de PDF com folheamento (`pág. X de Y`) e controles de zoom.
- **Arrasto Interativo (Drag & Drop):** Toque e arraste do campo de assinatura diretamente sobre a página do PDF em tempo real.
- **Mover e Excluir:** Painel de controle para mudar de página ou excluir campos antes do envio.
- **Múltiplos Campos por Signatário:** Possibilidade de posicionar mais de um campo para a mesma pessoa com papéis diferentes (ex.: *João Lima como Locador na Pág. 1 e como Fiador na Pág. 2*).
- **Requisito Técnico de Coordenadas Normalizadas:** As coordenadas (`normX`, `normY`, `normLargura`, `normAltura`) são salvas como frações relativas (`0.0` a `1.0`), garantindo posicionamento responsivo e idêntico em qualquer tela, densidade ou resolução.
- **Limites da Página:** Travamento automático para impedir que o campo transborde as margens da folha.

### 4. Revisão Geral e Disparo Idempotente (ADM-10)
- **Prévia do Documento:** Visualização final do PDF com os campos posicionados e dimensionados em cada página.
- **Lista de Signatários:** Resumo dos participantes convocados, papéis e detalhamento dos campos.
- **Mensagem de Convite:** Campo de mensagem personalizada enviada por e-mail e push aos signatários.
- **Tratamento de Falhas e Idempotência:** Em caso de oscilação de rede, o documento é mantido como `FALHA_ENVIO` com orientação ao usuário. O botão **"Tentar novamente"** reutiliza a mesma `chaveIdempotencia`, impedindo duplicações.

### 5. Assinatura Eletrônica pelo Signatário (SIG-01 a SIG-08)
- Lista de documentos pendentes e concluídos.
- Visualização do PDF e navegação guiada entre os campos atribuídos.
- **Captura da Assinatura:**
  - *Modo Desenhar:* Canvas interativo com sensibilidade ao toque e limpeza.
  - *Modo Digitar:* Escolha de estilos caligráficos pré-definidos.
- Termos de aceite e carimbo de data/hora UTC e IP do dispositivo.
- Atualização automática de status:
  - Se faltam outros obrigatórios: `PARCIALMENTE_ASSINADO`
  - Quando o último obrigatório assina: `CONCLUIDO` (com registro do hash SHA-256 final e carimbo ICP-Brasil).

### 6. Acompanhamento e Trilha de Auditoria (ADM-11 a ADM-13)
- **Situação Geral:** Selo visual de status (`CONCLUIDO`, `ENVIADO`, `PARCIALMENTE_ASSINADO`, `RECUSADO`, `CANCELADO`).
- **Situação Individual:** Status em tempo real de cada participante com resumo de campos.
- **Trilha de Auditoria Completa:** Linha do tempo cronológica com data e hora de criação, upload, envio, assinaturas individuais, cancelamentos e recusas.
- **Download do Documento Final:** Botão de exportação e compartilhamento do PDF concluído ou original.

---

## 🧪 Como Executar e Testar a Aplicação

### Pré-requisitos
- **Android Studio** Hedgehog (2023.1.1) ou superior / Ladybug.
- **JDK 17** ou superior configurado no ambiente.
- Dispositivo físico Android (com Depuração USB ativada) ou Emulador Android (API 26 ou superior).

### 1. Clonar o Repositório
```bash
git clone https://github.com/dantyrocha/lets-_sign.git
cd lets-_sign
```

### 2. Executar os Testes Unitários Automatizados
Para rodar a suíte completa de testes de regras de negócio, persistência, criptografia e fluxos end-to-end:
```bash
./gradlew test
```
*No Windows (PowerShell / CMD):*
```powershell
.\gradlew.bat test
```

Os testes cobrem:
- `CamposEnvioTest.kt`: Posicionamento de múltiplos campos, papéis múltiplos por signatário, coordenadas normalizadas e envio com retentativa idempotente sem duplicações.
- `AcompanhamentoAuditoriaTest.kt`: Consulta completa de status, auditoria cronológica e validação do documento final.
- `AssinaturaEConclusaoTest.kt`: Fluxo de assinatura pelo signatário e transição de estado para concluído.
- `DesignSystemTest.kt`: Conformidade com paleta de cores (34 tokens) e suporte aos temas Claro e Escuro.
- `CryptoManagerTest.kt`: Criptografia de credenciais e geração de hashes SHA-256.

### 3. Executar o Aplicativo no Dispositivo / Emulador
Abra o projeto no **Android Studio**, aguarde a sincronização do Gradle e clique em **Run 'app'** (`Shift + F10`).

---

## 📱 Roteiro de Teste Manual Recomendado

1. **Testar Tema Claro e Escuro:**
   - Alterne o tema do sistema operacional Android entre Claro e Escuro.
   - Observe a mudança dinâmica das cores e da logo do LetsSign (logo escura no tema claro e logo branca no tema escuro).
2. **Entrar como Administrador:**
   - Na tela inicial, toque no botão rápido de login como Administrador (`Carla Mendes`).
3. **Cadastrar Signatário:**
   - Acesse o menu Signatários (`ADM-02`), toque em `+ Novo signatário`, preencha os dados e salve.
4. **Criar Novo Documento para Assinatura:**
   - No Dashboard, toque em `Novo Documento`.
   - Preencha o título (ex.: *Contrato de Locação Residencial*).
   - Selecione um arquivo PDF de teste.
   - Selecione signatários (ex.: *João Lima* e mais um signatário).
   - Defina os papéis oficiais (ex.: *Locador*, *Fiador*, *Testemunha*).
   - Escolha o modo de envio (Paralelo ou Sequencial).
5. **Posicionar Assinaturas:**
   - Toque em `+ Adicionar campo`.
   - Arraste a caixa de assinatura sobre a folha do PDF.
   - Toque em `Atribuir / Papel` e selecione *João Lima* como *Locador*.
   - Adicione outro campo para *João Lima* e defina o papel como *Fiador*.
   - Alterne entre páginas e verifique que os campos mantêm suas posições relativas.
6. **Revisar e Disparar:**
   - Avance para a tela de revisão, verifique a prévia do documento com os campos posicionados e a lista de signatários.
   - Preencha a mensagem de convite e clique em **Enviar para assinatura**.
7. **Assinar como Signatário:**
   - Faça logout e entre como Signatário (`João Lima`).
   - Acesse o documento pendente, revise o PDF e desenhe sua assinatura no canvas.
   - Confirme a assinatura.
8. **Acompanhar e Auditar:**
   - Retorne com o perfil Administrador, acesse o documento no menu Acompanhamento e confira a situação individual, o progresso percentual, as datas/horários dos eventos e a trilha de auditoria completa.


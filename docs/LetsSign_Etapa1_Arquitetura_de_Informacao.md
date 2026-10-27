# LETSSIGN — ETAPA 1: ARQUITETURA DE INFORMAÇÃO, REGRAS DE NEGÓCIO E MODELO DE DADOS

---

## 1. VISÃO GERAL DO PRODUTO
O **LetsSign** é um aplicativo Android nativo para assinatura digital de documentos eletrônicos com dois perfis de acesso:
1. **Admin (Cliente)**: cria documentos, define signatários, obrigatoriedade, ordem de assinatura, posiciona campos de assinatura no PDF, envia convites e acompanha o ciclo de vida.
2. **Signatário**: visualiza documentos pendentes, confirma identidade, desenha ou digita sua assinatura, concorda com os termos e acompanha o status de seus documentos.

---

## 2. TELAS DO SISTEMA (38 TELAS COM IDS ESTÁVEIS)

### 2.1 Telas Comuns (6 telas — COM)
- **COM-01 — Splash e verificação de sessão**: Carregamento inicial, validação de sessão ativa/biometria e resolução de links pendentes.
- **COM-02 — Login**: Autenticação com e-mail e senha, autofill, bloqueio de tentativas e suporte a links pendentes.
- **COM-03 — Cadastro inicial do Admin**: Criação de conta do cliente Admin com validação de senha forte e termos de uso.
- **COM-04 — Entrada por link**: Resolução de App Links/Deep Links para acesso direto a documentos, com validação de credencial e integridade.
- **COM-05 — Central de notificações**: Lista cronológica de notificações e alertas em tempo real (versão Admin e Signatário).
- **COM-06 — Minha conta**: Dados do perfil logado, versão do aplicativo e encerramento seguro de sessão com limpeza de dados locais.

### 2.2 Telas do Administrador (13 telas — ADM)
- **ADM-01 — Início / Painel de documentos**: Dashboard com resumo de contadores, busca, filtros por situação, lista de documentos e FAB de novo documento.
- **ADM-02 — Signatários**: Gestão da base de signatários cadastrados pelo Admin, busca por nome/e-mail e lista de contatos.
- **ADM-03 — Cadastro/edição de signatário**: Formulário com validação de nome, e-mail, telefone, data de nascimento e credencial de acesso.
- **ADM-04 — Novo documento — Título (Wizard Etapa 1–2)**: Definição do título do documento com autosave e indicação de nova versão.
- **ADM-05 — Selecionar PDF (Wizard Etapa 3)**: Importação de arquivo PDF via SAF, validação de formato/tamanho/páginas e geração de SHA-256.
- **ADM-06 — Selecionar signatários (Wizard Etapa 4)**: Associação de signatários ao documento e atribuição de papéis (sem duplicidade).
- **ADM-07 — Definir obrigatoriedade (Wizard Etapa 5)**: Configuração de signatários obrigatórios e opcionais (mínimo 1 obrigatório).
- **ADM-08 — Definir ordem de assinatura (Wizard Etapa 6)**: Configuração de ordem Sequencial (reordenável por drag ou botões) ou Paralela.
- **ADM-09 — Visualizar páginas e posicionar campos (Wizard Etapas 7–8)**: PdfRenderer com posicionamento de caixas de assinatura em coordenadas normalizadas (0–1).
- **ADM-10 — Revisão e confirmação de envio (Wizard Etapa 9)**: Revisão consolidada de todas as etapas, mensagem personalizada de convite e congelamento de dados.
- **ADM-11 — Acompanhamento do documento**: Visão detalhada do status de coleta de assinaturas, progresso de obrigatórios, vez de assinatura e ações de ciclo de vida.
- **ADM-12 — Histórico do documento**: Trilha de auditoria cronológica completa com filtros por tipo de evento e reversão de ordenação.
- **ADM-13 — Visualizador de documento (Admin)**: Visualização em modo leitura do documento enviado ou finalizado com folha de registro e compartilhamento.

### 2.3 Telas do Signatário (8 telas — SIG)
- **SIG-01 — Início / Pendentes**: Dashboard do signatário com contador sonoro/visual de pendências, itens prontos para assinar e itens aguardando vez.
- **SIG-02 — Meus documentos**: Histórico e arquivo pessoal de documentos assinados, recusados e encerrados com filtros por status.
- **SIG-03 — Detalhe do documento**: Resumo do documento recebido, remetente, mensagem, número de campos e ações de decisão (Assinar ou Recusar).
- **SIG-04 — Confirmação de identidade**: Validação de dados cadastrais (nome, e-mail, data de nascimento) sem pré-preenchimento, com controle de 5 tentativas e bloqueio de 15 min.
- **SIG-05 — Assinar documento**: Visualizador interativo guiado com contagem de campos pendentes e navegação entre marcações.
- **SIG-06 — Criar assinatura**: Interface de desenho em Canvas (com suporte a landscape e limpar) ou escolha tipográfica cursiva (4 fontes).
- **SIG-07 — Assinatura registrada**: Tela de confirmação e sucesso após registro idempotente da assinatura, informando pendências restantes.
- **SIG-08 — Visualizador somente leitura**: Leitura do documento com as assinaturas já apostas ou aguardando conclusão.

### 2.4 Diálogos, Folhas e Modais (11 telas — MOD)
- **MOD-01 — Resultado do envio**: Feedback modal de sucesso, erro (com retry sob mesma chave) ou sucesso parcial de notificações (SMS/e-mail).
- **MOD-02 — Excluir/alterar participante**: Diálogo transacional para remoção ou alteração de participante com campos já mapeados, permitindo exclusão em cascata ou reatribuição.
- **MOD-03 — Selecionar signatário do campo**: Bottom sheet para vincular um signatário a uma nova caixa de assinatura no visualizador.
- **MOD-04 — Sair do assistente**: Confirmação ao sair do wizard, oferecendo salvar rascunho, descartar ou continuar editando.
- **MOD-05 — Retomar rascunho**: Diálogo que detecta rascunho pendente e permite retomar na etapa exata ou descartar.
- **MOD-06 — Erro de arquivo PDF**: Modal explicativo em caso de arquivo inválido, corrompido, protegido por senha ou superior a 20 MB / 100 páginas.
- **MOD-07 — Confirmar cancelamento**: Diálogo seguro com exibição de consequências, caixa de confirmação explícita e validação de senha do Admin.
- **MOD-08 — Recusar assinatura**: Diálogo seguro do signatário com justificativa opcional, consequências diferenciadas (obrigatório vs opcional) e confirmação por senha.
- **MOD-09 — Detalhe da recusa**: Visualização da justificativa e metadados de recusa de um signatário para o Admin.
- **MOD-10 — Confirmar assinatura**: Confirmação final do signatário com aceite formal dos termos ("Li e concordo") antes da gravação irreversível.
- **MOD-11 — Criar nova versão**: Diálogo para clonagem de documento recusado/cancelado gerando versão n+1 com reaproveitamento de campos e configurações.

---

## 3. REGRAS FUNCIONAIS (RF01 a RF09)
- **RF01 — Criação e Gestão de Documentos**: O Admin cria documentos importando arquivos PDF via SAF, definindo metadados, signatários e configurações de fluxo.
- **RF02 — Cadastro e Associação de Signatários**: O Admin cadastra signatários na sua carteira e os vincula a documentos específicos, sem permitir e-mails duplicados em um mesmo documento.
- **RF03 — Definição de Obrigatoriedade e Papéis**: Cada signatário recebe um papel (Contratante, Contratado, Testemunha, Interveniente, etc.) e um status de obrigatoriedade. Todo documento exige ao menos 1 signatário obrigatório.
- **RF04 — Configuração da Ordem de Assinatura**: O fluxo pode ser Sequencial (com ordem estrita 1, 2, 3...) ou Paralelo (todos liberados simultaneamente).
- **RF05 — Posicionamento de Campos e Envio Idempotente**: Posicionamento de retângulos de assinatura com coordenadas normalizadas. O envio utiliza chave única de idempotência (`chaveIdempotencia`) para prevenir duplicidades.
- **RF06 — Confirmação de Identidade do Signatário**: Antes de assinar, o signatário deve preencher seus dados cadastrais (nome, e-mail, data de nascimento) para conferência estrita.
- **RF07 — Assinatura Digital (Desenhar / Digitar)**: O signatário cria sua representação gráfica desenhando no canvas ou digitando seu nome com seleção de fonte cursiva estilizada. A mesma representação é aplicada em todos os seus campos.
- **RF08 — Gestão de Recusa e Cancelamento**: Participantes podem recusar a assinatura (com justificativa); o Admin pode cancelar documentos em andamento mediante senha.
- **RF09 — Trilha de Auditoria e Composição do PDF Final**: Registro imutável de todos os eventos com data/hora UTC, autor, IP/dispositivo e geração do PDF final com folha de registro e hash SHA-256.

---

## 4. REGRAS DE NEGÓCIO (RN01 a RN11)
- **RN01 — Unicidade de Signatários no Documento**: O mesmo signatário não pode ser adicionado mais de uma vez na mesma versão do documento.
- **RN02 — Mínimo de Signatários Obrigatórios**: Pelo menos 1 participante deve ser marcado como Obrigatório para que o envio seja autorizado.
- **RN03 — Delimitação dos Campos de Assinatura**: Todos os campos de assinatura devem estar contidos integralmente dentro das dimensões da página do PDF (0.0 <= x, y, x+w, y+h <= 1.0).
- **RN04 — Pelo Menos Um Campo por Signatário**: Todos os signatários associados ao documento devem possuir ao menos um campo de assinatura posicionado no PDF antes do envio.
- **RN05 — Idempotência de Envio**: Tentativas repetidas de envio com a mesma `chaveIdempotencia` retornam a resposta original congelada sem criar duplicatas.
- **RN06 — Sequenciamento Estrito de Assinaturas**: Em fluxos sequenciais, apenas o participante da vez (`ordem = N`) tem seu convite ativado e permissão de assinatura; os demais permanecem em "Aguardando vez".
- **RN07 — Isolamento de Dados por Usuário (Multi-tenant Local)**: Repositórios e consultas Room devem obrigatoriamente filtrar os dados pelo ID do usuário autenticado na sessão.
- **RN08 — Revalidação no Ato da Assinatura**: Ao confirmar a assinatura no MOD-10, o sistema deve revalidar atomicamente se o documento não foi cancelado ou recusado concorrentemente.
- **RN09 — Regras de Recusa (Obrigatório vs Opcional)**:
  - Recusa de participante **Obrigatório**: encerra o documento imediatamente, marcando-o como "Recusado".
  - Recusa de participante **Opcional**: registra o evento, marca o participante como "Recusado" e libera a vez para o próximo da fila; o documento permanece em andamento.
- **RN10 — Conclusão de Documento**: Quando todos os participantes obrigatórios assinam, o documento transiciona para "Concluído" e os participantes opcionais que ainda não assinaram têm seus fluxos encerrados com status "Encerrado".
- **RN11 — Imutabilidade Pós-Envio**: Uma versão enviada nunca pode ter seus metadados, participantes, PDF ou campos alterados. Alterações exigem a criação de uma versão n+1 ("Substituído").

---

## 5. REGRAS TÉCNICAS E DE PLATAFORMA (RT01 a RT05)
- **RT01 — Arquitetura Recomendada pelo Google**: Clean Architecture + MVVM unidirecional com UI -> Domain (Use Cases) -> Data (Repositories).
- **RT02 — Criptografia e Armazenamento Seguro**: Sessão e tokens armazenados em DataStore criptografado com chaves gerenciadas pelo Android Keystore.
- **RT03 — Navegação Type-Safe**: Navigation Compose utilizando classes/objetos Kotlin Serialization com rotas explícitas.
- **RT04 — Manipulação e Renderização de PDF**: Exibição eficiente com `android.graphics.pdf.PdfRenderer` (cache LRU em memória) e composição/carimbo com `com.tom-roush:pdfbox-android`.
- **RT05 — Simulação de Servidor Remoto (FakeRemoteDataSource)**: Camada de rede implementada localmente emulando latência, falhas de conexão, persistência mockada e regras de integridade sem acoplamento.

---

## 6. PARÂMETROS E LIMITES DO SISTEMA (P01 a P16)
- **P01 — Unicidade de E-mail de Usuário**: E-mails de login devem ser únicos globalmente no sistema.
- **P02 — Complexidade de Senha**: Mínimo de 8 caracteres contendo letras e números.
- **P03 — Unicidade de Signatário por Admin**: Um Admin não pode cadastrar dois signatários com o mesmo e-mail em sua carteira.
- **P04 — Normalização de Texto**: Comparação de dados na confirmação de identidade (nome, e-mail) com remoção de espaços extras, caixa baixa e remoção de acentos.
- **P05 — Limite de Tentativas de Identidade**: Máximo de 5 tentativas divergentes consecutivas. Ao atingir o limite, o signatário é bloqueado por 15 minutos.
- **P06 — Chave de Idempotência**: UUID v4 gerado uma única vez por versão de documento no momento do primeiro disparo de envio.
- **P07 — Alerta de Reordenação**: Avisar o usuário se a reordenação de signatários alterar a precedência de participantes com campos já posicionados.
- **P08 — Coordenadas Normalizadas**: Todas as posições (x, y, largura, altura) gravadas no banco na escala 0.0 a 1.0 relativa ao tamanho da página.
- **P09 — Confirmação Segura por Senha**: Ações destrutivas (cancelamento e recusa) exigem digitação da senha do usuário logado e marcação explícita de checkbox.
- **P10 — Limites de Arquivo PDF**: Tamanho máximo de 20 MB e limite de 100 páginas por documento.
- **P11 — Cache de Miniaturas e Páginas**: Cache LRU em memória para bitmaps gerados pelo `PdfRenderer`.
- **P12 — Unicidade de Traço da Assinatura**: A mesma assinatura capturada (desenhada ou tipográfica) é replicada em todos os campos do signatário no documento.
- **P13 — Formato da Assinatura Capturada**: PNG com fundo transparente salvo internamente no armazenamento privado do aplicativo.
- **P14 — Hash de Integridade**: Cálculo do SHA-256 do PDF original e do PDF final após adição da folha de registro e carimbos.
- **P15 — Armazenamento de Arquivos**: PDFs armazenados estritamente no diretório interno seguro: `files/documentos/{documentoId}/v{versao}.pdf`.
- **P16 — Autosave do Wizard**: Cada avanço de etapa no wizard do Admin realiza persistência imediata no banco local Room em status de Rascunho.

---

## 7. MÁQUINAS DE ESTADO (SEÇÃO 8)

### 7.1 Máquina de Estados do Documento
```
+-------------+
|  RASCUNHO   |
+------+------+
       | (Confirmar envio)
       v
+-------------+      (Falha no envio)      +------------------+
|  ENVIANDO   | -------------------------> | FALHA NO ENVIO   |
+------+------+                            +--------+---------+
       |                                            | (Tentar novamente)
       | (Sucesso)                                  v
       v                                   +------------------+
+-------------+ <--------------------------+    ENVIANDO      |
|   ENVIADO   |
+------+------+
       |
       +------------------------------------+------------------------------------+
       | (1ª assinatura de obrigatório)     | (Recusa de obrigatório)            | (Admin cancela)
       v                                    v                                    v
+-------------------------+        +------------------+                 +------------------+
|  PARCIALMENTE ASSINADO  |        |     RECUSADO     |                 |    CANCELADO     |
+------------+------------+        +------------------+                 +------------------+
             |                                                                    ^
             +------------------------------+-------------------------------------+
             |                              | (Admin cancela)
             | (Todos obrigatórios          v
             |  assinaram)         +------------------+
             v                     |   SUBSTITUÍDO    | (Quando nova versão enviada)
+-------------------------+        +------------------+
|        CONCLUÍDO        |
+-------------------------+
```

### 7.2 Máquina de Estados do Participante
```
+--------------------+
|   AGUARDANDO VEZ   | (Em fluxo sequencial, enquanto anterior não assina)
+---------+----------+
          | (Vez liberada pelo anterior ou fluxo paralelo)
          v
+--------------------+
|      PENDENTE      |
+---------+----------+
          |
          +-----------------------+-----------------------+
          | (Assinou com sucesso) | (Recusou assinatura)  | (Doc concluído ou cancelado)
          v                       v                       v
+--------------------+   +--------------------+   +--------------------+
|      ASSINADO      |   |      RECUSADO      |   |     ENCERRADO      |
+--------------------+   +--------------------+   +--------------------+
```

---

## 8. CATÁLOGO DE EXCEÇÕES (SEÇÃO 9: E01 a E30)
- **E01**: Falha de conexão de rede durante autenticação ou carregamento de dados.
- **E02**: Credenciais inválidas (e-mail ou senha incorretos) no login.
- **E03**: Limite de tentativas de login excedido (bloqueio temporário).
- **E04**: Senha não atende aos requisitos mínimos de complexidade.
- **E05**: Senhas não conferem no cadastro ou alteração.
- **E06**: E-mail já cadastrado no sistema (conflito de e-mail).
- **E07**: Link de documento inválido ou malformado.
- **E08**: Link de documento expirado.
- **E09**: Link de documento pertence a outra conta de signatário.
- **E10**: Documento acessado via link já foi cancelado pelo remetente.
- **E11**: Documento acessado via link já foi assinado pelo participante.
- **E12**: Arquivo selecionado não é um documento PDF válido.
- **E13**: Arquivo PDF corrompido ou ilegível.
- **E14**: Arquivo PDF protegido por senha ou criptografia.
- **E15**: Arquivo PDF excede o limite de tamanho (20 MB).
- **E16**: Arquivo PDF excede o limite de páginas (100 páginas).
- **E17**: Espaço em disco insuficiente no dispositivo para importação ou cópia do PDF.
- **E18**: Tentativa de adicionar signatário duplicado ao documento.
- **E19**: Tentativa de avançar wizard sem nenhum signatário obrigatório.
- **E20**: Campo de assinatura posicionado fora dos limites da página.
- **E21**: Tentativa de enviar documento com signatário sem nenhum campo atribuído.
- **E22**: Falha na comunicação durante o disparo de envio do documento.
- **E23**: Dados divergentes na confirmação de identidade do signatário.
- **E24**: Limite de tentativas de confirmação de identidade excedido (bloqueio de 15 minutos).
- **E25**: Documento cancelado concorrentemente durante o processo de assinatura.
- **E26**: Documento recusado concorrentemente por signatário obrigatório durante assinatura.
- **E27**: Senha incorreta ao confirmar ação de cancelamento pelo Admin.
- **E28**: Senha incorreta ao confirmar ação de recusa pelo Signatário.
- **E29**: Falha na renderização de página com `PdfRenderer`.
- **E30**: Falha na composição do PDF final ou na gravação da folha de registro.

---

## 9. MODELO DE DADOS (SEÇÃO 10)

### 9.1 Entidades Room Principais
1. **UsuarioEntity**: `id`, `nome`, `email`, `telefone`, `perfil` (ADMIN | SIGNATARIO), `senhaHash`, `dataCriacao`.
2. **AdminSignatarioEntity**: `adminId`, `signatarioId`, `nome`, `email`, `telefone`, `dataNascimento`, `dataVinculo`.
3. **DocumentoEntity**: `id`, `usuarioAdminId`, `titulo`, `dataCriacao`, `dataUltimaAtualizacao`, `situacaoAtual` (RASCUNHO, ENVIADO, PARCIALMENTE_ASSINADO, CONCLUIDO, RECUSADO, CANCELADO, SUBSTITUIDO, FALHA_ENVIO).
4. **VersaoDocumentoEntity**: `id`, `documentoId`, `numeroVersao`, `pdfPathInterno`, `sha256Original`, `sha256Final`, `tipoOrdem` (SEQUENCIAL | PARALELA), `mensagemConvite`, `chaveIdempotencia`, `etapaWizard`, `situacao`.
5. **ParticipanteDocumentoEntity**: `id`, `versaoDocumentoId`, `usuarioId`, `nomeSnapshot`, `emailSnapshot`, `telefoneSnapshot`, `dataNascimentoSnapshot`, `papel`, `obrigatorio` (Boolean), `ordem`, `situacao` (AGUARDANDO_VEZ, PENDENTE, ASSINADO, RECUSADO, ENCERRADO).
6. **CampoAssinaturaEntity**: `id`, `versaoDocumentoId`, `participanteId`, `numeroPagina`, `normX`, `normY`, `normLargura`, `normAltura`.
7. **AssinaturaEntity**: `id`, `campoAssinaturaId`, `participanteId`, `tipoAssinatura` (DESENHADA | DIGITADA), `caminhoPngAssinatura`, `dataHoraUtc`, `ipDispositivo`, `hashValidador`.
8. **RecusaEntity**: `id`, `participanteId`, `dataHoraUtc`, `observacao`, `senhaConfirmada`.
9. **CancelamentoEntity**: `id`, `versaoDocumentoId`, `adminId`, `dataHoraUtc`, `motivo`, `senhaConfirmada`.
10. **EnvioEntity**: `id`, `versaoDocumentoId`, `chaveIdempotencia`, `dataHoraUtc`, `statusEnvio`, `respostaServidorJson`.
11. **NotificacaoEntity**: `id`, `usuarioId`, `documentoId`, `titulo`, `mensagem`, `tipoNotificacao`, `dataHoraUtc`, `lida` (Boolean).
12. **EventoHistoricoEntity**: `id`, `versaoDocumentoId`, `dataHoraUtc`, `tipoEvento`, `autorNome`, `detalhe`. *(Apenas inserção, append-only)*.

### 9.2 Repositórios (Seção 10.3)
- `UsuarioRepository`: Autenticação, sessão e carteira de signatários do Admin.
- `DocumentoRepository`: Ciclo de vida de documentos, versões, filtros e busca com filtro estrito de `usuarioAdminId` (RN07).
- `WizardDocumentoRepository`: Gerenciamento do estado transacional do wizard de criação (etapas 1 a 9).
- `SignatarioDocumentoRepository`: Acesso aos documentos atribuídos ao signatário logado, confirmação de identidade e submissão de assinaturas.
- `NotificacaoRepository`: Gerenciamento das notificações e contadores de não lidas.
- `HistoricoRepository`: Leitura e inserção imutável da trilha de auditoria.

---

## 10. REGRAS DO SERVIDOR SIMULADO (SEÇÃO 11 / RT05)
O `FakeRemoteDataSource` atua como servidor local em memória/Room isolado:
- Mantém latência configurável (ex.: 500 ms) e simulação de falha de rede sob flag.
- Valida unicidade global de e-mails em tempo de cadastro.
- Aplica a idempotência estrita via `chaveIdempotencia`: requisições repetidas retornam o resultado persistido originalmente.
- Executa a validação de identidade (P04) com normalização de strings e contagem de tentativas (P05).
- Despacha notificações simuladas para Admin e Signatários conforme as transições da máquina de estados.
- Realiza a composição do PDF final via PDFBox-Android com a folha de registro e carimbos de assinatura no fechamento do documento.

---
*Fim da Fonte Única da Verdade — LetsSign Etapa 1.*

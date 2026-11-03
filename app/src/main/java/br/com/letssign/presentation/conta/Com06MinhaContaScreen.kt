package br.com.letssign.presentation.conta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.common.transformation.CpfVisualTransformation
import br.com.letssign.core.common.transformation.DataNascimentoVisualTransformation
import br.com.letssign.core.common.transformation.TelefoneVisualTransformation
import br.com.letssign.core.common.validation.CadastroValidator
import br.com.letssign.core.designsystem.components.base.BannerInformativo
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.base.DialogBasico
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.base.TipoBanner
import br.com.letssign.core.designsystem.theme.FormaExtraGrande
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.shells.AdminBottomBar
import br.com.letssign.core.navigation.shells.DestinoAdmin
import br.com.letssign.core.navigation.shells.DestinoSignatario
import br.com.letssign.core.navigation.shells.SignatarioBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Com06MinhaContaScreen(
    aoSairParaLogin: () -> Unit,
    aoNavegarBottomNavAdmin: (DestinoAdmin) -> Unit,
    aoNavegarBottomNavSignatario: (DestinoSignatario) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Com06MinhaContaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ehAdmin = uiState.perfil.equals("ADMIN", ignoreCase = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val naoInformado = stringResource(R.string.com06_nao_informado)

    LaunchedEffect(uiState.logoutConcluido) {
        if (uiState.logoutConcluido) {
            aoSairParaLogin()
        }
    }

    LaunchedEffect(uiState.mensagemSucesso) {
        uiState.mensagemSucesso?.let { mensagem ->
            snackbarHostState.showSnackbar(mensagem)
            viewModel.aoLimparMensagemSucesso()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        br.com.letssign.core.designsystem.components.logo.LogoPlaceholder(
                            tamanho = br.com.letssign.core.designsystem.components.logo.LogoSize.PEQUENO
                        )
                        Text(
                            text = stringResource(R.string.com06_titulo),
                            style = MaterialTheme.typography.titleLarge,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            if (ehAdmin) {
                AdminBottomBar(
                    destinoAtual = DestinoAdmin.CONTA,
                    aoNavegarPara = aoNavegarBottomNavAdmin
                )
            } else {
                SignatarioBottomBar(
                    destinoAtual = DestinoSignatario.CONTA,
                    aoNavegarPara = aoNavegarBottomNavSignatario
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = LetsSignTheme.colors.corFundo
    ) { paddingVal ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVal)
        ) {
            if (uiState.carregando || uiState.saindo) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IndicadorCarregamentoCircular()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Avatar grande com iniciais
                    val iniciais = uiState.nome
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                LetsSignTheme.colors.corPrimariaContainer,
                                shape = FormaTotal
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciais.ifBlank { "U" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = LetsSignTheme.colors.corSobrePrimariaContainer
                        )
                    }

                    Text(
                        text = uiState.nome,
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )

                    // 1. Cartão Completo de Dados Cadastrais Pessoais
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FormaMedia,
                        colors = CardDefaults.cardColors(
                            containerColor = LetsSignTheme.colors.corSuperficie
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.com06_secao_dados_cadastrais),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                IconButton(onClick = viewModel::aoAbrirEdicaoConta) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.com06_btn_editar_conta),
                                        tint = LetsSignTheme.colors.corPrimaria
                                    )
                                }
                            }
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Nome
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_nome),
                                valor = uiState.nome.ifBlank { naoInformado }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // CPF
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_cpf),
                                valor = if (uiState.cpf.isNotBlank()) CadastroValidator.formatarCpf(uiState.cpf) else naoInformado
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Data de Nascimento
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_data_nascimento),
                                valor = if (uiState.dataNascimento.isNotBlank()) CadastroValidator.formatarDataNascimento(uiState.dataNascimento) else naoInformado
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // E-mail
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_email),
                                valor = uiState.email.ifBlank { naoInformado }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Telefone
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_telefone),
                                valor = if (uiState.telefone.isNotBlank()) CadastroValidator.formatarTelefone(uiState.telefone) else naoInformado
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Profissão
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_profissao),
                                valor = uiState.profissao.ifBlank { naoInformado }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Endereço
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_endereco),
                                valor = uiState.endereco.ifBlank { naoInformado }
                            )
                        }
                    }

                    // 2. Cartão de Informações da Conta e Acesso
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FormaMedia,
                        colors = CardDefaults.cardColors(
                            containerColor = LetsSignTheme.colors.corSuperficie
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.com06_secao_dados_conta),
                                style = MaterialTheme.typography.titleMedium,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Perfil
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_perfil),
                                valor = if (ehAdmin) stringResource(R.string.com06_perfil_admin)
                                else stringResource(R.string.com06_perfil_signatario)
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Identificador
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_id),
                                valor = uiState.usuarioId.ifBlank { naoInformado }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Data de Cadastro
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_data_cadastro),
                                valor = uiState.dataCriacao.ifBlank { naoInformado }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Status da Conta
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_status),
                                valor = uiState.statusConta.ifBlank { stringResource(R.string.com06_status_ativo) }
                            )
                            HorizontalDivider(color = LetsSignTheme.colors.corBorda.copy(alpha = 0.5f))

                            // Versão do Aplicativo
                            ItemDadoConta(
                                rotulo = stringResource(R.string.com06_label_versao),
                                valor = uiState.versaoApp
                            )
                        }
                    }

                    // Opção de editar a conta visível diretamente tanto para admin quanto para signatário
                    BotaoPrincipal(
                        texto = stringResource(R.string.com06_btn_editar_conta),
                        onClick = viewModel::aoAbrirEdicaoConta,
                        icone = Icons.Default.Edit,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botão Sair
                    BotaoSecundario(
                        texto = stringResource(R.string.btn_sair),
                        onClick = viewModel::aoSolicitarSair,
                        icone = Icons.AutoMirrored.Filled.Logout,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Modal BottomSheet de Edição Completa da Conta (Admin ou Signatário)
            if (uiState.mostrandoModalEditar) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = viewModel::aoFecharEdicaoConta,
                    sheetState = sheetState,
                    shape = FormaExtraGrande,
                    containerColor = LetsSignTheme.colors.corSuperficie,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.com06_modal_editar_titulo),
                                style = MaterialTheme.typography.titleLarge,
                                color = LetsSignTheme.colors.corTextoPrincipal
                            )
                            IconButton(onClick = viewModel::aoFecharEdicaoConta) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cd_voltar),
                                    tint = LetsSignTheme.colors.corTextoPrincipal
                                )
                            }
                        }

                        if (uiState.erroGeralEdicao != null) {
                            BannerInformativo(
                                texto = uiState.erroGeralEdicao ?: "",
                                tipo = TipoBanner.ERRO
                            )
                        }

                        // Nome completo
                        CampoTextoContornado(
                            valor = uiState.editandoNome,
                            aoMudarValor = viewModel::aoAlterarNomeEdicao,
                            rotulo = stringResource(R.string.com06_label_nome),
                            placeholder = "Digite o nome completo",
                            mensagemErro = uiState.erroNome,
                            habilitado = !uiState.salvandoEdicao
                        )

                        // CPF (Apenas números - caracteres não numéricos são rejeitados)
                        CampoTextoContornado(
                            valor = uiState.editandoCpf,
                            aoMudarValor = viewModel::aoAlterarCpfEdicao,
                            rotulo = stringResource(R.string.com06_label_cpf),
                            placeholder = "000.000.000-00",
                            mensagemErro = uiState.erroCpf,
                            visualTransformation = CpfVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            limiteCaracteres = 11,
                            habilitado = !uiState.salvandoEdicao
                        )

                        // Data de nascimento (Apenas números - caracteres não numéricos são rejeitados)
                        CampoTextoContornado(
                            valor = uiState.editandoDataNascimento,
                            aoMudarValor = viewModel::aoAlterarDataNascimentoEdicao,
                            rotulo = stringResource(R.string.com06_label_data_nascimento),
                            placeholder = "DD/MM/AAAA",
                            mensagemErro = uiState.erroDataNascimento,
                            visualTransformation = DataNascimentoVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            limiteCaracteres = 8,
                            habilitado = !uiState.salvandoEdicao
                        )

                        // Telefone (Apenas números - caracteres não numéricos são rejeitados)
                        CampoTextoContornado(
                            valor = uiState.editandoTelefone,
                            aoMudarValor = viewModel::aoAlterarTelefoneEdicao,
                            rotulo = stringResource(R.string.com06_label_telefone),
                            placeholder = "(11) 98765-4321",
                            mensagemErro = uiState.erroTelefone,
                            visualTransformation = TelefoneVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            limiteCaracteres = 11,
                            habilitado = !uiState.salvandoEdicao
                        )

                        // E-mail
                        CampoTextoContornado(
                            valor = uiState.editandoEmail,
                            aoMudarValor = viewModel::aoAlterarEmailEdicao,
                            rotulo = stringResource(R.string.com06_label_email),
                            placeholder = "exemplo@dominio.com",
                            mensagemErro = uiState.erroEmail,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            habilitado = !uiState.salvandoEdicao
                        )

                        // Profissão
                        CampoTextoContornado(
                            valor = uiState.editandoProfissao,
                            aoMudarValor = viewModel::aoAlterarProfissaoEdicao,
                            rotulo = stringResource(R.string.com06_label_profissao),
                            placeholder = "Ex.: Administrador, Advogado",
                            mensagemErro = uiState.erroProfissao,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            habilitado = !uiState.salvandoEdicao
                        )

                        // Endereço
                        CampoTextoContornado(
                            valor = uiState.editandoEndereco,
                            aoMudarValor = viewModel::aoAlterarEnderecoEdicao,
                            rotulo = stringResource(R.string.com06_label_endereco),
                            placeholder = "Ex.: Rua das Flores, 123",
                            mensagemErro = uiState.erroEndereco,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            habilitado = !uiState.salvandoEdicao
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        BotaoPrincipal(
                            texto = stringResource(R.string.com06_btn_salvar_alteracoes),
                            onClick = viewModel::aoSalvarEdicaoConta,
                            carregando = uiState.salvandoEdicao,
                            modifier = Modifier.fillMaxWidth()
                        )

                        BotaoSecundario(
                            texto = stringResource(R.string.adm01_dialog_voltar),
                            onClick = viewModel::aoFecharEdicaoConta,
                            habilitado = !uiState.salvandoEdicao,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Dialog de confirmação de saída
            if (uiState.mostrandoDialogSair) {
                DialogBasico(
                    titulo = stringResource(R.string.com06_dialog_sair_titulo),
                    mensagem = stringResource(R.string.com06_dialog_sair_msg),
                    textoBotaoPrincipal = stringResource(R.string.btn_sair),
                    textoBotaoSecundario = stringResource(R.string.adm01_dialog_voltar),
                    aoConfirmar = viewModel::aoConfirmarSair,
                    aoCancelar = viewModel::aoCancelarSair,
                    aoDispensar = viewModel::aoCancelarSair
                )
            }
        }
    }
}

@Composable
private fun ItemDadoConta(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = LetsSignTheme.colors.corTextoDesabilitado
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = LetsSignTheme.colors.corTextoPrincipal
        )
    }
}

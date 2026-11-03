package br.com.letssign.presentation.signatario.assinatura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.BotaoPrincipal
import br.com.letssign.core.designsystem.components.base.BotaoSecundario
import br.com.letssign.core.designsystem.components.base.CampoTextoContornado
import br.com.letssign.core.designsystem.components.reusable.AreaDesenhoAssinatura
import br.com.letssign.core.designsystem.components.reusable.EstiloFonteCursiva
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import br.com.letssign.data.local.file.AssinaturaStorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sig06CapturaAssinaturaScreen(
    aoVoltar: () -> Unit,
    aoAssinaturaConfirmada: (documentoId: String, tipoAssinatura: String, detalhesCertificado: String, caminhoPng: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Sig06CapturaAssinaturaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pontosDesenho by remember { mutableStateOf<List<Offset>>(emptyList()) }

    LaunchedEffect(uiState.assinaturaPronta) {
        if (uiState.assinaturaPronta) {
            val bitmap = when (uiState.abaSelecionada) {
                AbaCapturaAssinatura.DESENHAR -> {
                    AssinaturaStorageManager.gerarBitmapDesenho(pontosDesenho)
                }
                AbaCapturaAssinatura.DIGITAR -> {
                    AssinaturaStorageManager.gerarBitmapDigitado(
                        nome = uiState.textoDigitado,
                        estiloFonteNome = uiState.estiloFonteSelecionado.nomeExibicao
                    )
                }
                AbaCapturaAssinatura.CERTIFICADO_DIGITAL -> {
                    AssinaturaStorageManager.gerarBitmapCertificado(
                        titular = uiState.titularCertificado,
                        documento = uiState.documentoCertificado,
                        tipoCertificado = uiState.tipoCertificado,
                        autoridadeCertificadora = uiState.autoridadeCertificadora
                    )
                }
            }
            val nomeArquivo = "sig_${viewModel.documentoId}_${System.currentTimeMillis()}.png"
            val caminhoSalvo = AssinaturaStorageManager.salvarBitmapComoPng(context, nomeArquivo, bitmap)

            aoAssinaturaConfirmada(
                viewModel.documentoId,
                uiState.tipoAssinatura.name,
                uiState.detalhesCertificado,
                caminhoSalvo
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sig06_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                actions = {
                    br.com.letssign.core.designsystem.components.logo.LogoPlaceholder(
                        tamanho = br.com.letssign.core.designsystem.components.logo.LogoSize.PEQUENO,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LetsSignTheme.colors.corSuperficie,
                tonalElevation = LetsSignTheme.elevation.nivel2
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotaoSecundario(
                        texto = stringResource(R.string.sig06_btn_limpar),
                        onClick = { viewModel.limpar() },
                        icone = Icons.Filled.Delete,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoPrincipal(
                        texto = stringResource(R.string.sig06_btn_confirmar),
                        onClick = { viewModel.confirmarAssinatura() },
                        habilitado = uiState.podeConfirmar,
                        icone = Icons.Filled.Done,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LetsSignTheme.colors.corFundo)
        ) {
            // Abas Desenhar / Digitar / Certificado Digital
            val tabIndex = when (uiState.abaSelecionada) {
                AbaCapturaAssinatura.DESENHAR -> 0
                AbaCapturaAssinatura.DIGITAR -> 1
                AbaCapturaAssinatura.CERTIFICADO_DIGITAL -> 2
            }

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = LetsSignTheme.colors.corSuperficie,
                contentColor = LetsSignTheme.colors.corPrimaria
            ) {
                Tab(
                    selected = uiState.abaSelecionada == AbaCapturaAssinatura.DESENHAR,
                    onClick = { viewModel.selecionarAba(AbaCapturaAssinatura.DESENHAR) },
                    text = { Text(stringResource(R.string.sig06_aba_desenhar)) },
                    icon = { Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = uiState.abaSelecionada == AbaCapturaAssinatura.DIGITAR,
                    onClick = { viewModel.selecionarAba(AbaCapturaAssinatura.DIGITAR) },
                    text = { Text(stringResource(R.string.sig06_aba_digitar)) },
                    icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = uiState.abaSelecionada == AbaCapturaAssinatura.CERTIFICADO_DIGITAL,
                    onClick = { viewModel.selecionarAba(AbaCapturaAssinatura.CERTIFICADO_DIGITAL) },
                    text = { Text("Certificado") },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.abaSelecionada) {
                    AbaCapturaAssinatura.DESENHAR -> {
                        Text(
                            text = stringResource(R.string.sig06_dica_desenho),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LetsSignTheme.colors.corTextoSecundario
                        )

                        AreaDesenhoAssinatura(
                            aoMudarTemDesenho = { tem -> viewModel.atualizarTemDesenho(tem) },
                            aoAtualizarPontos = { pontos -> pontosDesenho = pontos }
                        )
                    }

                    AbaCapturaAssinatura.DIGITAR -> {
                        CampoTextoContornado(
                            valor = uiState.textoDigitado,
                            aoMudarValor = { viewModel.atualizarTextoDigitado(it) },
                            rotulo = stringResource(R.string.sig06_placeholder_digitada)
                        )

                        Text(
                            text = "Selecione o estilo da assinatura:",
                            style = MaterialTheme.typography.titleSmall,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )

                        EstiloFonteCursiva.entries.forEach { estilo ->
                            val selecionado = uiState.estiloFonteSelecionado == estilo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selecionarEstiloFonte(estilo) }
                                    .border(
                                        width = if (selecionado) 2.dp else 1.dp,
                                        color = if (selecionado) LetsSignTheme.colors.corPrimaria else LetsSignTheme.colors.corBorda,
                                        shape = FormaMedia
                                    ),
                                shape = FormaMedia,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selecionado) LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.3f)
                                    else LetsSignTheme.colors.corSuperficie
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = estilo.nomeExibicao,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                    Text(
                                        text = uiState.textoDigitado.ifBlank { "Sua Assinatura" },
                                        fontSize = 24.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = LetsSignTheme.colors.corTintaAssinatura
                                    )
                                }
                            }
                        }
                    }

                    AbaCapturaAssinatura.CERTIFICADO_DIGITAL -> {
                        // Banner explicativo ICP-Brasil
                        Surface(
                            shape = FormaMedia,
                            color = LetsSignTheme.colors.corPrimariaContainer.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corPrimaria)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = LetsSignTheme.colors.corPrimaria,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Assinatura Qualificada ICP-Brasil",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LetsSignTheme.colors.corTextoPrincipal
                                    )
                                    Text(
                                        text = "Validade jurídica plena conforme MP 2.200-2/2001 e Lei 14.063/2020.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Tipo de Certificado Digital:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LetsSignTheme.colors.corTextoPrincipal
                        )

                        // Seletor Radio: e-CPF vs e-CNPJ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .selectable(
                                        selected = uiState.tipoCertificado == "e-CPF",
                                        onClick = { viewModel.atualizarTipoCertificado("e-CPF") }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.tipoCertificado == "e-CPF",
                                    onClick = { viewModel.atualizarTipoCertificado("e-CPF") },
                                    colors = RadioButtonDefaults.colors(selectedColor = LetsSignTheme.colors.corPrimaria)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "e-CPF (PF)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .selectable(
                                        selected = uiState.tipoCertificado == "e-CNPJ",
                                        onClick = { viewModel.atualizarTipoCertificado("e-CNPJ") }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.tipoCertificado == "e-CNPJ",
                                    onClick = { viewModel.atualizarTipoCertificado("e-CNPJ") },
                                    colors = RadioButtonDefaults.colors(selectedColor = LetsSignTheme.colors.corPrimaria)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "e-CNPJ (PJ)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                            }
                        }

                        // Cartão de detalhes do certificado detectado
                        Card(
                            shape = FormaMedia,
                            colors = CardDefaults.cardColors(containerColor = LetsSignTheme.colors.corSuperficie),
                            elevation = CardDefaults.cardElevation(defaultElevation = LetsSignTheme.elevation.nivel1),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.tipoCertificado,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LetsSignTheme.colors.corPrimaria
                                    )
                                    Surface(
                                        shape = FormaPequena,
                                        color = LetsSignTheme.colors.corSucesso.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = LetsSignTheme.colors.corSucesso,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Válido",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = LetsSignTheme.colors.corSucesso
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = LetsSignTheme.colors.corBorda)

                                Text(
                                    text = "Titular: ${uiState.titularCertificado}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                                Text(
                                    text = "Documento: ${uiState.documentoCertificado}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = "Emissor: ${uiState.autoridadeCertificadora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = "Validade: até ${uiState.validadeCertificado}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = "Mídia: ${uiState.subtipoCertificado}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }

                        // Campo PIN de autorização
                        CampoTextoContornado(
                            valor = uiState.pinCertificado,
                            aoMudarValor = { viewModel.atualizarPinCertificado(it) },
                            rotulo = "PIN / Senha do Certificado",
                            dicaAjuda = "Digite a senha de 4 a 8 dígitos do certificado digital",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            iconeInicio = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = LetsSignTheme.colors.corPrimaria)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

package br.com.letssign.presentation.admin.documentos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.letssign.R
import br.com.letssign.core.designsystem.components.base.IndicadorCarregamentoCircular
import br.com.letssign.core.designsystem.components.reusable.ItemLinhaDoTempo
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.data.repository.FiltroAuditoria
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ADM-12 — Trilha de Auditoria.
 * Exibe eventos cronológicos reversos, integridade de SHA-256 e filtros por tipo de evento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Adm12AuditoriaScreen(
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Adm12AuditoriaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(uiState.mensagemFeedback) {
        uiState.mensagemFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.adm12_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.wizard_btn_voltar),
                            tint = LetsSignTheme.colors.corTextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignTheme.colors.corSuperficie
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LetsSignTheme.colors.corFundo,
        modifier = modifier
    ) { paddingValues ->
        if (uiState.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                IndicadorCarregamentoCircular()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de Integridade do Documento
                item {
                    val versao = uiState.versao
                    Surface(
                        shape = FormaMedia,
                        color = LetsSignTheme.colors.corSuperficie,
                        shadowElevation = LetsSignTheme.elevation.nivel1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = LetsSignTheme.colors.corPrimaria
                                )
                                Text(
                                    text = stringResource(R.string.adm12_card_integridade_titulo),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LetsSignTheme.colors.corTextoPrincipal
                                )
                            }

                            HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                            // SHA-256 Original
                            val shaOriginal = versao?.sha256Original ?: "sha256-original-demo"
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.adm12_sha256_original_rotulo),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = shaOriginal,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = LetsSignTheme.colors.corTextoPrincipal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("SHA-256 Original", shaOriginal)
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.definirFeedback(context.getString(R.string.adm12_sucesso_hash_copiado))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.adm12_btn_copiar_hash),
                                            tint = LetsSignTheme.colors.corPrimaria,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // SHA-256 Final (se houver)
                            versao?.sha256Final?.let { shaFinal ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.adm12_sha256_final_rotulo),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LetsSignTheme.colors.corTextoSecundario
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = shaFinal,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = LetsSignTheme.colors.corTextoPrincipal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("SHA-256 Final", shaFinal)
                                                clipboard.setPrimaryClip(clip)
                                                viewModel.definirFeedback(context.getString(R.string.adm12_sucesso_hash_copiado))
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = stringResource(R.string.adm12_btn_copiar_hash),
                                                tint = LetsSignTheme.colors.corPrimaria,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chips de Filtro
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filtroSelecionado == FiltroAuditoria.TODOS,
                            onClick = { viewModel.mudarFiltro(FiltroAuditoria.TODOS) },
                            label = { Text(stringResource(R.string.adm12_filtro_todos)) }
                        )
                        FilterChip(
                            selected = uiState.filtroSelecionado == FiltroAuditoria.ASSINATURAS,
                            onClick = { viewModel.mudarFiltro(FiltroAuditoria.ASSINATURAS) },
                            label = { Text(stringResource(R.string.adm12_filtro_assinaturas)) }
                        )
                        FilterChip(
                            selected = uiState.filtroSelecionado == FiltroAuditoria.ENVIOS_E_NOTIFICACOES,
                            onClick = { viewModel.mudarFiltro(FiltroAuditoria.ENVIOS_E_NOTIFICACOES) },
                            label = { Text(stringResource(R.string.adm12_filtro_envios)) }
                        )
                    }
                }

                // Eventos da Linha do Tempo
                if (uiState.eventos.isEmpty()) {
                    item {
                        Surface(
                            shape = FormaMedia,
                            color = LetsSignTheme.colors.corSuperficie,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.adm12_vazio),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LetsSignTheme.colors.corTextoSecundario,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(uiState.eventos, key = { _, ev -> ev.id }) { index, ev ->
                        val dataFormatada = remember(ev.dataHoraUtc) { formatoData.format(Date(ev.dataHoraUtc)) }
                        ItemLinhaDoTempo(
                            dataHora = dataFormatada,
                            evento = ev.tipoEvento.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                            autor = ev.autorNome,
                            detalhe = ev.detalhe,
                            ehUltimo = index == uiState.eventos.size - 1
                        )
                    }
                }
            }
        }
    }
}

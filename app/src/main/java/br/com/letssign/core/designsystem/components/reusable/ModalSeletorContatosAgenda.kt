package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.letssign.core.common.contacts.AgendaContatosMockHelper
import br.com.letssign.core.common.contacts.ContatoMockAgenda
import br.com.letssign.core.designsystem.theme.FormaMedia
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun ModalSeletorContatosAgenda(
    aoSelecionarContato: (nome: String, email: String, telefone: String) -> Unit,
    aoAbrirSeletorSistema: () -> Unit,
    aoFechar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var termoBusca by remember { mutableStateOf("") }

    val contatosFiltrados = remember(termoBusca) {
        if (termoBusca.isBlank()) {
            AgendaContatosMockHelper.CONTATOS_MOCKADOS
        } else {
            val t = termoBusca.trim().lowercase()
            AgendaContatosMockHelper.CONTATOS_MOCKADOS.filter {
                it.nome.lowercase().contains(t) ||
                        it.email.lowercase().contains(t) ||
                        it.telefone.contains(t) ||
                        it.cargo.lowercase().contains(t)
            }
        }
    }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        tint = LetsSignTheme.colors.corPrimaria,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Contatos da Agenda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }

                IconButton(onClick = aoFechar) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecione um contato salvo no celular deste administrador para preencher os dados automaticamente:",
                    style = MaterialTheme.typography.bodySmall,
                    color = LetsSignTheme.colors.corTextoSecundario
                )

                // Barra de pesquisa rápida
                OutlinedTextField(
                    value = termoBusca,
                    onValueChange = { termoBusca = it },
                    placeholder = { Text("Buscar por nome, e-mail ou cargo...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LetsSignTheme.colors.corTextoSecundario
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormaMedia,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LetsSignTheme.colors.corPrimaria,
                        unfocusedBorderColor = LetsSignTheme.colors.corBorda
                    )
                )

                // Lista dos 10 contatos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contatosFiltrados, key = { it.id }) { contato ->
                        ItemContatoCard(
                            contato = contato,
                            aoClicar = {
                                aoSelecionarContato(contato.nome, contato.email, contato.telefone)
                                aoFechar()
                            }
                        )
                    }
                }

                HorizontalDivider(color = LetsSignTheme.colors.corDivisor)

                // Opção alternativa de abrir o seletor do sistema operacional
                OutlinedButton(
                    onClick = {
                        aoFechar()
                        aoAbrirSeletorSistema()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormaMedia
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Abrir Agenda do Sistema Android",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        modifier = modifier
    )
}

@Composable
private fun ItemContatoCard(
    contato: ContatoMockAgenda,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = aoClicar),
        shape = FormaMedia,
        colors = CardDefaults.cardColors(
            containerColor = LetsSignTheme.colors.corSuperficieVariante.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, LetsSignTheme.colors.corBorda)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Iniciais no avatar
            val iniciais = contato.nome.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LetsSignTheme.colors.corPrimariaContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciais,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corPrimaria
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = contato.nome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = LetsSignTheme.colors.corTextoPrincipal
                    )
                }

                Surface(
                    shape = FormaPequena,
                    color = LetsSignTheme.colors.corPrimaria.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = contato.cargo,
                        style = MaterialTheme.typography.labelSmall,
                        color = LetsSignTheme.colors.corPrimaria,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = LetsSignTheme.colors.corTextoSecundario
                    )
                    Text(
                        text = formatarTelefoneVisual(contato.telefone),
                        style = MaterialTheme.typography.labelSmall,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = LetsSignTheme.colors.corTextoSecundario
                    )
                    Text(
                        text = contato.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = LetsSignTheme.colors.corTextoSecundario
                    )
                }
            }
        }
    }
}

private fun formatarTelefoneVisual(raw: String): String {
    val d = raw.filter { it.isDigit() }
    return if (d.length == 11) {
        "(${d.substring(0, 2)}) ${d.substring(2, 7)}-${d.substring(7)}"
    } else {
        raw
    }
}

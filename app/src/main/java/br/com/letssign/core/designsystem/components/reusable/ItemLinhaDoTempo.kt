package br.com.letssign.core.designsystem.components.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.letssign.core.designsystem.theme.FormaPequena
import br.com.letssign.core.designsystem.theme.FormaTotal
import br.com.letssign.core.designsystem.theme.LetsSignTheme

@Composable
fun ItemLinhaDoTempo(
    dataHora: String,
    evento: String,
    modifier: Modifier = Modifier,
    autor: String? = null,
    detalhe: String? = null,
    ehUltimo: Boolean = false,
    icone: ImageVector? = null,
    corMarcador: Color? = null,
    geolocalizacao: String? = null,
    ipDispositivo: String? = null
) {
    val corPonto = corMarcador ?: LetsSignTheme.colors.corPrimaria

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Coluna da linha e marcador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (icone != null) 24.dp else 12.dp)
                    .background(color = corPonto, shape = FormaTotal),
                contentAlignment = Alignment.Center
            ) {
                if (icone != null) {
                    Icon(
                        imageVector = icone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (!ehUltimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(72.dp)
                        .background(color = LetsSignTheme.colors.corDivisor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Conteúdo textual
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (ehUltimo) 0.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = evento,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LetsSignTheme.colors.corTextoPrincipal
                )
            }

            Text(
                text = dataHora,
                style = MaterialTheme.typography.labelSmall,
                color = LetsSignTheme.colors.corTextoSecundario
            )

            if (autor != null) {
                Text(
                    text = "Autor: $autor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }

            if (detalhe != null) {
                Text(
                    text = detalhe,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LetsSignTheme.colors.corTextoSecundario
                )
            }

            // Metadados enriquecidos de auditoria (Geolocalização e Dispositivo / IP)
            if (geolocalizacao != null || ipDispositivo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (geolocalizacao != null) {
                        Surface(
                            shape = FormaPequena,
                            color = LetsSignTheme.colors.corSuperficieVariante.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = LetsSignTheme.colors.corPrimaria
                                )
                                Text(
                                    text = geolocalizacao,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }
                    }

                    if (ipDispositivo != null) {
                        Surface(
                            shape = FormaPequena,
                            color = LetsSignTheme.colors.corSuperficieVariante.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = LetsSignTheme.colors.corTextoSecundario
                                )
                                Text(
                                    text = ipDispositivo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LetsSignTheme.colors.corTextoSecundario
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Item Linha do Tempo", showBackground = true)
@Composable
private fun LinhaTempoPreview() {
    LetsSignTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ItemLinhaDoTempo(
                dataHora = "26/10/2026 14:03",
                evento = "Convite enviado por e-mail para Ana Lima",
                autor = "Carla Mendes"
            )
            ItemLinhaDoTempo(
                dataHora = "26/10/2026 14:20",
                evento = "Assinado com sucesso",
                autor = "Ana Lima",
                ehUltimo = true
            )
        }
    }
}

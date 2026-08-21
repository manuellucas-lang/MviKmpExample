package com.example.mviexample.features.operaciones.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mviexample.designsystem.components.BrandBadge
import com.example.mviexample.designsystem.components.GooglePayIconButton
import com.example.mviexample.designsystem.components.GooglePayPriceTag
import com.example.mviexample.designsystem.components.InitialsAvatar
import com.example.mviexample.designsystem.components.PaidWithGooglePayBadge
import com.example.mviexample.designsystem.components.PostImage
import com.example.mviexample.features.payments.formatEuros
import com.example.mviexample.features.payments.operacionPrecio
import com.example.mviexample.shared.data.model.Operacion

@Composable
fun OperacionCard(
    operacion: Operacion,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBuy: () -> Unit = {},
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isRefreshing) 0.55f else 1f)
            .clickable(enabled = !isRefreshing, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InitialsAvatar(name = operacion.autor, size = 40.dp)
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = operacion.autor ?: "Anónimo",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Operación #${operacion.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (operacion.propia) {
                    BrandBadge(text = "Tuya")
                }
            }

            PostImage(
                url = operacion.imagenUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(0.dp)),
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = operacion.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val tipo = operacion.tipo
                if (tipo != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = tipo,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = operacion.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ver más",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.weight(1f))
                    if (operacion.guardada) {
                        GooglePayPriceTag(
                            price = formatEuros(operacionPrecio(operacion)),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        PaidWithGooglePayBadge(compact = true)
                    } else {
                    GooglePayIconButton(
                        onClick = onBuy,
                        enabled = !isRefreshing,
                    )
                    }
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar operación",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isRefreshing, onClick = onEdit)
                            .padding(10.dp)
                            .size(18.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar operación",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isRefreshing, onClick = onDelete)
                            .padding(10.dp)
                            .size(18.dp),
                    )
                }
            }
        }
    }
}

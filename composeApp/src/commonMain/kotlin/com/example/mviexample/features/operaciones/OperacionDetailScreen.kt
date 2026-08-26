package com.example.mviexample.features.operaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mvikmpexample.composeapp.generated.resources.Res
import mvikmpexample.composeapp.generated.resources.action_comprada_con
import mvikmpexample.composeapp.generated.resources.action_editar
import mvikmpexample.composeapp.generated.resources.action_eliminar
import mvikmpexample.composeapp.generated.resources.action_pagar
import mvikmpexample.composeapp.generated.resources.detail_anonimo
import mvikmpexample.composeapp.generated.resources.detail_autor
import mvikmpexample.composeapp.generated.resources.detail_operacion_id
import mvikmpexample.composeapp.generated.resources.detail_tuya
import mvikmpexample.composeapp.generated.resources.detail_volver
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import com.example.mviexample.designsystem.components.AppButton
import com.example.mviexample.designsystem.components.AppButtonStyle
import com.example.mviexample.designsystem.components.AppIconButton
import com.example.mviexample.designsystem.components.BrandBadge
import com.example.mviexample.designsystem.components.GooglePayButton
import com.example.mviexample.designsystem.components.InitialsAvatar
import com.example.mviexample.designsystem.components.PaidWithGooglePayBadge
import com.example.mviexample.designsystem.components.PostImage
import com.example.mviexample.features.payments.formatEuros
import com.example.mviexample.features.payments.operacionPrecio

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OperacionDetailScreen(
    state: OperacionesContract.OperacionesState,
    actions: OperacionesActions,
    snackbarHostState: SnackbarHostState,
) {
    val operacion = state.selectedOperacion ?: return
    val postScrim = Color.Black.copy(alpha = 0.35f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            ) {
                PostImage(
                    url = operacion.imagenUrl,
                    modifier = Modifier.fillMaxSize(),
                    overlayGradient = true,
                )
                AppIconButton(
                    onClick = actions.onCloseDetail,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(8.dp),
                    containerColor = postScrim,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.detail_volver))
                }
                Surface(
                    color = postScrim,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(8.dp),
                ) {
                    Text(
                        text = formatEuros(operacionPrecio(operacion)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
                if (operacion.guardada) {
                    PaidWithGooglePayBadge(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                    )
                }
                if (operacion.propia) {
                    BrandBadge(
                        text = stringResource(Res.string.detail_tuya),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        containerColor = Color.White.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(Res.string.detail_operacion_id, operacion.id),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = operacion.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val tipo = operacion.tipo
                if (tipo != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = tipo,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = operacion.autor, size = 44.dp)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            text = operacion.autor ?: stringResource(Res.string.detail_anonimo),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(Res.string.detail_autor),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = operacion.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (operacion.guardada) {
                        GooglePayButton(
                            onClick = {},
                            enabled = false,
                            label = stringResource(Res.string.action_comprada_con),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        GooglePayButton(
                            onClick = { actions.onBuy(operacion) },
                            label = stringResource(Res.string.action_pagar),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppButton(
                            text = stringResource(Res.string.action_editar),
                            onClick = { actions.onOpenEdit(operacion) },
                            style = AppButtonStyle.Outlined,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                        AppButton(
                            text = stringResource(Res.string.action_eliminar),
                            onClick = { actions.onRequestDelete(operacion) },
                            style = AppButtonStyle.Error,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

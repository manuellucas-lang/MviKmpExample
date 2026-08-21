package com.example.mviexample.features.operaciones

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.components.BrandHeader
import com.example.mviexample.designsystem.components.EmptyState
import com.example.mviexample.designsystem.components.ErrorState
import com.example.mviexample.designsystem.components.FilterPill
import com.example.mviexample.designsystem.components.PullToRefreshContainer
import com.example.mviexample.designsystem.components.GooglePayMark
import com.example.mviexample.designsystem.components.PostCardSkeleton
import com.example.mviexample.designsystem.components.ThemeToggleButton
import com.example.mviexample.designsystem.theme.BrandGradientEnd
import com.example.mviexample.designsystem.theme.BrandGradientStart
import com.example.mviexample.features.operaciones.components.OperacionCard

@Composable
fun OperacionesListScreen(
    state: OperacionesContract.OperacionesState,
    actions: OperacionesActions,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BrandHeader(
                title = if (state.tab == OperacionesContract.OperacionesTab.Guardadas) "Guardadas" else "Operaciones",
                subtitle = if (state.tab == OperacionesContract.OperacionesTab.Guardadas) {
                    "${state.guardadasCount} operaciones compradas"
                } else {
                    "${state.operaciones.size} operaciones · ${state.operaciones.count { it.propia }} tuyas"
                },
                action = {
                    val infinite = rememberInfiniteTransition(label = "refreshSpin")
                    val rotation by infinite.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(700)),
                        label = "refreshAngle",
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ThemeToggleButton(
                            darkTheme = darkTheme,
                            onClick = onToggleTheme,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        )
                        IconButton(onClick = actions.onRefresh, enabled = !state.isRefreshing) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refrescar",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.rotateIf(state.isRefreshing, rotation),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = state.tab == OperacionesContract.OperacionesTab.Lista,
                    onClick = { actions.onTabChange(OperacionesContract.OperacionesTab.Lista) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Lista") },
                )
                NavigationBarItem(
                    selected = state.tab == OperacionesContract.OperacionesTab.Guardadas,
                    onClick = { actions.onTabChange(OperacionesContract.OperacionesTab.Guardadas) },
                    icon = {
                        GooglePayMark(modifier = Modifier.height(16.dp))
                    },
                    label = { Text("Guardadas") },
                )
            }
        },
        floatingActionButton = {
            if (state.tab == OperacionesContract.OperacionesTab.Lista) {
                ExtendedFloatingActionButton(
                    onClick = actions.onOpenCreate,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva operación") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = state.query,
                onValueChange = actions.onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = "Buscar operaciones o autores…",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { actions.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    null
                },
            )

            Spacer(Modifier.height(12.dp))

            if (state.tab == OperacionesContract.OperacionesTab.Lista) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterPill(
                        text = "Todas",
                        count = state.operaciones.size,
                        selected = state.filtro == OperacionesContract.OperacionFiltro.Todas,
                        onClick = { actions.onFilterChange(OperacionesContract.OperacionFiltro.Todas) },
                    )
                    FilterPill(
                        text = "Propias",
                        count = state.operaciones.count { it.propia },
                        selected = state.filtro == OperacionesContract.OperacionFiltro.Propias,
                        onClick = { actions.onFilterChange(OperacionesContract.OperacionFiltro.Propias) },
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            PullToRefreshContainer(
                isRefreshing = state.isRefreshing,
                onRefresh = actions.onRefresh,
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.operaciones.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(4) {
                                PostCardSkeleton()
                            }
                        }
                    }

                    state.error != null && state.operaciones.isEmpty() -> {
                        ErrorState(
                            message = state.error,
                            modifier = Modifier.align(Alignment.Center),
                            onRetry = actions.onRetry,
                        )
                    }

                    state.visibleOperaciones.isEmpty() -> {
                        val isSavedTab = state.tab == OperacionesContract.OperacionesTab.Guardadas
                        EmptyState(
                            iconContent = if (isSavedTab) {
                                { GooglePayMark(modifier = Modifier.height(26.dp)) }
                            } else {
                                null
                            },
                            icon = when {
                                isSavedTab -> null
                                state.operaciones.isEmpty() -> Icons.Default.PostAdd
                                else -> Icons.Default.SearchOff
                            },
                            title = when {
                                isSavedTab -> "No hay operaciones compradas"
                                state.operaciones.isEmpty() -> "No hay operaciones todavía"
                                else -> "Sin resultados"
                            },
                            message = when {
                                isSavedTab -> "Compra operaciones con Google Pay y aparecerán aquí."
                                state.operaciones.isEmpty() -> "Sé el primero en registrar una operación."
                                else -> "Nada coincide con “${state.query}”. Prueba con otra búsqueda o filtro."
                            },
                            modifier = Modifier.align(Alignment.Center),
                            actionLabel = when {
                                isSavedTab -> null
                                state.operaciones.isEmpty() -> "Crear tu primera operación"
                                else -> null
                            },
                            onAction = if (!isSavedTab && state.operaciones.isEmpty()) actions.onOpenCreate else null,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 96.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item(key = "hero") {
                                if (state.tab == OperacionesContract.OperacionesTab.Lista) {
                                    OperacionesHero(
                                        total = state.operaciones.size,
                                        conFotos = state.operaciones.count { it.imagenUrl != null },
                                        propias = state.operaciones.count { it.propia },
                                    )
                                } else {
                                    GuardadasHero(guardadasCount = state.guardadasCount)
                                }
                            }
                            items(state.visibleOperaciones, key = { it.id }) { operacion ->
                                OperacionCard(
                                    operacion = operacion,
                                    onClick = { actions.onOpenDetail(operacion) },
                                    onEdit = { actions.onOpenEdit(operacion) },
                                    onDelete = { actions.onRequestDelete(operacion) },
                                    onBuy = { actions.onBuy(operacion) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperacionesHero(
    total: Int,
    conFotos: Int,
    propias: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(BrandGradientStart, BrandGradientEnd)),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Text(
            text = "OPERACIONES",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$total operaciones",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$conFotos con foto · $propias creadas por ti",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GuardadasHero(
    guardadasCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(BrandGradientStart, BrandGradientEnd)),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Text(
            text = "GUARDADAS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (guardadasCount == 1) "1 operación comprada" else "$guardadasCount operaciones compradas",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tus operaciones pagadas con Google Pay.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.rotateIf(enabled: Boolean, rotation: Float): Modifier =
    if (enabled) {
        this.rotate(rotation)
    } else {
        this
    }

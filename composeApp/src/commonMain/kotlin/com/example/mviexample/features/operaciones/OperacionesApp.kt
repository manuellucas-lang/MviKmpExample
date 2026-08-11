package com.example.mviexample.features.operaciones

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mviexample.designsystem.components.ConfirmDialog
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesEffect
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesIntent
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesState
import com.example.mviexample.shared.AppGraph
import com.example.mviexample.shared.data.model.Operacion

private sealed interface AppScreen {
    data object List : AppScreen
    data object Detail : AppScreen
    data object Editor : AppScreen
}

private fun currentScreen(state: OperacionesState): AppScreen = when {
    state.isEditorOpen -> AppScreen.Editor
    state.selectedOperacion != null -> AppScreen.Detail
    else -> AppScreen.List
}

data class OperacionesActions(
    val onRefresh: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onFilterChange: (OperacionesContract.OperacionFiltro) -> Unit = {},
    val onTabChange: (OperacionesContract.OperacionesTab) -> Unit = {},
    val onOpenCreate: () -> Unit = {},
    val onOpenDetail: (Operacion) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRequestDelete: (Operacion) -> Unit = {},
    val onOpenEdit: (Operacion) -> Unit = {},
    val onToggleSave: (Operacion) -> Unit = {},
    val onCloseDetail: () -> Unit = {},
    val onCloseEditor: () -> Unit = {},
    val onSave: (String, String, String?) -> Unit = { _, _, _ -> },
)

@Composable
fun OperacionesApp(
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    viewModel: OperacionesViewModel = viewModel { OperacionesViewModel(AppGraph.container.operacionesRepository) },
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OperacionesEffect.MostrarMensaje -> snackbarHostState.showSnackbar(effect.mensaje)
            }
        }
    }

    val actions = OperacionesActions(
        onRefresh = { viewModel.onIntent(OperacionesIntent.RefrescarOperaciones) },
        onQueryChange = { viewModel.onIntent(OperacionesIntent.ActualizarQuery(it)) },
        onFilterChange = { viewModel.onIntent(OperacionesIntent.SeleccionarFiltro(it)) },
        onTabChange = { viewModel.onIntent(OperacionesIntent.SeleccionarTab(it)) },
        onOpenCreate = { viewModel.onIntent(OperacionesIntent.AbrirCrear) },
        onOpenDetail = { viewModel.onIntent(OperacionesIntent.AbrirDetalle(it)) },
        onRetry = { viewModel.onIntent(OperacionesIntent.Reintentar) },
        onRequestDelete = { viewModel.onIntent(OperacionesIntent.SolicitarBorrado(it)) },
        onOpenEdit = { viewModel.onIntent(OperacionesIntent.AbrirEditar(it)) },
        onToggleSave = { viewModel.onIntent(OperacionesIntent.ToggleGuardar(it)) },
        onCloseDetail = { viewModel.onIntent(OperacionesIntent.CerrarDetalle) },
        onCloseEditor = { viewModel.onIntent(OperacionesIntent.CerrarEditor) },
        onSave = { titulo, descripcion, imagenUrl ->
            viewModel.onIntent(OperacionesIntent.GuardarOperacion(titulo, descripcion, imagenUrl))
        },
    )

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = "¿Eliminar operación?",
            message = "“${target.titulo.take(60)}${if (target.titulo.length > 60) "…" else ""}” se eliminará permanentemente.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            destructive = true,
            onConfirm = { viewModel.onIntent(OperacionesIntent.ConfirmarBorrado) },
            onDismiss = { viewModel.onIntent(OperacionesIntent.DescartarBorrado) },
        )
    }

    AnimatedContent(
        targetState = currentScreen(state),
        transitionSpec = {
            when {
                targetState is AppScreen.Detail || targetState is AppScreen.Editor ->
                    (slideInHorizontally(tween(260)) { it / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(260)) { -it / 4 } + fadeOut(tween(220)))

                else ->
                    (slideInHorizontally(tween(260)) { -it / 4 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(260)) { it / 4 } + fadeOut(tween(220)))
            }
        },
        modifier = Modifier,
        label = "screenTransition",
    ) { screen ->
        when (screen) {
            AppScreen.List -> OperacionesListScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
            )

            AppScreen.Detail -> OperacionDetailScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
            )

            AppScreen.Editor -> OperacionEditorScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

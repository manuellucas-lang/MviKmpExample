package com.example.mviexample.features.operaciones

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mviexample.designsystem.components.AppButton
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.components.BrandTopBar
import com.example.mviexample.features.operaciones.components.OperacionCard
import com.example.mviexample.shared.data.model.Operacion

@Composable
fun OperacionEditorScreen(
    state: OperacionesContract.OperacionesState,
    actions: OperacionesActions,
    snackbarHostState: SnackbarHostState,
) {
    val editing = state.editorOperacion

    var titulo by rememberSaveable(editing?.id) { mutableStateOf(editing?.titulo ?: "") }
    var descripcion by rememberSaveable(editing?.id) { mutableStateOf(editing?.descripcion ?: "") }
    var imagenUrl by rememberSaveable(editing?.id) { mutableStateOf(editing?.imagenUrl ?: "") }

    val previewOperacion = editing?.copy(
        titulo = titulo.ifBlank { editing.titulo },
        descripcion = descripcion.ifBlank { editing.descripcion },
        imagenUrl = imagenUrl.ifBlank { null },
    ) ?: Operacion(
        id = 0,
        titulo = titulo.ifBlank { "Título de la operación" },
        descripcion = descripcion.ifBlank { "Aquí aparecerá la vista previa de tu contenido." },
        imagenUrl = imagenUrl.ifBlank { null },
        autor = "You",
        propia = true,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BrandTopBar(
                title = if (editing != null) "Editar operación" else "Nueva operación",
                subtitle = if (editing != null) "Actualiza los datos" else "Registra una nueva operación",
                onBack = actions.onCloseEditor,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "CONTENIDO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Título",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    supportingText = "Da a tu operación un título claro",
                )
                Spacer(Modifier.height(18.dp))
                AppTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Contenido",
                    singleLine = false,
                    minLines = 6,
                    maxLines = 12,
                    supportingText = "Al menos 3 caracteres",
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "FOTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = imagenUrl,
                    onValueChange = { imagenUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "URL de imagen (opcional)",
                    placeholder = "https://…",
                    supportingText = "Pega un enlace para adjuntar una imagen a tu operación",
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "VISTA PREVIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(10.dp))
                OperacionCard(
                    operacion = previewOperacion,
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                )
                Spacer(Modifier.height(8.dp))
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
                ) {
                    AppButton(
                        text = if (editing != null) "Guardar cambios" else "Registrar operación",
                        onClick = {
                            actions.onSave(titulo, descripcion, imagenUrl.ifBlank { null })
                        },
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Send,
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

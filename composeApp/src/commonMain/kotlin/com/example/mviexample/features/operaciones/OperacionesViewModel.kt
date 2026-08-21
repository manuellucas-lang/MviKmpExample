package com.example.mviexample.features.operaciones

import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesEffect
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesIntent
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesState
import com.example.mviexample.features.payments.GooglePayResult
import com.example.mviexample.features.payments.MockGooglePayGateway
import com.example.mviexample.features.payments.operacionPrecio
import com.example.mviexample.mvi.MviViewModel
import com.example.mviexample.shared.data.OperacionesRepository
import com.example.mviexample.shared.data.model.Operacion
import com.example.mviexample.shared.util.currentTimeMillis
import kotlinx.coroutines.delay

class OperacionesViewModel(
    private val repository: OperacionesRepository,
    private val googlePayGateway: MockGooglePayGateway = MockGooglePayGateway(),
    private val minRefreshFeedbackMillis: Long = 600L,
) : MviViewModel<OperacionesState, OperacionesIntent, OperacionesEffect>(
    initialState = OperacionesState(),
) {

    init {
        onIntent(OperacionesIntent.RefrescarOperaciones)
    }

    override suspend fun handleIntent(intent: OperacionesIntent) {
        when (intent) {
            is OperacionesIntent.CargarOperaciones -> cargarOperaciones(forceRefresh = false)
            is OperacionesIntent.RefrescarOperaciones -> cargarOperaciones(forceRefresh = true)
            is OperacionesIntent.ActualizarQuery -> setState { it.copy(query = intent.query) }
            is OperacionesIntent.SeleccionarFiltro -> setState { it.copy(filtro = intent.filtro) }
            is OperacionesIntent.SeleccionarTab -> setState { it.copy(tab = intent.tab) }
            is OperacionesIntent.AbrirCrear -> setState {
                it.copy(isEditorOpen = true, editorOperacion = null)
            }
            is OperacionesIntent.AbrirDetalle -> setState { it.copy(selectedOperacion = intent.operacion) }
            is OperacionesIntent.CerrarDetalle -> setState { it.copy(selectedOperacion = null) }
            is OperacionesIntent.AbrirEditar -> setState {
                it.copy(isEditorOpen = true, editorOperacion = intent.operacion, isSaving = false)
            }
            is OperacionesIntent.CerrarEditor -> setState {
                it.copy(isEditorOpen = false, editorOperacion = null, isSaving = false)
            }
            is OperacionesIntent.GuardarOperacion -> guardarOperacion(intent.titulo, intent.descripcion, intent.imagenUrl)
            is OperacionesIntent.IniciarPago -> iniciarPago(intent.operacion)
            is OperacionesIntent.ConfirmarPago -> confirmarPago()
            is OperacionesIntent.CancelarPago -> cancelarPago()
            is OperacionesIntent.SolicitarBorrado -> setState { it.copy(deleteTarget = intent.operacion) }
            is OperacionesIntent.ConfirmarBorrado -> borrarOperacionActual()
            is OperacionesIntent.DescartarBorrado -> setState { it.copy(deleteTarget = null) }
            is OperacionesIntent.Reintentar -> cargarOperaciones(forceRefresh = false)
        }
    }

    private suspend fun cargarOperaciones(forceRefresh: Boolean) {
        setState {
            it.copy(
                isLoading = if (forceRefresh) it.isLoading else true,
                isRefreshing = if (forceRefresh) true else it.isRefreshing,
                error = null,
            )
        }
        try {
            val startedAt = currentTimeMillis()
            val result = repository.getOperaciones(forceRefresh)
            if (forceRefresh) {
                val elapsed = currentTimeMillis() - startedAt
                if (elapsed < minRefreshFeedbackMillis) {
                    delay(minRefreshFeedbackMillis - elapsed)
                }
            }
            setState {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    operaciones = result.operaciones,
                    error = null,
                )
            }
            if (result.fromCache) {
                emitEffect(OperacionesEffect.MostrarMensaje("Sin conexión — mostrando operaciones en caché"))
            }
        } catch (e: Exception) {
            setState {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "No se pudieron cargar las operaciones. Comprueba tu conexión.",
                )
            }
            emitEffect(OperacionesEffect.MostrarMensaje("Error de red: ${e.message ?: "desconocido"}"))
        }
    }

    private suspend fun guardarOperacion(rawTitulo: String, rawDescripcion: String, imagenUrl: String?) {
        val titulo = rawTitulo.trim()
        val descripcion = rawDescripcion.trim()
        if (titulo.isEmpty()) {
            emitEffect(OperacionesEffect.MostrarMensaje("El título es obligatorio"))
            return
        }
        if (descripcion.length < 3) {
            emitEffect(OperacionesEffect.MostrarMensaje("El contenido es demasiado corto"))
            return
        }
        val editing = state.value.editorOperacion
        setState { it.copy(isSaving = true) }
        try {
            if (editing == null) {
                val creada = repository.crearOperacion(titulo, descripcion, imagenUrl, tipo = null, autor = "You")
                setState { current ->
                    current.copy(
                        operaciones = (listOf(creada) + current.operaciones.filterNot { it.id == creada.id })
                            .sortedByDescending { it.id },
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(OperacionesEffect.MostrarMensaje("Operación creada"))
            } else {
                val actualizada = repository.actualizarOperacion(editing.id, titulo, descripcion, imagenUrl, tipo = null, autor = "You")
                setState { current ->
                    current.copy(
                        operaciones = current.operaciones.map { if (it.id == actualizada.id) actualizada else it },
                        selectedOperacion = if (current.selectedOperacion?.id == actualizada.id) actualizada else current.selectedOperacion,
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(OperacionesEffect.MostrarMensaje("Operación actualizada"))
            }
        } catch (e: Exception) {
            setState { it.copy(isSaving = false) }
            emitEffect(OperacionesEffect.MostrarMensaje("Error al guardar: ${e.message ?: "error de red"}"))
        }
    }

    private suspend fun iniciarPago(operacion: Operacion) {
        if (operacion.guardada) {
            emitEffect(OperacionesEffect.MostrarMensaje("Esta operación ya está comprada"))
            return
        }
        setState { it.copy(paymentTarget = operacion, isProcessingPayment = false) }
    }

    private suspend fun confirmarPago() {
        val target = state.value.paymentTarget ?: return
        if (state.value.isProcessingPayment) return
        setState { it.copy(isProcessingPayment = true) }
        val request = googlePayGateway.buildPaymentRequest(target, operacionPrecio(target))
        when (val result = googlePayGateway.processPayment(request)) {
            is GooglePayResult.Success -> {
                try {
                    repository.setOperacionGuardada(target.id, true)
                    setState { current ->
                        current.copy(
                            operaciones = current.operaciones.map {
                                if (it.id == target.id) it.copy(guardada = true) else it
                            },
                            selectedOperacion = if (current.selectedOperacion?.id == target.id) {
                                current.selectedOperacion?.copy(guardada = true)
                            } else {
                                current.selectedOperacion
                            },
                            paymentTarget = null,
                            isProcessingPayment = false,
                        )
                    }
                    emitEffect(
                        OperacionesEffect.PagoCompletado(
                            operacion = target.copy(guardada = true),
                            transactionId = result.transactionId,
                        ),
                    )
                    emitEffect(OperacionesEffect.MostrarMensaje("Pago completado · Operación guardada"))
                } catch (e: Exception) {
                    setState { it.copy(isProcessingPayment = false) }
                    emitEffect(
                        OperacionesEffect.MostrarMensaje(
                            "El pago se procesó pero no se pudo guardar: ${e.message ?: "error desconocido"}",
                        ),
                    )
                }
            }

            is GooglePayResult.Cancelled -> {
                setState { it.copy(paymentTarget = null, isProcessingPayment = false) }
                emitEffect(OperacionesEffect.MostrarMensaje("Pago cancelado"))
            }

            is GooglePayResult.Error -> {
                setState { it.copy(isProcessingPayment = false) }
                emitEffect(OperacionesEffect.MostrarMensaje("Error de pago: ${result.mensaje}"))
            }
        }
    }

    private fun cancelarPago() {
        if (state.value.isProcessingPayment) return
        setState { it.copy(paymentTarget = null, isProcessingPayment = false) }
    }

    private suspend fun borrarOperacionActual() {
        val target = state.value.deleteTarget ?: return
        setState { it.copy(isDeleting = true) }
        try {
            repository.borrarOperacion(target.id)
            setState { current ->
                current.copy(
                    operaciones = current.operaciones.filterNot { it.id == target.id },
                    selectedOperacion = if (current.selectedOperacion?.id == target.id) null else current.selectedOperacion,
                    deleteTarget = null,
                    isDeleting = false,
                )
            }
            emitEffect(OperacionesEffect.MostrarMensaje("Operación eliminada"))
        } catch (e: Exception) {
            setState { it.copy(isDeleting = false, deleteTarget = null) }
            emitEffect(OperacionesEffect.MostrarMensaje("Error al eliminar: ${e.message ?: "error de red"}"))
        }
    }
}

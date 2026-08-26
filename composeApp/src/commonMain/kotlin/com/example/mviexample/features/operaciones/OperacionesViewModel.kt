package com.example.mviexample.features.operaciones

import mvikmpexample.composeapp.generated.resources.Res
import mvikmpexample.composeapp.generated.resources.editor_autor
import mvikmpexample.composeapp.generated.resources.msg_already_purchased
import mvikmpexample.composeapp.generated.resources.msg_content_short
import mvikmpexample.composeapp.generated.resources.msg_delete_error
import mvikmpexample.composeapp.generated.resources.msg_network_error_detail
import mvikmpexample.composeapp.generated.resources.msg_network_short
import mvikmpexample.composeapp.generated.resources.msg_no_connection
import mvikmpexample.composeapp.generated.resources.msg_operation_created
import mvikmpexample.composeapp.generated.resources.msg_operation_deleted
import mvikmpexample.composeapp.generated.resources.msg_operation_updated
import mvikmpexample.composeapp.generated.resources.msg_payment_cancelled
import mvikmpexample.composeapp.generated.resources.msg_payment_completed
import mvikmpexample.composeapp.generated.resources.msg_payment_error
import mvikmpexample.composeapp.generated.resources.msg_payment_save_error
import mvikmpexample.composeapp.generated.resources.msg_save_error
import mvikmpexample.composeapp.generated.resources.msg_title_required
import mvikmpexample.composeapp.generated.resources.msg_unknown
import mvikmpexample.composeapp.generated.resources.msg_network_error
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
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

@OptIn(ExperimentalResourceApi::class)
class OperacionesViewModel(
    private val repository: OperacionesRepository,
    private val googlePayGateway: MockGooglePayGateway = MockGooglePayGateway(),
    private val minRefreshFeedbackMillis: Long = 800L,
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
                emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_no_connection)))
            }
        } catch (e: Exception) {
            val errorMsg = getString(Res.string.msg_network_error)
            setState {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = errorMsg,
                )
            }
            val detailMsg = getString(Res.string.msg_network_error_detail, e.message ?: getString(Res.string.msg_unknown))
            emitEffect(OperacionesEffect.MostrarMensaje(detailMsg))
        }
    }

    private suspend fun guardarOperacion(rawTitulo: String, rawDescripcion: String, imagenUrl: String?) {
        val titulo = rawTitulo.trim()
        val descripcion = rawDescripcion.trim()
        if (titulo.isEmpty()) {
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_title_required)))
            return
        }
        if (descripcion.length < 3) {
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_content_short)))
            return
        }
        val editing = state.value.editorOperacion
        setState { it.copy(isSaving = true) }
        try {
            if (editing == null) {
                val creada = repository.crearOperacion(titulo, descripcion, imagenUrl, tipo = null, autor = getString(Res.string.editor_autor))
                setState { current ->
                    current.copy(
                        operaciones = (listOf(creada) + current.operaciones.filterNot { it.id == creada.id })
                            .sortedByDescending { it.id },
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_operation_created)))
            } else {
                val actualizada = repository.actualizarOperacion(editing.id, titulo, descripcion, imagenUrl, tipo = null, autor = getString(Res.string.editor_autor))
                setState { current ->
                    current.copy(
                        operaciones = current.operaciones.map { if (it.id == actualizada.id) actualizada else it },
                        selectedOperacion = if (current.selectedOperacion?.id == actualizada.id) actualizada else current.selectedOperacion,
                        isEditorOpen = false,
                        isSaving = false,
                    )
                }
                emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_operation_updated)))
            }
        } catch (e: Exception) {
            setState { it.copy(isSaving = false) }
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_save_error, e.message ?: getString(Res.string.msg_network_short))))
        }
    }

    private suspend fun iniciarPago(operacion: Operacion) {
        if (operacion.guardada) {
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_already_purchased)))
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
                    emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_payment_completed)))
                } catch (e: Exception) {
                    setState { it.copy(isProcessingPayment = false) }
                    emitEffect(
                        OperacionesEffect.MostrarMensaje(
                            getString(Res.string.msg_payment_save_error, e.message ?: getString(Res.string.msg_unknown)),
                        ),
                    )
                }
            }

            is GooglePayResult.Cancelled -> {
                setState { it.copy(paymentTarget = null, isProcessingPayment = false) }
                emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_payment_cancelled)))
            }

            is GooglePayResult.Error -> {
                setState { it.copy(isProcessingPayment = false) }
                emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_payment_error, result.mensaje)))
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
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_operation_deleted)))
        } catch (e: Exception) {
            setState { it.copy(isDeleting = false, deleteTarget = null) }
            emitEffect(OperacionesEffect.MostrarMensaje(getString(Res.string.msg_delete_error, e.message ?: getString(Res.string.msg_network_short))))
        }
    }
}

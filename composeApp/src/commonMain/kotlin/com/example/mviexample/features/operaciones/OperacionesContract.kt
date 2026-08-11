package com.example.mviexample.features.operaciones

import com.example.mviexample.mvi.MviContract
import com.example.mviexample.shared.data.model.Operacion

object OperacionesContract : MviContract {

    enum class OperacionFiltro {
        Todas,
        Propias,
    }

    enum class OperacionesTab {
        Lista,
        Guardadas,
    }

    data class OperacionesState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val operaciones: List<Operacion> = emptyList(),
        val query: String = "",
        val filtro: OperacionFiltro = OperacionFiltro.Todas,
        val tab: OperacionesTab = OperacionesTab.Lista,
        val selectedOperacion: Operacion? = null,
        val editorOperacion: Operacion? = null,
        val isEditorOpen: Boolean = false,
        val isSaving: Boolean = false,
        val deleteTarget: Operacion? = null,
        val isDeleting: Boolean = false,
        val error: String? = null,
    ) : MviContract.UiState {

        val guardadasCount: Int
            get() = operaciones.count { it.guardada }

        val visibleOperaciones: List<Operacion>
            get() = operaciones.filter(::matches).filter { operacion ->
                when (tab) {
                    OperacionesTab.Lista -> when (filtro) {
                        OperacionFiltro.Todas -> true
                        OperacionFiltro.Propias -> operacion.propia
                    }

                    OperacionesTab.Guardadas -> operacion.guardada
                }
            }

        private fun matches(operacion: Operacion): Boolean {
            val q = query.trim()
            if (q.isEmpty()) return true
            return operacion.titulo.contains(q, ignoreCase = true) ||
                operacion.descripcion.contains(q, ignoreCase = true) ||
                operacion.autor?.contains(q, ignoreCase = true) == true
        }
    }

    sealed interface OperacionesIntent : MviContract.UiIntent {
        data object CargarOperaciones : OperacionesIntent
        data object RefrescarOperaciones : OperacionesIntent
        data class ActualizarQuery(val query: String) : OperacionesIntent
        data class SeleccionarFiltro(val filtro: OperacionFiltro) : OperacionesIntent
        data class SeleccionarTab(val tab: OperacionesTab) : OperacionesIntent
        data object AbrirCrear : OperacionesIntent
        data class AbrirDetalle(val operacion: Operacion) : OperacionesIntent
        data object CerrarDetalle : OperacionesIntent
        data class AbrirEditar(val operacion: Operacion) : OperacionesIntent
        data object CerrarEditor : OperacionesIntent
        data class GuardarOperacion(val titulo: String, val descripcion: String, val imagenUrl: String?) : OperacionesIntent
        data class ToggleGuardar(val operacion: Operacion) : OperacionesIntent
        data class SolicitarBorrado(val operacion: Operacion) : OperacionesIntent
        data object ConfirmarBorrado : OperacionesIntent
        data object DescartarBorrado : OperacionesIntent
        data object Reintentar : OperacionesIntent
    }

    sealed interface OperacionesEffect : MviContract.UiEffect {
        data class MostrarMensaje(val mensaje: String) : OperacionesEffect
    }
}

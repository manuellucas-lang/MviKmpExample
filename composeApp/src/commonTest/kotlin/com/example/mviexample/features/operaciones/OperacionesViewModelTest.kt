package com.example.mviexample.features.operaciones

import com.example.mviexample.features.operaciones.OperacionesContract.OperacionFiltro
import com.example.mviexample.features.operaciones.OperacionesContract.OperacionesIntent
import com.example.mviexample.shared.data.OperacionesRepository
import com.example.mviexample.shared.data.OperacionesResult
import com.example.mviexample.shared.data.model.Operacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OperacionesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleOperacion = Operacion(id = 1, titulo = "Titulo", descripcion = "Cuerpo", autor = "Leanne")

    private fun createViewModel(
        result: OperacionesResult = OperacionesResult(emptyList(), fromCache = false),
        onCreate: (Operacion) -> Operacion = { it },
        onSetGuardada: ((Long, Boolean) -> Unit)? = null,
    ) = OperacionesViewModel(
        repository = FakeOperacionesRepository(result, onCreate, onSetGuardada),
        minRefreshFeedbackMillis = 0L,
    )

    @Test
    fun initialState_usesDefaults() {
        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(emptyList<Operacion>(), viewModel.state.value.operaciones)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isEditorOpen)
        assertNull(viewModel.state.value.selectedOperacion)
        assertNull(viewModel.state.value.deleteTarget)
    }

    @Test
    fun cargarOperaciones_success_updatesState() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()

        assertEquals(listOf(sampleOperacion), viewModel.state.value.operaciones)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun cargarOperaciones_fromCache_emitsMessage() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = true))

        advanceUntilIdle()

        assertEquals(listOf(sampleOperacion), viewModel.state.value.operaciones)
        assertTrue(viewModel.effects.first() is OperacionesContract.OperacionesEffect.MostrarMensaje)
    }

    @Test
    fun cargarOperaciones_failure_setsErrorAndEmitsEffect() = runTest(dispatcher) {
        val viewModel = OperacionesViewModel(FailingOperacionesRepository(), minRefreshFeedbackMillis = 0L)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.effects.first() is OperacionesContract.OperacionesEffect.MostrarMensaje)
    }

    @Test
    fun search_queryFiltersVisibleOperaciones() = runTest(dispatcher) {
        val alpha = Operacion(1, "Alpha operacion", "Contenido sobre alpha", autor = "Leanne")
        val beta = Operacion(2, "Beta operacion", "Contenido sobre beta", autor = "Leanne")
        val viewModel = createViewModel(OperacionesResult(listOf(alpha, beta), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.ActualizarQuery("alpha"))
        advanceUntilIdle()

        assertEquals(listOf(alpha), viewModel.state.value.visibleOperaciones)
    }

    @Test
    fun filtro_propias_showsOnlyMyOperaciones() = runTest(dispatcher) {
        val remota = Operacion(1, "Remota", "Contenido", autor = "Leanne", propia = false)
        val propia = Operacion(101, "Propia", "Contenido", autor = "You", propia = true)
        val viewModel = createViewModel(OperacionesResult(listOf(remota, propia), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.SeleccionarFiltro(OperacionFiltro.Propias))
        advanceUntilIdle()

        assertEquals(listOf(propia), viewModel.state.value.visibleOperaciones)
    }

    @Test
    fun crearOperacion_addsAndClosesEditor() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirCrear)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isEditorOpen)

        viewModel.onIntent(OperacionesIntent.GuardarOperacion("Hola mundo", "Una nueva operación", "https://example.com/img.png"))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEditorOpen)
        val creada = viewModel.state.value.operaciones.first()
        assertEquals("Hola mundo", creada.titulo)
        assertEquals("Una nueva operación", creada.descripcion)
        assertTrue(creada.propia)
    }

    @Test
    fun crearOperacion_withBlankTitle_rejectsWithMessage() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirCrear)
        advanceUntilIdle()

        viewModel.onIntent(OperacionesIntent.GuardarOperacion("   ", "Cuerpo de la operación", null))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isEditorOpen)
        assertTrue(viewModel.effects.first() is OperacionesContract.OperacionesEffect.MostrarMensaje)
    }

    @Test
    fun actualizarOperacion_updatesAndSelection() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirDetalle(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirEditar(sampleOperacion))
        advanceUntilIdle()

        viewModel.onIntent(OperacionesIntent.GuardarOperacion("Titulo actualizado", "Cuerpo actualizado", null))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEditorOpen)
        assertEquals("Titulo actualizado", viewModel.state.value.operaciones.first().titulo)
        assertEquals("Titulo actualizado", viewModel.state.value.selectedOperacion?.titulo)
    }

    @Test
    fun borrarOperacion_removesAndClearsSelection() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirDetalle(sampleOperacion))
        advanceUntilIdle()

        viewModel.onIntent(OperacionesIntent.SolicitarBorrado(sampleOperacion))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.deleteTarget)

        viewModel.onIntent(OperacionesIntent.ConfirmarBorrado)
        advanceUntilIdle()

        assertEquals(emptyList<Operacion>(), viewModel.state.value.operaciones)
        assertNull(viewModel.state.value.selectedOperacion)
        assertNull(viewModel.state.value.deleteTarget)
    }

    @Test
    fun descartarBorrado_keepsOperacion() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.SolicitarBorrado(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.DescartarBorrado)
        advanceUntilIdle()

        assertEquals(listOf(sampleOperacion), viewModel.state.value.operaciones)
        assertNull(viewModel.state.value.deleteTarget)
    }

    @Test
    fun iniciarPago_showsPaymentSheet() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(sampleOperacion))
        advanceUntilIdle()

        assertEquals(sampleOperacion, viewModel.state.value.paymentTarget)
        assertFalse(viewModel.state.value.isProcessingPayment)
    }

    @Test
    fun iniciarPago_onAlreadyPurchased_doesNotOpenSheet() = runTest(dispatcher) {
        val comprada = sampleOperacion.copy(guardada = true)
        val viewModel = createViewModel(OperacionesResult(listOf(comprada), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(comprada))
        advanceUntilIdle()

        assertNull(viewModel.state.value.paymentTarget)
        assertTrue(viewModel.effects.first() is OperacionesContract.OperacionesEffect.MostrarMensaje)
    }

    @Test
    fun confirmarPago_marksGuardadaAndPersists() = runTest(dispatcher) {
        var persistedId: Long? = null
        var persistedGuardada: Boolean? = null
        val viewModel = createViewModel(
            result = OperacionesResult(listOf(sampleOperacion), fromCache = false),
            onSetGuardada = { id, guardada ->
                persistedId = id
                persistedGuardada = guardada
            },
        )

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.ConfirmarPago)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.operaciones.first().guardada)
        assertEquals(sampleOperacion.id, persistedId)
        assertEquals(true, persistedGuardada)
        assertNull(viewModel.state.value.paymentTarget)
        assertFalse(viewModel.state.value.isProcessingPayment)
        val completado = viewModel.effects.first()
        assertTrue(completado is OperacionesContract.OperacionesEffect.PagoCompletado)
        assertEquals(
            sampleOperacion.id,
            (completado as OperacionesContract.OperacionesEffect.PagoCompletado).operacion.id,
        )
    }

    @Test
    fun confirmarPago_updatesSelectedOperacion() = runTest(dispatcher) {
        val viewModel = createViewModel(OperacionesResult(listOf(sampleOperacion), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.AbrirDetalle(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.ConfirmarPago)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.selectedOperacion?.guardada == true)
    }

    @Test
    fun cancelarPago_closesSheetWithoutSaving() = runTest(dispatcher) {
        var persistCalls = 0
        val viewModel = createViewModel(
            result = OperacionesResult(listOf(sampleOperacion), fromCache = false),
            onSetGuardada = { _, _ -> persistCalls++ },
        )

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.CancelarPago)
        advanceUntilIdle()

        assertNull(viewModel.state.value.paymentTarget)
        assertFalse(viewModel.state.value.operaciones.first().guardada)
        assertEquals(0, persistCalls)
    }

    @Test
    fun tabGuardadas_showsOnlyGuardadas() = runTest(dispatcher) {
        val guardada = Operacion(1, "Guardada", "Contenido", autor = "Leanne", guardada = true)
        val otra = Operacion(2, "Otra", "Contenido", autor = "Leanne")
        val viewModel = createViewModel(OperacionesResult(listOf(guardada, otra), fromCache = false))

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.SeleccionarTab(OperacionesContract.OperacionesTab.Guardadas))
        advanceUntilIdle()

        assertEquals(listOf(guardada), viewModel.state.value.visibleOperaciones)
    }

    @Test
    fun confirmarPago_failure_keepsSheetOpenAndEmitsMessage() = runTest(dispatcher) {
        val viewModel = OperacionesViewModel(FailingOperacionesRepository(), minRefreshFeedbackMillis = 0L)

        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.IniciarPago(sampleOperacion))
        advanceUntilIdle()
        viewModel.onIntent(OperacionesIntent.ConfirmarPago)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.paymentTarget)
        assertFalse(viewModel.state.value.isProcessingPayment)
        assertFalse(viewModel.state.value.operaciones.any { it.guardada })
        assertTrue(viewModel.effects.first() is OperacionesContract.OperacionesEffect.MostrarMensaje)
    }
}

private class FakeOperacionesRepository(
    private val result: OperacionesResult,
    private val onCreate: (Operacion) -> Operacion = { it },
    private val onSetGuardada: ((Long, Boolean) -> Unit)? = null,
) : OperacionesRepository {
    override suspend fun getOperaciones(forceRefresh: Boolean): OperacionesResult = result

    override suspend fun crearOperacion(
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion =
        onCreate(
            Operacion(
                id = 101,
                titulo = titulo,
                descripcion = descripcion,
                imagenUrl = imagenUrl,
                tipo = tipo,
                autor = autor ?: "You",
                propia = true,
            ),
        )

    override suspend fun actualizarOperacion(
        id: Long,
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ): Operacion = Operacion(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        imagenUrl = imagenUrl,
        tipo = tipo,
        autor = autor ?: "You",
        propia = true,
    )

    override suspend fun borrarOperacion(id: Long) = Unit

    override suspend fun setOperacionGuardada(id: Long, guardada: Boolean) {
        onSetGuardada?.invoke(id, guardada)
    }
}

private class FailingOperacionesRepository : OperacionesRepository {
    override suspend fun getOperaciones(forceRefresh: Boolean): OperacionesResult =
        throw RuntimeException("network down")

    override suspend fun crearOperacion(
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ) = throw RuntimeException("network down")

    override suspend fun actualizarOperacion(
        id: Long,
        titulo: String,
        descripcion: String,
        imagenUrl: String?,
        tipo: String?,
        autor: String?,
    ) = throw RuntimeException("network down")

    override suspend fun borrarOperacion(id: Long) =
        throw RuntimeException("network down")

    override suspend fun setOperacionGuardada(id: Long, guardada: Boolean) =
        throw RuntimeException("network down")
}

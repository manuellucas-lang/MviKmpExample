package com.example.mviexample.features.payments

import com.example.mviexample.shared.data.model.Operacion
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class GooglePayPaymentRequest(
    val operacionId: Long,
    val titulo: String,
    val precio: Double,
    val currencyCode: String = "EUR",
    val gateway: String = "example",
    val merchantName: String = "MviKmpExample (TEST)",
    val allowedCardNetworks: List<String> = listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA"),
    val environment: String = "MOCK_TEST",
)

sealed interface GooglePayResult {
    data class Success(
        val transactionId: String,
        val paymentMethodToken: String,
        val paymentMethodDescription: String,
    ) : GooglePayResult

    data object Cancelled : GooglePayResult

    data class Error(val mensaje: String) : GooglePayResult
}

data class MockPaymentCard(
    val network: String,
    val lastDigits: String,
    val holder: String,
) {
    val description: String get() = "$network •••• $lastDigits"
}

val MockTestCards: List<MockPaymentCard> = listOf(
    MockPaymentCard(network = "Visa", lastDigits = "4242", holder = "Manuel Lucas"),
    MockPaymentCard(network = "Mastercard", lastDigits = "5454", holder = "Manuel Lucas"),
    MockPaymentCard(network = "Amex", lastDigits = "0005", holder = "Manuel Lucas"),
)

fun GooglePayPaymentRequest.formattedPrice(): String = formatEuros(precio)

fun operacionPrecio(operacion: Operacion): Double =
    ((9.99 + (operacion.id % 7) * 4.5) * 100).roundToInt() / 100.0

fun formatEuros(value: Double): String {
    val enteros = value.toInt()
    val centimos = ((value - enteros) * 100).roundToInt()
    return "$enteros,${centimos.toString().padStart(2, '0')} €"
}

class MockGooglePayGateway(
    private val processingDelayMillis: Long = 1_500L,
) {

    fun buildPaymentRequest(operacion: Operacion, precio: Double): GooglePayPaymentRequest =
        GooglePayPaymentRequest(
            operacionId = operacion.id,
            titulo = operacion.titulo,
            precio = precio,
        )

    suspend fun isReadyToPay(): Boolean {
        delay(200)
        return true
    }

    suspend fun processPayment(request: GooglePayPaymentRequest): GooglePayResult {
        delay(processingDelayMillis)
        val card = MockTestCards.first()
        return GooglePayResult.Success(
            transactionId = "MOCK-GPAY-${request.operacionId}-${request.precio}",
            paymentMethodToken = "mock_token_${request.operacionId}",
            paymentMethodDescription = card.description,
        )
    }
}

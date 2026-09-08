package com.example.mviexample.features.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUUID
import platform.darwin.NSObjectProtocol

private const val REQUEST_NAME = "AuthOperationRequest"
private const val RESPONSE_NAME = "AuthOperationResponse"
private const val STATE_NAME = "AuthStateChanged"
private const val GET_CURRENT_USER_NAME = "AuthGetCurrentUserRequest"
private const val OPERATION_TIMEOUT_SECONDS = 20

/**
 * iOS implementation that bridges to the native Firebase iOS SDK through
 * `FirebaseAuthBridge.swift` (in the iosApp target) using `NSNotificationCenter`.
 *
 * * Kotlin posts an `AuthOperationRequest` (flat string payload) and observes an
 *   `AuthOperationResponse` matching its request id.
 * * FirebaseAuthBridge pushes `AuthStateChanged` notifications whenever the auth
 *   state changes; this repository mirrors them into [authState].
 * * On init the bridge is asked to replay the current user
 *   (`AuthGetCurrentUserRequest`) so [authState] is never stuck at `null`.
 */
class IosAuthRepository : AuthRepository {

    private val center: NSNotificationCenter = NSNotificationCenter.defaultCenter

    private val _authState = MutableStateFlow<AppUser?>(null)
    override val authState: StateFlow<AppUser?> = _authState.asStateFlow()

    private val stateObserver: NSObjectProtocol = center.addObserverForName(
        name = STATE_NAME,
        `object` = null,
        queue = NSOperationQueue.mainQueue,
    ) { notification -> handleAuthState(notification) }

    init {
        // `FirebaseAuthBridge` may have emitted its initial state before this
        // repository existed, so ask it to replay the current user.
        center.postNotificationName(GET_CURRENT_USER_NAME, `object` = null, userInfo = null)
    }

    private fun handleAuthState(notification: NSNotification?) {
        _authState.value = notification?.userInfo?.toAppUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult =
        performOperation("signInWithEmail", mapOf("email" to email, "password" to password))

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult =
        performOperation("signUpWithEmail", mapOf("email" to email, "password" to password))

    override suspend fun signInWithGoogle(): AuthResult =
        performOperation("signInWithGoogle")

    override suspend fun signOut(): AuthResult =
        performOperation("signOut")

    override suspend fun sendPasswordReset(email: String): AuthResult =
        performOperation("sendPasswordReset", mapOf("email" to email))

    override suspend fun sendEmailVerification(): AuthResult =
        performOperation("sendEmailVerification")

    override suspend fun linkWithGoogle(): AuthResult =
        performOperation("linkWithGoogle")

    override suspend fun deleteAccount(): AuthResult =
        performOperation("deleteAccount")

    override suspend fun reauthenticate(password: String): AuthResult =
        performOperation("reauthenticate", mapOf("password" to password))

    override suspend fun refreshUser(): AuthResult =
        performOperation("refreshUser")

    private suspend fun performOperation(action: String, extras: Map<String, String> = emptyMap()): AuthResult =
        try {
            withTimeout(OPERATION_TIMEOUT_SECONDS.seconds) {
                requestOperation(action, extras)
            }
        } catch (e: TimeoutCancellationException) {
            AuthResult.Error(AuthErrorCode.UNKNOWN, "La operación tardó demasiado")
        } catch (e: CancellationException) {
            throw e
        }

    private suspend fun requestOperation(action: String, extras: Map<String, String>): AuthResult =
        suspendCancellableCoroutine { continuation ->
            val requestId = NSUUID().UUIDString
            var observer: NSObjectProtocol? = null

            observer = center.addObserverForName(
                name = RESPONSE_NAME,
                `object` = null,
                queue = NSOperationQueue.mainQueue,
            ) { notification ->
                val userInfo = notification?.userInfo
                if (userInfo?.get("requestId") as? String == requestId) {
                    observer?.let { center.removeObserver(it) }
                    val result = when (userInfo?.get("status") as? String) {
                        "success" -> AuthResult.Success(_authState.value)
                        "cancelled" -> AuthResult.Cancelled
                        else -> AuthResult.Error(
                            code = userInfo?.get("errorCode") as? String,
                            message = userInfo?.get("message") as? String ?: "Error de autenticación",
                        )
                    }
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            }

            continuation.invokeOnCancellation {
                observer?.let { center.removeObserver(it) }
            }

            center.postNotificationName(
                REQUEST_NAME,
                `object` = null,
                userInfo = buildMap {
                    put("requestId", requestId)
                    put("action", action)
                    putAll(extras)
                },
            )
        }
}

private fun Map<*, *>?.toAppUser(): AppUser? {
    if (this == null) return null
    val uid = this["uid"] as? String ?: return null
    return AppUser(
        uid = uid,
        email = this["email"] as? String,
        displayName = this["displayName"] as? String,
        photoUrl = this["photoUrl"] as? String,
        isEmailVerified = (this["isEmailVerified"] as? String) == "true",
        providerIds = (this["providers"] as? String)
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList(),
        isAnonymous = (this["isAnonymous"] as? String) == "true",
        creationTimestampMillis = (this["createdAt"] as? String)?.toLongOrNull(),
    )
}
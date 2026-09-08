package com.example.mviexample.features.auth

/**
 * Firebase user projected into common code. All fields are nullable on purpose:
 * Firebase allows anonymous / partially-provided accounts.
 */
data class AppUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val providerIds: List<String>,
    val isAnonymous: Boolean,
    val creationTimestampMillis: Long?,
) {
    /** Firebase uses `google.com` for the Google provider. */
    val hasGoogleProvider: Boolean get() = providerIds.contains("google.com")

    /** Firebase uses `password` for Email/Password accounts. */
    val hasEmailProvider: Boolean get() = providerIds.contains("password")

    val providerLabels: List<String>
        get() = providerIds.map { provider ->
            when (provider) {
                "google.com" -> "Google"
                "password" -> "Email / Contraseña"
                "apple.com" -> "Apple"
                "phone" -> "Teléfono"
                "anonymous" -> "Anónimo"
                else -> provider
            }
        }
}

sealed interface AuthResult {
    data class Success(val user: AppUser?) : AuthResult
    data class Error(val code: String?, val message: String) : AuthResult
    data object Cancelled : AuthResult
}

/** Well-known Firebase auth error codes surfaced by platform implementations. */
object AuthErrorCode {
    const val CANCELLED = "cancelled"
    const val EMAIL_IN_USE = "email-already-in-use"
    const val INVALID_EMAIL = "invalid-email"
    const val WRONG_PASSWORD = "wrong-password"
    const val USER_NOT_FOUND = "user-not-found"
    const val WEAK_PASSWORD = "weak-password"
    const val REQUIRES_RECENT_LOGIN = "requires-recent-login"
    const val NETWORK = "network-request-failed"
    const val UNKNOWN = "unknown"
}
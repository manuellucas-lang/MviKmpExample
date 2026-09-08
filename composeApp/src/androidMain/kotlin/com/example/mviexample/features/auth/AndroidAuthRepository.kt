package com.example.mviexample.features.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "AndroidAuthRepository"

/**
 * Android implementation backed by the official Firebase Android SDK.
 *
 * - Email / password: [FirebaseAuth].
 * - Google Sign-In: Credential Manager API (androidx.credentials + googleid), which
 *   needs a resumed [android.app.Activity] exposed via [AuthActivityHolder] and the
 *   `default_web_client_id` string resource generated from `google-services.json`
 *   (present once Google Sign-In is enabled and the file is re-downloaded).
 */
class AndroidAuthRepository(
    private val context: Context,
) : AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow(auth.currentUser?.toAppUser())
    override val authState: StateFlow<AppUser?> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authState.value = firebaseAuth.currentUser?.toAppUser()
        Log.d(TAG, "Auth state changed: ${firebaseAuth.currentUser?.uid}")
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult =
        runCatchingAuth {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(auth.currentUser?.toAppUser())
        }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult =
        runCatchingAuth {
            auth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success(auth.currentUser?.toAppUser())
        }

    override suspend fun signInWithGoogle(): AuthResult {
        val serverClientId = defaultWebClientId()
        if (serverClientId == null) {
            return AuthResult.Error(
                code = AuthErrorCode.UNKNOWN,
                message = "Google Sign-In no está configurado. Descarga de nuevo google-services.json con el inicio de sesión de Google habilitado.",
            )
        }
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(serverClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val activity = AuthActivityHolder.current
            if (activity == null) {
                AuthResult.Error(AuthErrorCode.UNKNOWN, "Actividad no disponible para el inicio de sesión con Google")
            } else {
                val response = credentialManager.getCredential(context = activity, request = request)
                val credential = response.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                    auth.signInWithCredential(firebaseCredential).await()
                    AuthResult.Success(auth.currentUser?.toAppUser())
                } else {
                    AuthResult.Error(AuthErrorCode.UNKNOWN, "Tipo de credencial no soportado")
                }
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Cancelled
        } catch (e: GetCredentialUnknownException) {
            AuthResult.Cancelled
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            AuthResult.Error(AuthErrorCode.UNKNOWN, credentialErrorMessage(e, "Error al iniciar sesión con Google"))
        } catch (e: Exception) {
            mapAuthError(e)
        }
    }

    override suspend fun signOut(): AuthResult =
        runCatchingAuth {
            auth.signOut()
            AuthResult.Success(null)
        }

    override suspend fun sendPasswordReset(email: String): AuthResult =
        runCatchingAuth {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(auth.currentUser?.toAppUser())
        }

    override suspend fun sendEmailVerification(): AuthResult =
        runCatchingAuth {
            val user = auth.currentUser ?: return@runCatchingAuth AuthResult.Error(AuthErrorCode.UNKNOWN, "No hay sesión iniciada")
            user.sendEmailVerification().await()
            AuthResult.Success(user.toAppUser())
        }

    override suspend fun linkWithGoogle(): AuthResult {
        val serverClientId = defaultWebClientId()
        val user = auth.currentUser
        if (user == null) return AuthResult.Error(AuthErrorCode.UNKNOWN, "No hay sesión iniciada")
        if (serverClientId == null) {
            return AuthResult.Error(
                code = AuthErrorCode.UNKNOWN,
                message = "Google Sign-In no está configurado. Descarga de nuevo google-services.json con el inicio de sesión de Google habilitado.",
            )
        }
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(serverClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        return try {
            val activity = AuthActivityHolder.current
            if (activity == null) {
                AuthResult.Error(AuthErrorCode.UNKNOWN, "Actividad no disponible para vincular con Google")
            } else {
                val response = credentialManager.getCredential(context = activity, request = request)
                val credential = response.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                    user.linkWithCredential(firebaseCredential).await()
                    AuthResult.Success(auth.currentUser?.toAppUser())
                } else {
                    AuthResult.Error(AuthErrorCode.UNKNOWN, "Tipo de credencial no soportado")
                }
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Cancelled
        } catch (e: GetCredentialUnknownException) {
            AuthResult.Cancelled
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            AuthResult.Error(AuthErrorCode.UNKNOWN, credentialErrorMessage(e, "Error al vincular con Google"))
        } catch (e: Exception) {
            mapAuthError(e)
        }
    }

    override suspend fun deleteAccount(): AuthResult =
        runCatchingAuth {
            val user = auth.currentUser ?: return@runCatchingAuth AuthResult.Error(AuthErrorCode.UNKNOWN, "No hay sesión iniciada")
            user.delete().await()
            AuthResult.Success(null)
        }

    override suspend fun reauthenticate(password: String): AuthResult =
        runCatchingAuth {
            val user = auth.currentUser ?: return@runCatchingAuth AuthResult.Error(AuthErrorCode.UNKNOWN, "No hay sesión iniciada")
            val email = user.email ?: return@runCatchingAuth AuthResult.Error(AuthErrorCode.UNKNOWN, "El usuario no tiene email")
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            AuthResult.Success(user.toAppUser())
        }

    override suspend fun refreshUser(): AuthResult =
        runCatchingAuth {
            val user = auth.currentUser
            if (user != null) {
                user.reload().await()
                _authState.value = auth.currentUser?.toAppUser()
            }
            AuthResult.Success(auth.currentUser?.toAppUser())
        }

    private fun defaultWebClientId(): String? {
        val res = context.resources
        val resId = res.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    private fun credentialErrorMessage(e: GetCredentialException, fallback: String): String =
        if (e is NoCredentialException) {
            "No hay ninguna cuenta de Google en este dispositivo. Añade una cuenta en Ajustes > Cuentas y vuelve a intentarlo."
        } else {
            e.message ?: fallback
        }

    private suspend fun runCatchingAuth(block: suspend () -> AuthResult): AuthResult =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mapAuthError(e)
        }

    private fun mapAuthError(e: Exception): AuthResult.Error {
        val code = when (e) {
            is FirebaseAuthWeakPasswordException -> AuthErrorCode.WEAK_PASSWORD
            is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthErrorCode.INVALID_EMAIL
                else -> AuthErrorCode.WRONG_PASSWORD
            }
            is FirebaseAuthUserCollisionException -> AuthErrorCode.EMAIL_IN_USE
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> AuthErrorCode.USER_NOT_FOUND
                "ERROR_WRONG_PASSWORD" -> AuthErrorCode.WRONG_PASSWORD
                "ERROR_INVALID_EMAIL" -> AuthErrorCode.INVALID_EMAIL
                "ERROR_EMAIL_ALREADY_IN_USE" -> AuthErrorCode.EMAIL_IN_USE
                "ERROR_WEAK_PASSWORD" -> AuthErrorCode.WEAK_PASSWORD
                "ERROR_REQUIRES_RECENT_LOGIN" -> AuthErrorCode.REQUIRES_RECENT_LOGIN
                "ERROR_NETWORK_REQUEST_FAILED" -> AuthErrorCode.NETWORK
                else -> AuthErrorCode.UNKNOWN
            }
            else -> AuthErrorCode.UNKNOWN
        }
        val message = e.message ?: "Error de autenticación"
        Log.w(TAG, "Auth error [$code]: ${e.message}")
        return AuthResult.Error(code, message)
    }
}

private fun FirebaseUser.toAppUser(): AppUser = AppUser(
    uid = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl?.toString(),
    isEmailVerified = isEmailVerified,
    providerIds = providerData
        .mapNotNull { it.providerId }
        .filterNot { it == "firebase" }
        .distinct(),
    isAnonymous = isAnonymous,
    creationTimestampMillis = metadata?.creationTimestamp,
)

/** Provides the [android.app.Application] context through the ambient context holder. */
actual fun createAuthRepository(): AuthRepository {
    val context = AuthContextHolder.context
        ?: error("AuthContextHolder.init(context) must be called before createAuthRepository()")
    return AndroidAuthRepository(context)
}

/** Holds the application [Context] so the repository factory needs no constructor args. */
object AuthContextHolder {
    @Volatile
    var context: Context? = null
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
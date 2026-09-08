package com.example.mviexample.features.auth

import androidx.lifecycle.viewModelScope
import com.example.mviexample.features.auth.AuthContract.AuthEffect
import com.example.mviexample.features.auth.AuthContract.AuthIntent
import com.example.mviexample.features.auth.AuthContract.AuthState
import com.example.mviexample.mvi.MviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mvikmpexample.composeapp.generated.resources.Res
import mvikmpexample.composeapp.generated.resources.auth_error_unknown
import mvikmpexample.composeapp.generated.resources.auth_msg_account_deleted
import mvikmpexample.composeapp.generated.resources.auth_msg_already_linked
import mvikmpexample.composeapp.generated.resources.auth_msg_email_already_verified
import mvikmpexample.composeapp.generated.resources.auth_msg_linked_google
import mvikmpexample.composeapp.generated.resources.auth_msg_reauth_needed_to_delete
import mvikmpexample.composeapp.generated.resources.auth_msg_reauth_ok
import mvikmpexample.composeapp.generated.resources.auth_msg_reset_sent
import mvikmpexample.composeapp.generated.resources.auth_msg_signed_in
import mvikmpexample.composeapp.generated.resources.auth_msg_signed_out
import mvikmpexample.composeapp.generated.resources.auth_msg_verify_email_sent
import mvikmpexample.composeapp.generated.resources.auth_validation_email_invalid
import mvikmpexample.composeapp.generated.resources.auth_validation_email_required
import mvikmpexample.composeapp.generated.resources.auth_validation_password_mismatch
import mvikmpexample.composeapp.generated.resources.auth_validation_password_short
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Resolves a string resource to its localized text.
 *
 * Injected into [AuthViewModel] instead of calling `getString(...)` directly so that
 * ViewModel tests in `commonTest` can run on both Android and iOS without a resource
 * environment (same pattern as features/operaciones).
 */
fun interface AuthMessageProvider {
    suspend operator fun invoke(resource: StringResource, vararg formatArgs: Any): String
}

@OptIn(ExperimentalResourceApi::class)
class AuthViewModel(
    private val repository: AuthRepository,
    private val messageProvider: AuthMessageProvider = AuthMessageProvider { resource, args -> getString(resource, *args) },
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(initialState = AuthState()) {

    init {
        viewModelScope.launch {
            repository.authState.collectLatest { user ->
                setState { it.copy(user = user, isInitializing = false) }
            }
        }
        onIntent(AuthIntent.Init)
    }

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Init -> refreshSession()
            is AuthIntent.SetModeLogin -> setState { it.copy(mode = AuthContract.AuthMode.Login, deleteRequiresReauth = false) }
            is AuthIntent.SetModeSignUp -> setState { it.copy(mode = AuthContract.AuthMode.SignUp, deleteRequiresReauth = false) }
            is AuthIntent.SetModeForgotPassword -> setState { it.copy(mode = AuthContract.AuthMode.ForgotPassword, deleteRequiresReauth = false) }
            is AuthIntent.SetEmail -> setState { it.copy(email = intent.email) }
            is AuthIntent.SetPassword -> setState { it.copy(password = intent.password) }
            is AuthIntent.SetConfirmPassword -> setState { it.copy(confirmPassword = intent.password) }
            is AuthIntent.SetResetEmail -> setState { it.copy(resetEmail = intent.email) }
            is AuthIntent.Submit -> submitForm()
            is AuthIntent.SignInWithGoogle -> signInWithGoogle()
            is AuthIntent.SignOut -> signOut()
            is AuthIntent.VerifyEmail -> verifyEmail()
            is AuthIntent.ResetPassword -> resetPasswordForCurrentUser()
            is AuthIntent.LinkWithGoogle -> linkWithGoogle()
            is AuthIntent.DeleteAccount -> deleteAccount()
            is AuthIntent.SetReauthPassword -> setState { it.copy(reauthPassword = intent.password) }
            is AuthIntent.ConfirmReauthAndDelete -> reauthenticateAndDelete()
            is AuthIntent.DismissReauth -> setState { it.copy(deleteRequiresReauth = false, reauthPassword = "") }
            is AuthIntent.RefreshUser -> refreshSession()
        }
    }

    private suspend fun refreshSession() {
        repository.refreshUser()
    }

    private suspend fun submitForm() {
        when (state.value.mode) {
            AuthContract.AuthMode.Login -> signInWithEmail()
            AuthContract.AuthMode.SignUp -> signUp()
            AuthContract.AuthMode.ForgotPassword -> sendPasswordReset()
        }
    }

    private suspend fun signInWithEmail() {
        val email = state.value.email.trim()
        if (email.isEmpty()) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_validation_email_required)))
            return
        }
        setState { it.copy(isSubmitting = true) }
        when (val result = repository.signInWithEmail(email, state.value.password)) {
            is AuthResult.Success -> {
                setState { it.copy(isSubmitting = false, password = "", confirmPassword = "") }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_signed_in)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isSubmitting = false) }
            is AuthResult.Error -> {
                setState { it.copy(isSubmitting = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun signUp() {
        val email = state.value.email.trim()
        val password = state.value.password
        if (email.isEmpty()) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_validation_email_required)))
            return
        }
        if (password.length < 6) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_validation_password_short)))
            return
        }
        if (password != state.value.confirmPassword) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_validation_password_mismatch)))
            return
        }
        setState { it.copy(isSubmitting = true) }
        when (val result = repository.signUpWithEmail(email, password)) {
            is AuthResult.Success -> {
                setState { it.copy(isSubmitting = false, password = "", confirmPassword = "") }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_signed_in)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isSubmitting = false) }
            is AuthResult.Error -> {
                setState { it.copy(isSubmitting = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun sendPasswordReset() {
        val email = state.value.resetEmail.trim()
        if (email.isEmpty()) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_validation_email_required)))
            return
        }
        setState { it.copy(isSubmitting = true) }
        when (val result = repository.sendPasswordReset(email)) {
            is AuthResult.Success -> {
                setState { it.copy(isSubmitting = false, mode = AuthContract.AuthMode.Login) }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_reset_sent)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isSubmitting = false) }
            is AuthResult.Error -> {
                setState { it.copy(isSubmitting = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun signInWithGoogle() {
        if (state.value.isGoogleActionInProgress) return
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.signInWithGoogle()) {
            is AuthResult.Success -> {
                setState { it.copy(isGoogleActionInProgress = false, password = "", confirmPassword = "") }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_signed_in)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun signOut() {
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.signOut()) {
            is AuthResult.Success -> {
                setState {
                    it.copy(isGoogleActionInProgress = false, password = "", confirmPassword = "", deleteRequiresReauth = false)
                }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_signed_out)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun verifyEmail() {
        val user = state.value.user ?: return
        if (!user.hasEmailProvider) return
        if (user.isEmailVerified) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_email_already_verified)))
            return
        }
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.sendEmailVerification()) {
            is AuthResult.Success -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_verify_email_sent)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun resetPasswordForCurrentUser() {
        val user = state.value.user ?: return
        if (!user.hasEmailProvider) return
        val email = user.email ?: return
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.sendPasswordReset(email)) {
            is AuthResult.Success -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_reset_sent)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun linkWithGoogle() {
        if (state.value.isGoogleActionInProgress) return
        val user = state.value.user ?: return
        if (user.hasGoogleProvider) {
            emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_already_linked)))
            return
        }
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.linkWithGoogle()) {
            is AuthResult.Success -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_linked_google)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
            }
        }
    }

    private suspend fun deleteAccount() {
        val user = state.value.user ?: return
        setState { it.copy(isGoogleActionInProgress = true) }
        when (val result = repository.deleteAccount()) {
            is AuthResult.Success -> {
                setState {
                    it.copy(
                        isGoogleActionInProgress = false,
                        password = "",
                        confirmPassword = "",
                        deleteRequiresReauth = false,
                        reauthPassword = "",
                    )
                }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_account_deleted)))
            }

            is AuthResult.Cancelled -> setState { it.copy(isGoogleActionInProgress = false) }
            is AuthResult.Error -> {
                setState { it.copy(isGoogleActionInProgress = false) }
                if (result.code == AuthErrorCode.REQUIRES_RECENT_LOGIN && (user.hasEmailProvider)) {
                    setState { it.copy(deleteRequiresReauth = true, reauthPassword = "") }
                    emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_reauth_needed_to_delete)))
                } else {
                    emitEffect(AuthEffect.MostrarMensaje(messageFor(result)))
                }
            }
        }
    }

    private suspend fun reauthenticateAndDelete() {
        val user = state.value.user ?: return
        if (!user.hasEmailProvider || state.value.reauthPassword.isEmpty()) {
            setState { it.copy(deleteRequiresReauth = false) }
            return
        }
        setState { it.copy(isSubmitting = true) }
        when (val reauth = repository.reauthenticate(state.value.reauthPassword)) {
            is AuthResult.Success -> {
                setState { it.copy(isSubmitting = false, deleteRequiresReauth = false, reauthPassword = "") }
                emitEffect(AuthEffect.MostrarMensaje(messageProvider(Res.string.auth_msg_reauth_ok)))
                deleteAccount()
            }

            is AuthResult.Cancelled -> setState { it.copy(isSubmitting = false) }
            is AuthResult.Error -> {
                setState { it.copy(isSubmitting = false) }
                emitEffect(AuthEffect.MostrarMensaje(messageFor(reauth)))
            }
        }
    }

    private suspend fun messageFor(result: AuthResult.Error): String = when (result.code) {
        AuthErrorCode.INVALID_EMAIL -> messageProvider(Res.string.auth_validation_email_invalid)
        else -> result.message.ifBlank { messageProvider(Res.string.auth_error_unknown) }
    }
}
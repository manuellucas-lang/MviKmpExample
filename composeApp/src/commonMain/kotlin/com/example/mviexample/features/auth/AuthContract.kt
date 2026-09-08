package com.example.mviexample.features.auth

import com.example.mviexample.mvi.MviContract

object AuthContract : MviContract {

    enum class AuthMode {
        Login,
        SignUp,
        ForgotPassword,
    }

    data class AuthState(
        val isInitializing: Boolean = true,
        val user: AppUser? = null,
        val mode: AuthMode = AuthMode.Login,
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val resetEmail: String = "",
        val isSubmitting: Boolean = false,
        val isGoogleActionInProgress: Boolean = false,
        val deleteRequiresReauth: Boolean = false,
        val reauthPassword: String = "",
    ) : MviContract.UiState {

        val isAuthenticated: Boolean get() = user != null

        /** True while any authentication operation is running. */
        val isBusy: Boolean get() = isSubmitting || isGoogleActionInProgress

        /** Email field that makes sense for the current [mode]. */
        val activeEmail: String
            get() = if (mode == AuthMode.ForgotPassword) resetEmail else email
    }

    sealed interface AuthIntent : MviContract.UiIntent {
        data object Init : AuthIntent
        data object SetModeLogin : AuthIntent
        data object SetModeSignUp : AuthIntent
        data object SetModeForgotPassword : AuthIntent
        data class SetEmail(val email: String) : AuthIntent
        data class SetPassword(val password: String) : AuthIntent
        data class SetConfirmPassword(val password: String) : AuthIntent
        data class SetResetEmail(val email: String) : AuthIntent

        /** Submits the current form depending on [AuthState.mode]. */
        data object Submit : AuthIntent
        data object SignInWithGoogle : AuthIntent
        data object SignOut : AuthIntent
        data object VerifyEmail : AuthIntent
        data object ResetPassword : AuthIntent
        data object LinkWithGoogle : AuthIntent
        data object DeleteAccount : AuthIntent
        data class SetReauthPassword(val password: String) : AuthIntent
        data object ConfirmReauthAndDelete : AuthIntent
        data object DismissReauth : AuthIntent
        data object RefreshUser : AuthIntent
    }

    sealed interface AuthEffect : MviContract.UiEffect {
        data class MostrarMensaje(val mensaje: String) : AuthEffect
    }
}
package com.example.mviexample.features.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mviexample.features.auth.AuthContract.AuthEffect
import com.example.mviexample.features.auth.AuthContract.AuthIntent
import com.example.mviexample.features.operaciones.OperacionesApp

/**
 * Root composable that gates the app behind Firebase Authentication:
 *
 * - While the auth state is being resolved: full-screen loader.
 * - Not signed in: [LoginScreen].
 * - Signed in: either [OperacionesApp] or [ProfileScreen] when the profile is
 *   open — only one screen is composed at a time, so they never overlap.
 */
@Composable
fun AuthRoot(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AuthViewModel = viewModel { AuthViewModel(createAuthRepository()) },
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthEffect.MostrarMensaje -> snackbarHostState.showSnackbar(effect.mensaje)
            }
        }
    }

    val actions = remember {
        AuthActions(
            onEmailChange = { viewModel.onIntent(AuthIntent.SetEmail(it)) },
            onPasswordChange = { viewModel.onIntent(AuthIntent.SetPassword(it)) },
            onConfirmPasswordChange = { viewModel.onIntent(AuthIntent.SetConfirmPassword(it)) },
            onResetEmailChange = { viewModel.onIntent(AuthIntent.SetResetEmail(it)) },
            onSubmitLogin = { viewModel.onIntent(AuthIntent.Submit) },
            onSubmitSignUp = { viewModel.onIntent(AuthIntent.Submit) },
            onSubmitReset = { viewModel.onIntent(AuthIntent.Submit) },
            onGoogle = { viewModel.onIntent(AuthIntent.SignInWithGoogle) },
            onGoLogin = { viewModel.onIntent(AuthIntent.SetModeLogin) },
            onGoSignUp = { viewModel.onIntent(AuthIntent.SetModeSignUp) },
            onGoForgot = { viewModel.onIntent(AuthIntent.SetModeForgotPassword) },
            onSignOut = { viewModel.onIntent(AuthIntent.SignOut) },
            onVerifyEmail = { viewModel.onIntent(AuthIntent.VerifyEmail) },
            onResetPassword = { viewModel.onIntent(AuthIntent.ResetPassword) },
            onLinkGoogle = { viewModel.onIntent(AuthIntent.LinkWithGoogle) },
            onDeleteAccount = { viewModel.onIntent(AuthIntent.DeleteAccount) },
            onReauthPasswordChange = { viewModel.onIntent(AuthIntent.SetReauthPassword(it)) },
            onConfirmReauthAndDelete = { viewModel.onIntent(AuthIntent.ConfirmReauthAndDelete) },
            onDismissReauth = { viewModel.onIntent(AuthIntent.DismissReauth) },
        )
    }

    var isProfileOpen by rememberSaveable { mutableStateOf(false) }

    // Close the profile if the user signs out while it is open.
    LaunchedEffect(state.user?.uid) {
        if (state.user == null) isProfileOpen = false
    }

    when {
        state.isInitializing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        state.user == null -> {
            LoginScreen(
                state = state,
                actions = actions,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.fillMaxSize(),
            )
        }

        else -> {
            if (isProfileOpen) {
                ProfileScreen(
                    state = state,
                    actions = actions,
                    snackbarHostState = snackbarHostState,
                    onClose = { isProfileOpen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                OperacionesApp(
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    onOpenProfile = { isProfileOpen = true },
                )
            }
        }
    }
}
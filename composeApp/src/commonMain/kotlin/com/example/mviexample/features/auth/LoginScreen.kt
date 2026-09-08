package com.example.mviexample.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mvikmpexample.composeapp.generated.resources.Res
import mvikmpexample.composeapp.generated.resources.auth_confirm_password
import mvikmpexample.composeapp.generated.resources.auth_email_hint
import mvikmpexample.composeapp.generated.resources.auth_go_forgot
import mvikmpexample.composeapp.generated.resources.auth_go_login
import mvikmpexample.composeapp.generated.resources.auth_go_signup
import mvikmpexample.composeapp.generated.resources.auth_google_login
import mvikmpexample.composeapp.generated.resources.auth_login_password_hint
import mvikmpexample.composeapp.generated.resources.auth_reset_submit
import mvikmpexample.composeapp.generated.resources.auth_subtitle
import mvikmpexample.composeapp.generated.resources.auth_toggle_password
import mvikmpexample.composeapp.generated.resources.auth_title
import mvikmpexample.composeapp.generated.resources.login_submit
import mvikmpexample.composeapp.generated.resources.signup_submit
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import com.example.mviexample.designsystem.components.AppButton
import com.example.mviexample.designsystem.components.AppButtonStyle
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.theme.BrandGradientEnd
import com.example.mviexample.designsystem.theme.BrandGradientStart

data class AuthActions(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
    val onResetEmailChange: (String) -> Unit = {},
    val onSubmitLogin: () -> Unit = {},
    val onSubmitSignUp: () -> Unit = {},
    val onSubmitReset: () -> Unit = {},
    val onGoogle: () -> Unit = {},
    val onGoLogin: () -> Unit = {},
    val onGoSignUp: () -> Unit = {},
    val onGoForgot: () -> Unit = {},
    val onSignOut: () -> Unit = {},
    val onVerifyEmail: () -> Unit = {},
    val onResetPassword: () -> Unit = {},
    val onLinkGoogle: () -> Unit = {},
    val onDeleteAccount: () -> Unit = {},
    val onReauthPasswordChange: (String) -> Unit = {},
    val onConfirmReauthAndDelete: () -> Unit = {},
    val onDismissReauth: () -> Unit = {},
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScreen(
    state: AuthContract.AuthState,
    actions: AuthActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(BrandGradientStart, BrandGradientEnd)),
            ),
    ) {
        AuthHero(
            title = stringResource(Res.string.auth_title),
            subtitle = stringResource(Res.string.auth_subtitle),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.mode) {
                AuthContract.AuthMode.Login -> LoginForm(state, actions)
                AuthContract.AuthMode.SignUp -> SignUpForm(state, actions)
                AuthContract.AuthMode.ForgotPassword -> ForgotPasswordForm(state, actions)
            }
            Spacer(Modifier.height(24.dp))
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun AuthHero(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun LoginForm(
    state: AuthContract.AuthState,
    actions: AuthActions,
) {
    var showPassword by remember { mutableStateOf(false) }

    AppTextField(
        value = state.email,
        onValueChange = actions.onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_email_hint),
        leadingIcon = Icons.Default.Email,
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(12.dp))
    AppTextField(
        value = state.password,
        onValueChange = actions.onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_login_password_hint),
        leadingIcon = Icons.Default.Lock,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(Res.string.auth_toggle_password),
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(20.dp))
    AppButton(
        text = stringResource(Res.string.login_submit),
        onClick = actions.onSubmitLogin,
        isLoading = state.isSubmitting,
        enabled = !state.isGoogleActionInProgress,
    )
    Spacer(Modifier.height(12.dp))
    GoogleSignInButton(
        text = stringResource(Res.string.auth_google_login),
        onClick = actions.onGoogle,
        enabled = !state.isBusy,
        isLoading = state.isGoogleActionInProgress,
    )
    Spacer(Modifier.height(16.dp))
    TextButton(onClick = actions.onGoSignUp, enabled = !state.isBusy) {
        Text(
            text = stringResource(Res.string.auth_go_signup),
            color = MaterialTheme.colorScheme.primary,
        )
    }
    TextButton(onClick = actions.onGoForgot, enabled = !state.isBusy) {
        Text(
            text = stringResource(Res.string.auth_go_forgot),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SignUpForm(
    state: AuthContract.AuthState,
    actions: AuthActions,
) {
    var showPassword by remember { mutableStateOf(false) }

    AppTextField(
        value = state.email,
        onValueChange = actions.onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_email_hint),
        leadingIcon = Icons.Default.Email,
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(12.dp))
    AppTextField(
        value = state.password,
        onValueChange = actions.onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_login_password_hint),
        leadingIcon = Icons.Default.Lock,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(Res.string.auth_toggle_password),
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(12.dp))
    AppTextField(
        value = state.confirmPassword,
        onValueChange = actions.onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_confirm_password),
        leadingIcon = Icons.Default.Lock,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(20.dp))
    AppButton(
        text = stringResource(Res.string.signup_submit),
        onClick = actions.onSubmitSignUp,
        isLoading = state.isSubmitting,
        enabled = !state.isGoogleActionInProgress,
    )
    Spacer(Modifier.height(16.dp))
    TextButton(onClick = actions.onGoLogin, enabled = !state.isBusy) {
        Text(
            text = stringResource(Res.string.auth_go_login),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ForgotPasswordForm(
    state: AuthContract.AuthState,
    actions: AuthActions,
) {
    AppTextField(
        value = state.resetEmail,
        onValueChange = actions.onResetEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.auth_email_hint),
        leadingIcon = Icons.Default.Email,
        enabled = !state.isBusy,
    )
    Spacer(Modifier.height(20.dp))
    AppButton(
        text = stringResource(Res.string.auth_reset_submit),
        onClick = actions.onSubmitReset,
        isLoading = state.isSubmitting,
    )
    Spacer(Modifier.height(16.dp))
    TextButton(onClick = actions.onGoLogin, enabled = !state.isBusy) {
        Text(
            text = stringResource(Res.string.auth_go_login),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun GoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
) {
    AppButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        style = AppButtonStyle.Outlined,
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, RectangleShape),
            ) {
                androidx.compose.material3.Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        },
        isLoading = isLoading,
    )
}
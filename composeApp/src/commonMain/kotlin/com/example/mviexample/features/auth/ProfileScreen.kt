package com.example.mviexample.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import mvikmpexample.composeapp.generated.resources.Res
import mvikmpexample.composeapp.generated.resources.auth_confirm
import mvikmpexample.composeapp.generated.resources.auth_cancel
import mvikmpexample.composeapp.generated.resources.auth_delete_confirm_message
import mvikmpexample.composeapp.generated.resources.auth_delete_confirm_title
import mvikmpexample.composeapp.generated.resources.auth_delete_account
import mvikmpexample.composeapp.generated.resources.auth_email_verified
import mvikmpexample.composeapp.generated.resources.auth_email_not_verified
import mvikmpexample.composeapp.generated.resources.auth_link_google
import mvikmpexample.composeapp.generated.resources.auth_member_since
import mvikmpexample.composeapp.generated.resources.auth_provider_label
import mvikmpexample.composeapp.generated.resources.auth_reauth_confirm
import mvikmpexample.composeapp.generated.resources.auth_reauth_hint
import mvikmpexample.composeapp.generated.resources.auth_reauth_message
import mvikmpexample.composeapp.generated.resources.auth_reauth_title
import mvikmpexample.composeapp.generated.resources.auth_reset_password
import mvikmpexample.composeapp.generated.resources.auth_sign_out
import mvikmpexample.composeapp.generated.resources.auth_uid_label
import mvikmpexample.composeapp.generated.resources.auth_verify_email
import mvikmpexample.composeapp.generated.resources.profile_title
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import com.example.mviexample.designsystem.components.AppButton
import com.example.mviexample.designsystem.components.AppButtonStyle
import com.example.mviexample.designsystem.components.AppTextField
import com.example.mviexample.designsystem.components.BrandTopBar
import com.example.mviexample.designsystem.components.ConfirmDialog
import com.example.mviexample.designsystem.components.InitialsAvatar

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProfileScreen(
    state: AuthContract.AuthState,
    actions: AuthActions,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = state.user ?: return

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        BrandTopBar(
            title = stringResource(Res.string.profile_title),
            onBack = onClose,
            useGradient = true,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            UserHeader(user, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(
                        label = stringResource(Res.string.auth_uid_label),
                        value = user.uid,
                    )
                    if (user.creationTimestampMillis != null) {
                        Spacer(Modifier.height(12.dp))
                        InfoRow(
                            label = stringResource(Res.string.auth_member_since),
                            value = formatEpochMillis(user.creationTimestampMillis),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.auth_provider_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user.providerLabels.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            if (user.hasEmailProvider && !user.isEmailVerified) {
                AppButton(
                    text = stringResource(Res.string.auth_verify_email),
                    onClick = actions.onVerifyEmail,
                    isLoading = state.isGoogleActionInProgress,
                    style = AppButtonStyle.Secondary,
                )
                Spacer(Modifier.height(10.dp))
            }
            if (user.hasEmailProvider) {
                AppButton(
                    text = stringResource(Res.string.auth_reset_password),
                    onClick = actions.onResetPassword,
                    style = AppButtonStyle.Outlined,
                )
                Spacer(Modifier.height(10.dp))
            }
            if (!user.hasGoogleProvider) {
                AppButton(
                    text = stringResource(Res.string.auth_link_google),
                    onClick = actions.onLinkGoogle,
                    isLoading = state.isGoogleActionInProgress,
                    style = AppButtonStyle.Outlined,
                )
                Spacer(Modifier.height(10.dp))
            }

            AppButton(
                text = stringResource(Res.string.auth_sign_out),
                onClick = actions.onSignOut,
                isLoading = state.isGoogleActionInProgress,
                style = AppButtonStyle.Outlined,
            )
            Spacer(Modifier.height(10.dp))

            AppButton(
                text = stringResource(Res.string.auth_delete_account),
                onClick = { showDeleteConfirmation = true },
                style = AppButtonStyle.Error,
                enabled = !state.isBusy,
            )

            Spacer(Modifier.height(16.dp))
            SnackbarHost(hostState = snackbarHostState)
        }
    }

    if (showDeleteConfirmation) {
        ConfirmDialog(
            title = stringResource(Res.string.auth_delete_confirm_title),
            message = stringResource(Res.string.auth_delete_confirm_message),
            confirmLabel = stringResource(Res.string.auth_confirm),
            dismissLabel = stringResource(Res.string.auth_cancel),
            destructive = true,
            onConfirm = {
                showDeleteConfirmation = false
                actions.onDeleteAccount()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }

    if (state.deleteRequiresReauth) {
        ReauthDialog(
            state = state,
            actions = actions,
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun UserHeader(
    user: AppUser,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!user.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = user.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RectangleShape),
            )
        } else {
            InitialsAvatar(
                name = user.displayName ?: user.email,
                size = 72.dp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: user.email ?: "Usuario",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconVerified(
                    verified = user.isEmailVerified,
                )
                Spacer(Modifier.padding(4.dp))
                Text(
                    text = stringResource(
                        if (user.isEmailVerified) Res.string.auth_email_verified else Res.string.auth_email_not_verified,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (user.isEmailVerified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun IconVerified(verified: Boolean) {
    androidx.compose.material3.Icon(
        imageVector = Icons.Default.VerifiedUser,
        contentDescription = null,
        tint = if (verified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReauthDialog(
    state: AuthContract.AuthState,
    actions: AuthActions,
) {
    var showPassword by remember { mutableStateOf(false) }

    ReauthSheet(
        title = stringResource(Res.string.auth_reauth_title),
        message = stringResource(Res.string.auth_reauth_message),
        confirmLabel = stringResource(Res.string.auth_reauth_confirm),
        dismissLabel = stringResource(Res.string.auth_cancel),
        isSubmitting = state.isSubmitting,
        password = state.reauthPassword,
        onPasswordChange = actions.onReauthPasswordChange,
        onShowPasswordToggle = { showPassword = !showPassword },
        showPassword = showPassword,
        onConfirm = actions.onConfirmReauthAndDelete,
        onDismiss = actions.onDismissReauth,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReauthSheet(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    isSubmitting: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onShowPasswordToggle: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0.dp) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.auth_reauth_hint),
                singleLine = true,
                enabled = !isSubmitting,
                visualTransformation = if (showPassword) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else {
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                },
            )
            Spacer(Modifier.height(16.dp))
            AppButton(
                text = confirmLabel,
                onClick = onConfirm,
                isLoading = isSubmitting,
                enabled = password.isNotEmpty(),
                style = AppButtonStyle.Error,
            )
            AppButton(
                text = dismissLabel,
                onClick = onDismiss,
                style = AppButtonStyle.Ghost,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
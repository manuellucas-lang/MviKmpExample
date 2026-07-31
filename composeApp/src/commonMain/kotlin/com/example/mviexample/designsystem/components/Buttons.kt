package com.example.mviexample.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppButtonStyle {
    Primary,
    Secondary,
    Error,
    Outlined,
    Ghost,
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    leadingIcon: @Composable (() -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    minHeight: Dp = 52.dp,
) {
    val shape = RoundedCornerShape(14.dp)
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.heightIn(min = minHeight),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                leadingIcon?.invoke()
                if (leadingIcon != null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }

    when (style) {
        AppButtonStyle.Primary -> Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = if (fullWidth) modifier else modifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            ),
        ) { content() }

        AppButtonStyle.Secondary -> Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
        ) { content() }

        AppButtonStyle.Error -> Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) { content() }

        AppButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier,
            shape = shape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) { content() }

        AppButtonStyle.Ghost -> TextButton(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier.height(minHeight),
        ) { content() }
    }
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = Color.Transparent,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            contentColor = contentColor,
            containerColor = containerColor,
        ),
    ) {
        icon()
    }
}

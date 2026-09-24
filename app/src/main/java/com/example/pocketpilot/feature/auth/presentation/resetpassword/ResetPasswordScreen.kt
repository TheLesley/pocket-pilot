package com.example.pocketpilot.feature.auth.presentation.resetpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotTextField
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

@Composable
fun ResetPasswordRoute(viewModel: ResetPasswordViewModel, onPasswordReset: () -> Unit, onNavigateToLogin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ResetPasswordEffect.NavigateToHome -> onPasswordReset()
                ResetPasswordEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ResetPasswordScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun ResetPasswordScreen(state: ResetPasswordState, onEvent: (ResetPasswordEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = "Set a new password",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Enter the code we sent to ${state.email} and pick a new password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.sm))

        PocketPilotTextField(
            value = state.code,
            onValueChange = { onEvent(ResetPasswordEvent.CodeChanged(it)) },
            label = "Reset code",
            keyboardType = KeyboardType.Number,
            errorMessage = state.codeError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.newPassword,
            onValueChange = { onEvent(ResetPasswordEvent.PasswordChanged(it)) },
            label = "New password",
            supportingText = "Minimum 8 characters, with a letter and a number.",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            errorMessage = state.passwordError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            errorMessage = state.confirmPasswordError,
            enabled = !state.isSubmitting
        )

        if (state.submitError != null) {
            Text(
                text = state.submitError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        PocketPilotPrimaryButton(
            text = "Update password",
            onClick = { onEvent(ResetPasswordEvent.Submit) },
            enabled = state.canSubmit,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        PocketPilotSecondaryButton(
            text = "Back to log in",
            onClick = { onEvent(ResetPasswordEvent.BackToLogin) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

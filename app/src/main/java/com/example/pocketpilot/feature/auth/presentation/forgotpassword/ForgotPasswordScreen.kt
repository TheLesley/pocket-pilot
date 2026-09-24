package com.example.pocketpilot.feature.auth.presentation.forgotpassword

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotTextField
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

@Composable
fun ForgotPasswordRoute(viewModel: ForgotPasswordViewModel, onNavigateToReset: (email: String) -> Unit, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ForgotPasswordEffect.NavigateToResetPassword -> onNavigateToReset(effect.email)
                ForgotPasswordEffect.NavigateBackToLogin -> onNavigateBack()
            }
        }
    }

    ForgotPasswordScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun ForgotPasswordScreen(state: ForgotPasswordState, onEvent: (ForgotPasswordEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = "Reset your password",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Enter the email you signed up with. We'll send a reset code.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.sm))

        PocketPilotTextField(
            value = state.email,
            onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
            label = "Email",
            keyboardType = KeyboardType.Email,
            errorMessage = state.emailError,
            enabled = !state.isSubmitting && !state.isSuccess
        )

        if (state.submitError != null) {
            Text(
                text = state.submitError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.isSuccess) {
            Text(
                text = "Reset code sent. For this demo build the code is: ${state.issuedCode}",
                style = MaterialTheme.typography.bodyMedium,
                color = PocketPilotTheme.extendedColors.income
            )
            PocketPilotPrimaryButton(
                text = "Enter reset code",
                onClick = { onEvent(ForgotPasswordEvent.ContinueToReset) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PocketPilotPrimaryButton(
                text = "Send reset code",
                onClick = { onEvent(ForgotPasswordEvent.Submit) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }

        PocketPilotSecondaryButton(
            text = "Back to log in",
            onClick = { onEvent(ForgotPasswordEvent.BackToLogin) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

package com.example.pocketpilot.feature.auth.presentation.login

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotTextField
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

@Composable
fun LoginRoute(viewModel: LoginViewModel, onLoggedIn: () -> Unit, onNavigateToSignUp: () -> Unit, onNavigateToForgotPassword: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToHome -> onLoggedIn()
                LoginEffect.NavigateToSignUp -> onNavigateToSignUp()
                LoginEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
            }
        }
    }

    LoginScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun LoginScreen(state: LoginState, onEvent: (LoginEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Log in to continue tracking your money.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.sm))

        PocketPilotTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = "Email",
            keyboardType = KeyboardType.Email,
            errorMessage = state.emailError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = "Password",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            errorMessage = state.passwordError,
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
            text = "Log in",
            onClick = { onEvent(LoginEvent.Submit) },
            enabled = state.canSubmit,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = { onEvent(LoginEvent.ForgotPasswordClicked) },
            modifier = Modifier.align(Alignment.End),
            enabled = !state.isSubmitting
        ) {
            Text("Forgot password?")
        }

        Spacer(Modifier.height(PocketPilotTheme.spacing.md))

        TextButton(
            onClick = { onEvent(LoginEvent.SignUpClicked) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !state.isSubmitting
        ) {
            Text("New here? Create an account")
        }
    }
}

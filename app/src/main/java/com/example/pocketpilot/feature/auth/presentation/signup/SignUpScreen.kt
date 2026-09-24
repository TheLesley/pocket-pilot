package com.example.pocketpilot.feature.auth.presentation.signup

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
fun SignUpRoute(viewModel: SignUpViewModel, onSignedUp: () -> Unit, onNavigateToLogin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SignUpEffect.NavigateToHome -> onSignedUp()
                SignUpEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SignUpScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun SignUpScreen(state: SignUpState, onEvent: (SignUpEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Start tracking your income and expenses in seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.sm))

        PocketPilotTextField(
            value = state.name,
            onValueChange = { onEvent(SignUpEvent.NameChanged(it)) },
            label = "Name",
            errorMessage = state.nameError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            label = "Email",
            keyboardType = KeyboardType.Email,
            errorMessage = state.emailError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            label = "Password",
            supportingText = "Minimum 8 characters, with a letter and a number.",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            errorMessage = state.passwordError,
            enabled = !state.isSubmitting
        )
        PocketPilotTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
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
            text = "Create account",
            onClick = { onEvent(SignUpEvent.Submit) },
            enabled = state.canSubmit,
            loading = state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = { onEvent(SignUpEvent.LoginClicked) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !state.isSubmitting
        ) {
            Text("Already have an account? Log in")
        }
    }
}

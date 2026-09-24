package com.example.pocketpilot.feature.security.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.R
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.feature.security.di.SecurityContainer
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Route wrapper. Owns the biometric-prompt bridge: the AndroidX prompt needs
 * a FragmentActivity, so we resolve it from [LocalContext] here and forward
 * the result back into the ViewModel.
 */
@Composable
fun LockRoute(viewModel: LockViewModel, onUnlocked: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LockEffect.Unlocked -> onUnlocked()
                LockEffect.LaunchBiometricPrompt -> if (activity != null) {
                    launchBiometricPrompt(scope, activity, viewModel)
                } else {
                    viewModel.onBiometricResult(
                        BiometricAuthResult.Error(
                            code = -1,
                            message = "Host activity does not support biometric prompt"
                        )
                    )
                }
                is LockEffect.ShowMessage -> Unit
            }
        }
    }

    // Auto-prompt biometrics on first display if enabled.
    LaunchedEffect(state.biometricEnabled) {
        if (state.biometricEnabled && !state.isAuthenticating) {
            viewModel.onEvent(LockEvent.PromptBiometric)
        }
    }

    LockScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

private fun launchBiometricPrompt(scope: CoroutineScope, activity: FragmentActivity, viewModel: LockViewModel) {
    scope.launch {
        val result = SecurityContainer.authenticateWithBiometric(
            activity = activity,
            title = "Unlock PocketPilot",
            subtitle = "Use your biometric to continue",
            negativeButtonText = "Use PIN"
        )
        viewModel.onBiometricResult(result)
    }
}

@Composable
fun LockScreen(state: LockState, onEvent: (LockEvent) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PocketPilotTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LockIcon()
            Spacer(Modifier.height(PocketPilotTheme.spacing.lg))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(PocketPilotTheme.spacing.xs))
            Text(
                text = lockSubtitleFor(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(PocketPilotTheme.spacing.xl))

            if (state.hasPin) {
                PinDots(entered = state.pinEntry.length)
                if (state.pinAttemptFailed) {
                    Spacer(Modifier.height(PocketPilotTheme.spacing.sm))
                    Text(
                        text = "Incorrect PIN, try again",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(PocketPilotTheme.spacing.lg))
                PinPad(
                    onDigit = { onEvent(LockEvent.PinDigitAppended(it)) },
                    onBackspace = { onEvent(LockEvent.PinBackspace) }
                )
            } else if (!state.biometricEnabled) {
                Text(
                    text = "No lock method is configured. Enable biometrics or a PIN in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            if (state.biometricEnabled) {
                Spacer(Modifier.height(PocketPilotTheme.spacing.lg))
                PocketPilotPrimaryButton(
                    text = if (state.isAuthenticating) "Authenticating…" else "Use biometrics",
                    onClick = { onEvent(LockEvent.PromptBiometric) },
                    enabled = !state.isAuthenticating
                )
            }

            state.biometricError?.let { msg ->
                Spacer(Modifier.height(PocketPilotTheme.spacing.sm))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun LockIcon() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun PinDots(entered: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val filled = index < entered
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun PinPad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (label in row) {
                    PinKey(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (label) {
                                "" -> Unit
                                "⌫" -> onBackspace()
                                else -> onDigit(label.single())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (label.isEmpty()) {
        Box(modifier = modifier.height(56.dp))
        return
    }
    TextButton(
        onClick = onClick,
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun lockSubtitleFor(state: LockState): String = when {
    state.hasPin && state.biometricEnabled ->
        "Use your biometric or PIN to continue"
    state.hasPin -> "Enter your PIN to continue"
    state.biometricEnabled -> "Use your biometric to continue"
    state.biometricAvailability == BiometricAvailability.NotEnrolled ->
        "Enrol a biometric in system settings, then enable it in Settings"
    else -> "Set up a lock method in Settings"
}

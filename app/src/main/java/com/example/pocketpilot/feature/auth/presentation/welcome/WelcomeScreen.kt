package com.example.pocketpilot.feature.auth.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onSignUpClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PocketPilot",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.padding(PocketPilotTheme.spacing.sm))
        Text(
            text = "Track your money. Reach your goals. All offline-first.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.padding(PocketPilotTheme.spacing.xl))
        PocketPilotPrimaryButton(
            text = "Log in",
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(PocketPilotTheme.spacing.xs))
        PocketPilotSecondaryButton(
            text = "Create an account",
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    PocketPilotTheme {
        WelcomeScreen(onLoginClick = {}, onSignUpClick = {})
    }
}

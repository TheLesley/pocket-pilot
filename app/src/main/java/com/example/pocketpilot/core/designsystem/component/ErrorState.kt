package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

/**
 * Full-viewport error state with optional retry action.
 * Use as the leaf of a screen when `UiState` is in an error branch.
 */
@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier, title: String = "Something went wrong", onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.md)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
        if (onRetry != null) {
            PocketPilotPrimaryButton(
                text = "Try again",
                onClick = onRetry,
                modifier = Modifier.padding(top = PocketPilotTheme.spacing.lg)
            )
        }
    }
}

@Preview
@Composable
private fun ErrorStatePreview() {
    PocketPilotTheme {
        ErrorState(
            message = "We couldn't load your transactions. Check your connection and try again.",
            onRetry = {}
        )
    }
}

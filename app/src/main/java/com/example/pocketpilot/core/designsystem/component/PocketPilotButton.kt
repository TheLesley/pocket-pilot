package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

private val MinButtonHeight = 48.dp

@Composable
fun PocketPilotPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinButtonHeight),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(
            horizontal = PocketPilotTheme.spacing.lg,
            vertical = PocketPilotTheme.spacing.sm
        )
    ) {
        ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
    }
}

@Composable
fun PocketPilotSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinButtonHeight),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(
            horizontal = PocketPilotTheme.spacing.lg,
            vertical = PocketPilotTheme.spacing.sm
        )
    ) {
        ButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean, leadingIcon: ImageVector?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = androidx.compose.material3.LocalContentColor.current
            )
            Spacer(Modifier.width(PocketPilotTheme.spacing.sm))
        } else if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(PocketPilotTheme.spacing.sm))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    PocketPilotTheme {
        PocketPilotPrimaryButton(text = "Save transaction", onClick = {})
    }
}

@Preview
@Composable
private fun SecondaryButtonPreview() {
    PocketPilotTheme {
        PocketPilotSecondaryButton(text = "Cancel", onClick = {})
    }
}

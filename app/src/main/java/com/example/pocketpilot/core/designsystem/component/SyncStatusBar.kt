package com.example.pocketpilot.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.core.designsystem.theme.LocalSpacing
import com.example.pocketpilot.core.sync.SyncWorkStatus
import com.example.pocketpilot.core.sync.isActive
import com.example.pocketpilot.core.sync.toDisplayLabel

/**
 * Compact banner surfaced above authenticated screens so users can see whether
 * the app currently has a background sync operation in flight. Stays hidden
 * whenever no sync is actively [SyncWorkStatus.Running] — idle, enqueued, or
 * terminal states never surface a banner — and also hides entirely when no
 * remote backend URL is configured, since there is nothing to sync against.
 */
@Composable
fun SyncStatusBar(status: SyncWorkStatus, onSyncNowClick: () -> Unit, modifier: Modifier = Modifier, hasRemoteBackend: Boolean = true,) {
    val visible = hasRemoteBackend && status is SyncWorkStatus.Running
    AnimatedVisibility(visible = visible, modifier = modifier) {
        val (background, foreground) = status.colors()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
                .padding(
                    horizontal = LocalSpacing.current.md,
                    vertical = LocalSpacing.current.sm
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm)
        ) {
            if (status.isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = foreground
                )
            }
            Text(
                text = status.toDisplayLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = foreground,
                modifier = Modifier.weight(1f)
            )
            if (status is SyncWorkStatus.Failed) {
                TextButton(onClick = onSyncNowClick) {
                    Text("Retry", color = foreground)
                }
            }
        }
    }
}

@Composable
private fun SyncWorkStatus.colors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        is SyncWorkStatus.Failed -> scheme.errorContainer to scheme.onErrorContainer
        is SyncWorkStatus.Succeeded -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        SyncWorkStatus.Cancelled -> scheme.surfaceVariant to scheme.onSurfaceVariant
        else -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
}

package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

/**
 * Primary navigation destination surfaced by [AdaptiveNavScaffold].
 *
 * `label` doubles as the accessibility content description for the item so
 * TalkBack announces the destination name when the item is focused.
 */
data class AdaptiveNavItem(val label: String, val icon: ImageVector, val selected: Boolean, val onClick: () -> Unit,)

/**
 * Adaptive shell that renders top-level navigation appropriate to the current
 * [WindowSizeClass]:
 *
 * - Compact: bottom [NavigationBar].
 * - Medium: side [NavigationRail].
 * - Expanded: a permanent left-hand rail rendered as a slim column of
 *   [NavigationDrawerItem]s so the surrounding chrome remains lightweight.
 *
 * The `content` slot receives the remaining space so it can host the current
 * screen without needing to know which chrome is visible.
 */
@Composable
fun AdaptiveNavScaffold(
    windowSizeClass: WindowSizeClass,
    items: List<AdaptiveNavItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> ExpandedShell(items = items, modifier = modifier, content = content)
        WindowWidthSizeClass.Medium -> MediumShell(items = items, modifier = modifier, content = content)
        else -> CompactShell(items = items, modifier = modifier, content = content)
    }
}

@Composable
private fun CompactShell(items: List<AdaptiveNavItem>, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) { content() }
        if (items.isNotEmpty()) {
            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = PocketPilotTheme.extendedColors.textSecondary
            HorizontalDivider(
                thickness = 1.dp,
                color = PocketPilotTheme.extendedColors.border,
            )
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = unselectedColor,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = item.selected,
                        onClick = item.onClick,
                        icon = { Icon(imageVector = item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = selectedColor.copy(alpha = 0.16f),
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = item.label
                            selected = item.selected
                            role = Role.Tab
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediumShell(items: List<AdaptiveNavItem>, modifier: Modifier, content: @Composable () -> Unit) {
    Row(modifier = modifier.fillMaxSize()) {
        NavigationRail {
            items.forEach { item ->
                NavigationRailItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    modifier = Modifier.semantics {
                        contentDescription = item.label
                        selected = item.selected
                        role = Role.Tab
                    },
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun ExpandedShell(items: List<AdaptiveNavItem>, modifier: Modifier, content: @Composable () -> Unit) {
    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(min = 240.dp)
                .fillMaxWidth(fraction = 0.22f)
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "PocketPilot",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            )
            items.forEach { item ->
                NavigationDrawerItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    modifier = Modifier.semantics {
                        contentDescription = item.label
                        selected = item.selected
                        role = Role.Tab
                    },
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

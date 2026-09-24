package com.example.pocketpilot.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.notifications.di.NotificationSettingsViewModelFactory
import com.example.pocketpilot.feature.notifications.presentation.NotificationSettingsRoute
import com.example.pocketpilot.feature.notifications.presentation.NotificationSettingsViewModel
import com.example.pocketpilot.feature.settings.di.SettingsViewModelFactory
import com.example.pocketpilot.feature.settings.presentation.currency.CurrencySelectorRoute
import com.example.pocketpilot.feature.settings.presentation.data.DataManagementRoute
import com.example.pocketpilot.feature.settings.presentation.theme.ThemeSelectorRoute

/**
 * Settings navigation graph. All destinations share a single [SettingsViewModel]
 * so a change made on the theme or currency selector is immediately visible on
 * the home screen without re-observing.
 */
sealed interface SettingsDestination {
    data object Home : SettingsDestination
    data object Theme : SettingsDestination
    data object Currency : SettingsDestination
    data object DataManagement : SettingsDestination
    data object Notifications : SettingsDestination
}

@Composable
fun SettingsNavHost() {
    var destination: SettingsDestination by rememberSaveable(stateSaver = SettingsDestinationSaver) {
        mutableStateOf(SettingsDestination.Home)
    }

    // Share one ViewModel across every destination in this graph so mutations
    // reflect immediately on the home screen without a second observation pass.
    val viewModel: SettingsViewModel = viewModel(
        key = "settings.root",
        factory = remember { SettingsViewModelFactory() }
    )

    when (destination) {
        SettingsDestination.Home -> SettingsRoute(
            viewModel = viewModel,
            onNavigateToThemeSelector = { destination = SettingsDestination.Theme },
            onNavigateToCurrencySelector = { destination = SettingsDestination.Currency },
            onNavigateToDataManagement = { destination = SettingsDestination.DataManagement },
            onNavigateToNotificationSettings = { destination = SettingsDestination.Notifications }
        )

        SettingsDestination.Theme -> ThemeSelectorRoute(
            viewModel = viewModel,
            onNavigateBack = { destination = SettingsDestination.Home }
        )

        SettingsDestination.Currency -> CurrencySelectorRoute(
            viewModel = viewModel,
            onNavigateBack = { destination = SettingsDestination.Home }
        )

        SettingsDestination.DataManagement -> DataManagementRoute(
            viewModel = viewModel,
            onNavigateBack = { destination = SettingsDestination.Home }
        )

        SettingsDestination.Notifications -> {
            val notificationViewModel: NotificationSettingsViewModel = viewModel(
                key = "settings.notifications",
                factory = remember { NotificationSettingsViewModelFactory() }
            )
            NotificationSettingsRoute(viewModel = notificationViewModel)
        }
    }
}

private val SettingsDestinationSaver = Saver<SettingsDestination, String>(
    save = { dest ->
        when (dest) {
            SettingsDestination.Home -> "home"
            SettingsDestination.Theme -> "theme"
            SettingsDestination.Currency -> "currency"
            SettingsDestination.DataManagement -> "data"
            SettingsDestination.Notifications -> "notifications"
        }
    },
    restore = { key ->
        when (key) {
            "theme" -> SettingsDestination.Theme
            "currency" -> SettingsDestination.Currency
            "data" -> SettingsDestination.DataManagement
            "notifications" -> SettingsDestination.Notifications
            else -> SettingsDestination.Home
        }
    }
)

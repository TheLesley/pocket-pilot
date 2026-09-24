package com.example.pocketpilot.feature.settings.presentation.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.settings.presentation.SettingsEvent
import com.example.pocketpilot.feature.settings.presentation.SettingsSelectableRow
import com.example.pocketpilot.feature.settings.presentation.SettingsViewModel
import java.util.Currency

/**
 * Curated shortlist of common currencies. The full ISO 4217 set (~180
 * entries) would need a search field to remain usable; a search UI is out of
 * scope for Phase 14, so we surface the ones a portfolio user is most likely
 * to want and rely on the underlying use case to accept any valid code.
 */
private val SupportedCurrencyCodes = listOf(
    "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY",
    "INR", "NGN", "ZAR", "BRL", "MXN", "SGD", "HKD", "SEK", "NOK", "NZD"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorRoute(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentCode = (state.preferences as? UiState.Success)?.data?.defaultCurrencyCode

    val currencies = remember {
        SupportedCurrencyCodes.mapNotNull { code ->
            runCatching { Currency.getInstance(code) }.getOrNull()
                ?.let { code to it.displayName }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Default currency") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentCode == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = PocketPilotTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xxs),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = PocketPilotTheme.spacing.md
                    )
                ) {
                    items(currencies, key = { it.first }) { (code, name) ->
                        Column {
                            SettingsSelectableRow(
                                title = "$code — $name",
                                selected = code == currentCode,
                                onClick = {
                                    viewModel.onEvent(SettingsEvent.CurrencySelected(code))
                                }
                            )
                        }
                    }
                    item(key = "footer") {
                        Text(
                            text = "Applied to new transactions and budgets. Existing " +
                                "records keep their original currency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(
                                horizontal = PocketPilotTheme.spacing.sm,
                                vertical = PocketPilotTheme.spacing.md
                            )
                        )
                    }
                }
            }
        }
    }
}

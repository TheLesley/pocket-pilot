package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Card that displays a currency amount with a label.
 *
 * [amountColor] lets callers tint the value using the semantic income/expense
 * colors from `PocketPilotTheme.extendedColors`.
 */
@Composable
fun CurrencyCard(
    label: String,
    amount: BigDecimal,
    modifier: Modifier = Modifier,
    currencyCode: String = "USD",
    locale: Locale = Locale.getDefault(),
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    caption: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = formatCurrency(amount, currencyCode, locale),
                style = MaterialTheme.typography.headlineMedium,
                color = amountColor,
                modifier = Modifier.padding(top = PocketPilotTheme.spacing.xs)
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = PocketPilotTheme.spacing.xxs)
                )
            }
        }
    }
}

private fun formatCurrency(amount: BigDecimal, currencyCode: String, locale: Locale): String {
    val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode)
    }
    return formatter.format(amount)
}

@Preview
@Composable
private fun CurrencyCardPreview() {
    PocketPilotTheme {
        CurrencyCard(
            label = "Balance",
            amount = BigDecimal("1284.53"),
            caption = "Updated 5 min ago"
        )
    }
}

@Preview
@Composable
private fun CurrencyCardIncomePreview() {
    PocketPilotTheme {
        CurrencyCard(
            label = "Income this month",
            amount = BigDecimal("3200.00"),
            amountColor = PocketPilotTheme.extendedColors.income
        )
    }
}

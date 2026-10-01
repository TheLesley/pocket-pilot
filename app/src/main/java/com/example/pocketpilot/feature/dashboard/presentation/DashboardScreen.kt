package com.example.pocketpilot.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.dashboard.domain.model.CategorySpend
import com.example.pocketpilot.feature.dashboard.domain.model.DashboardSummary
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToTransactionList: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DashboardEffect.NavigateToAddIncome -> onNavigateToAddIncome()
                DashboardEffect.NavigateToAddExpense -> onNavigateToAddExpense()
                DashboardEffect.NavigateToTransfer -> onNavigateToTransfer()
                DashboardEffect.NavigateToTransactionList -> onNavigateToTransactionList()
                is DashboardEffect.NavigateToTransactionDetail -> onNavigateToTransactionDetail(effect.id)
                is DashboardEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    DashboardScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onEvent: (DashboardEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = state.summary,
                label = "DashboardState",
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                }
            ) { summary ->
                when (summary) {
                    UiState.Idle,
                    UiState.Loading -> LoadingIndicator()

                    UiState.Empty -> DashboardContent(
                        summary = zeroedSummary(),
                        onEvent = onEvent
                    )

                    is UiState.Success -> DashboardContent(
                        summary = summary.data,
                        onEvent = onEvent
                    )

                    is UiState.Error -> ErrorState(
                        message = summary.message,
                        onRetry = { onEvent(DashboardEvent.Retry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardContent(summary: DashboardSummary, onEvent: (DashboardEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.md
            ),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        BalanceCard(summary = summary)
        MonthlyTotalsRow(summary = summary)
        BudgetCard(summary = summary)
        QuickActionsRow(onEvent = onEvent)
        SpendingBreakdownCard(summary = summary)
        RecentTransactionsCard(summary = summary, onEvent = onEvent)
        Spacer(Modifier.height(PocketPilotTheme.spacing.sm))
    }
}

@Composable
private fun BalanceCard(summary: DashboardSummary) {
    val amountColor = if (summary.currentBalanceMinorUnits >= 0L) {
        MaterialTheme.colorScheme.onSurface
    } else {
        PocketPilotTheme.extendedColors.expense
    }
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .border(
                    width = 1.dp,
                    color = PocketPilotTheme.extendedColors.border,
                    shape = MaterialTheme.shapes.large
                )
                .padding(
                    horizontal = PocketPilotTheme.spacing.lg,
                    vertical = PocketPilotTheme.spacing.lg
                )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)) {
                Text(
                    text = "Current balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = PocketPilotTheme.extendedColors.textSecondary
                )
                Text(
                    text = MoneyFormatter.format(
                        summary.currentBalanceMinorUnits,
                        summary.currencyCode
                    ),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = "All time",
                    style = MaterialTheme.typography.bodySmall,
                    color = PocketPilotTheme.extendedColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun MonthlyTotalsRow(summary: DashboardSummary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        MonthlyTotalCard(
            label = "Income this month",
            amountText = MoneyFormatter.format(
                summary.monthlyIncomeMinorUnits,
                summary.currencyCode
            ),
            icon = Icons.Default.KeyboardArrowDown,
            accent = PocketPilotTheme.extendedColors.income,
            modifier = Modifier.weight(1f)
        )
        MonthlyTotalCard(
            label = "Expenses this month",
            amountText = MoneyFormatter.format(
                summary.monthlyExpenseMinorUnits,
                summary.currencyCode
            ),
            icon = Icons.Default.KeyboardArrowUp,
            accent = PocketPilotTheme.extendedColors.expense,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MonthlyTotalCard(label: String, amountText: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = PocketPilotTheme.extendedColors.border,
                    shape = MaterialTheme.shapes.large
                )
                .padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = PocketPilotTheme.extendedColors.textSecondary
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }
    }
}

@Composable
private fun BudgetCard(summary: DashboardSummary) {
    val budgetSet = summary.monthlyBudgetMinorUnits > 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
        ) {
            Text(
                text = "Remaining budget",
                style = MaterialTheme.typography.labelLarge
            )
            if (!budgetSet) {
                Text(
                    text = "No budgets set for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val remaining = summary.remainingBudgetMinorUnits
                Text(
                    text = MoneyFormatter.format(remaining, summary.currencyCode),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (remaining >= 0L) {
                        PocketPilotTheme.extendedColors.income
                    } else {
                        PocketPilotTheme.extendedColors.expense
                    },
                    fontWeight = FontWeight.SemiBold
                )
                val progress = (
                    summary.monthlyExpenseMinorUnits.toFloat() /
                        summary.monthlyBudgetMinorUnits.toFloat()
                    ).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PocketPilotTheme.spacing.xs)
                )
                Text(
                    text = "Spent ${MoneyFormatter.format(summary.monthlyExpenseMinorUnits, summary.currencyCode)}" +
                        " of ${MoneyFormatter.format(summary.monthlyBudgetMinorUnits, summary.currencyCode)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(onEvent: (DashboardEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionButton(
                label = "Add income",
                icon = Icons.Default.KeyboardArrowDown,
                tint = PocketPilotTheme.extendedColors.income,
                onClick = { onEvent(DashboardEvent.AddIncomeClicked) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Add expense",
                icon = Icons.Default.KeyboardArrowUp,
                tint = PocketPilotTheme.extendedColors.expense,
                onClick = { onEvent(DashboardEvent.AddExpenseClicked) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Transfer",
                icon = Icons.Default.Refresh,
                tint = PocketPilotTheme.extendedColors.transfer,
                onClick = { onEvent(DashboardEvent.TransferClicked) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { contentDescription = label }
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = PocketPilotTheme.spacing.sm,
                vertical = PocketPilotTheme.spacing.md
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SpendingBreakdownCard(summary: DashboardSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Text(
                text = "Spending by category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (summary.categoryBreakdown.isEmpty()) {
                Text(
                    text = "No expenses recorded this month yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                summary.categoryBreakdown.forEach { spend ->
                    CategoryRow(spend = spend, currencyCode = summary.currencyCode)
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(spend: CategorySpend, currencyCode: String) {
    val accent = categoryColorFor(spend)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xxs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Text(
                    text = spend.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = MoneyFormatter.format(spend.amountMinorUnits, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = { spend.share.coerceIn(0f, 1f) },
            color = accent,
            trackColor = accent.copy(alpha = 0.16f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun categoryColorFor(spend: CategorySpend): Color {
    val ext = PocketPilotTheme.extendedColors
    val key = (spend.categoryId ?: spend.label).lowercase(Locale.getDefault())
    return when {
        listOf("food", "dining", "grocer", "restaurant").any { it in key } -> ext.categoryFood
        listOf("transport", "transit", "travel", "fuel", "uber", "taxi").any { it in key } -> ext.categoryTransport
        listOf("shop", "cloth", "retail").any { it in key } -> ext.categoryShopping
        listOf("bill", "util", "rent", "insurance").any { it in key } -> ext.categoryBills
        listOf("entertain", "media", "subscription", "movie", "music").any { it in key } -> ext.categoryEntertainment
        else -> ext.categoryOther
    }
}

@Composable
private fun RecentTransactionsCard(summary: DashboardSummary, onEvent: (DashboardEvent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = PocketPilotTheme.spacing.md,
                end = PocketPilotTheme.spacing.md,
                top = PocketPilotTheme.spacing.md,
                bottom = PocketPilotTheme.spacing.sm
            ),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { onEvent(DashboardEvent.ViewAllTransactionsClicked) }) {
                    Text("View all")
                }
            }
            if (summary.recentTransactions.isEmpty()) {
                EmptyTransactions(onEvent = onEvent)
            } else {
                summary.recentTransactions.forEach { tx ->
                    RecentTransactionRow(
                        transaction = tx,
                        onClick = { onEvent(DashboardEvent.TransactionClicked(tx.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactions(onEvent: (DashboardEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PocketPilotTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Add your first income or expense to see it here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { onEvent(DashboardEvent.AddExpenseClicked) }) {
            Text("Add a transaction")
        }
    }
}

@Composable
private fun RecentTransactionRow(transaction: Transaction, onClick: () -> Unit) {
    val typeLabel = when (transaction.type) {
        TransactionType.INCOME -> "Income"
        TransactionType.EXPENSE -> "Expense"
    }
    val a11yLabel = "$typeLabel · ${transaction.title} · ${signedAmount(transaction)}"
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) { contentDescription = a11yLabel }
    ) {
        Row(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
        ) {
            TypeDot(type = transaction.type)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xxs)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateFormatter.formatShort(transaction.occurredAtEpochMillis) +
                        (transaction.categoryId?.let { " • $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = PocketPilotTheme.extendedColors.textSecondary
                )
            }
            Text(
                text = signedAmount(transaction),
                style = MaterialTheme.typography.titleMedium,
                color = amountColor(transaction.type),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TypeDot(type: TransactionType) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(amountColor(type))
    )
}

@Composable
private fun amountColor(type: TransactionType): Color = when (type) {
    TransactionType.INCOME -> PocketPilotTheme.extendedColors.income
    TransactionType.EXPENSE -> PocketPilotTheme.extendedColors.expense
}

private fun signedAmount(transaction: Transaction): String {
    val amount = MoneyFormatter.format(transaction.amountMinorUnits, transaction.currencyCode)
    val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    return "$prefix$amount"
}

private fun zeroedSummary(): DashboardSummary = DashboardSummary(
    currencyCode = runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }
        .getOrElse { "USD" },
    currentBalanceMinorUnits = 0L,
    monthlyIncomeMinorUnits = 0L,
    monthlyExpenseMinorUnits = 0L,
    monthlyBudgetMinorUnits = 0L,
    categoryBreakdown = emptyList(),
    recentTransactions = emptyList(),
    monthRange = com.example.pocketpilot.feature.dashboard.domain.model.MonthRange.containing(
        System.currentTimeMillis()
    )
)

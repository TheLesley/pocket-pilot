package com.example.pocketpilot.feature.dashboard.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.dashboard.domain.usecase.CalculateBalanceUseCase
import com.example.pocketpilot.feature.dashboard.domain.usecase.CalculateMonthlyBudgetUseCase
import com.example.pocketpilot.feature.dashboard.domain.usecase.CalculateMonthlyTotalsUseCase
import com.example.pocketpilot.feature.dashboard.domain.usecase.CalculateSpendingByCategoryUseCase
import com.example.pocketpilot.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.example.pocketpilot.feature.dashboard.presentation.DashboardViewModel
import com.example.pocketpilot.feature.finance.di.FinanceContainer

/**
 * Service locator for the dashboard feature. Mirrors the shape of the eventual
 * Hilt module (`repositories → pure calculators → orchestrator → view model`)
 * so the swap to `@Module`/`@Provides` bindings is mechanical.
 */
object DashboardContainer {

    private val calculateBalance by lazy { CalculateBalanceUseCase() }
    private val calculateMonthlyTotals by lazy { CalculateMonthlyTotalsUseCase() }
    private val calculateMonthlyBudget by lazy { CalculateMonthlyBudgetUseCase() }
    private val calculateSpendingByCategory by lazy { CalculateSpendingByCategoryUseCase() }

    val observeDashboardSummary: ObserveDashboardSummaryUseCase by lazy {
        ObserveDashboardSummaryUseCase(
            transactionRepository = FinanceContainer.transactionRepository,
            budgetRepository = FinanceContainer.budgetRepository,
            calculateBalance = calculateBalance,
            calculateMonthlyTotals = calculateMonthlyTotals,
            calculateMonthlyBudget = calculateMonthlyBudget,
            calculateSpendingByCategory = calculateSpendingByCategory
        )
    }
}

class DashboardViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return DashboardViewModel(DashboardContainer.observeDashboardSummary) as T
    }
}

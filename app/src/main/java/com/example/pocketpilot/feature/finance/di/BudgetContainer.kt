package com.example.pocketpilot.feature.finance.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.finance.domain.usecase.AddBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.CalculateBudgetSpentUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetProgressUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetsProgressUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetsUseCase
import com.example.pocketpilot.feature.finance.presentation.budget.detail.BudgetDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.budget.form.BudgetFormMode
import com.example.pocketpilot.feature.finance.presentation.budget.form.BudgetFormViewModel
import com.example.pocketpilot.feature.finance.presentation.budget.list.BudgetListViewModel

/**
 * Service locator for the budget feature. Mirrors [TransactionContainer] so
 * that when Hilt wiring lands the swap is mechanical (repository → use cases
 * → view models).
 */
object BudgetContainer {

    val calculateBudgetSpentUseCase by lazy { CalculateBudgetSpentUseCase() }

    val observeBudgetsUseCase by lazy {
        ObserveBudgetsUseCase(FinanceContainer.budgetRepository)
    }
    val observeBudgetUseCase by lazy {
        ObserveBudgetUseCase(FinanceContainer.budgetRepository)
    }
    val getBudgetUseCase by lazy {
        GetBudgetUseCase(FinanceContainer.budgetRepository)
    }
    val addBudgetUseCase by lazy {
        AddBudgetUseCase(FinanceContainer.budgetRepository)
    }
    val editBudgetUseCase by lazy {
        EditBudgetUseCase(FinanceContainer.budgetRepository)
    }
    val deleteBudgetUseCase by lazy {
        DeleteBudgetUseCase(FinanceContainer.budgetRepository)
    }
    val observeBudgetsProgressUseCase by lazy {
        ObserveBudgetsProgressUseCase(
            budgetRepository = FinanceContainer.budgetRepository,
            transactionRepository = FinanceContainer.transactionRepository,
            calculateBudgetSpent = calculateBudgetSpentUseCase
        )
    }
    val observeBudgetProgressUseCase by lazy {
        ObserveBudgetProgressUseCase(
            budgetRepository = FinanceContainer.budgetRepository,
            transactionRepository = FinanceContainer.transactionRepository,
            calculateBudgetSpent = calculateBudgetSpentUseCase
        )
    }
}

class BudgetListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BudgetListViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return BudgetListViewModel(BudgetContainer.observeBudgetsProgressUseCase) as T
    }
}

class BudgetDetailViewModelFactory(private val budgetId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BudgetDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return BudgetDetailViewModel(
            budgetId = budgetId,
            observeBudgetProgressUseCase = BudgetContainer.observeBudgetProgressUseCase,
            deleteBudgetUseCase = BudgetContainer.deleteBudgetUseCase
        ) as T
    }
}

class BudgetFormViewModelFactory(private val mode: BudgetFormMode) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BudgetFormViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return BudgetFormViewModel(
            initialMode = mode,
            addBudgetUseCase = BudgetContainer.addBudgetUseCase,
            editBudgetUseCase = BudgetContainer.editBudgetUseCase,
            getBudgetUseCase = BudgetContainer.getBudgetUseCase
        ) as T
    }
}

package com.example.pocketpilot.feature.finance.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.finance.domain.usecase.AddSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.AdjustSavingsContributionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.MarkSavingsGoalCompleteUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveSavingsGoalProgressUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveSavingsGoalsProgressUseCase
import com.example.pocketpilot.feature.finance.presentation.savings.detail.SavingsGoalDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.savings.form.SavingsGoalFormMode
import com.example.pocketpilot.feature.finance.presentation.savings.form.SavingsGoalFormViewModel
import com.example.pocketpilot.feature.finance.presentation.savings.list.SavingsGoalListViewModel

/**
 * Service locator for the savings goal feature. Mirrors [BudgetContainer] so
 * that when Hilt lands the swap is mechanical (repository → use cases → view
 * models).
 */
object SavingsGoalContainer {

    val observeSavingsGoalsProgressUseCase by lazy {
        ObserveSavingsGoalsProgressUseCase(FinanceContainer.savingsGoalRepository)
    }
    val observeSavingsGoalProgressUseCase by lazy {
        ObserveSavingsGoalProgressUseCase(FinanceContainer.savingsGoalRepository)
    }
    val getSavingsGoalUseCase by lazy {
        GetSavingsGoalUseCase(FinanceContainer.savingsGoalRepository)
    }
    val addSavingsGoalUseCase by lazy {
        AddSavingsGoalUseCase(FinanceContainer.savingsGoalRepository)
    }
    val editSavingsGoalUseCase by lazy {
        EditSavingsGoalUseCase(FinanceContainer.savingsGoalRepository)
    }
    val deleteSavingsGoalUseCase by lazy {
        DeleteSavingsGoalUseCase(FinanceContainer.savingsGoalRepository)
    }
    val adjustSavingsContributionUseCase by lazy {
        AdjustSavingsContributionUseCase(FinanceContainer.savingsGoalRepository)
    }
    val markSavingsGoalCompleteUseCase by lazy {
        MarkSavingsGoalCompleteUseCase(FinanceContainer.savingsGoalRepository)
    }
}

class SavingsGoalListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SavingsGoalListViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SavingsGoalListViewModel(
            observeSavingsGoalsProgressUseCase =
            SavingsGoalContainer.observeSavingsGoalsProgressUseCase
        ) as T
    }
}

class SavingsGoalDetailViewModelFactory(private val goalId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SavingsGoalDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SavingsGoalDetailViewModel(
            goalId = goalId,
            observeProgress = SavingsGoalContainer.observeSavingsGoalProgressUseCase,
            adjustContribution = SavingsGoalContainer.adjustSavingsContributionUseCase,
            markComplete = SavingsGoalContainer.markSavingsGoalCompleteUseCase,
            deleteGoal = SavingsGoalContainer.deleteSavingsGoalUseCase
        ) as T
    }
}

class SavingsGoalFormViewModelFactory(private val mode: SavingsGoalFormMode) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SavingsGoalFormViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SavingsGoalFormViewModel(
            initialMode = mode,
            addSavingsGoalUseCase = SavingsGoalContainer.addSavingsGoalUseCase,
            editSavingsGoalUseCase = SavingsGoalContainer.editSavingsGoalUseCase,
            getSavingsGoalUseCase = SavingsGoalContainer.getSavingsGoalUseCase
        ) as T
    }
}

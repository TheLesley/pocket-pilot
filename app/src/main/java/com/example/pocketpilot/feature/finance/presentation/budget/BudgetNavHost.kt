package com.example.pocketpilot.feature.finance.presentation.budget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.finance.di.BudgetDetailViewModelFactory
import com.example.pocketpilot.feature.finance.di.BudgetFormViewModelFactory
import com.example.pocketpilot.feature.finance.di.BudgetListViewModelFactory
import com.example.pocketpilot.feature.finance.presentation.budget.detail.BudgetDetailRoute
import com.example.pocketpilot.feature.finance.presentation.budget.detail.BudgetDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.budget.form.BudgetFormMode
import com.example.pocketpilot.feature.finance.presentation.budget.form.BudgetFormRoute
import com.example.pocketpilot.feature.finance.presentation.budget.form.BudgetFormViewModel
import com.example.pocketpilot.feature.finance.presentation.budget.list.BudgetListRoute
import com.example.pocketpilot.feature.finance.presentation.budget.list.BudgetListViewModel

sealed interface BudgetDestination {
    data object List : BudgetDestination
    data class Detail(val id: String) : BudgetDestination
    data class Form(val mode: BudgetFormMode) : BudgetDestination
}

@Composable
fun BudgetNavHost() {
    var destination: BudgetDestination by rememberSaveable(stateSaver = BudgetDestinationSaver) {
        mutableStateOf(BudgetDestination.List)
    }

    when (val current = destination) {
        BudgetDestination.List -> {
            val vm: BudgetListViewModel = viewModel(
                key = "budgets.list",
                factory = remember { BudgetListViewModelFactory() }
            )
            BudgetListRoute(
                viewModel = vm,
                onNavigateToAdd = { destination = BudgetDestination.Form(BudgetFormMode.Add) },
                onNavigateToDetail = { destination = BudgetDestination.Detail(it) }
            )
        }
        is BudgetDestination.Detail -> {
            val id = current.id
            val vm: BudgetDetailViewModel = viewModel(
                key = "budgets.detail.$id",
                factory = remember(id) { BudgetDetailViewModelFactory(id) }
            )
            BudgetDetailRoute(
                viewModel = vm,
                onNavigateBack = { destination = BudgetDestination.List },
                onNavigateToEdit = { editId ->
                    destination = BudgetDestination.Form(BudgetFormMode.Edit(editId))
                }
            )
        }
        is BudgetDestination.Form -> {
            val mode = current.mode
            val key = when (mode) {
                BudgetFormMode.Add -> "budgets.form.add"
                is BudgetFormMode.Edit -> "budgets.form.edit.${mode.id}"
            }
            val vm: BudgetFormViewModel = viewModel(
                key = key,
                factory = remember(mode) { BudgetFormViewModelFactory(mode) }
            )
            BudgetFormRoute(
                viewModel = vm,
                onSaved = { destination = BudgetDestination.List },
                onCancelled = { destination = BudgetDestination.List }
            )
        }
    }
}

private val BudgetDestinationSaver = Saver<BudgetDestination, List<String>>(
    save = { dest ->
        when (dest) {
            BudgetDestination.List -> listOf("list")
            is BudgetDestination.Detail -> listOf("detail", dest.id)
            is BudgetDestination.Form -> when (val mode = dest.mode) {
                BudgetFormMode.Add -> listOf("form", "add")
                is BudgetFormMode.Edit -> listOf("form", "edit", mode.id)
            }
        }
    },
    restore = { parts ->
        when (parts.firstOrNull()) {
            "list" -> BudgetDestination.List
            "detail" -> parts.getOrNull(1)?.let { BudgetDestination.Detail(it) }
            "form" -> when (parts.getOrNull(1)) {
                "add" -> BudgetDestination.Form(BudgetFormMode.Add)
                "edit" -> parts.getOrNull(2)?.let {
                    BudgetDestination.Form(BudgetFormMode.Edit(it))
                }
                else -> null
            }
            else -> null
        }
    }
)

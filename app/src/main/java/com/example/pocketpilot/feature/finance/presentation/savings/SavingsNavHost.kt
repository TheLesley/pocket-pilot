package com.example.pocketpilot.feature.finance.presentation.savings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.finance.di.SavingsGoalDetailViewModelFactory
import com.example.pocketpilot.feature.finance.di.SavingsGoalFormViewModelFactory
import com.example.pocketpilot.feature.finance.di.SavingsGoalListViewModelFactory
import com.example.pocketpilot.feature.finance.presentation.savings.detail.SavingsGoalDetailRoute
import com.example.pocketpilot.feature.finance.presentation.savings.detail.SavingsGoalDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.savings.form.SavingsGoalFormMode
import com.example.pocketpilot.feature.finance.presentation.savings.form.SavingsGoalFormRoute
import com.example.pocketpilot.feature.finance.presentation.savings.form.SavingsGoalFormViewModel
import com.example.pocketpilot.feature.finance.presentation.savings.list.SavingsGoalListRoute
import com.example.pocketpilot.feature.finance.presentation.savings.list.SavingsGoalListViewModel

sealed interface SavingsDestination {
    data object List : SavingsDestination
    data class Detail(val id: String) : SavingsDestination
    data class Form(val mode: SavingsGoalFormMode) : SavingsDestination
}

@Composable
fun SavingsNavHost() {
    var destination: SavingsDestination by rememberSaveable(stateSaver = SavingsDestinationSaver) {
        mutableStateOf(SavingsDestination.List)
    }

    when (val current = destination) {
        SavingsDestination.List -> {
            val vm: SavingsGoalListViewModel = viewModel(
                key = "savings.list",
                factory = remember { SavingsGoalListViewModelFactory() }
            )
            SavingsGoalListRoute(
                viewModel = vm,
                onNavigateToAdd = {
                    destination = SavingsDestination.Form(SavingsGoalFormMode.Add)
                },
                onNavigateToDetail = { destination = SavingsDestination.Detail(it) }
            )
        }
        is SavingsDestination.Detail -> {
            val id = current.id
            val vm: SavingsGoalDetailViewModel = viewModel(
                key = "savings.detail.$id",
                factory = remember(id) { SavingsGoalDetailViewModelFactory(id) }
            )
            SavingsGoalDetailRoute(
                viewModel = vm,
                onNavigateBack = { destination = SavingsDestination.List },
                onNavigateToEdit = { editId ->
                    destination = SavingsDestination.Form(SavingsGoalFormMode.Edit(editId))
                }
            )
        }
        is SavingsDestination.Form -> {
            val mode = current.mode
            val key = when (mode) {
                SavingsGoalFormMode.Add -> "savings.form.add"
                is SavingsGoalFormMode.Edit -> "savings.form.edit.${mode.id}"
            }
            val vm: SavingsGoalFormViewModel = viewModel(
                key = key,
                factory = remember(mode) { SavingsGoalFormViewModelFactory(mode) }
            )
            SavingsGoalFormRoute(
                viewModel = vm,
                onSaved = { destination = SavingsDestination.List },
                onCancelled = { destination = SavingsDestination.List }
            )
        }
    }
}

private val SavingsDestinationSaver = Saver<SavingsDestination, List<String>>(
    save = { dest ->
        when (dest) {
            SavingsDestination.List -> listOf("list")
            is SavingsDestination.Detail -> listOf("detail", dest.id)
            is SavingsDestination.Form -> when (val mode = dest.mode) {
                SavingsGoalFormMode.Add -> listOf("form", "add")
                is SavingsGoalFormMode.Edit -> listOf("form", "edit", mode.id)
            }
        }
    },
    restore = { parts ->
        when (parts.firstOrNull()) {
            "list" -> SavingsDestination.List
            "detail" -> parts.getOrNull(1)?.let { SavingsDestination.Detail(it) }
            "form" -> when (parts.getOrNull(1)) {
                "add" -> SavingsDestination.Form(SavingsGoalFormMode.Add)
                "edit" -> parts.getOrNull(2)?.let {
                    SavingsDestination.Form(SavingsGoalFormMode.Edit(it))
                }
                else -> null
            }
            else -> null
        }
    }
)

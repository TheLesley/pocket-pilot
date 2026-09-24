package com.example.pocketpilot.feature.finance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.finance.di.TransactionDetailViewModelFactory
import com.example.pocketpilot.feature.finance.di.TransactionFormViewModelFactory
import com.example.pocketpilot.feature.finance.di.TransactionListViewModelFactory
import com.example.pocketpilot.feature.finance.presentation.detail.TransactionDetailRoute
import com.example.pocketpilot.feature.finance.presentation.detail.TransactionDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.form.FormMode
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormRoute
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormViewModel
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListRoute
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListViewModel

sealed interface TransactionDestination {
    data object List : TransactionDestination
    data class Detail(val id: String) : TransactionDestination
    data class Form(val mode: FormMode) : TransactionDestination
}

@Composable
fun TransactionNavHost() {
    var destination: TransactionDestination by rememberSaveable(
        stateSaver = TransactionDestinationSaver
    ) { mutableStateOf(TransactionDestination.List) }

    when (val current = destination) {
        TransactionDestination.List -> {
            val vm: TransactionListViewModel = viewModel(
                key = "transactions.list",
                factory = remember { TransactionListViewModelFactory() }
            )
            TransactionListRoute(
                viewModel = vm,
                onNavigateToAdd = { destination = TransactionDestination.Form(FormMode.Add) },
                onNavigateToDetail = { destination = TransactionDestination.Detail(it) }
            )
        }
        is TransactionDestination.Detail -> {
            val id = current.id
            val vm: TransactionDetailViewModel = viewModel(
                key = "transactions.detail.$id",
                factory = remember(id) { TransactionDetailViewModelFactory(id) }
            )
            TransactionDetailRoute(
                viewModel = vm,
                onNavigateBack = { destination = TransactionDestination.List },
                onNavigateToEdit = { editId ->
                    destination = TransactionDestination.Form(FormMode.Edit(editId))
                }
            )
        }
        is TransactionDestination.Form -> {
            val mode = current.mode
            val key = when (mode) {
                FormMode.Add -> "transactions.form.add"
                is FormMode.Edit -> "transactions.form.edit.${mode.id}"
            }
            val vm: TransactionFormViewModel = viewModel(
                key = key,
                factory = remember(mode) { TransactionFormViewModelFactory(mode) }
            )
            TransactionFormRoute(
                viewModel = vm,
                onSaved = { destination = TransactionDestination.List },
                onCancelled = { destination = TransactionDestination.List }
            )
        }
    }
}

private val TransactionDestinationSaver = androidx.compose.runtime.saveable.Saver<TransactionDestination, List<String>>(
    save = { dest ->
        when (dest) {
            TransactionDestination.List -> listOf("list")
            is TransactionDestination.Detail -> listOf("detail", dest.id)
            is TransactionDestination.Form -> when (val mode = dest.mode) {
                FormMode.Add -> listOf("form", "add")
                is FormMode.Edit -> listOf("form", "edit", mode.id)
            }
        }
    },
    restore = { parts ->
        when (parts.firstOrNull()) {
            "list" -> TransactionDestination.List
            "detail" -> parts.getOrNull(1)?.let { TransactionDestination.Detail(it) }
            "form" -> when (parts.getOrNull(1)) {
                "add" -> TransactionDestination.Form(FormMode.Add)
                "edit" -> parts.getOrNull(2)?.let { TransactionDestination.Form(FormMode.Edit(it)) }
                else -> null
            }
            else -> null
        }
    }
)

package com.example.pocketpilot.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.dashboard.di.DashboardViewModelFactory
import com.example.pocketpilot.feature.finance.di.TransactionDetailViewModelFactory
import com.example.pocketpilot.feature.finance.di.TransactionFormViewModelFactory
import com.example.pocketpilot.feature.finance.di.TransactionListViewModelFactory
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.detail.TransactionDetailRoute
import com.example.pocketpilot.feature.finance.presentation.detail.TransactionDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.form.FormMode
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormEvent
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormRoute
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormViewModel
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListRoute
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListViewModel

/**
 * Post-authentication navigation graph. Starts at the dashboard and can push
 * into the transaction list, detail, or form flows. Kept as a plain sealed
 * hierarchy so the feature works without depending on `androidx.navigation` —
 * that dependency will be introduced when the full app graph lands.
 */
sealed interface HomeDestination {
    /** Stable identifier for the top-level tab this destination belongs to. */
    val tab: HomeTab

    data object Dashboard : HomeDestination {
        override val tab: HomeTab = HomeTab.Dashboard
    }
    data object TransactionList : HomeDestination {
        override val tab: HomeTab = HomeTab.Transactions
    }
    data class TransactionDetail(val id: String) : HomeDestination {
        override val tab: HomeTab = HomeTab.Transactions
    }
    data class TransactionForm(val mode: FormMode, val prefillType: TransactionType? = null) : HomeDestination {
        override val tab: HomeTab = HomeTab.Transactions
    }
}

/** Primary navigation tabs surfaced by the adaptive shell. */
enum class HomeTab { Dashboard, Transactions }

/**
 * Hoistable controller so the adaptive navigation shell above [HomeNavHost]
 * can observe the current destination and switch tabs.
 */
class HomeNavController internal constructor(private val state: MutableState<HomeDestination>) {
    val destination: HomeDestination get() = state.value

    fun navigateTo(destination: HomeDestination) {
        state.value = destination
    }

    fun selectTab(tab: HomeTab) {
        state.value = when (tab) {
            HomeTab.Dashboard -> HomeDestination.Dashboard
            HomeTab.Transactions -> HomeDestination.TransactionList
        }
    }

    internal fun setDestination(destination: HomeDestination) {
        state.value = destination
    }
}

@Composable
fun rememberHomeNavController(): HomeNavController {
    val state = rememberSaveable(stateSaver = HomeDestinationSaver) {
        mutableStateOf<HomeDestination>(HomeDestination.Dashboard)
    }
    return remember(state) { HomeNavController(state) }
}

@Composable
fun HomeNavHost(controller: HomeNavController = rememberHomeNavController()) {
    val destination = controller.destination

    AnimatedContent(
        targetState = destination,
        label = "HomeNavHost",
        transitionSpec = {
            (
                slideInHorizontally(animationSpec = tween(220)) { it / 6 } +
                    fadeIn(animationSpec = tween(220))
                ) togetherWith
                (
                    slideOutHorizontally(animationSpec = tween(180)) { -it / 8 } +
                        fadeOut(animationSpec = tween(180))
                    )
        },
    ) { current ->
        when (current) {
            HomeDestination.Dashboard -> {
                val vm: DashboardViewModel = viewModel(
                    key = "home.dashboard",
                    factory = remember { DashboardViewModelFactory() },
                )
                DashboardRoute(
                    viewModel = vm,
                    onNavigateToAddIncome = {
                        controller.setDestination(
                            HomeDestination.TransactionForm(
                                mode = FormMode.Add,
                                prefillType = TransactionType.INCOME,
                            ),
                        )
                    },
                    onNavigateToAddExpense = {
                        controller.setDestination(
                            HomeDestination.TransactionForm(
                                mode = FormMode.Add,
                                prefillType = TransactionType.EXPENSE,
                            ),
                        )
                    },
                    onNavigateToTransfer = {
                        // Transfers aren't a separate transaction type in the domain
                        // model yet; route the user through the standard "add" flow
                        // for now so the shortcut still has a landing screen.
                        controller.setDestination(
                            HomeDestination.TransactionForm(
                                mode = FormMode.Add,
                                prefillType = null,
                            ),
                        )
                    },
                    onNavigateToTransactionList = {
                        controller.setDestination(HomeDestination.TransactionList)
                    },
                    onNavigateToTransactionDetail = { id ->
                        controller.setDestination(HomeDestination.TransactionDetail(id))
                    },
                )
            }
            HomeDestination.TransactionList -> {
                val vm: TransactionListViewModel = viewModel(
                    key = "home.transactions.list",
                    factory = remember { TransactionListViewModelFactory() },
                )
                TransactionListRoute(
                    viewModel = vm,
                    onNavigateToAdd = {
                        controller.setDestination(HomeDestination.TransactionForm(mode = FormMode.Add))
                    },
                    onNavigateToDetail = { id ->
                        controller.setDestination(HomeDestination.TransactionDetail(id))
                    },
                )
            }
            is HomeDestination.TransactionDetail -> {
                val id = current.id
                val vm: TransactionDetailViewModel = viewModel(
                    key = "home.transactions.detail.$id",
                    factory = remember(id) { TransactionDetailViewModelFactory(id) },
                )
                TransactionDetailRoute(
                    viewModel = vm,
                    onNavigateBack = { controller.setDestination(HomeDestination.Dashboard) },
                    onNavigateToEdit = { editId ->
                        controller.setDestination(HomeDestination.TransactionForm(mode = FormMode.Edit(editId)))
                    },
                )
            }
            is HomeDestination.TransactionForm -> {
                val mode = current.mode
                val prefill = current.prefillType
                val key = when (mode) {
                    FormMode.Add -> "home.transactions.form.add.${prefill?.name ?: "any"}"
                    is FormMode.Edit -> "home.transactions.form.edit.${mode.id}"
                }
                val vm: TransactionFormViewModel = viewModel(
                    key = key,
                    factory = remember(mode) { TransactionFormViewModelFactory(mode) },
                )
                if (prefill != null && mode is FormMode.Add) {
                    androidx.compose.runtime.LaunchedEffect(prefill, vm) {
                        vm.onEvent(TransactionFormEvent.TypeChanged(prefill))
                    }
                }
                TransactionFormRoute(
                    viewModel = vm,
                    onSaved = { controller.setDestination(HomeDestination.Dashboard) },
                    onCancelled = { controller.setDestination(HomeDestination.Dashboard) },
                )
            }
        }
    }
}

private val HomeDestinationSaver = Saver<HomeDestination, List<String>>(
    save = { dest ->
        when (dest) {
            HomeDestination.Dashboard -> listOf("dashboard")
            HomeDestination.TransactionList -> listOf("txList")
            is HomeDestination.TransactionDetail -> listOf("txDetail", dest.id)
            is HomeDestination.TransactionForm -> when (val mode = dest.mode) {
                FormMode.Add -> listOf("txForm", "add", dest.prefillType?.name ?: "")
                is FormMode.Edit -> listOf("txForm", "edit", mode.id)
            }
        }
    },
    restore = { parts ->
        when (parts.firstOrNull()) {
            "dashboard" -> HomeDestination.Dashboard
            "txList" -> HomeDestination.TransactionList
            "txDetail" -> parts.getOrNull(1)?.let { HomeDestination.TransactionDetail(it) }
            "txForm" -> when (parts.getOrNull(1)) {
                "add" -> {
                    val prefillName = parts.getOrNull(2).orEmpty()
                    val prefill = TransactionType.entries.firstOrNull { it.name == prefillName }
                    HomeDestination.TransactionForm(mode = FormMode.Add, prefillType = prefill)
                }
                "edit" -> parts.getOrNull(2)?.let {
                    HomeDestination.TransactionForm(mode = FormMode.Edit(it))
                }
                else -> null
            }
            else -> null
        }
    },
)

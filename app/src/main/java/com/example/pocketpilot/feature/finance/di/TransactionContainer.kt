package com.example.pocketpilot.feature.finance.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.finance.domain.usecase.AddTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveTransactionsUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.SearchTransactionsUseCase
import com.example.pocketpilot.feature.finance.presentation.detail.TransactionDetailViewModel
import com.example.pocketpilot.feature.finance.presentation.form.FormMode
import com.example.pocketpilot.feature.finance.presentation.form.TransactionFormViewModel
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListViewModel

/**
 * Service locator for the transaction feature. Mirrors the shape of the
 * eventual Hilt module (`repository → use cases → view models`) so the swap
 * is mechanical when DI wiring lands.
 */
object TransactionContainer {

    val observeTransactionsUseCase by lazy {
        ObserveTransactionsUseCase(FinanceContainer.transactionRepository)
    }
    val searchTransactionsUseCase by lazy {
        SearchTransactionsUseCase(FinanceContainer.transactionRepository)
    }
    val observeTransactionUseCase by lazy {
        ObserveTransactionUseCase(FinanceContainer.transactionRepository)
    }
    val getTransactionUseCase by lazy {
        GetTransactionUseCase(FinanceContainer.transactionRepository)
    }
    val addTransactionUseCase by lazy {
        AddTransactionUseCase(FinanceContainer.transactionRepository)
    }
    val editTransactionUseCase by lazy {
        EditTransactionUseCase(FinanceContainer.transactionRepository)
    }
    val deleteTransactionUseCase by lazy {
        DeleteTransactionUseCase(FinanceContainer.transactionRepository)
    }
}

class TransactionListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TransactionListViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return TransactionListViewModel(TransactionContainer.searchTransactionsUseCase) as T
    }
}

class TransactionDetailViewModelFactory(private val transactionId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TransactionDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return TransactionDetailViewModel(
            transactionId = transactionId,
            observeTransactionUseCase = TransactionContainer.observeTransactionUseCase,
            deleteTransactionUseCase = TransactionContainer.deleteTransactionUseCase
        ) as T
    }
}

class TransactionFormViewModelFactory(private val mode: FormMode) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TransactionFormViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return TransactionFormViewModel(
            initialMode = mode,
            addTransactionUseCase = TransactionContainer.addTransactionUseCase,
            editTransactionUseCase = TransactionContainer.editTransactionUseCase,
            getTransactionUseCase = TransactionContainer.getTransactionUseCase
        ) as T
    }
}

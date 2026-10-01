package com.example.pocketpilot.feature.finance.di

import android.content.Context
import com.example.pocketpilot.feature.auth.data.local.dao.AccountDao
import com.example.pocketpilot.feature.finance.data.local.PocketPilotDatabase
import com.example.pocketpilot.feature.finance.data.repository.BudgetRepositoryImpl
import com.example.pocketpilot.feature.finance.data.repository.SavingsGoalRepositoryImpl
import com.example.pocketpilot.feature.finance.data.repository.TransactionRepositoryImpl
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository

/**
 * Service locator that assembles the finance persistence graph. A Hilt module
 * will replace this in a later phase; the `@Provides` shape (database → DAOs →
 * repositories) already mirrors what those bindings will look like.
 */
object FinanceContainer {

    @Volatile
    private var database: PocketPilotDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            synchronized(this) {
                if (database == null) {
                    val db = PocketPilotDatabase.create(context)
                    database = db
                    SyncContainer.init(context, db)
                }
            }
        }
    }

    private fun requireDatabase(): PocketPilotDatabase =
        checkNotNull(database) { "FinanceContainer.init(context) must be called before accessing repositories." }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(requireDatabase().transactionDao())
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(requireDatabase().budgetDao())
    }

    val savingsGoalRepository: SavingsGoalRepository by lazy {
        SavingsGoalRepositoryImpl(requireDatabase().savingsGoalDao())
    }

    val accountDao: AccountDao
        get() = requireDatabase().accountDao()
}

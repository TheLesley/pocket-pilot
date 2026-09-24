package com.example.pocketpilot.feature.finance.di

import android.content.Context
import com.example.pocketpilot.core.network.NetworkContainer
import com.example.pocketpilot.core.sync.ConnectivityManagerNetworkMonitor
import com.example.pocketpilot.core.sync.NetworkMonitor
import com.example.pocketpilot.core.sync.SyncManager
import com.example.pocketpilot.feature.finance.data.local.PocketPilotDatabase
import com.example.pocketpilot.feature.finance.data.remote.RetrofitBudgetRemoteDataSource
import com.example.pocketpilot.feature.finance.data.remote.RetrofitSavingsGoalRemoteDataSource
import com.example.pocketpilot.feature.finance.data.remote.RetrofitTransactionRemoteDataSource
import com.example.pocketpilot.feature.finance.data.sync.BudgetSyncEngine
import com.example.pocketpilot.feature.finance.data.sync.SavingsGoalSyncEngine
import com.example.pocketpilot.feature.finance.data.sync.TransactionSyncEngine

/**
 * Assembles the offline-first sync graph. Kept separate from
 * [FinanceContainer] so the persistence layer can be initialised — and used —
 * even in test contexts that never wire up the network stack.
 */
object SyncContainer {

    @Volatile
    private var networkMonitor: NetworkMonitor? = null

    @Volatile
    private var syncManager: SyncManager? = null

    fun init(context: Context, database: PocketPilotDatabase) {
        if (syncManager != null) return
        synchronized(this) {
            if (syncManager != null) return
            val monitor = ConnectivityManagerNetworkMonitor(context.applicationContext)
            val engines = listOf(
                TransactionSyncEngine(
                    dao = database.transactionDao(),
                    remote = RetrofitTransactionRemoteDataSource(NetworkContainer.transactionApi)
                ),
                BudgetSyncEngine(
                    dao = database.budgetDao(),
                    remote = RetrofitBudgetRemoteDataSource(NetworkContainer.budgetApi)
                ),
                SavingsGoalSyncEngine(
                    dao = database.savingsGoalDao(),
                    remote = RetrofitSavingsGoalRemoteDataSource(NetworkContainer.savingsGoalApi)
                )
            )
            networkMonitor = monitor
            syncManager = SyncManager(engines = engines, networkMonitor = monitor)
        }
    }

    val manager: SyncManager
        get() = checkNotNull(syncManager) {
            "SyncContainer.init(context, database) must be called before accessing the sync manager."
        }

    val monitor: NetworkMonitor
        get() = checkNotNull(networkMonitor) {
            "SyncContainer.init(context, database) must be called before accessing the network monitor."
        }
}

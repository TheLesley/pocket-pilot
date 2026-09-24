package com.example.pocketpilot.feature.finance.data.repository

import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.dao.TransactionDao
import com.example.pocketpilot.feature.finance.data.local.query.TransactionQuerySqlBuilder
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toEntity
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(private val dao: TransactionDao, private val clock: () -> Long = System::currentTimeMillis) :
    TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observe(query: TransactionQuery): Flow<List<Transaction>> {
        val sql = TransactionQuerySqlBuilder.build(query)
        return dao.observeByRawQuery(sql).map { list -> list.map { it.toDomain() } }
    }

    override fun observeByBudget(budgetId: String): Flow<List<Transaction>> =
        dao.observeByBudget(budgetId).map { list -> list.map { it.toDomain() } }

    override fun observeInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Transaction>> =
        dao.observeInRange(fromEpochMillis, toEpochMillis).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Transaction?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Transaction? = dao.findById(id)?.toDomain()

    /**
     * Offline-first write. The row is committed to Room immediately and marked
     * with a pending sync status; the [com.example.pocketpilot.core.sync.SyncManager]
     * uploads it later. A previously-`PENDING_INSERT` row keeps that status on
     * subsequent edits so we still create — never PATCH — a record the server
     * has never seen.
     */
    override suspend fun upsert(transaction: Transaction) {
        val now = clock()
        val existing = dao.findById(transaction.id)
        val nextStatus = when (existing?.syncStatus) {
            null -> SyncStatus.PENDING_INSERT
            SyncStatus.PENDING_INSERT -> SyncStatus.PENDING_INSERT
            else -> SyncStatus.PENDING_UPDATE
        }
        dao.upsert(
            transaction.toEntity(
                syncStatus = nextStatus,
                localUpdatedAtEpochMillis = now
            )
        )
    }

    /**
     * Tombstones the row (or hard-deletes it if it was never uploaded). The
     * server-side DELETE happens later during sync.
     */
    override suspend fun delete(id: String) {
        val existing = dao.findById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_INSERT) {
            dao.deleteById(id)
            return
        }
        dao.upsert(
            existing.copy(
                syncStatus = SyncStatus.PENDING_DELETE,
                localUpdatedAtEpochMillis = clock()
            )
        )
    }
}

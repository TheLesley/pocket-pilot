package com.example.pocketpilot.feature.finance.data.repository

import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.dao.BudgetDao
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toEntity
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(private val dao: BudgetDao, private val clock: () -> Long = System::currentTimeMillis) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Budget?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Budget? = dao.findById(id)?.toDomain()

    override suspend fun upsert(budget: Budget) {
        val now = clock()
        val existing = dao.findById(budget.id)
        val nextStatus = when (existing?.syncStatus) {
            null -> SyncStatus.PENDING_INSERT
            SyncStatus.PENDING_INSERT -> SyncStatus.PENDING_INSERT
            else -> SyncStatus.PENDING_UPDATE
        }
        dao.upsert(
            budget.toEntity(
                syncStatus = nextStatus,
                localUpdatedAtEpochMillis = now
            )
        )
    }

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
